package org.innovateuk.ifs.assessment.upcoming.controller;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.assessment.upcoming.populator.UpcomingCompetitionModelPopulator;
import org.innovateuk.ifs.assessment.upcoming.viewmodel.UpcomingCompetitionViewModel;
import org.innovateuk.ifs.commons.exception.ObjectNotFoundException;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.service.CompetitionRestService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.TestPropertySource;

import java.time.ZonedDateTime;

import static org.innovateuk.ifs.commons.rest.RestResult.restSuccess;
import static org.innovateuk.ifs.competition.builder.CompetitionResourceBuilder.newCompetitionResource;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.Silent.class)
@TestPropertySource("classpath:application.properties")
public class UpcomingCompetitionControllerTest extends BaseControllerMockMVCTest<UpcomingCompetitionController> {

    @Spy
    @InjectMocks
    private UpcomingCompetitionModelPopulator upcomingCompetitionModelPopulator;

    @Mock
    private CompetitionRestService competitionRestService;

    private static final String restUrl = "/competition";

    @Override
    protected UpcomingCompetitionController supplyControllerUnderTest() {
        return new UpcomingCompetitionController();
    }

    @Test
    public void viewSummary_loggedIn() throws Exception {
        ZonedDateTime dateTime = ZonedDateTime.now();

        CompetitionResource competitionResource = newCompetitionResource()
                .withId(1L)
                .withName("name")
                .withAssessorAcceptsDate(dateTime)
                .withAssessorDeadlineDate(dateTime)
                .build();

        UpcomingCompetitionViewModel expectedViewModel = new UpcomingCompetitionViewModel(competitionResource);

        when(competitionRestService.getCompetitionById(1L)).thenReturn(restSuccess(competitionResource));

        mockMvc.perform(get(restUrl + "/{competitionId}/upcoming", "1"))
                .andExpect(model().attribute("model", expectedViewModel))
                .andExpect(status().isOk());

        verify(competitionRestService).getCompetitionById(1L);
    }

    @Test
    public void viewSummary_competitionNotExists() throws Exception {
        when(competitionRestService.getCompetitionById(1L)).thenThrow(new ObjectNotFoundException());

        mockMvc.perform(get(restUrl + "/{competitionId}/upcoming", "1"))
                .andExpect(model().attributeDoesNotExist("model"))
                .andExpect(status().isNotFound());

        verify(competitionRestService).getCompetitionById(1L);
    }
}
