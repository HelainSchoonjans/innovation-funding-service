package org.innovateuk.ifs.project.projectdetails.controller;

import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.service.CompetitionRestService;
import org.innovateuk.ifs.controller.ValidationHandler;
import org.innovateuk.ifs.organisation.resource.OrganisationResource;
import org.innovateuk.ifs.project.ProjectService;
import org.innovateuk.ifs.project.projectdetails.form.PartnerProjectLocationForm;
import org.innovateuk.ifs.project.projectdetails.form.ProjectDetailsStartDateForm;
import org.innovateuk.ifs.project.projectdetails.viewmodel.PartnerProjectLocationViewModel;
import org.innovateuk.ifs.project.projectdetails.viewmodel.ProjectDetailsStartDateViewModel;
import org.innovateuk.ifs.project.projectdetails.viewmodel.ProjectDetailsViewModel;
import org.innovateuk.ifs.project.resource.PartnerOrganisationResource;
import org.innovateuk.ifs.project.resource.ProjectResource;
import org.innovateuk.ifs.project.resource.ProjectUserResource;
import org.innovateuk.ifs.project.service.PartnerOrganisationRestService;
import org.innovateuk.ifs.project.status.resource.ProjectTeamStatusResource;
import org.innovateuk.ifs.project.status.security.SetupSectionAccessibilityHelper;
import org.innovateuk.ifs.projectdetails.ProjectDetailsService;
import org.innovateuk.ifs.status.StatusService;
import org.innovateuk.ifs.user.resource.UserResource;
import org.innovateuk.ifs.user.service.OrganisationRestService;
import org.innovateuk.ifs.util.PrioritySorting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.innovateuk.ifs.controller.ErrorToObjectErrorConverterFactory.toField;
import static org.innovateuk.ifs.user.resource.Role.PARTNER;
import static org.innovateuk.ifs.util.CollectionFunctions.simpleFilter;
import static org.innovateuk.ifs.util.CollectionFunctions.simpleMap;

@Controller
@RequestMapping("/project")
public class ProjectDetailsController {
    private static final String FORM_ATTR_NAME = "form";

    @Autowired
    private ProjectService projectService;

    @Autowired
    private StatusService statusService;

    @Autowired
    private ProjectDetailsService projectDetailsService;

    @Autowired
    private CompetitionRestService competitionRestService;

    @Autowired
    private OrganisationRestService organisationRestService;

    @Autowired
    private PartnerOrganisationRestService partnerOrganisationService;

    @PreAuthorize("hasPermission(#projectId, 'org.innovateuk.ifs.project.resource.ProjectCompositeId', 'ACCESS_PROJECT_DETAILS_SECTION')")
    @GetMapping("/{projectId}/details")
    public String viewProjectDetails(@PathVariable("projectId") final Long projectId, Model model,
                                     UserResource loggedInUser) {
        ProjectResource projectResource = projectService.getById(projectId);
        CompetitionResource competitionResource = competitionRestService.getCompetitionById(projectResource.getCompetition()).getSuccess();
        boolean partnerProjectLocationRequired = competitionResource.isLocationPerPartner();

        List<ProjectUserResource> projectUsers = projectService.getProjectUsersForProject(projectResource.getId());
        OrganisationResource leadOrganisation = projectService.getLeadOrganisation(projectId);
        List<OrganisationResource> organisations
                = new PrioritySorting<>(getPartnerOrganisations(projectUsers), leadOrganisation, OrganisationResource::getName).unwrap();

        ProjectTeamStatusResource teamStatus = statusService.getProjectTeamStatus(projectId, Optional.empty());
        SetupSectionAccessibilityHelper statusAccessor = new SetupSectionAccessibilityHelper(teamStatus);
        boolean spendProfileGenerated = statusAccessor.isSpendProfileGenerated();

        model.addAttribute("model", new ProjectDetailsViewModel(projectResource, loggedInUser,
                getUsersPartnerOrganisations(loggedInUser, projectUsers),
                organisations,
                partnerProjectLocationRequired ? partnerOrganisationService.getProjectPartnerOrganisations(projectId).getSuccess()
                        : Collections.emptyList(),
                leadOrganisation,
                projectService.isUserLeadPartner(projectId, loggedInUser.getId()),
                spendProfileGenerated, statusAccessor.isGrantOfferLetterGenerated(), false));

        return "project/details";
    }

