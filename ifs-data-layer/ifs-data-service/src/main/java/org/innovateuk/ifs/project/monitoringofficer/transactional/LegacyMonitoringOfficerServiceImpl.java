package org.innovateuk.ifs.project.monitoringofficer.transactional;

import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.notifications.resource.Notification;
import org.innovateuk.ifs.notifications.resource.NotificationTarget;
import org.innovateuk.ifs.notifications.resource.SystemNotificationSource;
import org.innovateuk.ifs.notifications.resource.UserNotificationTarget;
import org.innovateuk.ifs.notifications.service.NotificationService;
import org.innovateuk.ifs.organisation.domain.Organisation;
import org.innovateuk.ifs.project.core.domain.Project;
import org.innovateuk.ifs.project.core.domain.ProjectUser;
import org.innovateuk.ifs.project.core.transactional.AbstractProjectServiceImpl;
import org.innovateuk.ifs.project.monitoringofficer.domain.LegacyMonitoringOfficer;
import org.innovateuk.ifs.project.monitoringofficer.mapper.LegacyMonitoringOfficerMapper;
import org.innovateuk.ifs.project.monitoringofficer.repository.LegacyMonitoringOfficerRepository;
import org.innovateuk.ifs.project.monitoringofficer.resource.LegacyMonitoringOfficerResource;
import org.innovateuk.ifs.project.projectdetails.workflow.configuration.ProjectDetailsWorkflowHandler;
import org.innovateuk.ifs.user.domain.ProcessRole;
import org.innovateuk.ifs.user.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.innovateuk.ifs.commons.error.CommonErrors.notFoundError;
import static org.innovateuk.ifs.commons.error.CommonFailureKeys.PROJECT_SETUP_MONITORING_OFFICER_CANNOT_BE_ASSIGNED_UNTIL_PROJECT_DETAILS_SUBMITTED;
import static org.innovateuk.ifs.commons.error.CommonFailureKeys.PROJECT_SETUP_PROJECT_ID_IN_URL_MUST_MATCH_PROJECT_ID_IN_MONITORING_OFFICER_RESOURCE;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceFailure;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.notifications.resource.NotificationMedium.EMAIL;
import static org.innovateuk.ifs.util.CollectionFunctions.getOnlyElementOrEmpty;
import static org.innovateuk.ifs.util.CollectionFunctions.simpleFilter;
import static org.innovateuk.ifs.util.EntityLookupCallbacks.find;

@Service
@Transactional(readOnly = true)
public class LegacyMonitoringOfficerServiceImpl extends AbstractProjectServiceImpl implements LegacyMonitoringOfficerService {

    @Autowired
    private LegacyMonitoringOfficerMapper legacyMonitoringOfficerMapper;

    @Autowired
    private ProjectDetailsWorkflowHandler projectDetailsWorkflowHandler;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private SystemNotificationSource systemNotificationSource;

    @Autowired
    private LegacyMonitoringOfficerRepository legacyMonitoringOfficerRepository;

    @Value("${ifs.web.baseURL}")
    private String webBaseUrl;

    enum Notifications {
        MONITORING_OFFICER_ASSIGNED,
        MONITORING_OFFICER_ASSIGNED_PROJECT_MANAGER
    }

    @Override
    public ServiceResult<LegacyMonitoringOfficerResource> getMonitoringOfficer(Long projectId) {
        return getExistingMonitoringOfficerForProject(projectId).andOnSuccessReturn(legacyMonitoringOfficerMapper::mapToResource);
    }

    private ServiceResult<LegacyMonitoringOfficer> getExistingMonitoringOfficerForProject(Long projectId) {
        return find(legacyMonitoringOfficerRepository.findOneByProjectId(projectId), notFoundError(LegacyMonitoringOfficer.class, projectId));
    }

