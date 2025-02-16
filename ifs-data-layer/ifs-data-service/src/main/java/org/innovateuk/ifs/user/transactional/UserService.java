package org.innovateuk.ifs.user.transactional;

import org.innovateuk.ifs.commons.security.NotSecured;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.user.command.GrantRoleCommand;
import org.innovateuk.ifs.user.resource.*;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;

import java.util.List;
import java.util.Set;

/**
 * A Service that covers basic operations concerning Users
 */
public interface UserService {

    @PostAuthorize("hasPermission(returnObject, 'READ')")
    ServiceResult<UserResource> findByEmail(final String email);

    @PostAuthorize("hasPermission(returnObject, 'READ')")
    ServiceResult<UserResource> findInactiveByEmail(final String email);

    @PostFilter("hasPermission(filterObject, 'READ')")
    ServiceResult<Set<UserResource>> findAssignableUsers(final Long applicationId);

    @PostFilter("hasPermission(filterObject, 'READ')")
    ServiceResult<Set<UserResource>> findRelatedUsers(final Long applicationId);

    @PreAuthorize("hasPermission(#user, 'CHANGE_PASSWORD')")
    ServiceResult<Void> sendPasswordResetNotification(@P("user") UserResource user);

    @PreAuthorize("hasPermission(#hash, 'org.innovateuk.ifs.token.domain.Token', 'CHANGE_PASSWORD')")
    ServiceResult<Void> changePassword(@P("hash") String hash, String password);

    @PostAuthorize("hasPermission(returnObject, 'READ')")
    ServiceResult<UserPageResource> findActiveByRoles(Set<Role> roleTypes, Pageable pageable);

    @PostFilter("hasPermission(filterObject, 'READ_USER_ORGANISATION')")
    ServiceResult<List<UserOrganisationResource>> findByProcessRolesAndSearchCriteria(Set<Role> roleTypes, String searchString, SearchCategory searchCategory);

    @PostAuthorize("hasPermission(returnObject, 'READ')")
    ServiceResult<UserPageResource> findInactiveByRoles(Set<Role> roleTypes, Pageable pageable);

    @PreAuthorize("hasPermission(#userId, 'org.innovateuk.ifs.user.resource.UserResource', 'AGREE_TERMS')")
    ServiceResult<UserResource> agreeNewTermsAndConditions(long userId);

    @PreAuthorize("hasPermission(#grantRoleCommand, 'GRANT_ROLE')")
    ServiceResult<UserResource> grantRole(GrantRoleCommand grantRoleCommand);

    @PreAuthorize("hasPermission(#userId, 'org.innovateuk.ifs.user.resource.UserResource', 'UPDATE_USER_EMAIL')")
    ServiceResult<UserResource> updateEmail(long userId, String email);

    @PreAuthorize("hasPermission(#userBeingUpdated, 'UPDATE')")
    ServiceResult<UserResource> updateDetails(@P("userBeingUpdated") UserResource userBeingUpdated);

    @NotSecured(value = "Can be called anywhere by anyone", mustBeSecuredByOtherServices = false)
    ServiceResult<Void> evictUserCache(String uid);
}
