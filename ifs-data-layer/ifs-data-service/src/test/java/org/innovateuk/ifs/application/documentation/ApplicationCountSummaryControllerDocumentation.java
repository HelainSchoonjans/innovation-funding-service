package org.innovateuk.ifs.application.documentation;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.application.controller.ApplicationCountSummaryController;
import org.innovateuk.ifs.application.resource.ApplicationCountSummaryPageResource;
import org.innovateuk.ifs.application.resource.ApplicationCountSummaryResource;
import org.innovateuk.ifs.application.transactional.ApplicationCountSummaryService;
import org.junit.Test;
import org.mockito.Mock;

import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.documentation.ApplicationCountDocs.applicationCountSummaryResourceBuilder;
import static org.innovateuk.ifs.documentation.ApplicationCountDocs.applicationCountSummaryResourcesFields;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ApplicationCountSummaryControllerDocumentation extends BaseControllerMockMVCTest<ApplicationCountSummaryController> {

    @Mock
    private ApplicationCountSummaryService applicationCountSummaryServiceMock;

    @Override
    protected ApplicationCountSummaryController supplyControllerUnderTest() {
        return new ApplicationCountSummaryController();
    }

    @Test
    public void getApplicationCountSummariesByCompetitionId() throws Exception {
        Long competitionId = 1L;
        ApplicationCountSummaryResource applicationCountSummaryResource = applicationCountSummaryResourceBuilder.build();
        ApplicationCountSummaryPageResource pageResource = new ApplicationCountSummaryPageResource();
        pageResource.setContent(singletonList(applicationCountSummaryResource));

        when(applicationCountSummaryServiceMock.getApplicationCountSummariesByCompetitionId(competitionId, 0, 20, empty())).thenReturn(serviceSuccess(pageResource));

        mockMvc.perform(get("/application-count-summary/find-by-competition-id/{competitionId}", competitionId)
                .header("IFS_AUTH_TOKEN", "123abc"))
                .andExpect(status().isOk())
                .andDo(document("applicationCountSummary/{method-name}",
                        pathParameters(
                                parameterWithName("competitionId").description("Id of competition")
                        ),
                        responseFields(applicationCountSummaryResourcesFields)));

        verify(applicationCountSummaryServiceMock).getApplicationCountSummariesByCompetitionId(competitionId, 0, 20, empty());
    }

    @Test
    public void getApplicationCountSummariesByCompetitionIdAndInnovationArea() throws Exception {
        long competitionId = 1L;
        long assessorId = 2L;
        String sortField = "";
        String filter = "";
        ApplicationCountSummaryResource applicationCountSummaryResource = applicationCountSummaryResourceBuilder.build();
        ApplicationCountSummaryPageResource pageResource = new ApplicationCountSummaryPageResource();
        pageResource.setContent(singletonList(applicationCountSummaryResource));

        when(applicationCountSummaryServiceMock.getApplicationCountSummariesByCompetitionIdAndInnovationArea(competitionId, assessorId, 0, 20, empty(), "", "")).thenReturn(serviceSuccess(pageResource));

        mockMvc.perform(get("/application-count-summary/find-by-competition-id-and-innovation-area/{competitionId}?assessorId={assessorId}&sortField={sortField}&filter={filter}", competitionId, assessorId, sortField, filter)
                .header("IFS_AUTH_TOKEN", "123abc"))
                .andExpect(status().isOk())
                .andDo(document("applicationCountSummary/{method-name}",
                        pathParameters(
                                parameterWithName("competitionId").description("Id of competition")
                        ),
                        requestParameters(
                                parameterWithName("assessorId").description("Id of assessor to exclude results for"),
                                parameterWithName("sortField").description("Field to sort by"),
                                parameterWithName("filter").description("String to filter applications")
                        ),
                        responseFields(applicationCountSummaryResourcesFields)));

        verify(applicationCountSummaryServiceMock).getApplicationCountSummariesByCompetitionIdAndInnovationArea(competitionId, assessorId, 0, 20, empty(), "", "");
    }

}
