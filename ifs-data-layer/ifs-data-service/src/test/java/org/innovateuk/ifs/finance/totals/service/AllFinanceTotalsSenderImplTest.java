package org.innovateuk.ifs.finance.totals.service;

import org.innovateuk.ifs.application.domain.Application;
import org.innovateuk.ifs.application.transactional.ApplicationService;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.innovateuk.ifs.application.builder.ApplicationBuilder.newApplication;
import static org.innovateuk.ifs.competition.builder.CompetitionBuilder.newCompetition;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class AllFinanceTotalsSenderImplTest {

    @Mock
    private ApplicationFinanceTotalsSender applicationFinanceTotalsSender;

    @Mock
    private ApplicationService applicationService;

    @InjectMocks
    private AllFinanceTotalsSenderImpl allFinanceTotalsSender;

    @Before
    public void setUp() throws Exception {
        allFinanceTotalsSender = new AllFinanceTotalsSenderImpl();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void sendAllFinanceTotals() {
        List<Application> applications = newApplication()
                .withCompetition(newCompetition().withId(1L).build())
                .build(6);

        when(applicationService.getApplicationsByState(any())).thenReturn(ServiceResult.serviceSuccess(applications));
        ServiceResult<Void> serviceResult = allFinanceTotalsSender.sendAllFinanceTotals();

        assertTrue(serviceResult.isSuccess());
        verify(applicationFinanceTotalsSender, times(6)).sendFinanceTotalsForApplication(any());
    }

    @Test
    public void sendAllFinanceTotals_allFinanceTotalsSenderIsNotCalledOnException() {
        when(applicationService.getApplicationsByState(any())).thenThrow(Exception.class);
        assertThatThrownBy(() -> allFinanceTotalsSender.sendAllFinanceTotals())
                .isInstanceOf(Exception.class);
        verifyZeroInteractions(applicationFinanceTotalsSender);
    }
}