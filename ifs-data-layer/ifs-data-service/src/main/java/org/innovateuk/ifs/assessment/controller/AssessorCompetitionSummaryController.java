package org.innovateuk.ifs.assessment.controller;

import org.innovateuk.ifs.assessment.resource.AssessorCompetitionSummaryResource;
import org.innovateuk.ifs.assessment.transactional.AssessorCompetitionSummaryService;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes endpoints for retrieving assessor summaries for competitions.
 */
@RestController
@RequestMapping("/assessor/{assessorId}/competition/{competitionId}")
public class AssessorCompetitionSummaryController {

    @Autowired
    private AssessorCompetitionSummaryService assessorCompetitionSummaryService;

    @GetMapping("/summary")
    public RestResult<AssessorCompetitionSummaryResource> getAssessorSummary(@PathVariable("assessorId") long assessorId,
                                                                             @PathVariable("competitionId") long competitionId) {
        return assessorCompetitionSummaryService.getAssessorSummary(assessorId, competitionId).toGetResponse();
    }
}
