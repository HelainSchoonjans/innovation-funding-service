package org.innovateuk.ifs.project.monitoringofficer.security;

import org.innovateuk.ifs.BasePermissionRulesTest;
import org.innovateuk.ifs.competition.domain.Competition;
import org.innovateuk.ifs.competition.domain.Stakeholder;
import org.innovateuk.ifs.competition.repository.StakeholderRepository;
import org.innovateuk.ifs.project.core.domain.ProjectProcess;
import org.innovateuk.ifs.project.core.repository.ProjectProcessRepository;
import org.innovateuk.ifs.project.resource.ProjectResource;
import org.innovateuk.ifs.project.resource.ProjectState;
import org.innovateuk.ifs.user.domain.User;
import org.innovateuk.ifs.user.resource.UserResource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.innovateuk.ifs.competition.builder.CompetitionBuilder.newCompetition;
import static org.innovateuk.ifs.competition.builder.StakeholderBuilder.newStakeholder;
import static org.innovateuk.ifs.project.builder.ProjectResourceBuilder.newProjectResource;
import static org.innovateuk.ifs.project.core.builder.ProjectProcessBuilder.newProjectProcess;
import static org.innovateuk.ifs.project.core.domain.ProjectParticipantRole.PROJECT_PARTNER;
import static org.innovateuk.ifs.user.builder.UserBuilder.newUser;
import static org.innovateuk.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.innovateuk.ifs.user.resource.Role.STAKEHOLDER;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class LegacyMonitoringOfficerPermissionRulesTest extends BasePermissionRulesTest<LegacyMonitoringOfficerPermissionRules> {
    private ProjectProcess projectProcess;

    @Mock
    private ProjectProcessRepository projectProcessRepositoryMock;

    @Mock
    private StakeholderRepository stakeholderRepositoryMock;

    @Before
    public void setUp() throws Exception {
        projectProcess = newProjectProcess().withActivityState(ProjectState.SETUP).build();
    }

    @Override
    protected LegacyMonitoringOfficerPermissionRules supplyPermissionRulesUnderTest() {
        return new LegacyMonitoringOfficerPermissionRules();
    }

    @Test
    public void internalUsersCanViewMonitoringOfficersOnProjects() {

        ProjectResource project = newProjectResource().build();

        allGlobalRoleUsers.forEach(user -> {
            if (allInternalUsers.contains(user)) {
                assertTrue(rules.internalUsersCanViewMonitoringOfficersForAnyProject(project, user));
            } else {
                assertFalse(rules.internalUsersCanViewMonitoringOfficersForAnyProject(project, user));
            }
        });
    }

    @Test
    public void stakeholdersCanViewMonitoringOfficersForAProjectOnTheirCompetitions() {

        User stakeholderUserOnCompetition = newUser().withRoles(singleton(STAKEHOLDER)).build();
        UserResource stakeholderUserResourceOnCompetition = newUserResource().withId(stakeholderUserOnCompetition.getId()).withRolesGlobal(singletonList(STAKEHOLDER)).build();
        Stakeholder stakeholder = newStakeholder().withUser(stakeholderUserOnCompetition).build();
        Competition competition = newCompetition().build();

        ProjectResource project = newProjectResource()
                .withCompetition(competition.getId())
                .build();

        when(stakeholderRepositoryMock.findStakeholders(competition.getId())).thenReturn(singletonList(stakeholder));

        assertTrue(rules.stakeholdersCanViewMonitoringOfficersForAProjectOnTheirCompetitions(project, stakeholderUserResourceOnCompetition));

        allInternalUsers.forEach(user -> {
            assertFalse(rules.stakeholdersCanViewMonitoringOfficersForAProjectOnTheirCompetitions(newProjectResource().build(), user));
        });
    }

    @Test
    public void partnersCanViewMonitoringOfficersOnTheirOwnProjects() {

        UserResource user = newUserResource().build();
        ProjectResource project = newProjectResource().build();

        setupUserAsPartner(project, user);

        assertTrue(rules.partnersCanViewMonitoringOfficersOnTheirProjects(project, user));
    }

    @Test
    public void monitoringOfficersCanViewThemselves() {

        UserResource user = newUserResource().build();
        ProjectResource project = newProjectResource().build();

        setupUserAsMonitoringOfficer(project, user);

        when(projectMonitoringOfficerRepositoryMock.existsByProjectIdAndUserId(project.getId(), user.getId())).thenReturn(true);

        assertTrue(rules.monitoringOfficersCanViewThemselves(project, user));
    }

    @Test
    public void partnersCanViewMonitoringOfficersOnTheirOwnProjectsButUserNotPartner() {

        UserResource user = newUserResource().build();

        ProjectResource project = newProjectResource().build();

        when(projectUserRepositoryMock.findByProjectIdAndUserIdAndRole(project.getId(), user.getId(), PROJECT_PARTNER)).thenReturn(emptyList());

        assertFalse(rules.partnersCanViewMonitoringOfficersOnTheirProjects(project, user));
    }

    @Test
    public void internalUsersCanEditMonitoringOfficersOnProjects() {

        ProjectResource project = newProjectResource()
                .withProjectState(ProjectState.SETUP)
                .build();

        when(projectProcessRepositoryMock.findOneByTargetId(project.getId())).thenReturn(projectProcess);

        allGlobalRoleUsers.forEach(user -> {
            if (allInternalUsers.contains(user)) {
                assertTrue(rules.internalUsersCanAssignMonitoringOfficersForAnyProject(project, user));
            } else {
                assertFalse(rules.internalUsersCanAssignMonitoringOfficersForAnyProject(project, user));
            }
        });
    }
}