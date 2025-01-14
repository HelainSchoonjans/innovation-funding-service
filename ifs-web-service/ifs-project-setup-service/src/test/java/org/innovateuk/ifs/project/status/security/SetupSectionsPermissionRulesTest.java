package org.innovateuk.ifs.project.status.security;

import org.innovateuk.ifs.BasePermissionRulesTest;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.service.ApplicationService;
import org.innovateuk.ifs.commons.BaseIntegrationTest;
import org.innovateuk.ifs.commons.exception.ForbiddenActionException;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.service.CompetitionRestService;
import org.innovateuk.ifs.organisation.resource.OrganisationResource;
import org.innovateuk.ifs.organisation.resource.OrganisationTypeEnum;
import org.innovateuk.ifs.project.ProjectService;
import org.innovateuk.ifs.project.constant.ProjectActivityStates;
import org.innovateuk.ifs.project.resource.*;
import org.innovateuk.ifs.project.status.resource.ProjectTeamStatusResource;
import org.innovateuk.ifs.sections.SectionAccess;
import org.innovateuk.ifs.status.StatusService;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.user.service.OrganisationRestService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static java.util.Collections.singletonList;
import static org.innovateuk.ifs.application.builder.ApplicationResourceBuilder.newApplicationResource;
import static org.innovateuk.ifs.commons.error.CommonFailureKeys.CANNOT_GET_ANY_USERS_FOR_PROJECT;
import static org.innovateuk.ifs.commons.rest.RestResult.restSuccess;
import static org.innovateuk.ifs.competition.builder.CompetitionResourceBuilder.newCompetitionResource;
import static org.innovateuk.ifs.organisation.builder.OrganisationResourceBuilder.newOrganisationResource;
import static org.innovateuk.ifs.organisation.resource.OrganisationTypeEnum.BUSINESS;
import static org.innovateuk.ifs.project.builder.ProjectPartnerStatusResourceBuilder.newProjectPartnerStatusResource;
import static org.innovateuk.ifs.project.builder.ProjectResourceBuilder.newProjectResource;
import static org.innovateuk.ifs.project.builder.ProjectTeamStatusResourceBuilder.newProjectTeamStatusResource;
import static org.innovateuk.ifs.project.builder.ProjectUserResourceBuilder.newProjectUserResource;
import static org.innovateuk.ifs.sections.SectionAccess.ACCESSIBLE;
import static org.innovateuk.ifs.sections.SectionAccess.NOT_ACCESSIBLE;
import static org.innovateuk.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.innovateuk.ifs.user.resource.Role.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class SetupSectionsPermissionRulesTest extends BasePermissionRulesTest<SetupSectionsPermissionRules> {

    @Mock
    private SetupSectionsPermissionRules.SetupSectionPartnerAccessorSupplier accessorSupplierMock;

    @Mock
    private SetupSectionAccessibilityHelper accessorMock;

    @Mock
    private ApplicationService applicationServiceMock;

    @Mock
    private CompetitionRestService competitionRestService;

    @Mock
    private ProjectService projectServiceMock;

    @Mock
    private OrganisationRestService organisationRestService;

    @Mock
    private StatusService statusServiceMock;

    private UserResource user = newUserResource().build();

    private UserResource monitoringOfficer = newUserResource().withRoleGlobal(MONITORING_OFFICER).build();

    private CompetitionResource competition = newCompetitionResource().build();

    private ApplicationResource application = newApplicationResource().
            withCompetition(competition.getId()).
            build();

    private ProjectResource activeProject = newProjectResource().
            withProjectState(ProjectState.SETUP).
            withApplication(application).
            build();

    private ProjectResource withdrawnProject = newProjectResource().
            withProjectState(ProjectState.WITHDRAWN).
            withApplication(application).
            build();

    @Before
    public void setupAccessorLookup() {
        when(accessorSupplierMock.apply(isA(ProjectTeamStatusResource.class))).thenReturn(accessorMock);
    }

    @Test
    public void projectDetailsSectionAccess() {
        assertScenariosForSections(SetupSectionAccessibilityHelper::canAccessProjectDetailsSection, () -> rules.partnerCanAccessProjectDetailsSection(ProjectCompositeId.id(activeProject.getId()), user));
        verify(projectServiceMock).getById(activeProject.getId());
    }

    @Test
    public void projectManagerPageAccess() {
        assertLeadPartnerSuccessfulAccess(SetupSectionAccessibilityHelper::leadCanAccessProjectManagerPage,
                () -> rules.leadCanAccessProjectManagerPage(ProjectCompositeId.id(activeProject.getId()), user));
        verify(projectServiceMock, times(2)).getById(activeProject.getId());
    }

    @Test
    public void projectStartDatePageAccess() {
        assertLeadPartnerSuccessfulAccess(SetupSectionAccessibilityHelper::leadCanAccessProjectStartDatePage,
                () -> rules.leadCanAccessProjectStartDatePage(ProjectCompositeId.id(activeProject.getId()), user));
        verify(projectServiceMock, times(2)).getById(activeProject.getId());
    }

    @Test
    public void projectAddressPageAccess() {
        assertLeadPartnerSuccessfulAccess(SetupSectionAccessibilityHelper::leadCanAccessProjectAddressPage,
                () -> rules.leadCanAccessProjectAddressPage(ProjectCompositeId.id(activeProject.getId()), user));
        verify(projectServiceMock, times(2)).getById(activeProject.getId());
    }

    @Test
    public void financeContactPageAccess() {
        assertNonLeadPartnerSuccessfulAccess(SetupSectionAccessibilityHelper::canAccessFinanceContactPage,
                () -> rules.partnerCanAccessFinanceContactPage(ProjectCompositeId.id(activeProject.getId()), user));
        verify(projectServiceMock).getById(activeProject.getId());
    }

    @Test
    public void partnerProjectLocationPageAccess() {
        setUpPartnerProjectLocationRequiredMocking();

        assertNonLeadPartnerSuccessfulAccess((setupSectionAccessibilityHelper, organisation) ->
                setupSectionAccessibilityHelper.canAccessPartnerProjectLocationPage(organisation, true),
                () -> rules.partnerCanAccessProjectLocationPage(ProjectCompositeId.id(activeProject.getId()), user));
        verify(projectServiceMock, times(2)).getById(activeProject.getId());
    }

    @Test
    public void monitoringOfficerSectionAccess() {

        setUpPartnerProjectLocationRequiredMocking();

        assertNonLeadPartnerSuccessfulAccess((setupSectionAccessibilityHelper, organisation) ->
                setupSectionAccessibilityHelper.canAccessMonitoringOfficerSection(organisation, true),
                () -> rules.partnerCanAccessMonitoringOfficerSection(ProjectCompositeId.id(activeProject.getId()), user));
        verify(projectServiceMock, times(2)).getById(activeProject.getId());
    }

    private void setUpPartnerProjectLocationRequiredMocking() {
        when(applicationServiceMock.getById(activeProject.getApplication())).thenReturn(application);
        when(competitionRestService.getCompetitionById(application.getCompetition())).thenReturn(restSuccess(competition));
    }

    @Test
    public void bankDetailsSectionAccess() {
        assertNonLeadPartnerSuccessfulAccess((setupSectionAccessibilityHelper, organisation) ->
                setupSectionAccessibilityHelper.canAccessBankDetailsSection(organisation),
                () -> rules.partnerCanAccessBankDetailsSection(ProjectCompositeId.id(activeProject.getId()), user));
        verify(projectServiceMock, times(2)).getById(activeProject.getId());
    }

    @Test
    public void bankDetailsSectionAccessMonitoringOfficer() {
        activeProject.setMonitoringOfficerUser(monitoringOfficer.getId());
        when(projectServiceMock.getById(activeProject.getId())).thenReturn(activeProject);

        assertMonitoringOfficerUnSuccessfulAccess((setupSectionAccessibilityHelper, organisation) ->
                        setupSectionAccessibilityHelper.canAccessBankDetailsSection(organisation),
                () -> rules.partnerCanAccessBankDetailsSection(ProjectCompositeId.id(activeProject.getId()), monitoringOfficer));
        verify(projectServiceMock).getById(activeProject.getId());
    }

    @Test
    public void spendProfileSectionAccess() {
        assertNonLeadPartnerSuccessfulAccess(SetupSectionAccessibilityHelper::canAccessSpendProfileSection, () -> rules.partnerCanAccessSpendProfileSection(ProjectCompositeId.id(activeProject.getId()), user));
        verify(projectServiceMock).getById(activeProject.getId());
    }

    @Test
    public void projectManagerTotalSpendProfileSectionAccess() {
        assertLeadPartnerSuccessfulAccess(SetupSectionAccessibilityHelper::canAccessSpendProfileSection, () -> rules.projectManagerCanAccessSpendProfileSection(ProjectCompositeId.id(activeProject.getId()), user));
        verify(projectServiceMock).getById(activeProject.getId());
    }

    @Test
    public void partnerTotalSpendProfileSectionNoAccess() {
        assertNonLeadPartnerAndNotMOUnsuccessfulAccess(SetupSectionAccessibilityHelper::canAccessSpendProfileSection, () -> rules.projectManagerCanAccessSpendProfileSection(ProjectCompositeId.id(activeProject.getId()), user));
    }

    @Test
    public void spendProfileSectionAccessMonitoringOfficer() {
        assertMonitoringOfficerSuccessfulAccess(SetupSectionAccessibilityHelper::canAccessSpendProfileSection, () -> rules.partnerCanAccessSpendProfileSection(ProjectCompositeId.id(activeProject.getId()), monitoringOfficer));
        verify(projectServiceMock).getById(activeProject.getId());
    }

    @Test
    public void editSpendProfileSectionAccess() {
        ProjectOrganisationCompositeId projectOrganisationCompositeId = new ProjectOrganisationCompositeId(activeProject.getId(), 22L);
        assertNonLeadPartnerSuccessfulAccess((setupSectionAccessibilityHelper, organisation) -> setupSectionAccessibilityHelper.canEditSpendProfileSection(organisation, projectOrganisationCompositeId.getOrganisationId()),
                () -> rules.partnerCanEditSpendProfileSection(projectOrganisationCompositeId, user));
        verify(projectServiceMock).getById(activeProject.getId());
    }

    @Test
    public void documentsSectionAccessLead() {
        assertLeadPartnerSuccessfulAccess(SetupSectionAccessibilityHelper::canAccessDocumentsSection, () -> rules.canAccessDocumentsSection(ProjectCompositeId.id(activeProject.getId()), user));
        verify(projectServiceMock).getById(activeProject.getId());
    }

    @Test
    public void documentsSectionAccessMonitoringOfficer() {
        assertMonitoringOfficerSuccessfulAccess(SetupSectionAccessibilityHelper::canAccessDocumentsSection, () -> rules.canAccessDocumentsSection(ProjectCompositeId.id(activeProject.getId()), monitoringOfficer));
        verify(projectServiceMock).getById(activeProject.getId());
    }

    @Test
    public void documentsSectionAccessNonLead() {
        assertNonLeadPartnerSuccessfulAccess(SetupSectionAccessibilityHelper::canAccessDocumentsSection, () -> rules.canAccessDocumentsSection(ProjectCompositeId.id(activeProject.getId()), user));
        verify(projectServiceMock).getById(activeProject.getId());
    }

    @Test
    public void projectManagerCanEditDocumentsSection() {
        when(projectServiceMock.isProjectManager(user.getId(), activeProject.getId())).thenReturn(true);
        assertTrue(rules.projectManagerCanEditDocumentsSection(ProjectCompositeId.id(activeProject.getId()), user));
    }

    @Test
    public void nonProjectManagerCannotEditDocumentsSection() {
        when(projectServiceMock.isProjectManager(user.getId(), activeProject.getId())).thenReturn(false);
        assertFalse(rules.projectManagerCanEditDocumentsSection(ProjectCompositeId.id(activeProject.getId()), user));
    }

    @Test
    public void projectTeamStatusAccessUnavailableForWithdrawnProject() {
        assertLeadPartnerWithdrawnProjectAccess(() -> rules.partnerCanAccessProjectTeamStatus(ProjectCompositeId.id(withdrawnProject.getId()), user));
        verify(projectServiceMock).getById(withdrawnProject.getId());
    }

    @Test
    public void projectDetailsSectionAccessUnavailableForWithdrawnProject() {
        assertLeadPartnerWithdrawnProjectAccess(() -> rules.partnerCanAccessProjectDetailsSection(ProjectCompositeId.id(withdrawnProject.getId()), user));
        verify(projectServiceMock).getById(withdrawnProject.getId());
    }

    @Test
    public void projectManagerPageAccessUnavailableForWithdrawnProject() {
        assertLeadPartnerWithdrawnProjectAccess(() -> rules.leadCanAccessProjectManagerPage(ProjectCompositeId.id(withdrawnProject.getId()), user));
        verify(projectServiceMock, times(2)).getById(withdrawnProject.getId());
    }

    @Test
    public void projectStartDatePageAccessUnavailableForWithdrawnProject() {
        assertLeadPartnerWithdrawnProjectAccess(() -> rules.leadCanAccessProjectStartDatePage(ProjectCompositeId.id(withdrawnProject.getId()), user));
        verify(projectServiceMock, times(2)).getById(withdrawnProject.getId());
    }

    @Test
    public void projectAddressPageAccessUnavailableForWithdrawnProject() {
        assertLeadPartnerWithdrawnProjectAccess(() -> rules.leadCanAccessProjectAddressPage(ProjectCompositeId.id(withdrawnProject.getId()), user));
        verify(projectServiceMock, times(2)).getById(withdrawnProject.getId());
    }

    @Test
    public void financeContactPageAccessUnavailableForWithdrawnProject() {
        assertNonLeadPartnerWithdrawnProjectAccess(() -> rules.partnerCanAccessFinanceContactPage(ProjectCompositeId.id(withdrawnProject.getId()), user));
        verify(projectServiceMock).getById(withdrawnProject.getId());
    }

    @Test
    public void monitoringOfficerSectionAccessUnavailableForWithdrawnProject() {
        setUpPartnerProjectLocationRequiredMocking();
        assertNonLeadPartnerWithdrawnProjectAccess(() -> rules.partnerCanAccessMonitoringOfficerSection(ProjectCompositeId.id(withdrawnProject.getId()), user));
        verify(projectServiceMock, times(2)).getById(withdrawnProject.getId());
    }

    @Test
    public void projectLocationSectionAccessUnavailableForWithdrawnProject() {
        setUpPartnerProjectLocationRequiredMocking();
        assertNonLeadPartnerWithdrawnProjectAccess(() -> rules.partnerCanAccessProjectLocationPage(ProjectCompositeId.id(withdrawnProject.getId()), user));
        verify(projectServiceMock, times(2)).getById(withdrawnProject.getId());
    }

    @Test
    public void bankDetailsSectionAccessUnavailableForWithdrawnProject() {
        assertNonLeadPartnerWithdrawnProjectAccess(() -> rules.partnerCanAccessBankDetailsSection(ProjectCompositeId.id(withdrawnProject.getId()), user));
        verify(projectServiceMock, times(2)).getById(withdrawnProject.getId());
    }

    @Test
    public void spendProfileSectionAccessUnavailableForWithdrawnProject() {
        assertNonLeadPartnerWithdrawnProjectAccess(() -> rules.partnerCanAccessSpendProfileSection(ProjectCompositeId.id(withdrawnProject.getId()), user));
        verify(projectServiceMock).getById(withdrawnProject.getId());
    }

    @Test
    public void editSpendProfileSectionAccessUnavailableForWithdrawnProject() {
        ProjectOrganisationCompositeId projectOrganisationCompositeId = new ProjectOrganisationCompositeId(withdrawnProject.getId(), 22L);
        assertNonLeadPartnerWithdrawnProjectAccess(() -> rules.partnerCanEditSpendProfileSection(projectOrganisationCompositeId, user));
        verify(projectServiceMock).getById(withdrawnProject.getId());
    }

    @Test
    public void grantOfferLetterSectionAccess() {
        assertNonLeadPartnerSuccessfulAccess(SetupSectionAccessibilityHelper::canAccessGrantOfferLetterSection,
                () -> rules.partnerCanAccessGrantOfferLetterSection(ProjectCompositeId.id(activeProject.getId()), user));
        verify(projectServiceMock).getById(activeProject.getId());
    }

    @Test
    public void grantOfferLetterSectionAccessMonitoringOfficer() {
        assertMonitoringOfficerSuccessfulAccess(SetupSectionAccessibilityHelper::canAccessGrantOfferLetterSection,
                () -> rules.partnerCanAccessGrantOfferLetterSection(ProjectCompositeId.id(activeProject.getId()), monitoringOfficer));
        verify(projectServiceMock).getById(activeProject.getId());
    }

    @Test
    public void signedGrantOfferLetterSuccessfulAccessByLead() {
        assertLeadPartnerSuccessfulAccess(SetupSectionAccessibilityHelper::canAccessGrantOfferLetterSection,
                () -> rules.leadPartnerAccessToSignedGrantOfferLetter(ProjectCompositeId.id(activeProject.getId()), user));
        verify(projectServiceMock).getById(activeProject.getId());
    }

    @Test
    public void signedGrantOfferLetterUnSuccessfulAccessByNonLead() {
        assertNonLeadPartnerUnsuccessfulAccess(SetupSectionAccessibilityHelper::canAccessGrantOfferLetterSection,
                () -> rules.leadPartnerAccessToSignedGrantOfferLetter(ProjectCompositeId.id(activeProject.getId()), user));
    }

    @Test
    public void markSpendProfileIncompleteAccess() {
        ProjectUserResource leadPartnerProjectUserResource = newProjectUserResource().withUser(user.getId()).build();

        when(projectServiceMock.getLeadPartners(activeProject.getId())).thenReturn(singletonList(leadPartnerProjectUserResource));
        assertTrue(rules.userCanMarkSpendProfileIncomplete(ProjectCompositeId.id(activeProject.getId()), user));
        verify(projectServiceMock).getLeadPartners(activeProject.getId());
    }

    @Test
    public void userCannotMarkOwnOrganisationAsIncomplete() {
        Long userId = 1L;
        Long organisationId = 2L;
        Long projectId = 3L;
        UserResource userResource = newUserResource().withId(userId).build();
        OrganisationResource organisationResource = newOrganisationResource().withId(organisationId).build();

        when(organisationRestService.getByUserAndProjectId(userId, projectId)).thenReturn(restSuccess(organisationResource));
        assertFalse(rules.userCannotMarkOwnSpendProfileIncomplete(new ProjectOrganisationCompositeId(projectId, organisationId), userResource));
        verify(organisationRestService).getByUserAndProjectId(userId, projectId);
    }

    @Test
    public void userCanMarkOtherOrganisationAsIncomplete() {
        Long userId = 1L;
        Long organisationId = 2L;
        Long otherOrganisationId = 3L;
        Long projectId = 4L;
        UserResource userResource = newUserResource().withId(userId).build();
        OrganisationResource organisationResource = newOrganisationResource().withId(otherOrganisationId).build();

        when(organisationRestService.getByUserAndProjectId(userId, projectId)).thenReturn(restSuccess(organisationResource));
        assertTrue(rules.userCannotMarkOwnSpendProfileIncomplete(new ProjectOrganisationCompositeId(projectId, organisationId), userResource));
        verify(organisationRestService).getByUserAndProjectId(userId, projectId);
    }

    @Test
    public void partnerAccess() {
        long organisationId = 234L;

        UserResource user = newUserResource().withRolesGlobal(singletonList(PARTNER)).build();

        BaseIntegrationTest.setLoggedInUser(user);

        OrganisationResource o = newOrganisationResource().withId(organisationId).build();

        ProjectPartnerStatusResource partnerStatus = newProjectPartnerStatusResource().withProjectDetailsStatus(ProjectActivityStates.COMPLETE).withOrganisationId(organisationId).withOrganisationType(OrganisationTypeEnum.valueOf(BUSINESS.toString())).build();
        List<ProjectUserResource> pu = newProjectUserResource().withProject(activeProject.getId()).withOrganisation(o.getId()).withUser(user.getId()).build(1);
        pu.get(0).setRoleName(PARTNER.getName());

        ProjectTeamStatusResource teamStatus = newProjectTeamStatusResource().withPartnerStatuses(singletonList(partnerStatus)).build();

        when(projectServiceMock.getById(activeProject.getId())).thenReturn(activeProject);
        when(projectServiceMock.getOrganisationIdFromUser(activeProject.getId(), user)).thenReturn(organisationId);
        when(statusServiceMock.getProjectTeamStatus(activeProject.getId(), Optional.of(user.getId()))).thenReturn(teamStatus);
        when(accessorMock.canAccessFinanceChecksSection(any())).thenReturn(ACCESSIBLE);

        assertTrue(rules.partnerCanAccessFinanceChecksSection(ProjectCompositeId.id(activeProject.getId()), user));

        verify(accessorMock).canAccessFinanceChecksSection(any());
    }

    @Test
    public void partnerNoAccess() {
        
        long organisationId = 234L;

        UserResource user = newUserResource().withRolesGlobal(singletonList(PARTNER)).build();

        BaseIntegrationTest.setLoggedInUser(user);

        OrganisationResource o = newOrganisationResource().withId(organisationId).build();

        ProjectPartnerStatusResource partnerStatus = newProjectPartnerStatusResource().withProjectDetailsStatus(ProjectActivityStates.COMPLETE).withOrganisationId(organisationId).withOrganisationType(OrganisationTypeEnum.valueOf(BUSINESS.toString())).build();
        List<ProjectUserResource> pu = newProjectUserResource().withProject(activeProject.getId()).withOrganisation(o.getId()).withUser(user.getId()).build(1);
        pu.get(0).setRoleName(PARTNER.getName());

        when(projectServiceMock.getById(activeProject.getId())).thenReturn(activeProject);

        ProjectTeamStatusResource teamStatus = newProjectTeamStatusResource().withPartnerStatuses(singletonList(partnerStatus)).build();
        when(projectServiceMock.getProjectUsersForProject(activeProject.getId())).thenReturn(pu);
        when(statusServiceMock.getProjectTeamStatus(activeProject.getId(), Optional.of(user.getId()))).thenReturn(teamStatus);
        when(accessorMock.canAccessFinanceChecksSection(any())).thenReturn(NOT_ACCESSIBLE);
        when(projectServiceMock.getOrganisationIdFromUser(activeProject.getId(), user)).thenReturn(organisationId);

        assertFalse(rules.partnerCanAccessFinanceChecksSection(ProjectCompositeId.id(activeProject.getId()), user));

    }

    @Test
    public void financeContactAccess() {
        long organisationId = 234L;

        UserResource user = newUserResource().withRolesGlobal(singletonList(FINANCE_CONTACT)).build();

        BaseIntegrationTest.setLoggedInUser(user);

        OrganisationResource o = newOrganisationResource().withId(organisationId).build();

        ProjectPartnerStatusResource partnerStatus = newProjectPartnerStatusResource().withProjectDetailsStatus(ProjectActivityStates.COMPLETE).withOrganisationId(organisationId).withOrganisationType(OrganisationTypeEnum.valueOf(BUSINESS.toString())).build();
        List<ProjectUserResource> pu = newProjectUserResource().withProject(activeProject.getId()).withOrganisation(o.getId()).withUser(user.getId()).build(2);
        pu.get(0).setRoleName(PARTNER.getName());
        pu.get(1).setRoleName(FINANCE_CONTACT.getName());

        ProjectTeamStatusResource teamStatus = newProjectTeamStatusResource().withPartnerStatuses(singletonList(partnerStatus)).build();

        when(projectServiceMock.getById(activeProject.getId())).thenReturn(activeProject);
        when(projectServiceMock.getOrganisationIdFromUser(activeProject.getId(), user)).thenReturn(organisationId);
        when(statusServiceMock.getProjectTeamStatus(activeProject.getId(), Optional.of(user.getId()))).thenReturn(teamStatus);
        when(accessorMock.canAccessFinanceChecksSection(any())).thenReturn(ACCESSIBLE);

        assertTrue(rules.partnerCanAccessFinanceChecksSection(ProjectCompositeId.id(activeProject.getId()), user));

        verify(projectServiceMock, times(2)).getById(activeProject.getId());
        verify(projectServiceMock).getOrganisationIdFromUser(activeProject.getId(), user);
        verify(statusServiceMock).getProjectTeamStatus(activeProject.getId(), Optional.of(user.getId()));
    }

    private void assertLeadPartnerSuccessfulAccess(BiFunction<SetupSectionAccessibilityHelper, OrganisationResource, SectionAccess> accessorCheck,
                                                   Supplier<Boolean> ruleCheck) {

        ProjectTeamStatusResource teamStatus = newProjectTeamStatusResource().
                withProjectLeadStatus(newProjectPartnerStatusResource().
                        withOrganisationId(456L).
                        withOrganisationType(BUSINESS).
                        withIsLeadPartner(true).
                        build()).
                build();

        when(projectServiceMock.getById(activeProject.getId())).thenReturn(activeProject);
        
        when(projectServiceMock.getOrganisationIdFromUser(activeProject.getId(), user)).thenReturn(456L);

        when(statusServiceMock.getProjectTeamStatus(activeProject.getId(), Optional.of(user.getId()))).thenReturn(teamStatus);

        when(projectServiceMock.isUserLeadPartner(activeProject.getId(), user.getId())).thenReturn(true);

        when(projectServiceMock.isProjectManager(user.getId(), activeProject.getId())).thenReturn(true);

        OrganisationResource expectedOrganisation = new OrganisationResource();
        expectedOrganisation.setId(456L);
        expectedOrganisation.setOrganisationType(
                teamStatus.getPartnerStatusForOrganisation(456L).get().getOrganisationType().getId());

        when(accessorCheck.apply(accessorMock, expectedOrganisation)).thenReturn(ACCESSIBLE);

        assertTrue(ruleCheck.get());

        verify(projectServiceMock).getOrganisationIdFromUser(activeProject.getId(), user);
        verify(statusServiceMock).getProjectTeamStatus(activeProject.getId(), Optional.of(user.getId()));
    }

    private void assertLeadPartnerWithdrawnProjectAccess(Supplier<Boolean> ruleCheck) {

        when(projectServiceMock.getById(withdrawnProject.getId())).thenReturn(withdrawnProject);

        assertFalse(ruleCheck.get());

        verify(projectServiceMock, never()).getOrganisationIdFromUser(withdrawnProject.getId(), user);
        verify(statusServiceMock, never()).getProjectTeamStatus(withdrawnProject.getId(), Optional.of(user.getId()));
    }

    private void assertNonLeadPartnerSuccessfulAccess(BiFunction<SetupSectionAccessibilityHelper, OrganisationResource, SectionAccess> accessorCheck,
                                                      Supplier<Boolean> ruleCheck) {
        ProjectTeamStatusResource teamStatus = newProjectTeamStatusResource().
                withProjectLeadStatus(newProjectPartnerStatusResource().
                        withOrganisationId(456L).
                        withOrganisationType(BUSINESS).
                        withIsLeadPartner(true).
                        build()).
                withPartnerStatuses(newProjectPartnerStatusResource().
                        withOrganisationId(789L).
                        withOrganisationType(BUSINESS).
                        build(1)).
                build();

        List<ProjectUserResource> projectUsers = newProjectUserResource().
                withUser(user.getId()).
                withOrganisation(789L).
                withRole(PARTNER).
                build(1);

        when(projectServiceMock.getById(activeProject.getId())).thenReturn(activeProject);

        when(projectServiceMock.getProjectUsersForProject(activeProject.getId())).thenReturn(projectUsers);

        when(statusServiceMock.getProjectTeamStatus(activeProject.getId(), Optional.of(user.getId()))).thenReturn(teamStatus);

        when(projectServiceMock.getOrganisationIdFromUser(activeProject.getId(), user)).thenReturn(789L);

        when(projectServiceMock.isUserLeadPartner(activeProject.getId(), user.getId())).thenReturn(false);

        OrganisationResource expectedOrganisation = new OrganisationResource();
        expectedOrganisation.setId(789L);
        expectedOrganisation.setOrganisationType(
                teamStatus.getPartnerStatusForOrganisation(789L).get().getOrganisationType().getId());

        when(accessorCheck.apply(accessorMock, expectedOrganisation)).thenReturn(ACCESSIBLE);

        assertTrue(ruleCheck.get());

        verify(projectServiceMock, atLeastOnce()).getById(activeProject.getId());
        verify(projectServiceMock).getOrganisationIdFromUser(activeProject.getId(), user);
        verify(statusServiceMock).getProjectTeamStatus(activeProject.getId(), Optional.of(user.getId()));

        accessorCheck.apply(verify(accessorMock), expectedOrganisation);
    }

    private void assertMonitoringOfficerSuccessfulAccess(BiFunction<SetupSectionAccessibilityHelper, OrganisationResource, SectionAccess> accessorCheck,
                                                         Supplier<Boolean> ruleCheck) {
        ProjectTeamStatusResource teamStatus = newProjectTeamStatusResource().
                withProjectLeadStatus(newProjectPartnerStatusResource().
                        withOrganisationId(456L).
                        withOrganisationType(BUSINESS).
                        withIsLeadPartner(true).
                        build()).
                withPartnerStatuses(newProjectPartnerStatusResource().
                        withOrganisationId(789L).
                        withOrganisationType(BUSINESS).
                        build(1)).
                build();

        List<ProjectUserResource> projectUsers = newProjectUserResource().
                withUser(user.getId()).
                withOrganisation(789L).
                withRole(PARTNER).
                build(1);

        when(projectServiceMock.getById(activeProject.getId())).thenReturn(activeProject);

        when(projectServiceMock.getProjectUsersForProject(activeProject.getId())).thenReturn(projectUsers);

        when(statusServiceMock.getProjectTeamStatus(activeProject.getId(), Optional.of(user.getId()))).thenReturn(teamStatus);

        List<ProjectUserResource> leadPartners = newProjectUserResource().
                withUser(user.getId()).
                withOrganisation(789L).
                withRole(PROJECT_MANAGER).
                build(1);

        when(projectServiceMock.getLeadOrganisation(activeProject.getId())).thenReturn(newOrganisationResource().withId(789L).build());

        when(projectServiceMock.getLeadPartners(activeProject.getId())).thenReturn(leadPartners);

        OrganisationResource expectedOrganisation = new OrganisationResource();
        expectedOrganisation.setId(789L);
        expectedOrganisation.setOrganisationType(
                teamStatus.getPartnerStatusForOrganisation(789L).get().getOrganisationType().getId());

        when(accessorCheck.apply(accessorMock, expectedOrganisation)).thenReturn(ACCESSIBLE);

        assertTrue(ruleCheck.get());

        verify(projectServiceMock, atLeastOnce()).getById(activeProject.getId());
        verify(statusServiceMock).getProjectTeamStatus(activeProject.getId(), Optional.of(user.getId()));
        verify(projectServiceMock).getLeadPartners(activeProject.getId());

        accessorCheck.apply(verify(accessorMock), expectedOrganisation);
    }

    private void assertMonitoringOfficerUnSuccessfulAccess(BiFunction<SetupSectionAccessibilityHelper, OrganisationResource, SectionAccess> accessorCheck,
                                                           Supplier<Boolean> ruleCheck) {
        assertFalse(ruleCheck.get());
    }

    private void assertNonLeadPartnerUnsuccessfulAccess(BiFunction<SetupSectionAccessibilityHelper, OrganisationResource, SectionAccess> accessorCheck,
                                                      Supplier<Boolean> ruleCheck) {
        ProjectTeamStatusResource teamStatus = newProjectTeamStatusResource().
                withProjectLeadStatus(newProjectPartnerStatusResource().
                        withOrganisationId(456L).
                        withOrganisationType(BUSINESS).
                        withIsLeadPartner(true).
                        build()).
                withPartnerStatuses(newProjectPartnerStatusResource().
                        withOrganisationId(789L).
                        withOrganisationType(BUSINESS).
                        build(1)).
                build();

        List<ProjectUserResource> projectUsers = newProjectUserResource().
                withUser(user.getId()).
                withOrganisation(789L).
                withRole(PARTNER).
                build(1);

        when(projectServiceMock.getById(activeProject.getId())).thenReturn(activeProject);

        when(projectServiceMock.getProjectUsersForProject(activeProject.getId())).thenReturn(projectUsers);

        when(statusServiceMock.getProjectTeamStatus(activeProject.getId(), Optional.of(user.getId()))).thenReturn(teamStatus);

        when(projectServiceMock.getOrganisationIdFromUser(activeProject.getId(), user)).thenReturn(789L);

        when(projectServiceMock.isProjectManager(user.getId(), activeProject.getId())).thenReturn(false);

        when(projectServiceMock.isUserLeadPartner(activeProject.getId(), user.getId())).thenReturn(false);

        OrganisationResource expectedOrganisation = new OrganisationResource();
        expectedOrganisation.setId(789L);
        expectedOrganisation.setOrganisationType(
                teamStatus.getPartnerStatusForOrganisation(789L).get().getOrganisationType().getId());

        when(accessorCheck.apply(accessorMock, expectedOrganisation)).thenReturn(ACCESSIBLE);

        assertFalse(ruleCheck.get());

        verify(projectServiceMock, atLeastOnce()).getById(activeProject.getId());
        verify(projectServiceMock).getOrganisationIdFromUser(activeProject.getId(), user);
        verify(statusServiceMock).getProjectTeamStatus(activeProject.getId(), Optional.of(user.getId()));

        accessorCheck.apply(verify(accessorMock), expectedOrganisation);
    }

    private void assertNonLeadPartnerAndNotMOUnsuccessfulAccess(BiFunction<SetupSectionAccessibilityHelper, OrganisationResource, SectionAccess> accessorCheck,
                                                        Supplier<Boolean> ruleCheck) {
        assertFalse(ruleCheck.get());
    }


    private void assertNonLeadPartnerWithdrawnProjectAccess(Supplier<Boolean> ruleCheck) {

        when(projectServiceMock.getById(withdrawnProject.getId())).thenReturn(withdrawnProject);

        assertFalse(ruleCheck.get());

        verify(projectServiceMock, atLeastOnce()).getById(withdrawnProject.getId());
        verify(projectServiceMock, never()).getOrganisationIdFromUser(withdrawnProject.getId(), user);
        verify(statusServiceMock, never()).getProjectTeamStatus(withdrawnProject.getId(), Optional.of(user.getId()));
    }

    private void assertNotOnProjectExpectations(Supplier<Boolean> ruleCheck) {

        when(projectServiceMock.getById(activeProject.getId())).thenReturn(activeProject);

        when(projectServiceMock.getOrganisationIdFromUser(activeProject.getId(), user)).thenThrow(new ForbiddenActionException(CANNOT_GET_ANY_USERS_FOR_PROJECT.getErrorKey(), singletonList(activeProject.getId())));

        assertFalse(ruleCheck.get());

        verify(projectServiceMock).getById(activeProject.getId());
        verify(projectServiceMock).getOrganisationIdFromUser(activeProject.getId(), user);
        verify(statusServiceMock, never()).getProjectTeamStatus(activeProject.getId(), Optional.of(user.getId()));
    }

    private void assertForbiddenExpectations(Supplier<Boolean> ruleCheck) {

        when(projectServiceMock.getById(activeProject.getId())).thenReturn(activeProject);

        when(projectServiceMock.getOrganisationIdFromUser(activeProject.getId(), user)).thenThrow(new ForbiddenActionException(CANNOT_GET_ANY_USERS_FOR_PROJECT.getErrorKey(), singletonList(activeProject.getId())));

        assertFalse(ruleCheck.get());

        verify(projectServiceMock).getById(activeProject.getId());
        verify(projectServiceMock).getOrganisationIdFromUser(activeProject.getId(), user);
    }

    private void assertScenariosForSections(BiFunction<SetupSectionAccessibilityHelper, OrganisationResource, SectionAccess> accessorCheck, Supplier<Boolean> ruleCheck) {
        assertLeadPartnerSuccessfulAccess(accessorCheck, ruleCheck);
        resetMocks();

        assertNonLeadPartnerSuccessfulAccess(accessorCheck, ruleCheck);
        resetMocks();

        assertNotOnProjectExpectations(ruleCheck);
        resetMocks();

        assertForbiddenExpectations(ruleCheck);
    }

    private void resetMocks() {
        reset(projectServiceMock, statusServiceMock, accessorMock);
    }

    @Override
    protected SetupSectionsPermissionRules supplyPermissionRulesUnderTest() {
        return new SetupSectionsPermissionRules();
    }
}
