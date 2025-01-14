package org.innovateuk.ifs.user.transactional;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.apache.commons.lang3.StringUtils;
import org.innovateuk.ifs.authentication.service.IdentityProviderService;
import org.innovateuk.ifs.authentication.validator.PasswordPolicyValidator;
import org.innovateuk.ifs.commons.error.Error;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.transactional.TermsAndConditionsService;
import org.innovateuk.ifs.invite.repository.UserInviteRepository;
import org.innovateuk.ifs.notifications.resource.*;
import org.innovateuk.ifs.notifications.service.NotificationService;
import org.innovateuk.ifs.token.domain.Token;
import org.innovateuk.ifs.token.repository.TokenRepository;
import org.innovateuk.ifs.token.resource.TokenType;
import org.innovateuk.ifs.token.transactional.TokenService;
import org.innovateuk.ifs.transactional.UserTransactionalService;
import org.innovateuk.ifs.user.cache.UserCacheEvict;
import org.innovateuk.ifs.user.cache.UserUpdate;
import org.innovateuk.ifs.user.command.GrantRoleCommand;
import org.innovateuk.ifs.user.domain.ProcessRole;
import org.innovateuk.ifs.user.domain.User;
import org.innovateuk.ifs.user.mapper.UserMapper;
import org.innovateuk.ifs.user.repository.ProcessRoleRepository;
import org.innovateuk.ifs.user.resource.*;
import org.innovateuk.ifs.userorganisation.domain.UserOrganisation;
import org.innovateuk.ifs.userorganisation.mapper.UserOrganisationMapper;
import org.innovateuk.ifs.userorganisation.repository.UserOrganisationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.time.ZonedDateTime.now;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toSet;
import static org.innovateuk.ifs.commons.error.CommonErrors.notFoundError;
import static org.innovateuk.ifs.commons.error.CommonFailureKeys.USER_SEARCH_INVALID_INPUT_LENGTH;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceFailure;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.notifications.resource.NotificationMedium.EMAIL;
import static org.innovateuk.ifs.user.resource.Role.*;
import static org.innovateuk.ifs.user.resource.UserStatus.INACTIVE;
import static org.innovateuk.ifs.util.CollectionFunctions.simpleMap;
import static org.innovateuk.ifs.util.EntityLookupCallbacks.find;

/**
 * A Service that covers basic operations concerning Users
 */
@Service
public class UserServiceImpl extends UserTransactionalService implements UserService {

