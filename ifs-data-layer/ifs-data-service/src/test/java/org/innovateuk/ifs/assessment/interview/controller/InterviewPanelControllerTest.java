package org.innovateuk.ifs.assessment.interview.controller;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.invite.resource.*;
import org.junit.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

import static com.google.common.primitives.Longs.asList;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.invite.builder.AvailableApplicationPageResourceBuilder.newAvailableApplicationPageResource;
import static org.innovateuk.ifs.invite.builder.AvailableApplicationResourceBuilder.newAvailableApplicationResource;
import static org.innovateuk.ifs.invite.builder.InterviewPanelCreatedInviteResourceBuilder.newInterviewPanelStagedApplicationResource;
import static org.innovateuk.ifs.invite.builder.InterviewPanelStagedApplicationPageResourceBuilder.newInterviewPanelStagedApplicationPageResource;
import static org.innovateuk.ifs.invite.builder.StagedApplicationListResourceBuilder.newStagedApplicationListResource;
import static org.innovateuk.ifs.invite.builder.StagedApplicationResourceBuilder.newStagedApplicationResource;
import static org.innovateuk.ifs.util.JsonMappingUtil.toJson;
import static org.mockito.Mockito.*;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class InterviewPanelControllerTest extends BaseControllerMockMVCTest<InterviewPanelController> {

    private static final long COMPETITION_ID = 1L;

    @Override
    protected InterviewPanelController supplyControllerUnderTest() {
        return new InterviewPanelController();
    }

    @Test
    public void getAvailableApplications() throws Exception {
        int page = 5;
        int pageSize = 30;

        List<AvailableApplicationResource> expectedAvailableApplicationResources = newAvailableApplicationResource().build(2);

        AvailableApplicationPageResource expectedAvailableApplications = newAvailableApplicationPageResource()
                .withContent(expectedAvailableApplicationResources)
                .withNumber(page)
                .withTotalElements(300L)
                .withTotalPages(10)
                .withSize(30)
                .build();

        Pageable pageable = new PageRequest(page, pageSize, new Sort(ASC, "id"));

        when(interviewPanelInviteServiceMock.getAvailableApplications(COMPETITION_ID, pageable))
                .thenReturn(serviceSuccess(expectedAvailableApplications));

        mockMvc.perform(get("/interview-panel/available-applications/{competition-id}", COMPETITION_ID)
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(pageSize))
                .param("sort", "id"))
                .andExpect(status().isOk())
                .andExpect(content().json(toJson(expectedAvailableApplications)));

        verify(interviewPanelInviteServiceMock, only()).getAvailableApplications(COMPETITION_ID, pageable);
    }

    @Test
    public void getStagedApplications() throws Exception {
        int page = 5;
        int pageSize = 30;

        List<InterviewPanelStagedApplicationResource> expectedStagedApplicationResources = newInterviewPanelStagedApplicationResource().build(2);

        InterviewPanelStagedApplicationPageResource expectedStagedApplications = newInterviewPanelStagedApplicationPageResource()
                .withContent(expectedStagedApplicationResources)
                .withNumber(page)
                .withTotalElements(300L)
                .withTotalPages(10)
                .withSize(30)
                .build();

        Pageable pageable = new PageRequest(page, pageSize, new Sort(ASC, "id"));

        when(interviewPanelInviteServiceMock.getStagedApplications(COMPETITION_ID, pageable))
                .thenReturn(serviceSuccess(expectedStagedApplications));

        mockMvc.perform(get("/interview-panel/staged-applications/{competition-id}", COMPETITION_ID)
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(pageSize))
                .param("sort", "id"))
                .andExpect(status().isOk())
                .andExpect(content().json(toJson(expectedStagedApplications)));

        verify(interviewPanelInviteServiceMock, only()).getStagedApplications(COMPETITION_ID, pageable);

    }

    @Test
    public void getAvailableApplicationIds() throws Exception {
        List<Long> expectedAvailableApplicationIds = asList(1L, 2L);

        when(interviewPanelInviteServiceMock.getAvailableApplicationIds(COMPETITION_ID))
                .thenReturn(serviceSuccess(expectedAvailableApplicationIds));

        mockMvc.perform(get("/interview-panel/available-application-ids/{competitionId}", COMPETITION_ID))
                .andExpect(status().isOk())
                .andExpect(content().json(toJson(expectedAvailableApplicationIds)));

        verify(interviewPanelInviteServiceMock, only()).getAvailableApplicationIds(COMPETITION_ID);

    }

    @Test
    public void assignApplications() throws Exception {
        StagedApplicationListResource applications = newStagedApplicationListResource()
                .withInvites(newStagedApplicationResource().build(2))
                .build();

        when(interviewPanelInviteServiceMock.assignApplications(applications.getInvites())).thenReturn(serviceSuccess());

        mockMvc.perform(post("/interview-panel/assign-applications")
                .contentType(APPLICATION_JSON)
                .content(toJson(applications)))
                .andExpect(status().isOk());

        verify(interviewPanelInviteServiceMock, only()).assignApplications(applications.getInvites());
    }
}