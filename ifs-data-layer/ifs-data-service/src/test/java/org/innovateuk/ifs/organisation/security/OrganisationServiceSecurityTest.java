package org.innovateuk.ifs.organisation.security;

import org.innovateuk.ifs.BaseServiceSecurityTest;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.organisation.resource.OrganisationResource;
import org.innovateuk.ifs.organisation.resource.OrganisationSearchResult;
import org.innovateuk.ifs.organisation.transactional.OrganisationService;
import org.innovateuk.ifs.organisation.transactional.OrganisationServiceImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.innovateuk.ifs.address.builder.AddressResourceBuilder.newAddressResource;
import static org.innovateuk.ifs.address.resource.OrganisationAddressType.REGISTERED;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.organisation.builder.OrganisationResourceBuilder.newOrganisationResource;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

/**
 * Testing the permission rules applied to the secured methods in OrganisationService.  This set of tests tests for the
 * individual rules that are called whenever an OrganisationService method is called.  They do not however test the logic
 * within those rules
 */
public class OrganisationServiceSecurityTest extends BaseServiceSecurityTest<OrganisationService> {

    private OrganisationPermissionRules rules;
    private OrganisationLookupStrategies lookup;

    @Before
    public void lookupPermissionRules() {
        rules = getMockPermissionRulesBean(OrganisationPermissionRules.class);
        lookup = getMockPermissionEntityLookupStrategiesBean(OrganisationLookupStrategies.class);
    }

    @Override
    protected Class<? extends OrganisationService> getClassUnderTest() {
        return OrganisationServiceImpl.class;
    }

    @Test
    public void findByApplicationId() {
        when(classUnderTestMock.findByApplicationId(1L))
                .thenReturn(serviceSuccess(newOrganisationResource().buildSet(2)));

        ServiceResult<Set<OrganisationResource>> results = classUnderTest.findByApplicationId(1L);
        assertEquals(0, results.getSuccess().size());

        verifyUserHasAccessToOrganisation(2);
        verifyNoMoreInteractions(rules);
    }

    @Test
    public void findById() {
        when(classUnderTestMock.findById(1L))
                .thenReturn(serviceSuccess(newOrganisationResource().build()));

        assertAccessDenied(() -> classUnderTest.findById(1L), () -> verifyUserHasAccessToOrganisation(1));
    }

    private void verifyUserHasAccessToOrganisation(int times) {
        verify(rules, times(times))
                .systemRegistrationUserCanSeeOrganisationsNotYetConnectedToApplications(isA(OrganisationResource.class), eq(getLoggedInUser()));
        verify(rules, times(times))
                .memberOfOrganisationCanViewOwnOrganisation(isA(OrganisationResource.class), eq(getLoggedInUser()));
        verify(rules, times(times))
                .usersCanViewOrganisationsOnTheirOwnApplications(isA(OrganisationResource.class), eq(getLoggedInUser()));
        verify(rules, times(times))
                .internalUsersCanSeeAllOrganisations(isA(OrganisationResource.class), eq(getLoggedInUser()));
        verify(rules, times(times))
                .systemRegistrationUserCanSeeAllOrganisations(isA(OrganisationResource.class), eq(getLoggedInUser()));
        verify(rules, times(times))
                .projectPartnerUserCanSeePartnerOrganisationsWithinTheirProjects(isA(OrganisationResource.class), eq(getLoggedInUser()));
        verify(rules, times(times))
                .usersCanViewOrganisationsTheyAreInvitedTo(isA(OrganisationResource.class), eq(getLoggedInUser()));
        verify(rules, times(times))
                .stakeholdersCanSeeAllOrganisations(isA(OrganisationResource.class), eq(getLoggedInUser()));
        verify(rules, times(times))
                .monitoringOfficersCanSeeAllOrganisations(isA(OrganisationResource.class), eq(getLoggedInUser()));
        verifyNoMoreInteractions(rules);
    }

    @Test
    public void create() {
        assertAccessDenied(() -> classUnderTest.create(newOrganisationResource().build()), () -> {
            verify(rules).systemRegistrationUserCanCreateOrganisations(isA(OrganisationResource.class), eq(getLoggedInUser()));
            verifyNoMoreInteractions(rules);
        });
    }

    @Test
    public void update() {
        assertAccessDenied(() -> classUnderTest.update(newOrganisationResource().build()), () -> {
            verify(rules)
                    .systemRegistrationUserCanUpdateOrganisationsNotYetConnectedToApplicationsOrUsers(isA(OrganisationResource.class), eq(getLoggedInUser()));
            verify(rules)
                    .memberOfOrganisationCanUpdateOwnOrganisation(isA(OrganisationResource.class), eq(getLoggedInUser()));
            verify(rules)
                    .projectFinanceUserCanUpdateAnyOrganisation(isA(OrganisationResource.class), eq(getLoggedInUser()));
            verifyNoMoreInteractions(rules);
        });
    }

    @Test
    public void addAddress() {
        when(lookup.findOrganisationById(123L)).thenReturn(newOrganisationResource().build());

        assertAccessDenied(() -> classUnderTest.addAddress(123L, REGISTERED, newAddressResource().build()), () -> {
            verify(rules)
                    .systemRegistrationUserCanUpdateOrganisationsNotYetConnectedToApplicationsOrUsers(isA(OrganisationResource.class), eq(getLoggedInUser()));
            verify(rules)
                    .memberOfOrganisationCanUpdateOwnOrganisation(isA(OrganisationResource.class), eq(getLoggedInUser()));
            verify(rules)
                    .projectFinanceUserCanUpdateAnyOrganisation(isA(OrganisationResource.class), eq(getLoggedInUser()));
            verifyNoMoreInteractions(rules);
        });
    }

    @Test
    public void searchAcademic() {
        when(classUnderTestMock.searchAcademic("Univer", 10))
                .thenReturn(serviceSuccess(new ArrayList<>(asList(new OrganisationSearchResult(), new OrganisationSearchResult()))));

        ServiceResult<List<OrganisationSearchResult>> results = classUnderTest.searchAcademic("Univer", 10);
        assertEquals(0, results.getSuccess().size());

        verify(rules, times(2)).systemRegistrationUserCanSeeOrganisationSearchResults(isA(OrganisationSearchResult.class), eq(getLoggedInUser()));
        verifyNoMoreInteractions(rules);
    }

    @Test
    public void getSearchOrganisation() {
        when(classUnderTestMock.getSearchOrganisation(1L))
                .thenReturn(serviceSuccess(new OrganisationSearchResult()));

        assertAccessDenied(() -> classUnderTest.getSearchOrganisation(1L), () -> {
            verify(rules).systemRegistrationUserCanSeeOrganisationSearchResults(isA(OrganisationSearchResult.class), eq(getLoggedInUser()));
            verifyNoMoreInteractions(rules);
        });
    }
}
