package org.innovateuk.ifs.project.finance.service;

import org.innovateuk.ifs.BaseRestServiceUnitTest;
import org.innovateuk.ifs.commons.rest.RestResult;
import org.innovateuk.ifs.finance.resource.ProjectFinanceResource;
import org.innovateuk.ifs.project.finance.resource.*;
import org.junit.Test;

import java.util.List;

import static org.innovateuk.ifs.commons.service.ParameterizedTypeReferences.projectFinanceResourceListType;
import static org.innovateuk.ifs.finance.builder.ProjectFinanceResourceBuilder.newProjectFinanceResource;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpStatus.OK;

public class ProjectFinanceRestServiceImplTest extends BaseRestServiceUnitTest<ProjectFinanceRestServiceImpl> {

    private static final String projectFinanceRestURL = "/project";

    @Override
    protected ProjectFinanceRestServiceImpl registerRestServiceUnderTest() {
        return new ProjectFinanceRestServiceImpl();
    }

    @Test
    public void testGetProjectFinances() {

        Long projectId = 123L;

        List<ProjectFinanceResource> results = newProjectFinanceResource().build(2);

        setupGetWithRestResultExpectations(projectFinanceRestURL + "/" + projectId + "/project-finances", projectFinanceResourceListType(), results);

        RestResult<List<ProjectFinanceResource>> result = service.getProjectFinances(projectId);

        assertEquals(results, result.getSuccess());
    }

    @Test
    public void testGetViability() {

        ViabilityResource viability = new ViabilityResource(Viability.APPROVED, ViabilityRagStatus.GREEN);

        setupGetWithRestResultExpectations(projectFinanceRestURL + "/123/partner-organisation/456/viability", ViabilityResource.class, viability);

        RestResult<ViabilityResource> results = service.getViability(123L, 456L);

        assertEquals(Viability.APPROVED, results.getSuccess().getViability());
        assertEquals(ViabilityRagStatus.GREEN, results.getSuccess().getViabilityRagStatus());
    }

    @Test
    public void testSaveViability() {

        String postUrl = projectFinanceRestURL + "/123/partner-organisation/456/viability/" +
                Viability.APPROVED.name() + "/" + ViabilityRagStatus.RED.name();

        setupPostWithRestResultExpectations(postUrl, OK);

        RestResult<Void> result = service.saveViability(123L, 456L, Viability.APPROVED, ViabilityRagStatus.RED);

        assertTrue(result.isSuccess());

        setupPostWithRestResultVerifications(postUrl, Void.class);
    }

    @Test
    public void testGetEligibility() {

        EligibilityResource eligibility = new EligibilityResource(EligibilityState.APPROVED, EligibilityRagStatus.GREEN);

        setupGetWithRestResultExpectations(projectFinanceRestURL + "/123/partner-organisation/456/eligibility", EligibilityResource.class, eligibility);

        RestResult<EligibilityResource> results = service.getEligibility(123L, 456L);

        assertEquals(EligibilityState.APPROVED, results.getSuccess().getEligibility());
        assertEquals(EligibilityRagStatus.GREEN, results.getSuccess().getEligibilityRagStatus());
    }

    @Test
    public void testSaveEligibility() {

        String postUrl = projectFinanceRestURL + "/123/partner-organisation/456/eligibility/" +
                EligibilityState.APPROVED.name() + "/" + EligibilityRagStatus.RED.name();

        setupPostWithRestResultExpectations(postUrl, OK);

        RestResult<Void> result = service.saveEligibility(123L, 456L, EligibilityState.APPROVED, EligibilityRagStatus.RED);

        assertTrue(result.isSuccess());

        setupPostWithRestResultVerifications(postUrl, Void.class);
    }

    @Test
    public void testIsCreditReportConfirmed() {

        setupGetWithRestResultExpectations(projectFinanceRestURL + "/123/partner-organisation/456/credit-report", Boolean.class, true);
        RestResult<Boolean> results = service.isCreditReportConfirmed(123L, 456L);
        assertTrue(results.getSuccess());
    }

    @Test
    public void testSaveCreditReportConfirmed() {

        String postUrl = projectFinanceRestURL + "/123/partner-organisation/456/credit-report/true";
        setupPostWithRestResultExpectations(postUrl, OK);

        RestResult<Void> result = service.saveCreditReportConfirmed(123L, 456L, true);
        assertTrue(result.isSuccess());

        setupPostWithRestResultVerifications(postUrl, Void.class);
    }

    @Test
    public void testGetProjectFinance() {

        Long projectId = 123L;

        Long organisationId = 456L;

        ProjectFinanceResource expectedResult = newProjectFinanceResource().build();

        setupGetWithRestResultExpectations(projectFinanceRestURL + "/" + projectId + "/organisation/" + organisationId + "/finance-details", ProjectFinanceResource.class, expectedResult);

        RestResult<ProjectFinanceResource> result = service.getProjectFinance(projectId, organisationId);

        assertEquals(expectedResult, result.getSuccess());
    }
}
