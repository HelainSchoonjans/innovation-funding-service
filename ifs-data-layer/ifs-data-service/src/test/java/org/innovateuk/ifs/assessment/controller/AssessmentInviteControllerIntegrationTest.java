package org.innovateuk.ifs.assessment.controller;

import com.google.common.collect.Lists;
import org.apache.commons.collections.IteratorUtils;
import org.innovateuk.ifs.BaseControllerIntegrationTest;
import org.innovateuk.ifs.category.domain.InnovationArea;
import org.innovateuk.ifs.category.repository.InnovationAreaRepository;
import org.innovateuk.ifs.commons.error.Error;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.competition.domain.Competition;
import org.innovateuk.ifs.competition.repository.CompetitionRepository;
import org.innovateuk.ifs.invite.constant.InviteStatus;
import org.innovateuk.ifs.invite.domain.ParticipantStatus;
import org.innovateuk.ifs.assessment.domain.AssessmentInvite;
import org.innovateuk.ifs.assessment.domain.AssessmentParticipant;
import org.innovateuk.ifs.invite.domain.RejectionReason;
import org.innovateuk.ifs.assessment.repository.AssessmentInviteRepository;
import org.innovateuk.ifs.assessment.repository.AssessmentParticipantRepository;
import org.innovateuk.ifs.invite.resource.*;
import org.innovateuk.ifs.profile.domain.Profile;
import org.innovateuk.ifs.profile.repository.ProfileRepository;
import org.innovateuk.ifs.user.domain.Agreement;
import org.innovateuk.ifs.user.domain.User;
import org.innovateuk.ifs.user.repository.AgreementRepository;
import org.innovateuk.ifs.user.repository.UserRepository;
import org.innovateuk.ifs.user.resource.Role;
import org.innovateuk.ifs.user.resource.UserResource;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Sets.newHashSet;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.time.ZonedDateTime.now;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.innovateuk.ifs.assessment.builder.AssessmentParticipantBuilder.newAssessmentParticipant;
import static org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions.id;
import static org.innovateuk.ifs.category.builder.InnovationAreaBuilder.newInnovationArea;
import static org.innovateuk.ifs.commons.error.CommonErrors.forbiddenError;
import static org.innovateuk.ifs.commons.error.CommonErrors.notFoundError;
import static org.innovateuk.ifs.commons.error.CommonFailureKeys.*;
import static org.innovateuk.ifs.assessment.builder.AssessmentInviteBuilder.newAssessmentInvite;
import static org.innovateuk.ifs.invite.builder.AssessorInviteSendResourceBuilder.newAssessorInviteSendResource;
import static org.innovateuk.ifs.invite.builder.CompetitionInviteStatisticsResourceBuilder.newCompetitionInviteStatisticsResource;
import static org.innovateuk.ifs.invite.builder.ExistingUserStagedInviteResourceBuilder.newExistingUserStagedInviteResource;
import static org.innovateuk.ifs.invite.builder.NewUserStagedInviteResourceBuilder.newNewUserStagedInviteResource;
import static org.innovateuk.ifs.invite.builder.RejectionReasonResourceBuilder.newRejectionReasonResource;
import static org.innovateuk.ifs.invite.constant.InviteStatus.*;
import static org.innovateuk.ifs.invite.domain.ParticipantStatus.*;
import static org.innovateuk.ifs.competition.domain.CompetitionParticipantRole.ASSESSOR;
import static org.innovateuk.ifs.profile.builder.ProfileBuilder.newProfile;
import static org.innovateuk.ifs.user.builder.AffiliationBuilder.newAffiliation;
import static org.innovateuk.ifs.user.builder.UserBuilder.newUser;
import static org.innovateuk.ifs.user.resource.AffiliationType.PROFESSIONAL;
import static org.innovateuk.ifs.user.resource.BusinessType.ACADEMIC;
import static org.innovateuk.ifs.user.resource.BusinessType.BUSINESS;
import static org.innovateuk.ifs.util.CollectionFunctions.simpleMap;
import static org.junit.Assert.*;
import static org.springframework.data.domain.Sort.Direction.ASC;

public class AssessmentInviteControllerIntegrationTest extends BaseControllerIntegrationTest<CompetitionInviteController> {

    private static final long INNOVATION_AREA_ID = 5L;

    @Autowired
    @Override
    protected void setControllerUnderTest(CompetitionInviteController controller) {
        this.controller = controller;
    }

    @Autowired
    private AssessmentInviteRepository assessmentInviteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AssessmentParticipantRepository assessmentParticipantRepository;

    @Autowired
    private CompetitionRepository competitionRepository;

    @Autowired
    private InnovationAreaRepository innovationAreaRepository;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private AgreementRepository agreementRepository;

    private Competition competition;

    private User paulPlum;
    private User felixWilson;

    @Before
    public void setup() {
        loginSystemRegistrationUser();

        competition = competitionRepository.findOne(1L);

        List<Profile> profiles = newProfile()
                .withId()
                .build(2);

        List<Profile> savedProfiles = Lists.newArrayList(profileRepository.save(profiles));

        paulPlum = userRepository.findByEmail("paul.plum@gmail.com").orElse(null);
        felixWilson = userRepository.findByEmail("felix.wilson@gmail.com").orElse(null);

        paulPlum.setProfileId(savedProfiles.get(0).getId());
        felixWilson.setProfileId(savedProfiles.get(1).getId());

        userRepository.save(asList(paulPlum, felixWilson));
    }

    @Test
    public void getAllInvitesToSend() {
        InnovationArea innovationArea = newInnovationArea().withName("innovation area").build();
        assessmentInviteRepository.save(
                newAssessmentInvite()
                        .with(id(null))
                        .withName("James Smith", "Peter Mason")
                        .withEmail("james@email.com", "peter@email.com")
                        .withUser()
                        .withHash("hash1", "hash2")
                        .withCompetition(competition)
                        .withStatus(CREATED)
                        .withInnovationArea(innovationArea)
                        .build(2)
        );

        loginCompAdmin();

        RestResult<AssessorInvitesToSendResource> serviceResult = controller.getAllInvitesToSend(competition.getId());
        assertTrue(serviceResult.isSuccess());

        AssessorInvitesToSendResource inviteResource = serviceResult.getSuccess();
        assertEquals(1L, inviteResource.getCompetitionId());
        assertEquals("Connected digital additive manufacturing", inviteResource.getCompetitionName());
        assertEquals(2, inviteResource.getRecipients().size());
        assertEquals("James Smith", inviteResource.getRecipients().get(0));
        assertEquals("Peter Mason", inviteResource.getRecipients().get(1));
        assertTrue(inviteResource.getContent().startsWith("Dear [recipient name]\n\nWe are inviting you to assess "));
    }

