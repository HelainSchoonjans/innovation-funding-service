package org.innovateuk.ifs.review.builder;

import org.innovateuk.ifs.competition.domain.Competition;
import org.innovateuk.ifs.competition.domain.CompetitionParticipantRole;
import org.innovateuk.ifs.invite.domain.ParticipantStatus;
import org.innovateuk.ifs.invite.domain.RejectionReason;
import org.innovateuk.ifs.review.domain.ReviewInvite;
import org.innovateuk.ifs.review.domain.ReviewParticipant;
import org.innovateuk.ifs.user.domain.User;
import org.junit.Test;

import java.util.List;

import static org.innovateuk.ifs.competition.builder.CompetitionBuilder.newCompetition;
import static org.innovateuk.ifs.competition.domain.CompetitionParticipantRole.ASSESSOR;
import static org.innovateuk.ifs.invite.builder.RejectionReasonBuilder.newRejectionReason;
import static org.innovateuk.ifs.invite.domain.ParticipantStatus.ACCEPTED;
import static org.innovateuk.ifs.invite.domain.ParticipantStatus.REJECTED;
import static org.innovateuk.ifs.review.builder.ReviewInviteBuilder.newReviewInvite;
import static org.innovateuk.ifs.review.builder.ReviewParticipantBuilder.newReviewParticipant;
import static org.innovateuk.ifs.user.builder.UserBuilder.newUser;
import static org.junit.Assert.assertEquals;

public class ReviewParticipantBuilderTest {

    @Test
    public void buildOne() {
        Long expectedId = 7L;
        ParticipantStatus expectedStatus = REJECTED;
        User expectedUser = newUser().withId(5L).build();
        CompetitionParticipantRole expectedRole = ASSESSOR;
        Competition expectedCompetition = newCompetition().withName("Juggling Craziness").build();
        RejectionReason expectedRejectionReason = newRejectionReason().withReason("Unavailable").build();
        String expectedRejectionComment = "Too busy";
        ReviewInvite expectedInvite = newReviewInvite().build();

        ReviewParticipant participant = newReviewParticipant()
                .withId(expectedId)
                .withStatus(expectedStatus)
                .withUser(expectedUser)
                .withRole(expectedRole)
                .withCompetition(expectedCompetition)
                .withInvite(expectedInvite)
                .withRejectionReason(expectedRejectionReason)
                .withRejectionComment(expectedRejectionComment)
                .build();

        assertEquals(expectedId, participant.getId());
        assertEquals(expectedStatus, participant.getStatus());
        assertEquals(expectedUser, participant.getUser());
        assertEquals(expectedRole, participant.getRole());
        assertEquals(expectedInvite, participant.getInvite());
        assertEquals(expectedCompetition, participant.getProcess());
        assertEquals(expectedRejectionReason, participant.getRejectionReason() );
        assertEquals(expectedRejectionComment, participant.getRejectionReasonComment() );
    }

    @Test
    public void buildMany() {
        Long[] expectedIds = { 7L, 13L };
        ParticipantStatus[] expectedStatuses = { REJECTED, ACCEPTED };
        User[] expectedUsers = newUser().withId(5L, 11L).buildArray(2, User.class);
        CompetitionParticipantRole[] expectedRoles = { ASSESSOR, ASSESSOR };
        Competition[] expectedCompetitions = newCompetition().withName("Juggling Craziness", "Intermediate Juggling").buildArray(2, Competition.class);
        ReviewInvite[] expectedCompetitionInvites = newReviewInvite().buildArray(2, ReviewInvite.class);
        RejectionReason[] expectedRejectionReasons = { newRejectionReason().withReason("Unavailable").build(), null };
        String[] expectedRejectionComment = { "Too busy", null };

        List<ReviewParticipant> participants = newReviewParticipant()
                .withId(expectedIds)
                .withStatus(expectedStatuses)
                .withUser(expectedUsers)
                .withRole(expectedRoles)
                .withCompetition(expectedCompetitions)
                .withInvite(expectedCompetitionInvites)
                .withRejectionReason(expectedRejectionReasons)
                .withRejectionComment(expectedRejectionComment)
                .build(2);

        ReviewParticipant first = participants.get(0);
        assertEquals(expectedIds[0], first.getId());
        assertEquals(expectedStatuses[0], first.getStatus());
        assertEquals(expectedUsers[0], first.getUser());
        assertEquals(expectedRoles[0], first.getRole());
        assertEquals(expectedCompetitions[0], first.getProcess());
        assertEquals(expectedCompetitionInvites[0], first.getInvite());
        assertEquals(expectedRejectionReasons[0], first.getRejectionReason());
        assertEquals(expectedRejectionComment[0], first.getRejectionReasonComment());

        ReviewParticipant second = participants.get(1);
        assertEquals(expectedIds[1], second.getId());
        assertEquals(expectedStatuses[1], second.getStatus());
        assertEquals(expectedUsers[1], second.getUser());
        assertEquals(expectedRoles[1], second.getRole());
        assertEquals(expectedCompetitions[1], second.getProcess());
        assertEquals(expectedCompetitionInvites[1], second.getInvite());
        assertEquals(expectedRejectionReasons[1], second.getRejectionReason());
        assertEquals(expectedRejectionComment[1], second.getRejectionReasonComment());
    }
}
