package org.innovateuk.ifs.assessment.review.controller;

import org.innovateuk.ifs.assessment.review.form.AssessmentReviewForm;
import org.innovateuk.ifs.assessment.review.populator.AssessmentReviewModelPopulator;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.commons.security.SecuredBySpring;
import org.innovateuk.ifs.controller.ValidationHandler;
import org.innovateuk.ifs.review.resource.ReviewRejectOutcomeResource;
import org.innovateuk.ifs.review.resource.ReviewResource;
import org.innovateuk.ifs.review.service.ReviewRestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.function.Supplier;

import static java.lang.String.format;
import static org.innovateuk.ifs.controller.ErrorToObjectErrorConverterFactory.asGlobalErrors;
import static org.innovateuk.ifs.controller.ErrorToObjectErrorConverterFactory.fieldErrorsToFieldErrors;

/**
 * Controller to manage reviews of Applications
 */
@Controller
@RequestMapping(value = "/review/{reviewId}")
@SecuredBySpring(value = "Controller", description = "Assessors can access assessment reviews", securedType = AssessmentReviewController.class)
@PreAuthorize("hasAuthority('assessor')")
public class AssessmentReviewController {

    @Autowired
    private AssessmentReviewModelPopulator assessmentReviewModelPopulator;

    @Autowired
    private ReviewRestService reviewRestService;


    @GetMapping
    public String viewAssignment(@PathVariable("reviewId") long reviewId,
                                 @ModelAttribute(name = "form", binding = false) AssessmentReviewForm form,
                                 Model model) {
        model.addAttribute("model", assessmentReviewModelPopulator.populateModel(reviewId));
        return "assessment/review-invitation";
    }

    @PostMapping("/respond")
    public String respondToAssignment(Model model,
                                      @PathVariable("reviewId") long reviewId,
                                      @Valid @ModelAttribute("form") AssessmentReviewForm form,
                                      @SuppressWarnings("UnusedParameters") BindingResult bindingResult,
                                      ValidationHandler validationHandler) {
        Supplier<String> failureView = () -> viewAssignment(reviewId, form, model);

        return validationHandler.failNowOrSucceedWith(failureView, () -> {

            ReviewResource assessmentReview = reviewRestService.getAssessmentReview(reviewId).getSuccess();
            final RestResult<Void> updateResult;
            if (form.getReviewAccept()) {
                updateResult = reviewRestService.acceptAssessmentReview(reviewId);
            } else {
                updateResult = reviewRestService.rejectAssessmentReview(
                        reviewId,
                        new ReviewRejectOutcomeResource(form.getRejectComment())
                );
            }
            return validationHandler.addAnyErrors(updateResult, fieldErrorsToFieldErrors(), asGlobalErrors()).
                    failNowOrSucceedWith(failureView, () -> redirectToAssessorCompetitionDashboard(assessmentReview.getCompetition()));
        });
    }

    private  String redirectToAssessorCompetitionDashboard(long competitionId) {
        return format("redirect:/assessor/dashboard/competition/%s/panel", competitionId);
    }
}