    @Test
    public void getAllInvitesToResend() {
        InnovationArea innovationArea = newInnovationArea().withName("innovation area").build();

        List<Long> inviteIds =  simpleMap(IteratorUtils.toList(assessmentInviteRepository.save(
                newAssessmentInvite()
                        .with(id(null))
                        .withName("James Smith", "Peter Mason")
                        .withEmail("james@email.com", "peter@email.com")
                        .withUser()
                        .withHash("hash1", "hash2")
                        .withCompetition(competition)
                        .withStatus(SENT)
                        .withInnovationArea(innovationArea)
                        .build(2)
        ).iterator()), AssessmentInvite::getId);

        loginCompAdmin();

        RestResult<AssessorInvitesToSendResource> serviceResult = controller.getAllInvitesToResend(competition.getId(), inviteIds);
        assertTrue(serviceResult.isSuccess());

        AssessorInvitesToSendResource inviteResource = serviceResult.getSuccess();
        assertEquals(1L, inviteResource.getCompetitionId());
        assertEquals("Connected digital additive manufacturing", inviteResource.getCompetitionName());
        assertEquals(2, inviteResource.getRecipients().size());
        assertEquals("James Smith", inviteResource.getRecipients().get(0));
        assertEquals("Peter Mason", inviteResource.getRecipients().get(1));
        assertTrue(inviteResource.getContent().startsWith("Dear [recipient name]\n\nWe are inviting you to assess "));
    }

    @Test
    public void getInvite() {
        assessmentInviteRepository.save(newAssessmentInvite()
                .with(id(null))
                .withName("tom poly")
                .withEmail("tom@poly.io")
                .withUser((User) null)
                .withHash("hash")
                .withCompetition(competition)
                .build());

        RestResult<CompetitionInviteResource> serviceResult = controller.getInvite("hash");
        assertTrue(serviceResult.isSuccess());

        CompetitionInviteResource inviteResource = serviceResult.getSuccess();
        assertEquals("Connected digital additive manufacturing", inviteResource.getCompetitionName());
    }

    @Test
    public void getInvite_hashNotExists() {
        RestResult<CompetitionInviteResource> serviceResult = controller.getInvite("not exists hash");
        assertTrue(serviceResult.isFailure());
        assertTrue(serviceResult.getFailure().is(notFoundError(AssessmentInvite.class, "not exists hash")));
    }

    @Test
    public void openInvite() {
        assessmentInviteRepository.save(newAssessmentInvite()
                .with(id(null))
                .withName("name")
                .withEmail("tom@poly.io")
                .withUser((User) null)
                .withHash("hash")
                .withCompetition(competition)
                .build());

        RestResult<CompetitionInviteResource> serviceResult = controller.openInvite("hash");
        assertTrue(serviceResult.isSuccess());

        CompetitionInviteResource inviteResource = serviceResult.getSuccess();
        assertEquals("Connected digital additive manufacturing", inviteResource.getCompetitionName());
    }

    @Test
    public void openInvite_hashNotExists() {
        RestResult<CompetitionInviteResource> serviceResult = controller.openInvite("not exists hash");
        assertTrue(serviceResult.isFailure());
        assertTrue(serviceResult.getFailure().is(notFoundError(AssessmentInvite.class, "not exists hash")));
    }

    @Test
    public void checkExistingUser_userExistsOnInvite() {
        User user = userRepository.save(newUser()
                .with(id(null))
                .withEmailAddress("tom@poly.io")
                .withUid("a36c4aff-7840-4cd8-b5dd-5c945b8d9959")
                .build());

        // Save an invite for the User
        assessmentInviteRepository.save(newAssessmentInvite()
                .with(id(null))
                .withName("name")
                .withEmail("tom@poly.io")
                .withUser(user)
                .withHash("hash")
                .withCompetition(competition)
                .build());

        RestResult<Boolean> serviceResult = controller.checkExistingUser("hash");
        assertTrue(serviceResult.isSuccess());

        Boolean existingUser = serviceResult.getSuccess();
        assertTrue(existingUser);
    }

    @Test
    public void checkExistingUser_userExistsForEmail() {
        userRepository.save(newUser()
                .with(id(null))
                .withEmailAddress("user-exists@for-this.address")
                .withUid("a36c4aff-7840-4cd8-b5dd-5c945b8d9959")
                .build());

        // Save an invite without a User but with an e-mail address for which a User exists
        assessmentInviteRepository.save(newAssessmentInvite()
                .with(id(null))
                .withName("name")
                .withEmail("user-exists@for-this.address")
                .withUser((User) null)
                .withHash("hash")
                .withCompetition(competition)
                .build());

        RestResult<Boolean> serviceResult = controller.checkExistingUser("hash");
        assertTrue(serviceResult.isSuccess());

        Boolean existingUser = serviceResult.getSuccess();
        assertTrue(existingUser);
    }

    @Test
    public void checkExistingUser_userNotExists() {
        // Save an invite without a User and with an e-mail address for which no User exists
        assessmentInviteRepository.save(newAssessmentInvite()
                .with(id(null))
                .withName("name")
                .withEmail("no-user-exists@for-this.address")
                .withUser((User) null)
                .withHash("hash")
                .withCompetition(competition)
                .build());


        RestResult<Boolean> serviceResult = controller.checkExistingUser("hash");
        assertTrue(serviceResult.isSuccess());

        Boolean existingUser = serviceResult.getSuccess();
        assertFalse(existingUser);
    }

    @Test
    public void checkExistingUser_hashNotExists() {
        RestResult<Boolean> serviceResult = controller.checkExistingUser("not exists hash");
        assertTrue(serviceResult.isFailure());
        assertTrue(serviceResult.getFailure().is(notFoundError(AssessmentInvite.class, "not exists hash")));
    }

    @Test
    public void acceptInvite_participantIsDifferentUser() {
        assessmentParticipantRepository.save(newAssessmentParticipant()
                .with(id(null))
                .withStatus(PENDING)
                .withRole(ASSESSOR)
                .withCompetition(competition)
                .withInvite(newAssessmentInvite()
                        .with(id(null))
                        .withName("name")
                        .withEmail("no-user-exists@for-this.address")
                        .withUser(newUser().withId(1L))
                        .withHash("hash")
                        .withCompetition(competition)
                        .build())
                .withUser(newUser().withId(1L))
                .build());
        assertTrue(controller.openInvite("hash").isSuccess());
        loginPaulPlum();

        RestResult<Void> serviceResult = controller.acceptInvite("hash");
        assertTrue(serviceResult.isFailure());
        assertTrue(serviceResult.getFailure().is(forbiddenError(GENERAL_SPRING_SECURITY_FORBIDDEN_ACTION)));
    }

