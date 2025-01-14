package org.innovateuk.ifs.application.documentation;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.application.controller.QuestionSetupController;
import org.innovateuk.ifs.application.transactional.QuestionSetupService;
import org.innovateuk.ifs.competition.resource.CompetitionSetupSection;
import org.junit.Test;
import org.mockito.Mock;

import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.documentation.SetupStatusResourceDocs.setupStatusResourceBuilder;
import static org.innovateuk.ifs.util.MapFunctions.asMap;
import static org.mockito.Mockito.when;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class QuestionSetupControllerDocumentation extends BaseControllerMockMVCTest<QuestionSetupController> {

    @Mock
    private QuestionSetupService questionSetupService;

    @Override
    protected QuestionSetupController supplyControllerUnderTest() {
        return new QuestionSetupController();
    }

    @Test
    public void markQuestionSetupAsComplete() throws Exception {
        long competitionId = 2L;
        long questionId = 5L;
        CompetitionSetupSection parentSection = CompetitionSetupSection.APPLICATION_FORM;
        when(questionSetupService.markQuestionInSetupAsComplete(questionId, competitionId, parentSection)).thenReturn(serviceSuccess(setupStatusResourceBuilder.build()));

        mockMvc.perform(put("/question/setup/mark-as-complete/{competitionId}/{parentSection}/{questionId}", competitionId, parentSection, questionId)
                .header("IFS_AUTH_TOKEN", "123abc"))
                .andExpect(status().isOk())
                .andDo(document(
                        "question/setup/{method-name}",
                        pathParameters(
                                parameterWithName("competitionId").description("id of the competition on which the section should be marked as complete"),
                                parameterWithName("questionId").description("the id of the question to mark as complete"),
                                parameterWithName("parentSection").description("the parent section of the section that needs to be marked as complete")
                        )
                ));
    }

    @Test
    public void markQuestionSetupAsIncomplete() throws Exception {
        long competitionId = 2L;
        long questionId = 5L;
        CompetitionSetupSection parentSection = CompetitionSetupSection.APPLICATION_FORM;
        when(questionSetupService.markQuestionInSetupAsIncomplete(questionId, competitionId, parentSection)).thenReturn(serviceSuccess(setupStatusResourceBuilder.build()));

        mockMvc.perform(put("/question/setup/mark-as-incomplete/{competitionId}/{parentSection}/{questionId}", competitionId, parentSection, questionId)
                .header("IFS_AUTH_TOKEN", "123abc"))
                .andExpect(status().isOk())
                .andDo(document(
                        "question/setup/{method-name}",
                        pathParameters(
                                parameterWithName("competitionId").description("id of the competition on which the section should be marked as incomplete"),
                                parameterWithName("questionId").description("the id of the question to mark as incomplete"),
                                parameterWithName("parentSection").description("the parent section of the section that needs to be marked as complete")
                        )
                ));
    }

    @Test
    public void getQuestionStatuses() throws Exception {
        long competitionId = 2L;
        CompetitionSetupSection parentSection = CompetitionSetupSection.APPLICATION_FORM;
        when(questionSetupService.getQuestionStatuses(competitionId, parentSection)).thenReturn(serviceSuccess(asMap(1L, Boolean.TRUE)));

        mockMvc.perform(get("/question/setup/get-statuses/{competitionId}/{parentSection}", competitionId, parentSection)
                .header("IFS_AUTH_TOKEN", "123abc"))
                .andExpect(status().isOk())
                .andDo(document(
                        "question/setup/{method-name}",
                        pathParameters(
                                parameterWithName("competitionId").description("id of the competition which contains the questions on which to perform the status query"),
                                parameterWithName("parentSection").description("the parent section of the questions on which to perform the status query")
                        )
                ));
    }
}