    @PreAuthorize("hasPermission(#projectId, 'org.innovateuk.ifs.project.resource.ProjectCompositeId', 'ACCESS_PROJECT_DETAILS_SECTION')")
    @GetMapping("/{projectId}/readonly")
    public String viewProjectDetailsInReadOnly(@PathVariable("projectId") final Long projectId, Model model,
                                               UserResource loggedInUser) {

        ProjectResource projectResource = projectService.getById(projectId);
        CompetitionResource competitionResource = competitionRestService.getCompetitionById(projectResource.getCompetition()).getSuccess();
        boolean partnerProjectLocationRequired = competitionResource.isLocationPerPartner();

        List<ProjectUserResource> projectUsers = projectService.getProjectUsersForProject(projectResource.getId());
        OrganisationResource leadOrganisation = projectService.getLeadOrganisation(projectId);
        List<OrganisationResource> organisations
                = new PrioritySorting<>(getPartnerOrganisations(projectUsers), leadOrganisation, OrganisationResource::getName).unwrap();

        ProjectTeamStatusResource teamStatus = statusService.getProjectTeamStatus(projectId, Optional.empty());
        SetupSectionAccessibilityHelper statusAccessor = new SetupSectionAccessibilityHelper(teamStatus);
        boolean spendProfileGenerated = statusAccessor.isSpendProfileGenerated();

        model.addAttribute("model", new ProjectDetailsViewModel(projectResource, loggedInUser,
                getUsersPartnerOrganisations(loggedInUser, projectUsers),
                organisations,
                partnerProjectLocationRequired ? partnerOrganisationService.getProjectPartnerOrganisations(projectId).getSuccess()
                        : Collections.emptyList(),
                leadOrganisation,
                projectService.isUserLeadPartner(projectId, loggedInUser.getId()),
                spendProfileGenerated, true, true));

        return "project/details";
    }

    @PreAuthorize("hasPermission(#projectId, 'org.innovateuk.ifs.project.resource.ProjectCompositeId', 'ACCESS_PARTNER_PROJECT_LOCATION_PAGE')")
    @GetMapping("/{projectId}/organisation/{organisationId}/partner-project-location")
    public String viewPartnerProjectLocation(@PathVariable("projectId") final long projectId,
                                             @PathVariable("organisationId") final long organisationId,
                                             Model model,
                                             UserResource loggedInUser) {

        PartnerOrganisationResource partnerOrganisation = partnerOrganisationService.getPartnerOrganisation(projectId, organisationId).getSuccess();
        PartnerProjectLocationForm form = new PartnerProjectLocationForm(partnerOrganisation.getPostcode());

        return doViewPartnerProjectLocation(projectId, organisationId, loggedInUser, model, form);

    }

    @PreAuthorize("hasPermission(#projectId, 'org.innovateuk.ifs.project.resource.ProjectCompositeId', 'ACCESS_PARTNER_PROJECT_LOCATION_PAGE')")
    @PostMapping("/{projectId}/organisation/{organisationId}/partner-project-location")
    public String updatePartnerProjectLocation(@PathVariable("projectId") final long projectId,
                                               @PathVariable("organisationId") final long organisationId,
                                               @ModelAttribute(FORM_ATTR_NAME) PartnerProjectLocationForm form,
                                               @SuppressWarnings("unused") BindingResult bindingResult, ValidationHandler validationHandler,
                                               Model model,
                                               UserResource loggedInUser) {

        Supplier<String> failureView = () -> doViewPartnerProjectLocation(projectId, organisationId, loggedInUser, model, form);

        return validationHandler.failNowOrSucceedWith(failureView, () -> {

            ServiceResult<Void> updateResult = projectDetailsService.updatePartnerProjectLocation(projectId, organisationId, form.getPostcode());

            return validationHandler.addAnyErrors(updateResult, toField("postcode")).
                    failNowOrSucceedWith(failureView, () -> redirectToProjectDetails(projectId));
        });
    }