    @Test
    public void acceptInvite_noParticipantUserAndInviteHasSameEmail() {
        assessmentParticipantRepository.save(newAssessmentParticipant()
                .with(id(null))
                .withStatus(PENDING)
                .withRole(ASSESSOR)
                .withCompetition(competition)
                .withInvite(newAssessmentInvite()
                        .with(id(null))
                        .withName("name")
                        .withEmail("paul.plum@gmail.com")
                        .withUser((User[]) null)
                        .withHash("hash")
                        .withCompetition(competition)
                        .withInnovationArea(newInnovationArea().build())
                        .build())
                .withUser((User[]) null)
                .build());

        assertTrue(controller.openInvite("hash").isSuccess());
        loginPaulPlum();

        RestResult<Void> serviceResult = controller.acceptInvite("hash");
        assertTrue(serviceResult.isSuccess());
    }

    @Test
    public void acceptInvite_noParticipantUserAndInviteHasDifferentEmail() {
        assessmentParticipantRepository.save(newAssessmentParticipant()
                .with(id(null))
                .withStatus(PENDING)
                .withRole(ASSESSOR)
                .withCompetition(competition)
                .withInvite(newAssessmentInvite()
                        .with(id(null))
                        .withName("name")
                        .withEmail("no-user-exists@for-this.address")
                        .withUser((User[]) null)
                        .withHash("hash")
                        .withCompetition(competition)
                        .build())
                .withUser((User[]) null)
                .build());
        assertTrue(controller.openInvite("hash").isSuccess());

        loginPaulPlum();

        RestResult<Void> serviceResult = controller.acceptInvite("hash");

        assertTrue(serviceResult.isFailure());
        assertTrue(serviceResult.getFailure().is(forbiddenError(GENERAL_SPRING_SECURITY_FORBIDDEN_ACTION)));
    }

    @Test
    public void acceptInvite() throws Exception {
        assessmentParticipantRepository.save(newAssessmentParticipant()
                .with(id(null))
                .withStatus(PENDING)
                .withRole(ASSESSOR)
                .withCompetition(competition)
                .withInvite(newAssessmentInvite()
                        .with(id(null))
                        .withName("name")
                        .withEmail("paul.plum@gmail.com")
                        .withUser(newUser().withId(getPaulPlum().getId()).build())
                        .withHash("hash")
                        .withCompetition(competition)
                        .build())
                .build());

        assertTrue(controller.openInvite("hash").isSuccess());

        loginPaulPlum();

        RestResult<Void> serviceResult = controller.acceptInvite("hash");
        assertTrue(serviceResult.isSuccess());
    }

    @Test
    public void acceptInvite_newAssessor() throws Exception {
        InnovationArea innovationArea = innovationAreaRepository.findOne(5L);
        assessmentParticipantRepository.save(newAssessmentParticipant()
                .with(id(null))
                .withStatus(PENDING)
                .withRole(ASSESSOR)
                .withCompetition(competition)
                .withInvite(newAssessmentInvite()
                        .with(id(null))
                        .withName("name")
                        .withEmail("paul.plum@gmail.com")
                        .withHash("hash")
                        .withCompetition(competition)
                        .withInnovationArea(innovationArea)
                        .build())
                .build());

        assertTrue(controller.openInvite("hash").isSuccess());

        loginPaulPlum();

        RestResult<Void> serviceResult = controller.acceptInvite("hash");
        assertTrue(serviceResult.isSuccess());

        Profile profile = profileRepository.findOne(getPaulPlum().getProfileId());

        assertEquals(singleton(innovationArea), profile.getInnovationAreas());
    }

    @Test
    public void acceptInvite_hashNotExists() throws Exception {
        loginPaulPlum();
        RestResult<Void> serviceResult = controller.acceptInvite("hash not exists");
        assertTrue(serviceResult.isFailure());
        assertTrue(serviceResult.getFailure().is(forbiddenError(GENERAL_SPRING_SECURITY_FORBIDDEN_ACTION)));
    }

    @Test
    public void acceptInvite_notOpened() throws Exception {
        assessmentParticipantRepository.save(newAssessmentParticipant()
                .with(id(null))
                .withStatus(PENDING)
                .withRole(ASSESSOR)
                .withCompetition(competition)
                .withInvite(newAssessmentInvite()
                        .with(id(null))
                        .withName("name")
                        .withEmail("paul.plum@gmail.com")
                        .withUser((User) null)
                        .withHash("hash")
                        .withCompetition(competition)
                        .build())
                .build());

        loginPaulPlum();

        RestResult<Void> serviceResult = controller.acceptInvite("hash");

        assertTrue(serviceResult.isFailure());
        assertTrue(serviceResult.getFailure().is(new Error(COMPETITION_PARTICIPANT_CANNOT_ACCEPT_UNOPENED_INVITE, "Connected digital additive manufacturing")));
    }

    @Test
    public void acceptInvite_rejected() throws Exception {
        assessmentParticipantRepository.save(newAssessmentParticipant()
                .with(id(null))
                .withStatus(PENDING)
                .withRole(ASSESSOR)
                .withCompetition(competition)
                .withInvite(newAssessmentInvite()
                        .with(id(null))
                        .withName("name")
                        .withEmail("paul.plum@gmail.com")
                        .withUser((User) null)
                        .withHash("hash")
                        .withCompetition(competition)
                        .build())
                .build());
        controller.openInvite("hash");


        CompetitionRejectionResource competitionRejectionResource =
                new CompetitionRejectionResource(newRejectionReasonResource().withId(1L).build(), "too busy");
        RestResult<Void> serviceResult = controller.rejectInvite("hash", competitionRejectionResource);
        assertTrue(serviceResult.isSuccess());

        loginPaulPlum();
        RestResult<Void> acceptResult = controller.acceptInvite("hash");

        assertTrue(acceptResult.isFailure());
        assertTrue(acceptResult.getFailure().is(new Error(COMPETITION_PARTICIPANT_CANNOT_ACCEPT_ALREADY_REJECTED_INVITE, "Connected digital additive manufacturing")));
    }

