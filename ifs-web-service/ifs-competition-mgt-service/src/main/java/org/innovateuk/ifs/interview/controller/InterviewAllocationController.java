package org.innovateuk.ifs.interview.controller;

import org.innovateuk.ifs.application.service.CompetitionService;
import org.innovateuk.ifs.commons.security.SecuredBySpring;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.management.controller.CompetitionManagementAssessorProfileController;
import org.innovateuk.ifs.management.model.InterviewAcceptedAssessorsModelPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import static org.innovateuk.ifs.util.BackLinkUtil.buildOriginQueryString;
import static org.innovateuk.ifs.util.CollectionFunctions.simpleMap;

/**
 * This controller will handle all Competition Management requests related to allocating applications to assessors for interview panel.
 */
@Controller
@RequestMapping("/assessment/interview/competition/{competitionId}/assessors")
@SecuredBySpring(value = "Controller", description = "Comp Admins and Project Finance users can assign application to assessors for an Interview Panel", securedType = InterviewAllocationController.class)
@PreAuthorize("hasPermission(#competitionId, 'org.innovateuk.ifs.competition.resource.CompetitionCompositeId', 'INTERVIEW')")
public class InterviewAllocationController {

    @Autowired
    private InterviewAcceptedAssessorsModelPopulator interviewAcceptedAssessorsModelPopulator;

    @Autowired
    private CompetitionService competitionService;

    @GetMapping("/allocate-assessors")
    public String overview(Model model,
                           @PathVariable("competitionId") long competitionId,
                           @RequestParam MultiValueMap<String, String> queryParams,
                           @RequestParam(value = "page", defaultValue = "0") int page
    ) {
        CompetitionResource competitionResource = competitionService.getById(competitionId);

        String originQuery = buildOriginQueryString(CompetitionManagementAssessorProfileController.AssessorProfileOrigin.INTERVIEW_ALLOCATION, queryParams);

        model.addAttribute("model", interviewAcceptedAssessorsModelPopulator.populateModel(
                competitionResource,
                originQuery
        ));
        model.addAttribute("originQuery", originQuery);

        return "competition/interview-accepted-assessors";
    }

}