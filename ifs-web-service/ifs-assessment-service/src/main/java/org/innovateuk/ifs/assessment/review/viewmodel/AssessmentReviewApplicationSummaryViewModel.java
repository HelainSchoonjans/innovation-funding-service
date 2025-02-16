package org.innovateuk.ifs.assessment.review.viewmodel;

import org.innovateuk.ifs.application.readonly.viewmodel.ApplicationReadOnlyViewModel;
import org.innovateuk.ifs.assessment.resource.AssessmentResource;
import org.innovateuk.ifs.competition.resource.CompetitionResource;

public class AssessmentReviewApplicationSummaryViewModel {

    private final long applicationId;
    private final String applicationName;

    private final ApplicationReadOnlyViewModel applicationReadOnlyViewModel;

    private final CompetitionResource currentCompetition;

    private final AssessmentResource assessment;

    public AssessmentReviewApplicationSummaryViewModel(long applicationId, String applicationName, ApplicationReadOnlyViewModel applicationReadOnlyViewModel, CompetitionResource currentCompetition, AssessmentResource assessment) {
        this.applicationId = applicationId;
        this.applicationName = applicationName;
        this.applicationReadOnlyViewModel = applicationReadOnlyViewModel;
        this.currentCompetition = currentCompetition;
        this.assessment = assessment;
    }

    public long getApplicationId() {
        return applicationId;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public ApplicationReadOnlyViewModel getApplicationReadOnlyViewModel() {
        return applicationReadOnlyViewModel;
    }

    public CompetitionResource getCurrentCompetition() {
        return currentCompetition;
    }

    public AssessmentResource getAssessment() {
        return assessment;
    }
}