    @Test
    public void rejectInvite() throws Exception {
        assessmentParticipantRepository.save(newAssessmentParticipant()
                .with(id(null))
                .withStatus(PENDING)
                .withRole(ASSESSOR)
                .withCompetition(competition)
                .withInvite(newAssessmentInvite()
                        .with(id(null))
                        .withName("name")
                        .withEmail("no-user-exists@for-this.address")
                        .withUser((User) null)
                        .withHash("hash")
                        .withCompetition(competition)
                        .build())
                .build());
        controller.openInvite("hash");

        CompetitionRejectionResource competitionRejectionResource =
                new CompetitionRejectionResource(newRejectionReasonResource().withId(1L).build(), "too busy");
        RestResult<Void> serviceResult = controller.rejectInvite("hash", competitionRejectionResource);
        assertTrue(serviceResult.isSuccess());
    }

    @Test
    public void rejectInvite_noReasonComment() throws Exception {
        assessmentParticipantRepository.save(newAssessmentParticipant()
                .with(id(null))
                .withStatus(PENDING)
                .withRole(ASSESSOR)
                .withCompetition(competition)
                .withInvite(newAssessmentInvite()
                        .with(id(null))
                        .withName("name")
                        .withEmail("no-user-exists@for-this.address")
                        .withUser((User) null)
                        .withHash("hash")
                        .withCompetition(competition)
                        .build())
                .build());
        controller.openInvite("hash");

        CompetitionRejectionResource competitionRejectionResource =
                new CompetitionRejectionResource(newRejectionReasonResource().withId(1L).build(), null);
        RestResult<Void> serviceResult = controller.rejectInvite("hash", competitionRejectionResource);
        assertTrue(serviceResult.isSuccess());
    }

    @Test
    public void rejectInvite_accepted() throws Exception {
        assessmentParticipantRepository.save(newAssessmentParticipant()
                .with(id(null))
                .withStatus(PENDING)
                .withRole(ASSESSOR)
                .withCompetition(competition)
                .withInvite(newAssessmentInvite()
                        .with(id(null))
                        .withName("name")
                        .withEmail("paul.plum@gmail.com")
                        .withUser((User) null)
                        .withHash("hash")
                        .withCompetition(competition)
                        .withInnovationArea(newInnovationArea().build())
                        .build())
                .build());
        controller.openInvite("hash");

        loginPaulPlum();

        RestResult<Void> serviceResult = controller.acceptInvite("hash");
        assertTrue(serviceResult.isSuccess());

        CompetitionRejectionResource competitionRejectionResource =
                new CompetitionRejectionResource(newRejectionReasonResource().withId(1L).build(), "too busy");

        loginSystemRegistrationUser();
        RestResult<Void> rejectResult = controller.rejectInvite("hash", competitionRejectionResource);

        assertTrue(rejectResult.isFailure());
        assertTrue(rejectResult.getFailure().is(new Error(COMPETITION_PARTICIPANT_CANNOT_REJECT_ALREADY_ACCEPTED_INVITE, "Connected digital additive manufacturing")));
    }

    @Test
    public void rejectInvite_notOpened() throws Exception {
        assessmentParticipantRepository.save(newAssessmentParticipant()
                .with(id(null))
                .withStatus(PENDING)
                .withRole(ASSESSOR)
                .withCompetition(competition)
                .withInvite(newAssessmentInvite()
                        .with(id(null))
                        .withName("name")
                        .withEmail("no-user-exists@for-this.address")
                        .withUser((User) null)
                        .withHash("hash")
                        .withCompetition(competition)
                        .build())
                .build());

        CompetitionRejectionResource competitionRejectionResource =
                new CompetitionRejectionResource(newRejectionReasonResource().withId(1L).build(), "too busy");
        RestResult<Void> serviceResult = controller.rejectInvite("hash", competitionRejectionResource);

        assertTrue(serviceResult.isFailure());
        assertTrue(serviceResult.getFailure().is(new Error(COMPETITION_PARTICIPANT_CANNOT_REJECT_UNOPENED_INVITE, "Connected digital additive manufacturing")));
    }

    @Test
    public void rejectInvite_unknownReason() throws Exception {
        assessmentParticipantRepository.save(newAssessmentParticipant()
                .with(id(null))
                .withStatus(PENDING)
                .withRole(ASSESSOR)
                .withCompetition(competition)
                .withInvite(newAssessmentInvite()
                        .with(id(null))
                        .withName("name")
                        .withEmail("no-user-exists@for-this.address")
                        .withUser((User) null)
                        .withHash("hash")
                        .withCompetition(competition)
                        .build())
                .build());
        controller.openInvite("hash");

        CompetitionRejectionResource competitionRejectionResource =
                new CompetitionRejectionResource(newRejectionReasonResource().withId(-1L).build(), "too busy");
        RestResult<Void> serviceResult = controller.rejectInvite("hash", competitionRejectionResource);
        assertTrue(serviceResult.isFailure());
        assertTrue(serviceResult.getFailure().is(notFoundError(RejectionReason.class, -1L)));
    }

    @Test
    public void inviteNewUser() throws Exception {
        InnovationArea innovationArea = innovationAreaRepository.findOne(5L);

        NewUserStagedInviteResource newUserStagedInvite = newNewUserStagedInviteResource()
                .withName("new user name")
                .withEmail("no-other-user-exists@for-this.address")
                .withCompetitionId(competition.getId())
                .withInnovationAreaId(innovationArea.getId())
                .build();

        loginCompAdmin();
        RestResult<CompetitionInviteResource> serviceResult = controller.inviteNewUser(newUserStagedInvite);

        assertTrue(serviceResult.isSuccess());

        CompetitionInviteResource resource = serviceResult.getSuccess();

        assertEquals(competition.getName(), resource.getCompetitionName());
        assertEquals(CREATED, resource.getStatus());
    }

    @Test
    public void inviteNewUsers() throws Exception {
        InnovationArea innovationArea = innovationAreaRepository.findOne(5L);

        NewUserStagedInviteListResource newUserInvites = buildNewUserInviteList(competition.getId(), innovationArea.getId());

        loginCompAdmin();
        RestResult<Void> serviceResult = controller.inviteNewUsers(newUserInvites, competition.getId());

        assertTrue(serviceResult.isSuccess());
    }

    @Test
    public void inviteExistingUsers() throws Exception {
        ExistingUserStagedInviteListResource newUserInvites = buildExistingUserInviteList(competition.getId());

        loginCompAdmin();
        RestResult<Void> serviceResult = controller.inviteUsers(newUserInvites);

        assertTrue(serviceResult.isSuccess());
    }

