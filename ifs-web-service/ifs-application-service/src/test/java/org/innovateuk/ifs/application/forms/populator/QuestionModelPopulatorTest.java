package org.innovateuk.ifs.application.forms.populator;

import org.innovateuk.ifs.BaseUnitTest;
import org.innovateuk.ifs.applicant.resource.ApplicantQuestionResource;
import org.innovateuk.ifs.application.forms.viewmodel.QuestionViewModel;
import org.innovateuk.ifs.application.populator.ApplicationNavigationPopulator;
import org.innovateuk.ifs.application.populator.forminput.FormInputViewModelGenerator;
import org.innovateuk.ifs.application.resource.ApplicationState;
import org.innovateuk.ifs.application.resource.QuestionStatusResource;
import org.innovateuk.ifs.application.service.QuestionService;
import org.innovateuk.ifs.application.viewmodel.NavigationViewModel;
import org.innovateuk.ifs.application.viewmodel.forminput.AbstractFormInputViewModel;
import org.innovateuk.ifs.form.ApplicationForm;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.ui.Model;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.innovateuk.ifs.applicant.builder.ApplicantQuestionResourceBuilder.newApplicantQuestionResource;
import static org.innovateuk.ifs.applicant.builder.ApplicantResourceBuilder.newApplicantResource;
import static org.innovateuk.ifs.application.builder.ApplicationResourceBuilder.newApplicationResource;
import static org.innovateuk.ifs.application.builder.QuestionStatusResourceBuilder.newQuestionStatusResource;
import static org.innovateuk.ifs.form.builder.QuestionResourceBuilder.newQuestionResource;
import static org.innovateuk.ifs.organisation.builder.OrganisationResourceBuilder.newOrganisationResource;
import static org.innovateuk.ifs.user.builder.ProcessRoleResourceBuilder.newProcessRoleResource;
import static org.innovateuk.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.innovateuk.ifs.user.resource.Role.LEADAPPLICANT;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class QuestionModelPopulatorTest extends BaseUnitTest {

    @InjectMocks
    private QuestionModelPopulator populator;
    @Mock
    private ApplicationNavigationPopulator applicationNavigationPopulator;
    @Mock
    private FormInputViewModelGenerator formInputViewModelGenerator;
    @Mock
    private QuestionService questionService;

    @Test
    public void testPopulate() {
        ApplicantQuestionResource question =  newApplicantQuestionResource()
                .withQuestion(newQuestionResource().build())
                .withApplication(newApplicationResource().withApplicationState(ApplicationState.OPENED).build())
                .withCurrentApplicant(newApplicantResource()
                        .withOrganisation(newOrganisationResource().build())
                        .withProcessRole(newProcessRoleResource().withRole(LEADAPPLICANT).build())
                        .build())
                .withCurrentUser(newUserResource().build())
                .build();
        Model model = mock(Model.class);
        ApplicationForm form = mock(ApplicationForm.class);
        AbstractFormInputViewModel formInputViewModel = mock(AbstractFormInputViewModel.class);
        when(formInputViewModel.isReadonly()).thenReturn(true);
        QuestionStatusResource questionStatusResource = newQuestionStatusResource().build();
        List<QuestionStatusResource> notifications = newQuestionStatusResource().build(1);
        NavigationViewModel navigationViewModel = new NavigationViewModel();

        when(formInputViewModelGenerator.fromQuestion(question, form)).thenReturn(singletonList(formInputViewModel));
        when(applicationNavigationPopulator.addNavigation(question.getQuestion(), question.getApplication().getId())).thenReturn(navigationViewModel);
        when(questionService.getByQuestionIdAndApplicationIdAndOrganisationId(question.getQuestion().getId(), question.getApplication().getId(), question.getCurrentApplicant().getOrganisation().getId())).thenReturn(questionStatusResource);
        when(questionService.getNotificationsForUser(asList(questionStatusResource), question.getCurrentUser().getId())).thenReturn(notifications);

        QuestionViewModel viewModel = populator.populateModel(question, form);

        assertThat(viewModel.isAllReadOnly(), equalTo(true));
        assertThat(viewModel.getCurrentApplicant(), equalTo(question.getCurrentApplicant()));
        assertThat(viewModel.isQuestion(), equalTo(true));
        assertThat(viewModel.isSection(), equalTo(false));
        assertThat(viewModel.getNavigation(), equalTo(navigationViewModel));
        assertThat(viewModel.isLeadApplicant(), equalTo(true));

        verify(questionService).removeNotifications(notifications);
    }
}