    @Override
    @Transactional
    public ServiceResult<SaveMonitoringOfficerResult> saveMonitoringOfficer(final Long projectId, final LegacyMonitoringOfficerResource monitoringOfficerResource) {

        return validateMonitoringOfficer(projectId, monitoringOfficerResource).
                andOnSuccess(() -> validateInMonitoringOfficerAssignableState(projectId)).
                andOnSuccess(() -> saveMonitoringOfficer(monitoringOfficerResource));
    }

    private ServiceResult<Void> validateMonitoringOfficer(final Long projectId, final LegacyMonitoringOfficerResource monitoringOfficerResource) {

        if (!projectId.equals(monitoringOfficerResource.getProject())) {
            return serviceFailure(PROJECT_SETUP_PROJECT_ID_IN_URL_MUST_MATCH_PROJECT_ID_IN_MONITORING_OFFICER_RESOURCE);
        } else {
            return serviceSuccess();
        }
    }

    private ServiceResult<Void> validateInMonitoringOfficerAssignableState(final Long projectId) {

        return getProject(projectId).andOnSuccess(project -> {
            if (!projectDetailsWorkflowHandler.isSubmitted(project)) {
                return serviceFailure(PROJECT_SETUP_MONITORING_OFFICER_CANNOT_BE_ASSIGNED_UNTIL_PROJECT_DETAILS_SUBMITTED);
            } else {
                return serviceSuccess();
            }
        });
    }

    private ServiceResult<SaveMonitoringOfficerResult> saveMonitoringOfficer(final LegacyMonitoringOfficerResource monitoringOfficerResource) {

        return getExistingMonitoringOfficerForProject(monitoringOfficerResource.getProject()).handleSuccessOrFailure(
                noMonitoringOfficer -> saveNewMonitoringOfficer(monitoringOfficerResource),
                existingMonitoringOfficer -> updateExistingMonitoringOfficer(existingMonitoringOfficer, monitoringOfficerResource)
        );
    }

    private ServiceResult<SaveMonitoringOfficerResult> saveNewMonitoringOfficer(LegacyMonitoringOfficerResource monitoringOfficerResource) {
        SaveMonitoringOfficerResult result = new SaveMonitoringOfficerResult();
        LegacyMonitoringOfficer monitoringOfficer = legacyMonitoringOfficerMapper.mapToDomain(monitoringOfficerResource);
        legacyMonitoringOfficerRepository.save(monitoringOfficer);
        return serviceSuccess(result);
    }

    private ServiceResult<SaveMonitoringOfficerResult> updateExistingMonitoringOfficer(LegacyMonitoringOfficer existingMonitoringOfficer, LegacyMonitoringOfficerResource updateDetails) {
        SaveMonitoringOfficerResult result = new SaveMonitoringOfficerResult();

        if (isMonitoringOfficerDetailsChanged(existingMonitoringOfficer, updateDetails)) {
            existingMonitoringOfficer.setFirstName(updateDetails.getFirstName());
            existingMonitoringOfficer.setLastName(updateDetails.getLastName());
            existingMonitoringOfficer.setEmail(updateDetails.getEmail());
            existingMonitoringOfficer.setPhoneNumber(updateDetails.getPhoneNumber());
        } else {
            result.setMonitoringOfficerSaved(false);
        }

        return serviceSuccess(result);
    }

    private boolean isMonitoringOfficerDetailsChanged(LegacyMonitoringOfficer existingMonitoringOfficer, LegacyMonitoringOfficerResource updateDetails) {
        return !existingMonitoringOfficer.getFirstName().equals(updateDetails.getFirstName()) ||
                !existingMonitoringOfficer.getLastName().equals(updateDetails.getLastName()) ||
                !existingMonitoringOfficer.getEmail().equals(updateDetails.getEmail()) ||
                !existingMonitoringOfficer.getPhoneNumber().equals(updateDetails.getPhoneNumber());
    }