    @Test
    public void inviteNewUsers_competitionNotFound() throws Exception {
        long competitionId = 10000L;
        assertNull(competitionRepository.findOne(competitionId));

        InnovationArea innovationArea = innovationAreaRepository.findOne(5L);

        loginCompAdmin();
        NewUserStagedInviteListResource newUserInvites = buildNewUserInviteList(competitionId, innovationArea.getId());
        RestResult<Void> serviceResult = controller.inviteNewUsers(newUserInvites, competitionId);

        assertFalse(serviceResult.isSuccess());
        assertTrue(serviceResult.getFailure().is(notFoundError(Competition.class, competitionId)));
    }

    @Test
    public void inviteNewUsers_innovationAreaNotFound() throws Exception {
        long innovationAreaId = 10000L;
        assertNull(innovationAreaRepository.findOne(innovationAreaId));

        loginCompAdmin();
        NewUserStagedInviteListResource newUserInvites = buildNewUserInviteList(competition.getId(), innovationAreaId);
        RestResult<Void> serviceResult = controller.inviteNewUsers(newUserInvites, competition.getId());

        assertFalse(serviceResult.isSuccess());
        assertEquals(2, serviceResult.getFailure().getErrors().size());
        assertEquals("invites[0].innovationArea", serviceResult.getFailure().getErrors().get(0).getFieldName());
        assertEquals(innovationAreaId, serviceResult.getFailure().getErrors().get(0).getFieldRejectedValue());
        assertEquals("invites[1].innovationArea", serviceResult.getFailure().getErrors().get(1).getFieldName());
        assertEquals(innovationAreaId, serviceResult.getFailure().getErrors().get(1).getFieldRejectedValue());
    }

    @Test
    public void inviteNewUsers_userExists() throws Exception {
        InnovationArea innovationArea = innovationAreaRepository.findOne(5L);

        assessmentInviteRepository.save(new AssessmentInvite("Test Name 1", "testname1@for-this.address", "hash", competition, innovationArea));

        loginCompAdmin();
        NewUserStagedInviteListResource newUserInvites = buildNewUserInviteList(competition.getId(), innovationArea.getId());
        RestResult<Void> serviceResult = controller.inviteNewUsers(newUserInvites, competition.getId());

        assertFalse(serviceResult.isSuccess());
        assertEquals(1, serviceResult.getFailure().getErrors().size());
        assertEquals("invites[0].email", serviceResult.getFailure().getErrors().get(0).getFieldName());
        assertEquals("testname1@for-this.address", serviceResult.getFailure().getErrors().get(0).getFieldRejectedValue());
    }

    private NewUserStagedInviteListResource buildNewUserInviteList(long competitionId, long innovationAreaId) {
        return new NewUserStagedInviteListResource(
                newNewUserStagedInviteResource()
                        .withName("Test Name 1", "Test Name 2")
                        .withEmail("testname1@for-this.address", "testname2@for-this.address")
                        .withCompetitionId(competitionId)
                        .withInnovationAreaId(innovationAreaId)
                        .build(2)
        );
    }

    private ExistingUserStagedInviteListResource buildExistingUserInviteList(long competitionId) {
        return new ExistingUserStagedInviteListResource(
                newExistingUserStagedInviteResource()
                        .withCompetitionId(competitionId)
                        .build(2)
        );
    }

    @Test
    public void sendAllInvites() throws Exception {
        List<AssessmentInvite> invitesToSend = newAssessmentInvite()
                .with(id(null))
                .withName("tom poly", "cari poly")
                .withEmail("tom@poly.io", "cari@poly.io")
                .withUser()
                .withHash("hash1", "hash2")
                .withCompetition(competition, competition)
                .withStatus(CREATED, CREATED)
                .build(2);
        assessmentInviteRepository.save(invitesToSend);

        AssessorInviteSendResource assessorInviteSendResource = newAssessorInviteSendResource()
                .withSubject("subject")
                .withContent("content")
                .build();

        loginCompAdmin();

        RestResult<Void> serviceResult = controller.sendAllInvites(competition.getId(), assessorInviteSendResource);
        assertTrue(serviceResult.isSuccess());
    }

    @Test
    public void resendInvites() throws Exception {
        ZonedDateTime initialInviteDate = ZonedDateTime.now();
        List<AssessmentInvite> invitesToResend = newAssessmentInvite()
                .with(id(null))
                .withName("tom poly", "cari poly")
                .withEmail("tom@poly.io", "cari@poly.io")
                .withUser()
                .withHash("hash1", "hash2")
                .withCompetition(competition, competition)
                .withStatus(SENT, SENT)
                .withSentOn(initialInviteDate)
                .build(2);
        assessmentInviteRepository.save(invitesToResend);

        List<AssessmentParticipant> assessmentParticipants = newAssessmentParticipant()
                .with(id(null))
                .withStatus(PENDING, REJECTED)
                .withRole(ASSESSOR, ASSESSOR)
                .withCompetition(competition, competition)
                .withInvite(invitesToResend.get(0), invitesToResend.get(1))
                .withUser()
                .build(2);

        competitionParticipantRepository.save(assessmentParticipants);

        List<Long> inviteIds =  simpleMap(IteratorUtils.toList(assessmentInviteRepository.save(invitesToResend)
                .iterator()), AssessmentInvite::getId);

        AssessorInviteSendResource assessorInviteSendResource = newAssessorInviteSendResource()
                .withSubject("subject")
                .withContent("content")
                .build();

        loginCompAdmin();

        RestResult<Void> serviceResult = controller.resendInvites(inviteIds, assessorInviteSendResource);
        assertTrue(serviceResult.isSuccess());

        AssessmentInvite inviteOne = assessmentInviteRepository.findOne(inviteIds.get(0));
        ZonedDateTime inviteOneResendDate = inviteOne.getSentOn();
        AssessmentInvite inviteTwo = assessmentInviteRepository.findOne(inviteIds.get(1));
        ZonedDateTime inviteTwoResendDate = inviteTwo.getSentOn();

        assertTrue(inviteOneResendDate.isAfter(initialInviteDate));
        assertTrue(inviteTwoResendDate.isAfter(initialInviteDate));
    }

    @Test
    public void sendAllInvites_toExistingApplicant() throws Exception {
        final UserResource applicantUser = getSteveSmith();

        assessmentInviteRepository.save(
                newAssessmentInvite()
                        .with(id(null))
                        .withName(applicantUser.getName())
                        .withEmail(applicantUser.getEmail())
                        .withUser()
                        .withHash("hash")
                        .withCompetition(competition)
                        .withStatus(CREATED)
                        .withInnovationArea(innovationAreaRepository.findOne(INNOVATION_AREA_ID)) // 'new invite'
                        .build()
        );

        AssessorInviteSendResource assessorInviteSendResource = newAssessorInviteSendResource()
                .withSubject("subject")
                .withContent("content")
                .build();

        loginCompAdmin();

        controller.sendAllInvites(competition.getId(), assessorInviteSendResource).getSuccess();

        User invitedUser = userRepository.findByEmail(applicantUser.getEmail()).get();
        assertTrue(invitedUser.getRoles().contains(Role.ASSESSOR));
    }

