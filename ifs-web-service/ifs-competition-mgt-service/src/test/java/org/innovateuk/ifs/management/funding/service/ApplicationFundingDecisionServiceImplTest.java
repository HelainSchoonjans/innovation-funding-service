package org.innovateuk.ifs.management.funding.service;

import org.innovateuk.ifs.BaseServiceUnitTest;
import org.innovateuk.ifs.application.resource.ApplicationSummaryPageResource;
import org.innovateuk.ifs.application.resource.ApplicationSummaryResource;
import org.innovateuk.ifs.application.resource.FundingDecision;
import org.innovateuk.ifs.application.service.ApplicationFundingDecisionRestService;
import org.innovateuk.ifs.application.service.ApplicationSummaryRestService;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

import static org.innovateuk.ifs.application.builder.ApplicationSummaryResourceBuilder.newApplicationSummaryResource;
import static org.innovateuk.ifs.commons.rest.RestResult.restSuccess;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ApplicationFundingDecisionServiceImplTest extends BaseServiceUnitTest<ApplicationFundingDecisionServiceImpl> {
    @Mock
    private ApplicationFundingDecisionRestService applicationFundingDecisionRestService;

    @Mock
    private ApplicationSummaryRestService applicationSummaryRestService;

    @Before
    public void setUp() {
        super.setup();
    }

    protected ApplicationFundingDecisionServiceImpl supplyServiceUnderTest() {
        return new ApplicationFundingDecisionServiceImpl(applicationFundingDecisionRestService,
                applicationSummaryRestService);
    }

    @Test
    public void saveFundingDecision() throws Exception {
        List<Long> applicationIds = new ArrayList<>();
        applicationIds.add(8L);
        applicationIds.add(9L);

        ApplicationSummaryPageResource applicationSummaryPageResource = new ApplicationSummaryPageResource();
        List<ApplicationSummaryResource> applicationSummaryResources = newApplicationSummaryResource().withId(8L, 9L).build(4);
        applicationSummaryPageResource.setContent(applicationSummaryResources);

        when(applicationFundingDecisionRestService.saveApplicationFundingDecisionData(any(), any())).thenReturn(restSuccess());
        when(applicationSummaryRestService.getSubmittedApplications(1L, null, 0, Integer.MAX_VALUE, Optional.empty(), Optional.empty())).thenReturn(restSuccess(applicationSummaryPageResource));

        service.saveApplicationFundingDecisionData(1L, FundingDecision.ON_HOLD, applicationIds);

        Map<Long, FundingDecision> expectedDecisionMap = new HashMap<>();
        expectedDecisionMap.put(8L, FundingDecision.ON_HOLD);
        expectedDecisionMap.put(9L, FundingDecision.ON_HOLD);

        verify(applicationFundingDecisionRestService).saveApplicationFundingDecisionData(1L, expectedDecisionMap);
        when(applicationSummaryRestService.getSubmittedApplications(1L, null, 0, Integer.MAX_VALUE, Optional.empty(), Optional.empty())).thenReturn(restSuccess(applicationSummaryPageResource));
    }

    @Test
    public void saveFundingDecision_undecidedFundingDecisionChoice() throws Exception {
        List<Long> applicationIds = new ArrayList<>();
        applicationIds.add(8L);
        applicationIds.add(9L);

        ApplicationSummaryPageResource applicationSummaryPageResource = new ApplicationSummaryPageResource();
        List<ApplicationSummaryResource> applicationSummaryResources = newApplicationSummaryResource().withId(8L, 9L).build(2);
        applicationSummaryPageResource.setContent(applicationSummaryResources);

        ServiceResult<Void> result = service.saveApplicationFundingDecisionData(1L, FundingDecision.UNDECIDED, applicationIds);

        assertTrue(result.isFailure());

        verifyZeroInteractions(applicationFundingDecisionRestService);
        verifyZeroInteractions(applicationSummaryRestService);
    }

    @Test
    public void saveFundingDecision_unsubmittedApplicationsShouldNotReceiveFundingDecision() throws Exception {
        List<Long> applicationIds = new ArrayList<>();
        applicationIds.add(8L);
        applicationIds.add(9L);

        ApplicationSummaryPageResource applicationSummaryPageResource = new ApplicationSummaryPageResource();
        List<ApplicationSummaryResource> applicationSummaryResources = newApplicationSummaryResource().withId(10L, 11L).build(2);
        applicationSummaryPageResource.setContent(applicationSummaryResources);

        when(applicationFundingDecisionRestService.saveApplicationFundingDecisionData(any(), any())).thenReturn(restSuccess());
        when(applicationSummaryRestService.getSubmittedApplications(1L, null, 0, Integer.MAX_VALUE, Optional.empty(), Optional.empty())).thenReturn(restSuccess(applicationSummaryPageResource));

        service.saveApplicationFundingDecisionData(1L, FundingDecision.ON_HOLD, applicationIds);

        Map<Long, FundingDecision> expectedDecisionMap = new HashMap<>();

        verify(applicationFundingDecisionRestService).saveApplicationFundingDecisionData(1L, expectedDecisionMap);
        when(applicationSummaryRestService.getSubmittedApplications(1L, null, 0, Integer.MAX_VALUE, Optional.empty(), Optional.empty())).thenReturn(restSuccess(applicationSummaryPageResource));
    }

    @Test
    public void saveFundingDecision_onlySubmittedApplicationsShouldNotReceiveFundingDecision() throws Exception {
        List<Long> applicationIds = new ArrayList<>();
        applicationIds.add(8L);
        applicationIds.add(9L);

        ApplicationSummaryPageResource applicationSummaryPageResource = new ApplicationSummaryPageResource();
        List<ApplicationSummaryResource> applicationSummaryResources = newApplicationSummaryResource().withId(1L, 2L, 7L, 8L, 9L).build(5);
        applicationSummaryPageResource.setContent(applicationSummaryResources);

        when(applicationFundingDecisionRestService.saveApplicationFundingDecisionData(any(), any())).thenReturn(restSuccess());
        when(applicationSummaryRestService.getSubmittedApplications(1L, null, 0, Integer.MAX_VALUE, Optional.empty(), Optional.empty())).thenReturn(restSuccess(applicationSummaryPageResource));

        service.saveApplicationFundingDecisionData(1L, FundingDecision.ON_HOLD, applicationIds);

        Map<Long, FundingDecision> expectedDecisionMap = new HashMap<>();
        expectedDecisionMap.put(8L, FundingDecision.ON_HOLD);
        expectedDecisionMap.put(9L, FundingDecision.ON_HOLD);

        verify(applicationFundingDecisionRestService).saveApplicationFundingDecisionData(1L, expectedDecisionMap);
        when(applicationSummaryRestService.getSubmittedApplications(1L, null, 0, Integer.MAX_VALUE, Optional.empty(), Optional.empty())).thenReturn(restSuccess(applicationSummaryPageResource));
    }

    @Test
    public void saveFundingDecision_getFundingDecisionForStringShouldReturnAppropriateFundingDecision() throws Exception {
        String fundingDecisionString = "ON_HOLD";

        Optional<FundingDecision> fundingDecision = service.getFundingDecisionForString(fundingDecisionString);

        assertTrue(fundingDecision.isPresent());
        assertEquals(fundingDecision.get(), FundingDecision.ON_HOLD);
    }

    @Test
    public void saveFundingDecision_getFundingDecisionForStringShouldReturnEmptyOptionalIfFundingDecisionisUnrecognized() throws Exception {
        String fundingDecisionString = "NOT_A_FUNDING_DECISION_AT_ALL";

        Optional<FundingDecision> fundingDecision = service.getFundingDecisionForString(fundingDecisionString);

        assertTrue(!fundingDecision.isPresent());
    }
}
