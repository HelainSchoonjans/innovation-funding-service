package org.innovateuk.ifs.registration.controller;

import org.innovateuk.ifs.BaseControllerMockMVCTest;
import org.innovateuk.ifs.registration.controller.OrganisationCreationLeadInitializationController;
import org.innovateuk.ifs.registration.form.OrganisationCreationForm;
import org.innovateuk.ifs.registration.form.OrganisationTypeForm;
import org.innovateuk.ifs.registration.service.RegistrationCookieService;
import org.junit.Test;
import org.mockito.Mock;

import javax.servlet.http.HttpServletResponse;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

public class OrganisationCreationLeadInitializationControllerTest extends BaseControllerMockMVCTest<OrganisationCreationLeadInitializationController> {
    protected OrganisationCreationLeadInitializationController supplyControllerUnderTest() {
        return new OrganisationCreationLeadInitializationController();
    }

    @Mock
    private RegistrationCookieService registrationCookieService;

    private OrganisationTypeForm organisationTypeForm;
    private OrganisationCreationForm organisationForm;

    @Test
    public void testInitializeLeadRegistrationJourney_reinitializesCookiesAndRedirectsToTypePage() throws Exception {
        mockMvc.perform(get("/organisation/create/initialize"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/organisation/create/lead-organisation-type"));

        OrganisationTypeForm expectedOrganisationTypeForm = new OrganisationTypeForm();
        expectedOrganisationTypeForm.setLeadApplicant(true);

        verify(registrationCookieService, times(1)).saveToOrganisationTypeCookie(eq(expectedOrganisationTypeForm), isA(HttpServletResponse.class));
        verify(registrationCookieService, times(1)).deleteOrganisationCreationCookie(isA(HttpServletResponse.class));
    }
}