    @Test
    public void getInviteStatistics() throws Exception {
        loginCompAdmin();
        assessmentInviteRepository.save(newAssessmentInvite()
                .with(id(null))
                .withStatus(CREATED, SENT, OPENED)
                .withCompetition(competition)
                .withName("created", "sent", "opened")
                .withEmail("created@competition.com", "sent@competition.com", "opened@competition.com")
                .withHash("created", "sent", "opened")
                .build(3));
        assessmentParticipantRepository.save(newAssessmentParticipant()
                .with(id(null))
                .withCompetition(competition)
                .withRole(ASSESSOR)
                .withStatus(ACCEPTED, REJECTED, ACCEPTED, REJECTED, REJECTED, ACCEPTED, ACCEPTED)
                .build(7));
        CompetitionInviteStatisticsResource expected = newCompetitionInviteStatisticsResource()
                .withInviteList(1)
                .withInvited(2)
                .withDeclined(3)
                .withAccepted(4)
                .build();

        CompetitionInviteStatisticsResource statisticsResource = controller.getInviteStatistics(1L).getSuccess();
        assertEquals(expected, statisticsResource);
    }

    @Test
    public void getAvailableAssessors() throws Exception {
        loginCompAdmin();

        addTestAssessors();

        Pageable pageable = new PageRequest(0, 20, new Sort(ASC, "firstName"));
        Optional<Long> innovationArea = Optional.of(5L);

        AvailableAssessorPageResource availableAssessorPageResource = controller.getAvailableAssessors(competition.getId(), pageable, innovationArea)
                .getSuccess();

        assertEquals(20, availableAssessorPageResource.getSize());
        assertEquals(0, availableAssessorPageResource.getNumber());
        assertEquals(1, availableAssessorPageResource.getTotalPages());
        assertEquals(4, availableAssessorPageResource.getTotalElements());

        List<AvailableAssessorResource> availableAssessorResources = availableAssessorPageResource.getContent();

        assertEquals(4, availableAssessorResources.size());
        assertEquals("Andrew Marr", availableAssessorResources.get(0).getName());
        assertEquals("James Blake", availableAssessorResources.get(1).getName());
        assertEquals("Jessica Alba", availableAssessorResources.get(2).getName());
        assertEquals("Victoria Beckham", availableAssessorResources.get(3).getName());
    }

    @Test
    public void getAvailableAssessors_nextPage() throws Exception {
        loginCompAdmin();

        addTestAssessors();

        Pageable pageable = new PageRequest(1, 2, new Sort(ASC, "firstName"));
        Optional<Long> innovationArea = Optional.of(5L);

        AvailableAssessorPageResource availableAssessorPageResource = controller.getAvailableAssessors(competition.getId(), pageable, innovationArea)
                .getSuccess();

        assertEquals(2, availableAssessorPageResource.getSize());
        assertEquals(1, availableAssessorPageResource.getNumber());
        assertEquals(2, availableAssessorPageResource.getTotalPages());
        assertEquals(4, availableAssessorPageResource.getTotalElements());

        List<AvailableAssessorResource> availableAssessorResources = availableAssessorPageResource.getContent();

        assertEquals(2, availableAssessorResources.size());
        assertEquals("Jessica Alba", availableAssessorResources.get(0).getName());
        assertEquals("Victoria Beckham", availableAssessorResources.get(1).getName());
    }

    @Test
    public void getAvailableAssessors_noInnovationArea() throws Exception {
        loginCompAdmin();

        addTestAssessors();

        Pageable pageable = new PageRequest(0, 6, new Sort(ASC, "firstName"));

        AvailableAssessorPageResource availableAssessorPageResource = controller.getAvailableAssessors(competition.getId(), pageable, empty())
                .getSuccess();

        assertEquals(6, availableAssessorPageResource.getSize());
        assertEquals(0, availableAssessorPageResource.getNumber());
        assertEquals(1, availableAssessorPageResource.getTotalPages());
        assertEquals(6, availableAssessorPageResource.getTotalElements());

        List<AvailableAssessorResource> content = availableAssessorPageResource.getContent();
        assertEquals(6, content.size());
        assertEquals("Andrew Marr", content.get(0).getName());
        assertEquals("Felix Wilson", content.get(1).getName());
        assertEquals("James Blake", content.get(2).getName());
        assertEquals("Jessica Alba", content.get(3).getName());
        assertEquals("Professor Plum", content.get(4).getName());
        assertEquals("Victoria Beckham", content.get(5).getName());
    }

    @Test
    public void getAvailableAssessors_sortsFirstAndLastName() throws Exception {
        loginCompAdmin();

        InnovationArea innovationArea = innovationAreaRepository.findOne(INNOVATION_AREA_ID);

        List<Profile> profiles = newProfile()
                .withId()
                .withInnovationArea(innovationArea)
                .build(4);

        List<Profile> savedProfiles = Lists.newArrayList(profileRepository.save(profiles));

        Long[] profileIds = simpleMap(savedProfiles, Profile::getId).toArray(new Long[savedProfiles.size()]);

        List<User> users = newUser()
                .withId()
                .withUid("uid1", "uid2", "uid3", "uid4")
                .withFirstName("Robert", "Robert", "Alexis", "Alexis")
                .withLastName("Stark", "Salt", "Kinney", "Colon")
                .withRoles(singleton(Role.ASSESSOR))
                .withProfileId(profileIds)
                .build(4);

        userRepository.save(users);
        flushAndClearSession();

        Pageable pageable = new PageRequest(0, 10, new Sort(ASC, "firstName", "lastName"));

        AvailableAssessorPageResource availableAssessorPageResource = controller.getAvailableAssessors(competition.getId(), pageable, empty())
                .getSuccess();

        assertEquals(10, availableAssessorPageResource.getSize());
        assertEquals(0, availableAssessorPageResource.getNumber());
        assertEquals(1, availableAssessorPageResource.getTotalPages());
        assertEquals(6, availableAssessorPageResource.getTotalElements());

        List<AvailableAssessorResource> items = availableAssessorPageResource.getContent();

        assertEquals(6, items.size());
        assertEquals("Alexis Colon", items.get(0).getName());
        assertEquals("Alexis Kinney", items.get(1).getName());
        assertEquals("Felix Wilson", items.get(2).getName());
        assertEquals("Professor Plum", items.get(3).getName());
        assertEquals("Robert Salt", items.get(4).getName());
        assertEquals("Robert Stark", items.get(5).getName());
    }

