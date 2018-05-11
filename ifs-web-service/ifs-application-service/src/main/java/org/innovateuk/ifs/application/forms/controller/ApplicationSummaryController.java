package org.innovateuk.ifs.application.forms.controller;

import org.innovateuk.ifs.application.form.ApplicationForm;
import org.innovateuk.ifs.application.populator.ApplicationModelPopulator;
import org.innovateuk.ifs.application.resource.ApplicationResource;
import org.innovateuk.ifs.application.resource.FormInputResponseResource;
import org.innovateuk.ifs.application.service.*;
import org.innovateuk.ifs.assessment.service.AssessmentRestService;
import org.innovateuk.ifs.assessment.service.AssessorFormInputResponseRestService;
import org.innovateuk.ifs.commons.security.SecuredBySpring;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.form.service.FormInputResponseRestService;
import org.innovateuk.ifs.form.service.FormInputResponseService;
import org.innovateuk.ifs.interview.service.InterviewAssignmentRestService;
import org.innovateuk.ifs.project.ProjectService;
import org.innovateuk.ifs.project.resource.ProjectResource;
import org.innovateuk.ifs.user.resource.ProcessRoleResource;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.user.service.ProcessRoleService;
import org.innovateuk.ifs.user.service.UserRestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Optional;

/**
 * This controller will handle all requests that are related to the application summary.
 */
@Controller
@RequestMapping("/application")
public class ApplicationSummaryController {

    private ProcessRoleService processRoleService;
    private SectionService sectionService;
    private ApplicationService applicationService;
    private CompetitionService competitionService;
    private ApplicationModelPopulator applicationModelPopulator;
    private FormInputResponseService formInputResponseService;
    private FormInputResponseRestService formInputResponseRestService;
    private UserRestService userRestService;
    private AssessorFormInputResponseRestService assessorFormInputResponseRestService;
    private AssessmentRestService assessmentRestService;
    private ProjectService projectService;
    private InterviewAssignmentRestService interviewAssignmentRestService;

    public ApplicationSummaryController() {
    }

    @Autowired
    public ApplicationSummaryController(ProcessRoleService processRoleService,
                                        SectionService sectionService,
                                        ApplicationService applicationService,
                                        CompetitionService competitionService,
                                        ApplicationModelPopulator applicationModelPopulator,
                                        FormInputResponseService formInputResponseService,
                                        FormInputResponseRestService formInputResponseRestService,
                                        UserRestService userRestService,
                                        AssessorFormInputResponseRestService assessorFormInputResponseRestService,
                                        AssessmentRestService assessmentRestService,
                                        ProjectService projectService,
                                        InterviewAssignmentRestService interviewAssignmentRestService) {
        this.processRoleService = processRoleService;
        this.sectionService = sectionService;
        this.applicationService = applicationService;
        this.competitionService = competitionService;
        this.applicationModelPopulator = applicationModelPopulator;
        this.formInputResponseService = formInputResponseService;
        this.formInputResponseRestService = formInputResponseRestService;
        this.userRestService = userRestService;
        this.assessorFormInputResponseRestService = assessorFormInputResponseRestService;
        this.assessmentRestService = assessmentRestService;
        this.projectService = projectService;
        this.interviewAssignmentRestService = interviewAssignmentRestService;
    }

    @SecuredBySpring(value = "READ", description = "Applicants, support staff, and innovation leads have permission to view the application summary page")
    @PreAuthorize("hasAnyAuthority('applicant', 'support', 'innovation_lead')")
    @GetMapping("/{applicationId}/summary")
    public String applicationSummary(@ModelAttribute("form") ApplicationForm form,
                                     Model model,
                                     @PathVariable("applicationId") long applicationId,
                                     UserResource user) {

        List<FormInputResponseResource> responses = formInputResponseRestService.getResponsesByApplicationId(applicationId).getSuccess();
        model.addAttribute("incompletedSections", sectionService.getInCompleted(applicationId));
        model.addAttribute("responses", formInputResponseService.mapFormInputResponsesToFormInput(responses));

        ApplicationResource application = applicationService.getById(applicationId);
        CompetitionResource competition = competitionService.getById(application.getCompetition());
        List<ProcessRoleResource> userApplicationRoles = processRoleService.findProcessRolesByApplicationId(application.getId());

        applicationModelPopulator.addApplicationAndSectionsInternalWithOrgDetails(application, competition, user, model, form, userApplicationRoles, Optional.of(Boolean.FALSE));
        ProcessRoleResource userApplicationRole = userRestService.findProcessRole(user.getId(), applicationId).getSuccess();

        applicationModelPopulator.addOrganisationAndUserFinanceDetails(competition.getId(), applicationId, user, model, form, userApplicationRole.getOrganisationId());

        model.addAttribute("applicationReadyForSubmit", applicationService.isApplicationReadyForSubmit(application.getId()));

        ProjectResource project = projectService.getByApplicationId(applicationId);
        boolean projectWithdrawn = (project != null && project.isWithdrawn());
        model.addAttribute("projectWithdrawn", projectWithdrawn);

        boolean isApplicationAssignedToInterview = interviewAssignmentRestService.isAssignedToInterview(applicationId).getSuccess();

        if (competition.getCompetitionStatus().isFeedbackReleased() && !isApplicationAssignedToInterview) {
            addFeedbackAndScores(model, applicationId);
            return "application-feedback-summary";
        } else if (isApplicationAssignedToInterview) {
            addFeedbackAndScores(model, applicationId);
            return "application-interview-feedback";
        }
        else {
            return "application-summary";
        }
    }

    private void addFeedbackAndScores(Model model, long applicationId) {
        model.addAttribute("scores", assessorFormInputResponseRestService.getApplicationAssessmentAggregate(applicationId).getSuccess());
        model.addAttribute("feedback", assessmentRestService.getApplicationFeedback(applicationId)
                .getSuccess()
                .getFeedback()
        );
    }

}