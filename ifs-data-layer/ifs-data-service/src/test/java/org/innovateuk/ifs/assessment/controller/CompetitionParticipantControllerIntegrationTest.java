package org.innovateuk.ifs.assessment.controller;

import org.innovateuk.ifs.BaseControllerIntegrationTest;
import org.innovateuk.ifs.assessment.domain.AssessmentParticipant;
import org.innovateuk.ifs.assessment.repository.AssessmentParticipantRepository;
import org.innovateuk.ifs.competition.domain.Competition;
import org.innovateuk.ifs.competition.repository.CompetitionRepository;
import org.innovateuk.ifs.competition.resource.CompetitionStatus;
import org.innovateuk.ifs.invite.constant.InviteStatus;
import org.innovateuk.ifs.invite.domain.ParticipantStatus;
import org.innovateuk.ifs.invite.resource.CompetitionParticipantResource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static java.time.ZonedDateTime.now;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.innovateuk.ifs.assessment.builder.AssessmentInviteBuilder.newAssessmentInvite;
import static org.innovateuk.ifs.assessment.builder.AssessmentParticipantBuilder.newAssessmentParticipant;
import static org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions.id;
import static org.innovateuk.ifs.competition.builder.CompetitionBuilder.newCompetition;
import static org.innovateuk.ifs.competition.domain.CompetitionParticipantRole.ASSESSOR;
import static org.innovateuk.ifs.invite.constant.InviteStatus.OPENED;
import static org.innovateuk.ifs.invite.constant.InviteStatus.SENT;
import static org.innovateuk.ifs.invite.domain.ParticipantStatus.*;
import static org.innovateuk.ifs.user.builder.UserBuilder.newUser;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class CompetitionParticipantControllerIntegrationTest extends BaseControllerIntegrationTest<CompetitionParticipantController> {

    @Autowired
    @Override
    protected void setControllerUnderTest(CompetitionParticipantController controller) {
        this.controller = controller;
    }

    @Autowired
    private AssessmentParticipantRepository assessmentParticipantRepository;

    @Autowired
    private CompetitionRepository competitionRepository;

    @Before
    public void setUp() throws Exception {
        loginPaulPlum();
    }

    @After
    public void tearDown() throws Exception {
        flushAndClearSession();
    }

    @Test
    public void getParticipants() throws Exception {
        Competition competition1 = buildInAssessmentCompetition();
        Competition competition2 = buildInAssessmentCompetition();

        AssessmentParticipant expectedParticipant1 = buildAssessmentParticipant(competition1, SENT, PENDING);
        AssessmentParticipant expectedParticipant2 = buildAssessmentParticipant(competition2, OPENED, PENDING);

        assessmentParticipantRepository.saveAll(asList(
                expectedParticipant1,
                expectedParticipant2
        ));

        flushAndClearSession();

        List<CompetitionParticipantResource> participants = controller.getAssessorParticipants(
                getPaulPlum().getId())
                .getSuccess();

        assertEquals(2, participants.size());

        assertEquals(expectedParticipant1.getProcess().getId(), participants.get(0).getCompetitionId());
        assertEquals(getPaulPlum().getId(), participants.get(0).getUserId());
        assertEquals(0L, participants.get(0).getSubmittedAssessments());
        assertEquals(0L, participants.get(0).getTotalAssessments());

        assertEquals(expectedParticipant2.getProcess().getId(), participants.get(1).getCompetitionId());
        assertEquals(getPaulPlum().getId(), participants.get(1).getUserId());
        assertEquals(0L, participants.get(1).getSubmittedAssessments());
        assertEquals(0L, participants.get(1).getTotalAssessments());
    }

    @Test
    public void getParticipants_differentUser() throws Exception {
        loginFelixWilson();

        List<CompetitionParticipantResource> participants = controller.getAssessorParticipants(
                getPaulPlum().getId())
                .getSuccess();

        assertTrue(participants.isEmpty());
    }

    @Test
    public void getParticipants_accepted() throws Exception {
        Competition competition1 = competitionRepository.findById(1L).get();
        competition1.setStartDate(now().minusDays(10L));
        competition1.setEndDate(now().minusDays(5L));
        competition1.notifyAssessors(now().minusSeconds(1L));

        Competition competition2 = buildInAssessmentCompetition();

        AssessmentParticipant expectedParticipant1 = buildAssessmentParticipant(competition1, OPENED, ACCEPTED);
        AssessmentParticipant expectedParticipant2 = buildAssessmentParticipant(competition2, OPENED, PENDING);

        assessmentParticipantRepository.saveAll(asList(
                expectedParticipant1,
                expectedParticipant2
        ));

        flushAndClearSession();

        List<CompetitionParticipantResource> participants = controller.getAssessorParticipants(
                getPaulPlum().getId())
                .getSuccess();

        assertEquals(2, participants.size());

        assertEquals(expectedParticipant1.getProcess().getId(), participants.get(0).getCompetitionId());
        assertEquals(expectedParticipant1.getUser().getId(), participants.get(0).getUserId());
        assertEquals(1L, participants.get(0).getSubmittedAssessments());
        assertEquals(3L, participants.get(0).getTotalAssessments());

        assertEquals(expectedParticipant2.getProcess().getId(), participants.get(1).getCompetitionId());
        assertEquals(expectedParticipant2.getUser().getId(), participants.get(1).getUserId());
        assertEquals(0L, participants.get(1).getSubmittedAssessments());
        assertEquals(0L, participants.get(1).getTotalAssessments());
    }

    @Test
    public void getParticipants_filtersRejected() throws Exception {
        Competition competition1 = buildInAssessmentCompetition();
        AssessmentParticipant expectedParticipant1 = buildAssessmentParticipant(competition1, OPENED, REJECTED);

        assessmentParticipantRepository.saveAll(singletonList(expectedParticipant1));

        flushAndClearSession();

        List<CompetitionParticipantResource> participants = controller.getAssessorParticipants(
                getPaulPlum().getId())
                .getSuccess();

        assertEquals(0, participants.size());
    }

    @Test
    public void getParticipants_filtersInAssessment() throws Exception {
        Competition competition1 = buildOutOfAssessmentCompetition();
        Competition competition2 = buildOutOfAssessmentCompetition();

        AssessmentParticipant expectedParticipant1 = buildAssessmentParticipant(competition1, OPENED, ACCEPTED);
        AssessmentParticipant expectedParticipant2 = buildAssessmentParticipant(competition2, OPENED, PENDING);

        assessmentParticipantRepository.saveAll(asList(
                expectedParticipant1,
                expectedParticipant2
        ));

        flushAndClearSession();

        List<CompetitionParticipantResource> participants = controller.getAssessorParticipants(
                getPaulPlum().getId())
                .getSuccess();

        assertEquals(0, participants.size());
    }

    private Competition buildInAssessmentCompetition() {
        Competition competition = newCompetition()
                .with(id(null))
                .withCompetitionStatus(CompetitionStatus.IN_ASSESSMENT)
                .withAssessorsNotifiedDate(now())
                .build();

        competitionRepository.save(competition);

        return competition;
    }

    private Competition buildOutOfAssessmentCompetition() {
        Competition competition = newCompetition()
                .with(id(null))
                .withCompetitionStatus(CompetitionStatus.COMPETITION_SETUP)
                .build();

        competitionRepository.save(competition);

        return competition;
    }

    private AssessmentParticipant buildAssessmentParticipant(Competition competition, InviteStatus inviteStatus, ParticipantStatus participantStatus) {
        return newAssessmentParticipant()
                .with(id(null))
                .withCompetition(competition)
                .withUser(newUser()
                        .withId(3L)
                        .withFirstName("Professor")
                )
                .withInvite(newAssessmentInvite()
                        .with(id(null))
                        .withName("name")
                        .withEmail("joe@test.com")
                        .withCompetition(competition)
                        .withStatus(inviteStatus)
                )
                .withStatus(participantStatus)
                .withRole(ASSESSOR)
                .build();
    }
}