    @Test
    public void getAvailableAssessors_all() throws Exception {
        loginCompAdmin();
        addTestAssessors();

        Optional<Long> innovationArea = Optional.of(5L);

        List<Long> availableAssessorIds = controller.getAvailableAssessorIds(competition.getId(), innovationArea)
                .getSuccess();

        assertEquals(4, availableAssessorIds.size());
    }

    private void addTestAssessors() {
        InnovationArea innovationArea = innovationAreaRepository.findOne(INNOVATION_AREA_ID);

        List<Profile> profiles = newProfile()
                .withId()
                .withInnovationArea(innovationArea)
                .build(4);

        List<Profile> savedProfiles = Lists.newArrayList(profileRepository.save(profiles));

        Long[] profileIds = simpleMap(savedProfiles, Profile::getId).toArray(new Long[savedProfiles.size()]);

        List<User> users = newUser()
                .withId()
                .withUid("uid1", "uid2", "uid3", "uid4")
                .withFirstName("Victoria", "James", "Jessica", "Andrew")
                .withLastName("Beckham", "Blake", "Alba", "Marr")
                .withRoles(singleton(Role.ASSESSOR))
                .withProfileId(profileIds[0], profileIds[1], profileIds[2], profileIds[3])
                .build(4);

        userRepository.save(users);
        flushAndClearSession();
    }

    @Test
    public void getCreatedInvites() throws Exception {
        loginCompAdmin();

        addTestCreatedInvites();

        Pageable pageable = new PageRequest(0, 20, new Sort(ASC, "name"));

        AssessorCreatedInvitePageResource createdInvitesPageResource = controller.getCreatedInvites(competition.getId(), pageable)
                .getSuccess();

        assertEquals(20, createdInvitesPageResource.getSize());
        assertEquals(6, createdInvitesPageResource.getTotalElements());
        assertEquals(1, createdInvitesPageResource.getTotalPages());
        assertEquals(0, createdInvitesPageResource.getNumber());

        List<AssessorCreatedInviteResource> pageItems = createdInvitesPageResource.getContent();

        assertEquals(6, pageItems.size());
        assertEquals("Angela Merkel", pageItems.get(0).getName());
        assertEquals("Bill Gates", pageItems.get(1).getName());
        assertEquals("Felix Wilson", pageItems.get(2).getName());
        assertEquals("Paul Plum", pageItems.get(3).getName());
        assertEquals("Serena Williams", pageItems.get(4).getName());
        assertEquals("Will Smith", pageItems.get(5).getName());
    }

    @Test
    public void getCreatedInvites_nextPage() throws Exception {
        loginCompAdmin();

        addTestCreatedInvites();

        Pageable pageable = new PageRequest(1, 2, new Sort(ASC, "name"));

        AssessorCreatedInvitePageResource createdInvitesPageResource = controller.getCreatedInvites(competition.getId(), pageable)
                .getSuccess();

        assertEquals(2, createdInvitesPageResource.getSize());
        assertEquals(6, createdInvitesPageResource.getTotalElements());
        assertEquals(3, createdInvitesPageResource.getTotalPages());
        assertEquals(1, createdInvitesPageResource.getNumber());

        List<AssessorCreatedInviteResource> pageItems = createdInvitesPageResource.getContent();

        assertEquals(2, pageItems.size());
        assertEquals("Felix Wilson", pageItems.get(0).getName());
        assertEquals("Paul Plum", pageItems.get(1).getName());
    }

    private void addTestCreatedInvites() {
        InnovationArea innovationArea = innovationAreaRepository.findOne(5L);

        List<AssessmentInvite> createdInvites = newAssessmentInvite()
                .withId()
                .withName("Will Smith", "Bill Gates", "Serena Williams", "Angela Merkel")
                .withEmail("ws@test.com", "bg@test.com", "sw@test.com", "am@test.com")
                .withStatus(CREATED)
                .withCompetition(competition)
                .withInnovationArea(innovationArea)
                .build(4);

        List<AssessmentInvite> existingUserInvites = newAssessmentInvite()
                .withId()
                .withName("Paul Plum", "Felix Wilson")
                .withEmail("paul.plum@gmail.com", "felix.wilson@gmail.com")
                .withUser(paulPlum, felixWilson)
                .withCompetition(competition)
                .withStatus(CREATED)
                .withInnovationArea()
                .build(2);

        createdInvites.addAll(existingUserInvites);

        assessmentInviteRepository.save(createdInvites);
        flushAndClearSession();
    }

    @Test
    public void getAssessorsNotAcceptedInviteIds() throws Exception {
        loginCompAdmin();

        InnovationArea innovationArea = innovationAreaRepository.findOne(5L);
        InnovationArea otherInnovationArea = innovationAreaRepository.findOne(10L);

        Optional<Long> innovationAreaId = of(innovationArea.getId());
        List<ParticipantStatus> status = singletonList(PENDING);
        Optional<Boolean> hasContract = of(TRUE);

        Agreement agreement = agreementRepository.findOne(1L);

        Profile profile1 = profileRepository.findOne(paulPlum.getProfileId());
        Profile profile2 = profileRepository.findOne(felixWilson.getProfileId());

        profile1.setBusinessType(ACADEMIC);
        profile1.setSkillsAreas("Skill area 1");
        profile1.setAgreement(agreement);
        profile1.setAgreementSignedDate(now().minusDays(5));
        profile1.addInnovationArea(innovationArea);

        profile2.setBusinessType(BUSINESS);
        profile2.setSkillsAreas("Skill area 2");
        profile2.setAgreement(agreement);
        profile2.setAgreementSignedDate(now().minusDays(10));
        profile2.addInnovationArea(innovationArea);

        List<Long> inviteIds = controller.getAssessorsNotAcceptedInviteIds(
                competition.getId(),
                innovationAreaId,
                status,
                hasContract
        ).getSuccess();

        assertTrue(inviteIds.isEmpty());

        profileRepository.save(asList(profile1, profile2));

        paulPlum.setAffiliations(
                newAffiliation()
                        .withId()
                        .withUser(paulPlum)
                        .withExists(TRUE)
                        .withDescription("Affiliation 1")
                        .withAffiliationType(PROFESSIONAL)
                        .build(2)
        );

        felixWilson.setAffiliations(
                newAffiliation()
                        .withId()
                        .withUser(felixWilson)
                        .withExists(FALSE)
                        .withDescription("Affiliation 1")
                        .withAffiliationType(PROFESSIONAL)
                        .build(2)
        );

        userRepository.save(asList(paulPlum, felixWilson));

        List<AssessmentParticipant> competitionParticipants = newAssessmentParticipant()
                .withId()
                .withUser(null, null, null, null, paulPlum, felixWilson)
                .withInvite(
                        newAssessmentInvite()
                                .withId()
                                .withName("Will Smith", "Bill Gates", "Serena Williams", "Angela Merkel", paulPlum.getName(), felixWilson.getName())
                                .withEmail("ws@test.com", "bg@test.com", "sw@test.com", "am@test.com", paulPlum.getEmail(), felixWilson.getEmail())
                                .withInnovationArea(innovationArea, otherInnovationArea, innovationArea, otherInnovationArea, null, null)
                                .withCompetition(competition)
                                .withStatus(SENT)
                                .withSentOn(now().minusDays(1))
                                .buildArray(6, AssessmentInvite.class)
                )
                .withCompetition(competition)
                .withStatus(PENDING)
                .withRole(ASSESSOR)
                .build(6);

        assessmentParticipantRepository.save(competitionParticipants);
        flushAndClearSession();

        inviteIds = controller.getAssessorsNotAcceptedInviteIds(
                competition.getId(),
                innovationAreaId,
                status,
                hasContract
        ).getSuccess();

        assertEquals(2, inviteIds.size());
    }

