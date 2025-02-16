package org.innovateuk.ifs.fundingdecision.transactional;

import org.innovateuk.ifs.BaseServiceUnitTest;
import org.innovateuk.ifs.application.domain.Application;
import org.innovateuk.ifs.application.repository.ApplicationRepository;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.resource.ApplicationState;
import org.innovateuk.ifs.application.resource.FundingDecision;
import org.innovateuk.ifs.application.resource.FundingNotificationResource;
import org.innovateuk.ifs.application.transactional.ApplicationService;
import org.innovateuk.ifs.application.workflow.configuration.ApplicationWorkflowHandler;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.domain.Competition;
import org.innovateuk.ifs.competition.domain.CompetitionType;
import org.innovateuk.ifs.competition.repository.CompetitionRepository;
import org.innovateuk.ifs.competition.resource.CompetitionStatus;
import org.innovateuk.ifs.competition.transactional.CompetitionService;
import org.innovateuk.ifs.fundingdecision.domain.FundingDecisionStatus;
import org.innovateuk.ifs.fundingdecision.mapper.FundingDecisionMapper;
import org.innovateuk.ifs.fundingdecision.validator.ApplicationFundingDecisionValidator;
import org.innovateuk.ifs.notifications.resource.Notification;
import org.innovateuk.ifs.notifications.resource.NotificationTarget;
import org.innovateuk.ifs.notifications.resource.SystemNotificationSource;
import org.innovateuk.ifs.notifications.resource.UserNotificationTarget;
import org.innovateuk.ifs.notifications.service.NotificationService;
import org.innovateuk.ifs.user.domain.ProcessRole;
import org.innovateuk.ifs.user.domain.User;
import org.innovateuk.ifs.user.repository.ProcessRoleRepository;
import org.innovateuk.ifs.user.resource.Role;
import org.innovateuk.ifs.util.MapFunctions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import java.text.Collator;
import java.time.ZonedDateTime;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.innovateuk.ifs.LambdaMatcher.createLambdaMatcher;
import static org.innovateuk.ifs.application.builder.ApplicationBuilder.newApplication;
import static org.innovateuk.ifs.application.resource.FundingDecision.*;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.competition.builder.CompetitionBuilder.newCompetition;
import static org.innovateuk.ifs.competition.builder.CompetitionTypeBuilder.newCompetitionType;
import static org.innovateuk.ifs.fundingdecision.transactional.ApplicationFundingServiceImpl.Notifications.APPLICATION_FUNDING;
import static org.innovateuk.ifs.notifications.resource.NotificationMedium.EMAIL;
import static org.innovateuk.ifs.user.builder.ProcessRoleBuilder.newProcessRole;
import static org.innovateuk.ifs.user.builder.UserBuilder.newUser;
import static org.innovateuk.ifs.util.CollectionFunctions.simpleMap;
import static org.innovateuk.ifs.util.MapFunctions.asMap;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class ApplicationFundingServiceImplMockTest extends BaseServiceUnitTest<ApplicationFundingService> {

    private static final String webBaseUrl = "http://ifs-local-dev";

    @Mock
    private CompetitionRepository competitionRepositoryMock;

    @Mock
    private FundingDecisionMapper fundingDecisionMapper;

    @Mock
    private ApplicationFundingDecisionValidator applicationFundingDecisionValidator;

    @Mock
    private SystemNotificationSource systemNotificationSourceMock;

    @Mock
    private ApplicationRepository applicationRepositoryMock;

    @Mock
    private ProcessRoleRepository processRoleRepositoryMock;

    @Mock
    private NotificationService notificationServiceMock;

    @Mock
    private ApplicationService applicationServiceMock;

    @Mock
    private CompetitionService competitionServiceMock;

    @Mock
    private ApplicationWorkflowHandler applicationWorkflowHandlerMock;

    private Competition competition;
    
    @Override
    protected ApplicationFundingService supplyServiceUnderTest() {
        ApplicationFundingServiceImpl service = new ApplicationFundingServiceImpl();
        ReflectionTestUtils.setField(service, "webBaseUrl", webBaseUrl);
        return service;
    }

    @Before
    public void setup() {
    	competition = newCompetition().withAssessorFeedbackDate("01/02/2017 17:30:00").withCompetitionStatus(CompetitionStatus.FUNDERS_PANEL).withId(123L).build();
    	when(competitionRepositoryMock.findById(123L)).thenReturn(Optional.of(competition));
    	
    	when(fundingDecisionMapper.mapToDomain(any(FundingDecision.class))).thenAnswer(new Answer<FundingDecisionStatus>(){
			@Override
			public FundingDecisionStatus answer(InvocationOnMock invocation) throws Throwable {
				return FundingDecisionStatus.valueOf(((FundingDecision)invocation.getArguments()[0]).name());
			}});
    	when(fundingDecisionMapper.mapToResource(any(FundingDecisionStatus.class))).thenAnswer(new Answer<FundingDecision>(){
			@Override
			public FundingDecision answer(InvocationOnMock invocation) throws Throwable {
				return FundingDecision.valueOf(((FundingDecisionStatus)invocation.getArguments()[0]).name());
			}});
    }

    @Test
    public void testNotifyLeadApplicantsOfFundingDecisions() {
        Competition competition = newCompetition().build();

        Application application1 = newApplication().withCompetition(competition).build();
        Application application2 = newApplication().withCompetition(competition).build();
        Application application3 = newApplication().withCompetition(competition).build();

        User application1LeadApplicant = newUser().build();
        User application2LeadApplicant = newUser().build();
        User application3LeadApplicant = newUser().build();

        List<ProcessRole> leadApplicantProcessRoles = newProcessRole().
                withUser(application1LeadApplicant, application2LeadApplicant, application3LeadApplicant).
                withApplication(application1, application2, application3).
                withRole(Role.LEADAPPLICANT).
                build(3);

        UserNotificationTarget application1LeadApplicantTarget = new UserNotificationTarget(application1LeadApplicant.getName(), application1LeadApplicant.getEmail());
        UserNotificationTarget application2LeadApplicantTarget = new UserNotificationTarget(application2LeadApplicant.getName(), application2LeadApplicant.getEmail());
        UserNotificationTarget application3LeadApplicantTarget = new UserNotificationTarget(application3LeadApplicant.getName(), application3LeadApplicant.getEmail());
        List<NotificationTarget> expectedLeadApplicants = asList(application1LeadApplicantTarget, application2LeadApplicantTarget, application3LeadApplicantTarget);

        Map<Long, FundingDecision> decisions = MapFunctions.asMap(
                application1.getId(), FundingDecision.FUNDED,
                application2.getId(), FundingDecision.UNFUNDED,
                application3.getId(), FundingDecision.ON_HOLD);

        FundingNotificationResource fundingNotificationResource = new FundingNotificationResource("The message body.", decisions);

        Map<String, Object> expectedGlobalNotificationArguments = asMap("message", fundingNotificationResource.getMessageBody());

        Map<NotificationTarget, Map<String, Object>> expectedTargetSpecificNotificationArguments = asMap(
                application1LeadApplicantTarget, asMap(
                        "applicationName", application1.getName(),
                        "competitionName", application1.getCompetition().getName(),
                        "applicationId", application1.getId()),

                application2LeadApplicantTarget, asMap(
                        "applicationName", application2.getName(),
                        "competitionName", application2.getCompetition().getName(),
                        "applicationId", application2.getId()),

                application3LeadApplicantTarget, asMap(
                        "applicationName", application3.getName(),
                        "competitionName", application3.getCompetition().getName(),
                        "applicationId", application3.getId()));

        Notification expectedFundingNotification = new Notification(systemNotificationSourceMock, expectedLeadApplicants, APPLICATION_FUNDING, expectedGlobalNotificationArguments, expectedTargetSpecificNotificationArguments);

        List<Long> applicationIds = asList(application1.getId(), application2.getId(), application3.getId());
        List<Application> applications = asList(application1, application2, application3);
        when(applicationRepositoryMock.findAllById(applicationIds)).thenReturn(applications);

        leadApplicantProcessRoles.forEach(processRole ->
                when(processRoleRepositoryMock.findByApplicationIdAndRole(processRole.getApplicationId(), processRole.getRole())).thenReturn(singletonList(processRole))
        );
        when(notificationServiceMock.sendNotificationWithFlush(createNotificationExpectationsWithGlobalArgs(expectedFundingNotification), eq(EMAIL))).thenReturn(serviceSuccess());
        when(applicationServiceMock.setApplicationFundingEmailDateTime(any(Long.class), any(ZonedDateTime.class))).thenReturn(serviceSuccess(new ApplicationResource()));
        when(competitionServiceMock.manageInformState(competition.getId())).thenReturn(serviceSuccess());

        ServiceResult<Void> result = service.notifyApplicantsOfFundingDecisions(fundingNotificationResource);
        assertTrue(result.isSuccess());

        verify(notificationServiceMock).sendNotificationWithFlush(createNotificationExpectationsWithGlobalArgs(expectedFundingNotification), eq(EMAIL));
        verifyNoMoreInteractions(notificationServiceMock);

        verify(applicationServiceMock).setApplicationFundingEmailDateTime(eq(application1.getId()), any(ZonedDateTime.class));
        verify(applicationServiceMock).setApplicationFundingEmailDateTime(eq(application2.getId()), any(ZonedDateTime.class));
        verify(applicationServiceMock).setApplicationFundingEmailDateTime(eq(application3.getId()), any(ZonedDateTime.class));
        verifyNoMoreInteractions(applicationServiceMock);
    }

    @Test
    public void testNotifyAllApplicantsOfFundingDecisions() {

        Competition competition = newCompetition().build();

        Application application1 = newApplication().withCompetition(competition).build();
        Application application2 = newApplication().withCompetition(competition).build();

        // add some collaborators into the mix - they should receive Notifications, and applicants who should not
        User application1LeadApplicant = newUser().build();
        User application1Collaborator = newUser().build();
        User application1Applicant = newUser().build();
        User application2LeadApplicant = newUser().build();
        User application2Collaborator = newUser().build();
        User application2Applicant = newUser().build();


        List<ProcessRole> allProcessRoles = newProcessRole().
                withUser(application1LeadApplicant, application1Collaborator, application1Applicant, application2LeadApplicant, application2Collaborator, application2Applicant).
                withApplication(application1, application1, application1, application2, application2, application2).
                withRole(Role.LEADAPPLICANT, Role.COLLABORATOR, Role.APPLICANT, Role.LEADAPPLICANT, Role.COLLABORATOR, Role.APPLICANT).
                build(6);

        UserNotificationTarget application1LeadApplicantTarget = new UserNotificationTarget(application1LeadApplicant.getName(), application1LeadApplicant.getEmail());
        UserNotificationTarget application2LeadApplicantTarget = new UserNotificationTarget(application2LeadApplicant.getName(), application2LeadApplicant.getEmail());
        UserNotificationTarget application1CollaboratorTarget = new UserNotificationTarget(application1Collaborator.getName(), application1Collaborator.getEmail());
        UserNotificationTarget application2CollaboratorTarget = new UserNotificationTarget(application2Collaborator.getName(), application2Collaborator.getEmail());
        List<NotificationTarget> expectedApplicants = asList(application1LeadApplicantTarget, application2LeadApplicantTarget, application1CollaboratorTarget, application2CollaboratorTarget);

        Map<Long, FundingDecision> decisions = MapFunctions.asMap(
                application1.getId(), FundingDecision.FUNDED,
                application2.getId(), FundingDecision.UNFUNDED);
        FundingNotificationResource fundingNotificationResource = new FundingNotificationResource("The message body.", decisions);

        Notification expectedFundingNotification =
                new Notification(systemNotificationSourceMock, expectedApplicants, APPLICATION_FUNDING, emptyMap());
        
        List<Long> applicationIds = asList(application1.getId(), application2.getId());
        List<Application> applications = asList(application1, application2);
        when(applicationRepositoryMock.findAllById(applicationIds)).thenReturn(applications);

        asList(application1, application2).forEach(application ->
                when(applicationRepositoryMock.findById(application.getId())).thenReturn(Optional.of(application))
        );

        allProcessRoles.forEach(processRole ->
                when(processRoleRepositoryMock.findByApplicationIdAndRole(processRole.getApplicationId(), processRole.getRole())).thenReturn(singletonList(processRole))
        );

        when(notificationServiceMock.sendNotificationWithFlush(createSimpleNotificationExpectations(expectedFundingNotification), eq(EMAIL))).thenReturn(serviceSuccess());
        when(applicationServiceMock.setApplicationFundingEmailDateTime(any(Long.class), any(ZonedDateTime.class))).thenReturn(serviceSuccess(new ApplicationResource()));
        when(competitionServiceMock.manageInformState(competition.getId())).thenReturn(serviceSuccess());

        ServiceResult<Void> result = service.notifyApplicantsOfFundingDecisions(fundingNotificationResource);
        assertTrue(result.isSuccess());

        verify(notificationServiceMock).sendNotificationWithFlush(createSimpleNotificationExpectations(expectedFundingNotification), eq(EMAIL));
        verifyNoMoreInteractions(notificationServiceMock);

        verify(applicationServiceMock).setApplicationFundingEmailDateTime(eq(application1.getId()), any(ZonedDateTime.class));
        verify(applicationServiceMock).setApplicationFundingEmailDateTime(eq(application2.getId()), any(ZonedDateTime.class));
        verifyNoMoreInteractions(applicationServiceMock);
    }
    
    @Test
    public void testSaveFundingDecisionData() {
    	
    	Application application1 = newApplication().withId(1L).withCompetition(competition).withFundingDecision(FundingDecisionStatus.FUNDED).withApplicationState(ApplicationState.OPENED).build();
     	Application application2 = newApplication().withId(2L).withCompetition(competition).withFundingDecision(FundingDecisionStatus.UNFUNDED).withApplicationState(ApplicationState.OPENED).build();
    	when(applicationRepositoryMock.findByCompetitionId(competition.getId())).thenReturn(asList(application1, application2));
        when(applicationFundingDecisionValidator.isValid(any())).thenReturn(true);

    	Map<Long, FundingDecision> decision = asMap(1L, UNDECIDED);
    	
    	ServiceResult<Void> result = service.saveFundingDecisionData(competition.getId(), decision);
    	
    	assertTrue(result.isSuccess());
    	verify(applicationRepositoryMock).findByCompetitionId(competition.getId());
    	assertEquals(ApplicationState.OPENED, application1.getApplicationProcess().getProcessState());
    	assertEquals(ApplicationState.OPENED, application2.getApplicationProcess().getProcessState());
    	assertEquals(FundingDecisionStatus.UNDECIDED, application1.getFundingDecision());
    	assertEquals(FundingDecisionStatus.UNFUNDED, application2.getFundingDecision());
    	assertNull(competition.getFundersPanelEndDate());
    }

    @Test
    public void testSaveFundingDecisionDataWillResetEmailDate() {

        Long applicationId = 1L;
        Long competitionId = competition.getId();
        Application application1 = newApplication().withId(applicationId).withCompetition(competition).withFundingDecision(FundingDecisionStatus.FUNDED).withApplicationState(ApplicationState.OPENED).build();
        when(applicationRepositoryMock.findByCompetitionId(competitionId)).thenReturn(singletonList(application1));
        when(applicationFundingDecisionValidator.isValid(any())).thenReturn(true);


        Map<Long, FundingDecision> applicationDecisions = asMap(applicationId, UNDECIDED);

        ServiceResult<Void> result = service.saveFundingDecisionData(competitionId, applicationDecisions);

        assertTrue(result.isSuccess());
        verify(applicationRepositoryMock).findByCompetitionId(competitionId);
        verify(applicationServiceMock).setApplicationFundingEmailDateTime(applicationId, null);
    }

    @Test
    public void testSaveFundingDecisionDataWhenDecisionIsChanged() {
        Long applicationId = 1L;
        Long competitionId = competition.getId();
        Application application1 = newApplication()
                .withId(applicationId)
                .withCompetition(competition)
                .withFundingDecision(FundingDecisionStatus.UNDECIDED)
                .withApplicationState(ApplicationState.OPENED)
                .build();

        when(applicationRepositoryMock.findByCompetitionId(competitionId))
                .thenReturn(singletonList(application1));
        when(applicationFundingDecisionValidator.isValid(any()))
                .thenReturn(true);

        Map<Long, FundingDecision> applicationDecisions = asMap(applicationId, FUNDED);
        ServiceResult<Void> result = service.saveFundingDecisionData(competitionId, applicationDecisions);

        Map<Long, FundingDecision> changedApplicationDecisions = asMap(applicationId, UNFUNDED);
        ServiceResult<Void> changedResult = service.saveFundingDecisionData(competitionId, changedApplicationDecisions);

        assertTrue(result.isSuccess());
        assertTrue(changedResult.isSuccess());
        verify(applicationRepositoryMock, times(2)).findByCompetitionId(competitionId);
        verify(applicationServiceMock, times(2)).setApplicationFundingEmailDateTime(applicationId, null);
        verifyZeroInteractions(applicationWorkflowHandlerMock);

        assertTrue(FundingDecisionStatus.UNFUNDED.equals(application1.getFundingDecision()));
    }

    @Test
    public void testSaveFundingDecisionDataWontResetEmailDateForSameDecision() {
        Long applicationId = 1L;
        Long competitionId = competition.getId();
        Application application1 = newApplication().withId(applicationId).withCompetition(competition).withFundingDecision(FundingDecisionStatus.FUNDED).withApplicationState(ApplicationState.OPENED).build();
        when(applicationRepositoryMock.findByCompetitionId(competitionId)).thenReturn(singletonList(application1));
        when(applicationFundingDecisionValidator.isValid(any())).thenReturn(true);

        Map<Long, FundingDecision> applicationDecisions = asMap(applicationId, FUNDED);

        ServiceResult<Void> result = service.saveFundingDecisionData(competitionId, applicationDecisions);

        assertTrue(result.isSuccess());
        verify(applicationRepositoryMock).findByCompetitionId(competitionId);
        verify(applicationServiceMock, never()).setApplicationFundingEmailDateTime(any(Long.class), any(ZonedDateTime.class));
    }

    @Test
    public void testSaveFundingDecisionDataForCompetitionInProjectSetup() {
        Long unsuccessfulApplicationId = 246L;
        Long projectSetupCompetitionId = 456L;

        CompetitionType competitionType = newCompetitionType()
                .withName("Sector")
                .build();

        Competition projectSetupCompetition = newCompetition()
                .withCompetitionStatus(CompetitionStatus.PROJECT_SETUP)
                .withCompetitionType(competitionType)
                .withId(projectSetupCompetitionId)
                .build();

        projectSetupCompetition.setReleaseFeedbackDate(ZonedDateTime.now().minusDays(2L));
        when(competitionRepositoryMock.findById(projectSetupCompetitionId)).thenReturn(Optional.of(projectSetupCompetition));

        Application unsuccessfulApplication = newApplication()
                .withId(unsuccessfulApplicationId)
                .withCompetition(projectSetupCompetition)
                .withFundingDecision(FundingDecisionStatus.UNFUNDED)
                .withApplicationState(ApplicationState.SUBMITTED)
                .build();

        assertTrue(projectSetupCompetition.getCompetitionStatus().equals(CompetitionStatus.PROJECT_SETUP));

        when(applicationRepositoryMock.findByCompetitionId(projectSetupCompetitionId)).thenReturn(singletonList(unsuccessfulApplication));
        when(applicationFundingDecisionValidator.isValid(any())).thenReturn(true);
        when(applicationWorkflowHandlerMock.approve(unsuccessfulApplication)).thenReturn(true);

        Map<Long, FundingDecision> applicationDecision = asMap(unsuccessfulApplicationId, FUNDED);

        ServiceResult<Void> result = service.saveFundingDecisionData(projectSetupCompetitionId, applicationDecision);

        assertTrue(result.isSuccess());
        verify(applicationRepositoryMock).findByCompetitionId(projectSetupCompetitionId);
        verify(applicationServiceMock).setApplicationFundingEmailDateTime(unsuccessfulApplicationId, null);
        verify(applicationWorkflowHandlerMock).approve(any(Application.class));
        assertTrue(FundingDecisionStatus.FUNDED.equals(unsuccessfulApplication.getFundingDecision()));

    }

    public static Notification createNotificationExpectationsWithGlobalArgs(Notification expectedNotification) {

        return createLambdaMatcher(notification -> {
            assertEquals(expectedNotification.getFrom(), notification.getFrom());

            List<String> expectedToEmailAddresses = simpleMap(expectedNotification.getTo(), NotificationTarget::getEmailAddress);
            List<String> actualToEmailAddresses = simpleMap(notification.getTo(), NotificationTarget::getEmailAddress);
            assertEquals(expectedToEmailAddresses, actualToEmailAddresses);

            assertEquals(expectedNotification.getMessageKey(), notification.getMessageKey());
            assertEquals(expectedNotification.getGlobalArguments(), notification.getGlobalArguments());

            Map<NotificationTarget, Map<String, Object>> expectedTargetSpecifics = expectedNotification.getPerNotificationTargetArguments();
            Map<NotificationTarget, Map<String, Object>> actualTargetSpecifics = notification.getPerNotificationTargetArguments();

            assertEquals(expectedTargetSpecifics.size(), actualTargetSpecifics.size());

            expectedTargetSpecifics.forEach((target, expectedArguments) -> {
                Map<String, Object> actualArguments = actualTargetSpecifics.get(target);
                assertEquals(expectedArguments, actualArguments);
            });

            assertEquals(expectedTargetSpecifics, actualTargetSpecifics);
        });
    }

    public static Notification createSimpleNotificationExpectations(Notification expectedNotification) {

        return createLambdaMatcher(notification -> {
            assertEquals(expectedNotification.getFrom(), notification.getFrom());

            Collection<String> expectedTo = new TreeSet<>(Collator.getInstance());
            expectedTo.addAll(simpleMap(expectedNotification.getTo(), NotificationTarget::getEmailAddress));

            Collection<String> actualTo = new TreeSet<>(Collator.getInstance());
            actualTo.addAll(simpleMap(notification.getTo(), NotificationTarget::getEmailAddress));
            assertEquals(asList(expectedTo.toArray()), asList(actualTo.toArray()));

            assertEquals(expectedNotification.getMessageKey(), notification.getMessageKey());
        });
    }
}
