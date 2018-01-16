package org.innovateuk.ifs.assessment.panel.builder;

import org.innovateuk.ifs.BaseBuilder;
import org.innovateuk.ifs.application.domain.Application;
import org.innovateuk.ifs.assessment.panel.domain.AssessmentReview;
import org.innovateuk.ifs.assessment.panel.domain.AssessmentReviewRejectOutcome;
import org.innovateuk.ifs.assessment.panel.resource.AssessmentReviewState;
import org.innovateuk.ifs.user.domain.ProcessRole;
import org.innovateuk.ifs.workflow.domain.ActivityState;

import java.util.List;
import java.util.function.BiConsumer;

import static java.util.Collections.emptyList;
import static org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions.setField;
import static org.innovateuk.ifs.base.amend.BaseBuilderAmendFunctions.uniqueIds;
import static org.innovateuk.ifs.workflow.domain.ActivityType.ASSESSMENT_PANEL_APPLICATION_INVITE;

public class AssessmentReviewBuilder extends BaseBuilder<AssessmentReview, AssessmentReviewBuilder> {

    private AssessmentReviewBuilder(List<BiConsumer<Integer, AssessmentReview>> multiActions) {
        super(multiActions);
    }

    public static AssessmentReviewBuilder newAssessmentReview() {
        return new AssessmentReviewBuilder(emptyList()).with(uniqueIds());
    }

    @Override
    protected AssessmentReviewBuilder createNewBuilderWithActions(List<BiConsumer<Integer, AssessmentReview>> actions) {
        return new AssessmentReviewBuilder(actions);
    }

    @Override
    protected AssessmentReview createInitial() {
        return new AssessmentReview();
    }

    public AssessmentReviewBuilder withId(Long... ids) {
        return withArray((id, invite) -> setField("id", id, invite), ids);
    }

    public AssessmentReviewBuilder withRejection(AssessmentReviewRejectOutcome... rejections) {
        return withArray((rejection, invite) -> invite.setRejection(rejection), rejections);
    }

    public AssessmentReviewBuilder withTarget(Application... applications) {
        return withArray((application, invite) -> invite.setTarget(application), applications);
    }

    public AssessmentReviewBuilder withParticipant(ProcessRole... participants) {
        return withArray((participant, invite) -> invite.setParticipant(participant), participants);
    }

    public AssessmentReviewBuilder withState(AssessmentReviewState... states) {
        return withArray((state, invite) -> invite.setActivityState(new ActivityState(ASSESSMENT_PANEL_APPLICATION_INVITE, state.getBackingState())), states);
    }
}