    private final static Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);

    private final JsonNodeFactory factory = JsonNodeFactory.instance;

    public enum Notifications {
        VERIFY_EMAIL_ADDRESS,
        RESET_PASSWORD
    }

    @Value("${ifs.web.baseURL}")
    private String webBaseUrl;

    @Autowired
    private ProcessRoleRepository processRoleRepository;

    @Autowired
    private TermsAndConditionsService termsAndConditionsService;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private SystemNotificationSource systemNotificationSource;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private IdentityProviderService identityProviderService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordPolicyValidator passwordPolicyValidator;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private UserOrganisationRepository userOrganisationRepository;

    @Autowired
    private UserOrganisationMapper userOrganisationMapper;

    @Autowired
    private UserInviteRepository userInviteRepository;

    private Supplier<String> randomHashSupplier = () -> UUID.randomUUID().toString();

    @Override
    public ServiceResult<UserResource> findByEmail(final String email) {
        return find(userRepository.findByEmail(email), notFoundError(User.class, email)).andOnSuccessReturn(userMapper::mapToResource);
    }

    @Override
    public ServiceResult<UserResource> findInactiveByEmail(String email) {
        return find(userRepository.findByEmailAndStatus(email, INACTIVE), notFoundError(User.class, email, INACTIVE)).andOnSuccessReturn(userMapper::mapToResource);
    }

    @Override
    public ServiceResult<Set<UserResource>> findAssignableUsers(final Long applicationId) {

        List<ProcessRole> roles = processRoleRepository.findByApplicationId(applicationId);
        Set<UserResource> assignables = roles.stream()
                .filter(r -> "leadapplicant".equals(r.getRole().getName()) || "collaborator".equals(r.getRole().getName()))
                .map(ProcessRole::getUser)
                .map(userMapper::mapToResource)
                .collect(toSet());

        return serviceSuccess(assignables);
    }

    @Override
    public ServiceResult<Set<UserResource>> findRelatedUsers(final Long applicationId) {

        List<ProcessRole> roles = processRoleRepository.findByApplicationId(applicationId);

        Set<UserResource> related = roles.stream()
                .map(ProcessRole::getUser)
                .map(userMapper::mapToResource)
                .collect(toSet());

        return serviceSuccess(related);
    }

    @Override
    @Transactional
    public ServiceResult<Void> sendPasswordResetNotification(UserResource user) {
        if (UserStatus.ACTIVE.equals(user.getStatus())) {
            String hash = getAndSavePasswordResetToken(user);

            NotificationSource from = systemNotificationSource;
            NotificationTarget to = new UserNotificationTarget(user.getName(), user.getEmail());

            Map<String, Object> notificationArguments = new HashMap<>();
            notificationArguments.put("passwordResetLink", getPasswordResetLink(hash));

            Notification notification = new Notification(from, singletonList(to), Notifications.RESET_PASSWORD, notificationArguments);
            return notificationService.sendNotificationWithFlush(notification, EMAIL);
        } else if (userIsExternalNotOnlyAssessor(user) &&
                userNotYetVerified(user)) {
            return registrationService.resendUserVerificationEmail(user);
        } else {
            return serviceFailure(notFoundError(UserResource.class, user.getEmail(), UserStatus.ACTIVE));
        }
    }

    private String getAndSavePasswordResetToken(UserResource user) {
        String hash = getRandomHash();
        Token token = new Token(TokenType.RESET_PASSWORD, User.class.getName(), user.getId(), hash, now(), factory.objectNode());
        tokenRepository.save(token);
        return hash;
    }

    @Override
    @Transactional
    public ServiceResult<Void> changePassword(String hash, String password) {
        return tokenService.getPasswordResetToken(hash).andOnSuccess(token ->
            find(user(token.getClassPk())).andOnSuccess(user -> {

                UserResource userResource = userMapper.mapToResource(user);

                return passwordPolicyValidator.validatePassword(password, userResource).andOnSuccess(() ->
                        identityProviderService.updateUserPassword(userResource.getUid(), password).andOnSuccessReturnVoid(() ->
                            tokenRepository.delete(token))
                );
            })
        );
    }

    private ServiceResult<User> updateUserEmail(User existingUser, String emailToUpdate) {
        userInviteRepository.findByEmail(existingUser.getEmail()).forEach(invite -> invite.setEmail(emailToUpdate));
        existingUser.setEmail(emailToUpdate);
        User user = userRepository.save(existingUser);
        return identityProviderService.updateUserEmail(existingUser.getUid(), emailToUpdate)
                .andOnSuccessReturnVoid(() -> logEmailChange(existingUser.getEmail(), emailToUpdate))
                .andOnSuccessReturn(() -> user);
    }

    private void logEmailChange(String oldEmail, String newEmail){
        LOG.info("Updated email from  " + oldEmail + " to " + newEmail);
    }

    private String getRandomHash() {
        return randomHashSupplier.get();
    }

    private String getPasswordResetLink(String hash) {
        return String.format("%s/login/reset-password/hash/%s", webBaseUrl, hash);
    }

    @Override
    @Transactional
    @UserUpdate
    public ServiceResult<UserResource> updateDetails(UserResource userResource) {
        return find(userRepository.findByEmail(userResource.getEmail()), notFoundError(User.class, userResource.getEmail()))
                .andOnSuccess(user -> updateUser(user, userResource))
                .andOnSuccessReturn(userMapper::mapToResource);
    }

    @Override
    @UserCacheEvict
    public ServiceResult<Void> evictUserCache(String uid) {
        return serviceSuccess();
    }

    private ServiceResult<User> updateUser(User existingUser, UserResource updatedUserResource) {
        existingUser.setPhoneNumber(updatedUserResource.getPhoneNumber());
        existingUser.setTitle(updatedUserResource.getTitle());
        existingUser.setLastName(updatedUserResource.getLastName());
        existingUser.setFirstName(updatedUserResource.getFirstName());
        existingUser.setAllowMarketingEmails(updatedUserResource.getAllowMarketingEmails());
        return serviceSuccess(userRepository.save(existingUser));
    }

    private boolean userNotYetVerified(UserResource user) {
        return UserStatus.INACTIVE.equals(user.getStatus())
                && tokenRepository.findByTypeAndClassNameAndClassPk(TokenType.VERIFY_EMAIL_ADDRESS, User.class.getCanonicalName(), user.getId()).isPresent();
    }

    private boolean userIsExternalNotOnlyAssessor(UserResource user) {
        return user
                .getRoles()
                .stream()
                .anyMatch(r -> COLLABORATOR == r ||
                             APPLICANT == r ||
                             FINANCE_CONTACT == r ||
                             LEADAPPLICANT == r ||
                             PARTNER == r ||
                             PROJECT_MANAGER == r);
    }

    @Override
    public ServiceResult<UserPageResource> findActiveByRoles(Set<Role> roleTypes, Pageable pageable) {
        Page<User> pagedResult = userRepository.findDistinctByStatusAndRolesIn(UserStatus.ACTIVE, roleTypes.stream().map(r -> Role.getByName(r.getName())).collect(Collectors.toSet()), pageable);
        List<UserResource> userResources = simpleMap(pagedResult.getContent(), user -> userMapper.mapToResource(user));
        return serviceSuccess(new UserPageResource(pagedResult.getTotalElements(), pagedResult.getTotalPages(), userResources, pagedResult.getNumber(), pagedResult.getSize()));
    }

    @Override
    public ServiceResult<UserPageResource> findInactiveByRoles(Set<Role> roleTypes, Pageable pageable) {
        Page<User> pagedResult = userRepository.findDistinctByStatusAndRolesIn(UserStatus.INACTIVE, roleTypes.stream().map(r -> Role.getByName(r.getName())).collect(Collectors.toSet()), pageable);
        List<UserResource> userResources = simpleMap(pagedResult.getContent(), user -> userMapper.mapToResource(user));
        return serviceSuccess(new UserPageResource(pagedResult.getTotalElements(), pagedResult.getTotalPages(), userResources, pagedResult.getNumber(), pagedResult.getSize()));
    }

    @Override
    public ServiceResult<List<UserOrganisationResource>> findByProcessRolesAndSearchCriteria(Set<Role> roleTypes, String searchString, SearchCategory searchCategory) {

        return validateSearchString(searchString).andOnSuccess(() -> {
            String searchStringExpr = "%" + StringUtils.trim(searchString) + "%";
            Set<UserOrganisation> userOrganisations;
            switch (searchCategory) {
                case NAME:
                    userOrganisations = userOrganisationRepository.findByUserFirstNameLikeOrUserLastNameLikeAndUserRolesInOrderByUserEmailAsc(searchStringExpr, searchStringExpr, roleTypes);
                    break;

                case ORGANISATION_NAME:
                    userOrganisations = userOrganisationRepository.findByOrganisationNameLikeAndUserRolesInOrderByUserEmailAsc(searchStringExpr, roleTypes);
                    break;

                case EMAIL:
                default:
                    userOrganisations = userOrganisationRepository.findByUserEmailLikeAndUserRolesInOrderByUserEmailAsc(searchStringExpr, roleTypes);
                    break;
            }
            return serviceSuccess(simpleMap(userOrganisations, userOrganisationMapper::mapToResource));
        });
    }

    @Override
    @Transactional
    @UserUpdate
    public ServiceResult<UserResource> agreeNewTermsAndConditions(long userId) {
        return termsAndConditionsService.getLatestSiteTermsAndConditions().andOnSuccess(latest ->
                getUser(userId).andOnSuccessReturn(user -> {
                    user.getTermsAndConditionsIds().add(latest.getId());
                    return userRepository.save(user);
                }))
                .andOnSuccessReturn(userMapper::mapToResource);
    }

    @Override
    @Transactional
    @UserUpdate
    public ServiceResult<UserResource> grantRole(GrantRoleCommand grantRoleCommand) {
        return getUser(grantRoleCommand.getUserId())
                .andOnSuccessReturn(user -> {
                    user.getRoles().add(grantRoleCommand.getTargetRole());
                    return user;
                }).andOnSuccessReturn(userMapper::mapToResource);
    }

    @Override
    @Transactional
    @UserUpdate
    public ServiceResult<UserResource> updateEmail(long userId, String email) {
        return find(userRepository.findById(userId), notFoundError(User.class, userId))
                .andOnSuccess(user -> updateUserEmail(user, email))
                .andOnSuccessReturn(userMapper::mapToResource);
    }

    private ServiceResult<Void> validateSearchString(String searchString) {

        searchString = StringUtils.trim(searchString);

        if (StringUtils.isEmpty(searchString) || StringUtils.length(searchString) < 3) {
            return serviceFailure(new Error(USER_SEARCH_INVALID_INPUT_LENGTH, singletonList(3)) );
        } else {
            return serviceSuccess();
        }
    }
}