    @Override
    @Transactional
    public ServiceResult<Void> notifyStakeholdersOfMonitoringOfficerChange(LegacyMonitoringOfficerResource monitoringOfficer) {

        Project project = projectRepository.findById(monitoringOfficer.getProject()).get();

        User projectManager = getExistingProjectManager(project).get().getUser();

        NotificationTarget moTarget = createMonitoringOfficerNotificationTarget(monitoringOfficer);
        NotificationTarget pmTarget = createProjectManagerNotificationTarget(projectManager);

        Notification moNotification = new Notification(systemNotificationSource,
                                                       moTarget,
                                                       Notifications.MONITORING_OFFICER_ASSIGNED,
                                                       createGlobalArgsForMonitoringOfficerAssignedEmail(monitoringOfficer,
                                                                                                         project,
                                                                                                         projectManager));

        Notification pmNotification = new Notification(systemNotificationSource,
                                                       pmTarget,
                                                       Notifications.MONITORING_OFFICER_ASSIGNED_PROJECT_MANAGER,
                                                       createGlobalArgsForMonitoringOfficerAssignedEmail(monitoringOfficer,
                                                                                                         project,
                                                                                                         projectManager));

        return notificationService.sendNotificationWithFlush(moNotification, EMAIL).andOnSuccess(() ->
               notificationService.sendNotificationWithFlush(pmNotification, EMAIL));
    }

    private Optional<ProjectUser> getExistingProjectManager(Project project) {
        List<ProjectUser> projectUsers = project.getProjectUsers();
        List<ProjectUser> projectManagers = simpleFilter(projectUsers, pu -> pu.getRole().isProjectManager());
        return getOnlyElementOrEmpty(projectManagers);
    }

    private NotificationTarget createMonitoringOfficerNotificationTarget(LegacyMonitoringOfficerResource monitoringOfficer) {

        String fullName = getMonitoringOfficerFullName(monitoringOfficer);

        return new UserNotificationTarget(fullName, monitoringOfficer.getEmail());

    }

    private String getMonitoringOfficerFullName(LegacyMonitoringOfficerResource monitoringOfficer) {
        // At this stage, validation has already been done to ensure that first name and last name are not empty
        return monitoringOfficer.getFirstName() + " " + monitoringOfficer.getLastName();
    }

    private NotificationTarget createProjectManagerNotificationTarget(final User projectManager) {
        String fullName = getProjectManagerFullName(projectManager);

        return new UserNotificationTarget(fullName, projectManager.getEmail());
    }

    private String getProjectManagerFullName(User projectManager) {
        // At this stage, validation has already been done to ensure that first name and last name are not empty
        return projectManager.getFirstName() + " " + projectManager.getLastName();
    }

    private Map<String, Object> createGlobalArgsForMonitoringOfficerAssignedEmail(LegacyMonitoringOfficerResource monitoringOfficer, Project project, User projectManager) {
        Map<String, Object> globalArguments = new HashMap<>();
        globalArguments.put("dashboardUrl", webBaseUrl);
        globalArguments.put("projectName", project.getName());
        globalArguments.put("competitionName", project.getApplication().getCompetition().getName());
        globalArguments.put("applicationId", project.getApplication().getId());
        ProcessRole leadRole = project.getApplication().getLeadApplicantProcessRole();
        Organisation leadOrganisation = organisationRepository.findById(leadRole.getOrganisationId()).get();
        globalArguments.put("leadOrganisation", leadOrganisation.getName());
        globalArguments.put("projectManagerName", getProjectManagerFullName(projectManager));
        globalArguments.put("projectManagerEmail", projectManager.getEmail());
        globalArguments.put("monitoringOfficerName", getMonitoringOfficerFullName(monitoringOfficer));
        globalArguments.put("monitoringOfficerTelephone", monitoringOfficer.getPhoneNumber());
        globalArguments.put("monitoringOfficerEmail", monitoringOfficer.getEmail());
        return globalArguments;

    }
}
