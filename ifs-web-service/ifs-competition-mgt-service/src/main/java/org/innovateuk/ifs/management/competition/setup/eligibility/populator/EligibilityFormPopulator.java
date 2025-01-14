package org.innovateuk.ifs.management.competition.setup.eligibility.populator;

import org.innovateuk.ifs.application.service.QuestionRestService;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.management.funding.form.enumerable.ResearchParticipationAmount;
import org.innovateuk.ifs.competition.resource.CollaborationLevel;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.resource.CompetitionSetupSection;
import org.innovateuk.ifs.management.competition.setup.core.form.CompetitionSetupForm;
import org.innovateuk.ifs.management.competition.setup.core.populator.CompetitionSetupFormPopulator;
import org.innovateuk.ifs.management.competition.setup.core.util.CompetitionUtils;
import org.innovateuk.ifs.management.competition.setup.eligibility.form.EligibilityForm;
import org.innovateuk.ifs.finance.resource.GrantClaimMaximumResource;
import org.innovateuk.ifs.finance.service.GrantClaimMaximumRestService;
import org.innovateuk.ifs.form.resource.QuestionResource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.innovateuk.ifs.question.resource.QuestionSetupType.RESEARCH_CATEGORY;

/**
 * Form populator for the eligibility competition setup section.
 */
@Service
public class EligibilityFormPopulator implements CompetitionSetupFormPopulator {

    private GrantClaimMaximumRestService grantClaimMaximumRestService;

    private QuestionRestService questionRestService;

    public EligibilityFormPopulator(GrantClaimMaximumRestService grantClaimMaximumRestService,
                                    QuestionRestService questionRestService) {
        this.grantClaimMaximumRestService = grantClaimMaximumRestService;
        this.questionRestService = questionRestService;
    }

    @Override
    public CompetitionSetupSection sectionToFill() {
        return CompetitionSetupSection.ELIGIBILITY;
    }

    @Override
    public CompetitionSetupForm populateForm(CompetitionResource competitionResource) {
        EligibilityForm competitionSetupForm = new EligibilityForm();

        competitionSetupForm.setResearchCategoryId(competitionResource.getResearchCategories());

        ResearchParticipationAmount amount = ResearchParticipationAmount.fromAmount(competitionResource.getMaxResearchRatio());
        if (amount != null) {
            competitionSetupForm.setResearchParticipationAmountId(amount.getId());
        }

        competitionSetupForm.setMultipleStream("no");

        CollaborationLevel level = competitionResource.getCollaborationLevel();
        if (level != null) {
            competitionSetupForm.setSingleOrCollaborative(level.getCode());
        }

        competitionSetupForm.setResearchCategoriesApplicable(getResearchCategoriesApplicable(competitionResource,
                competitionSetupForm));

        List<Long> organisationTypes = competitionResource.getLeadApplicantTypes();
        if (organisationTypes != null) {
            competitionSetupForm.setLeadApplicantTypes(organisationTypes);
        }

        competitionSetupForm.setResubmission(CompetitionUtils.booleanToText(competitionResource.getResubmission()));

        Boolean overrideFundingRuleSet = getOverrideFundingRulesSet(competitionResource, competitionSetupForm);
        competitionSetupForm.setOverrideFundingRules(overrideFundingRuleSet);

        if (overrideFundingRuleSet != null && overrideFundingRuleSet) {
            competitionSetupForm.setFundingLevelPercentageOverride(getFundingLevelPercentage(competitionResource));
            competitionSetupForm.setFundingLevelPercentage(getFundingLevelPercentage(competitionResource));
        }

        return competitionSetupForm;
    }

    private Boolean getResearchCategoriesApplicable(CompetitionResource competitionResource,
                                                    EligibilityForm eligibilityForm) {
        if (!isFirstTimeInForm(eligibilityForm)) {
            RestResult<QuestionResource> researchCategoryQuestionResult = questionRestService
                    .getQuestionByCompetitionIdAndQuestionSetupType(competitionResource.getId(), RESEARCH_CATEGORY);
            return researchCategoryQuestionResult.isSuccess();
        }
        return null;
    }

    private Boolean getOverrideFundingRulesSet(CompetitionResource competitionResource,
                                               EligibilityForm eligibilityForm) {
        if (!isFirstTimeInForm(eligibilityForm)) {
            return grantClaimMaximumRestService.isMaximumFundingLevelOverridden(competitionResource.getId()).getSuccess();
        }

        return null;
    }

    private boolean isFirstTimeInForm(EligibilityForm eligibilityForm) {
        return  "no".equals(eligibilityForm.getMultipleStream())
                && eligibilityForm.getResearchCategoryId().isEmpty()
                && (eligibilityForm.getSingleOrCollaborative() == null)
                && (eligibilityForm.getLeadApplicantTypes() == null
                || eligibilityForm.getLeadApplicantTypes().isEmpty())
                && isBlank(eligibilityForm.getResubmission());
    }

    private Integer getFundingLevelPercentage(CompetitionResource competition) {
        // The same maximum funding level is set for all GrantClaimMaximums when overriding
        Optional<GrantClaimMaximumResource> grantClaimMaximumResource = competition.getGrantClaimMaximums().stream()
                .findAny().map(this::getGrantClaimMaximumById);
        return grantClaimMaximumResource.get().getMaximum();
    }

    private GrantClaimMaximumResource getGrantClaimMaximumById(long id) {
        return grantClaimMaximumRestService.getGrantClaimMaximumById(id).getSuccess();
    }

}