    @Test
    public void getInvitationOverview() throws Exception {
        loginCompAdmin();

        InnovationArea innovationArea = innovationAreaRepository.findOne(5L);
        InnovationArea otherInnovationArea = innovationAreaRepository.findOne(10L);

        Agreement agreement = agreementRepository.findOne(1L);

        Profile profile1 = profileRepository.findOne(paulPlum.getProfileId());
        Profile profile2 = profileRepository.findOne(felixWilson.getProfileId());

        profile1.setBusinessType(ACADEMIC);
        profile1.setSkillsAreas("Skill area 1");
        profile1.setAgreement(agreement);
        profile1.setAgreementSignedDate(now().minusDays(5));
        profile1.addInnovationArea(innovationArea);

        profile2.setBusinessType(BUSINESS);
        profile2.setSkillsAreas("Skill area 2");
        profile2.setAgreement(agreement);
        profile2.setAgreementSignedDate(now().minusDays(10));
        profile2.addInnovationArea(innovationArea);

        profileRepository.save(asList(profile1, profile2));

        paulPlum.setAffiliations(
                newAffiliation()
                        .withId()
                        .withUser(paulPlum)
                        .withExists(TRUE)
                        .withDescription("Affiliation 1")
                        .withAffiliationType(PROFESSIONAL)
                        .build(2)
        );

        felixWilson.setAffiliations(
                newAffiliation()
                        .withId()
                        .withUser(felixWilson)
                        .withExists(FALSE)
                        .withDescription("Affiliation 1")
                        .withAffiliationType(PROFESSIONAL)
                        .build(2)
        );

        userRepository.save(asList(paulPlum, felixWilson));

        List<AssessmentParticipant> competitionParticipants = newAssessmentParticipant()
                .withId()
                .withUser(null, null, null, null, paulPlum, felixWilson)
                .withInvite(
                        newAssessmentInvite()
                                .withId()
                                .withName("Will Smith", "Bill Gates", "Serena Williams", "Angela Merkel", paulPlum.getName(), felixWilson.getName())
                                .withEmail("ws@test.com", "bg@test.com", "sw@test.com", "am@test.com", paulPlum.getEmail(), felixWilson.getEmail())
                                .withInnovationArea(innovationArea, otherInnovationArea, innovationArea, otherInnovationArea, null, null)
                                .withCompetition(competition)
                                .withStatus(SENT)
                                .withSentOn(now().minusDays(1))
                                .buildArray(6, AssessmentInvite.class)
                )
                .withCompetition(competition)
                .withStatus(PENDING)
                .withRole(ASSESSOR)
                .build(6);

        assessmentParticipantRepository.save(competitionParticipants);
        flushAndClearSession();

        Optional<Long> innovationAreaId = of(innovationArea.getId());
        List<ParticipantStatus> status = singletonList(PENDING);
        Optional<Boolean> hasContract = of(TRUE);
        Pageable pageable = new PageRequest(0, 20, new Sort(ASC, "invite.name"));

        AssessorInviteOverviewPageResource pageResource = controller.getInvitationOverview(
                competition.getId(),
                pageable,
                innovationAreaId,
                status,
                hasContract
        )
                .getSuccess();

        assertEquals(0, pageResource.getNumber());
        assertEquals(20, pageResource.getSize());
        assertEquals(2, pageResource.getTotalElements());
        assertEquals(1, pageResource.getTotalPages());

        List<AssessorInviteOverviewResource> content = pageResource.getContent();

        assertEquals(2, content.size());
        assertEquals(felixWilson.getName(), content.get(0).getName());
        assertEquals(paulPlum.getName(), content.get(1).getName());
    }

    @Test
    public void deleteInvite() throws Exception {
        InnovationArea innovationArea = innovationAreaRepository.findOne(INNOVATION_AREA_ID);

        AssessmentInvite savedCompetition = assessmentInviteRepository.save(
                new AssessmentInvite("Test Tester", "test@test.com", "hash1", competition, innovationArea)
        );

        flushAndClearSession();
        loginCompAdmin();

        RestResult<Void> restResult = controller.deleteInvite("test@test.com", competition.getId());

        assertTrue(restResult.isSuccess());

        flushAndClearSession();

        assertNull(assessmentInviteRepository.findOne(savedCompetition.getId()));
    }


    @Test
    public void deleteAllInvites() throws Exception {
        HashSet<InviteStatus> inviteStatuses = newHashSet(CREATED);

        InnovationArea innovationArea = innovationAreaRepository.findOne(INNOVATION_AREA_ID);

        assessmentInviteRepository.save(asList(
                new AssessmentInvite("Test Tester 1", "test1@test.com", "hash1", competition, innovationArea),
                new AssessmentInvite("Test Tester 2", "test2@test.com", "hash2", competition, innovationArea)
        ));

        assertEquals(2, assessmentInviteRepository.countByCompetitionIdAndStatusIn(competition.getId(), inviteStatuses));

        flushAndClearSession();
        loginCompAdmin();

        RestResult<Void> restResult = controller.deleteAllInvites(competition.getId());

        assertTrue(restResult.isSuccess());

        flushAndClearSession();

        assertEquals(0, assessmentInviteRepository.countByCompetitionIdAndStatusIn(competition.getId(), inviteStatuses));
    }
}