    @PreAuthorize("hasPermission(#projectId, 'org.innovateuk.ifs.project.resource.ProjectCompositeId', 'ACCESS_PROJECT_START_DATE_PAGE')")
    @GetMapping("/{projectId}/details/start-date")
    public String viewStartDate(@PathVariable("projectId") final Long projectId, Model model,
                                @ModelAttribute(name = FORM_ATTR_NAME, binding = false) ProjectDetailsStartDateForm form,
                                UserResource loggedInUser) {

        ProjectResource projectResource = projectService.getById(projectId);
        LocalDate defaultStartDate = projectResource.getTargetStartDate().withDayOfMonth(1);
        form.setProjectStartDate(defaultStartDate);
        return doViewProjectStartDate(model, projectResource, form);

    }

    @PreAuthorize("hasPermission(#projectId, 'org.innovateuk.ifs.project.resource.ProjectCompositeId', 'ACCESS_PROJECT_START_DATE_PAGE')")
    @PostMapping("/{projectId}/details/start-date")
    public String updateStartDate(@PathVariable("projectId") final Long projectId,
                                  @ModelAttribute(FORM_ATTR_NAME) ProjectDetailsStartDateForm form,
                                  @SuppressWarnings("unused") BindingResult bindingResult, ValidationHandler validationHandler,
                                  Model model,
                                  UserResource loggedInUser) {

        Supplier<String> failureView = () -> doViewProjectStartDate(model, projectService.getById(projectId), form);
        return validationHandler.failNowOrSucceedWith(failureView, () -> {

            ServiceResult<Void> updateResult = projectDetailsService.updateProjectStartDate(projectId, form.getProjectStartDate());

            return validationHandler.addAnyErrors(updateResult, toField("projectStartDate")).
                    failNowOrSucceedWith(failureView, () -> redirectToProjectDetails(projectId));
        });
    }

    private String doViewPartnerProjectLocation(long projectId, long organisationId, UserResource loggedInUser, Model model, PartnerProjectLocationForm form) {

        if (!projectService.userIsPartnerInOrganisationForProject(projectId, organisationId, loggedInUser.getId())) {
            return redirectToProjectDetails(projectId);
        }

        ProjectResource projectResource = projectService.getById(projectId);

        model.addAttribute("model", new PartnerProjectLocationViewModel(projectId, projectResource.getName(), organisationId));
        model.addAttribute(FORM_ATTR_NAME, form);

        return "project/partner-project-location";
    }

    private String doViewProjectStartDate(Model model, ProjectResource projectResource, ProjectDetailsStartDateForm form) {
        model.addAttribute("model", new ProjectDetailsStartDateViewModel(projectResource));
        model.addAttribute(FORM_ATTR_NAME, form);
        return "project/details-start-date";
    }

    private List<OrganisationResource> getPartnerOrganisations(final List<ProjectUserResource> projectRoles) {

        final Comparator<OrganisationResource> compareById =
                Comparator.comparingLong(OrganisationResource::getId);

        final Supplier<SortedSet<OrganisationResource>> supplier = () -> new TreeSet<>(compareById);

        SortedSet<OrganisationResource> organisationSet = projectRoles.stream()
                .filter(uar -> uar.getRole() == PARTNER.getId())
                .map(uar -> organisationRestService.getOrganisationById(uar.getOrganisation()).getSuccess())
                .collect(Collectors.toCollection(supplier));

        return new ArrayList<>(organisationSet);
    }

    private List<Long> getUsersPartnerOrganisations(UserResource loggedInUser, List<ProjectUserResource> projectUsers) {
        List<ProjectUserResource> partnerProjectUsers = simpleFilter(projectUsers,
                user -> loggedInUser.getId().equals(user.getUser()) && user.getRoleName().equals(PARTNER.getName()));
        return simpleMap(partnerProjectUsers, ProjectUserResource::getOrganisation);
    }

    private String redirectToProjectDetails(long projectId) {
        return "redirect:/project/" + projectId + "/details";
    }

}
