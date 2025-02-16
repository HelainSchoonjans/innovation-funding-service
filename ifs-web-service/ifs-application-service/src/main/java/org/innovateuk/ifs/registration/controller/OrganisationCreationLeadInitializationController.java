package org.innovateuk.ifs.registration.controller;

import org.innovateuk.ifs.commons.security.SecuredBySpring;
import org.innovateuk.ifs.registration.form.OrganisationTypeForm;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Provides the initialization method and redirect when registering a new organisation as a lead applicant.
 */
@Controller
@RequestMapping(AbstractOrganisationCreationController.BASE_URL + "/initialize")
@SecuredBySpring(value = "Controller", description = "Anyone can start the lead applicant journey.", securedType = OrganisationCreationLeadInitializationController.class)
@PreAuthorize("permitAll")
public class OrganisationCreationLeadInitializationController extends AbstractOrganisationCreationController {
    @GetMapping
    public String initializeLeadRegistrationJourney(HttpServletRequest request, HttpServletResponse response) {
        //This is the first endpoint when creating a new account as lead applicant.
        registrationCookieService.deleteOrganisationCreationCookie(response);

        OrganisationTypeForm organisationTypeForm = new OrganisationTypeForm();
        organisationTypeForm.setLeadApplicant(true);
        registrationCookieService.saveToOrganisationTypeCookie(organisationTypeForm, response);
        return "redirect:" + BASE_URL + "/" + LEAD_ORGANISATION_TYPE;
    }
}
