package org.innovateuk.ifs.application.populator.forminput;

import org.innovateuk.ifs.applicant.resource.ApplicantFormInputResource;
import org.innovateuk.ifs.applicant.resource.ApplicantFormInputResponseResource;
import org.innovateuk.ifs.applicant.resource.ApplicantQuestionResource;
import org.innovateuk.ifs.applicant.resource.ApplicantResource;
import org.innovateuk.ifs.application.populator.AssignButtonsPopulator;
import org.innovateuk.ifs.application.viewmodel.AssignButtonsViewModel;
import org.innovateuk.ifs.application.viewmodel.forminput.TextAreaInputViewModel;
import org.innovateuk.ifs.organisation.resource.OrganisationTypeEnum;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.innovateuk.ifs.applicant.builder.ApplicantFormInputResourceBuilder.newApplicantFormInputResource;
import static org.innovateuk.ifs.applicant.builder.ApplicantFormInputResponseResourceBuilder.newApplicantFormInputResponseResource;
import static org.innovateuk.ifs.applicant.builder.ApplicantQuestionResourceBuilder.newApplicantQuestionResource;
import static org.innovateuk.ifs.applicant.builder.ApplicantQuestionStatusResourceBuilder.newApplicantQuestionStatusResource;
import static org.innovateuk.ifs.applicant.builder.ApplicantResourceBuilder.newApplicantResource;
import static org.innovateuk.ifs.application.builder.ApplicationResourceBuilder.newApplicationResource;
import static org.innovateuk.ifs.application.builder.QuestionStatusResourceBuilder.newQuestionStatusResource;
import static org.innovateuk.ifs.competition.builder.CompetitionResourceBuilder.newCompetitionResource;
import static org.innovateuk.ifs.form.builder.QuestionResourceBuilder.newQuestionResource;
import static org.innovateuk.ifs.organisation.builder.OrganisationResourceBuilder.newOrganisationResource;
import static org.innovateuk.ifs.user.builder.UserResourceBuilder.newUserResource;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link TextAreaViewModelPopulator}
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class TextAreaViewModelPopulatorTest {

    @InjectMocks
    private TextAreaViewModelPopulator textAreaViewModelPopulator;

    @Mock
    private AssignButtonsPopulator assignButtonsPopulator;

    @Test
    public void testPopulate() {
        ApplicantResource currentApplicant = newApplicantResource().withOrganisation(newOrganisationResource().withOrganisationType(OrganisationTypeEnum.BUSINESS.getId()).build()).build();
        ApplicantQuestionResource question = newApplicantQuestionResource()
                .withCurrentApplicant(currentApplicant)
                .withCurrentUser(newUserResource().build())
                .withCompetition(newCompetitionResource().build())
                .withApplication(newApplicationResource().build())
                .withApplicantQuestionStatuses(
                        newApplicantQuestionStatusResource()
                                .withStatus(newQuestionStatusResource().withMarkedAsComplete(true).build())
                                .withMarkedAsCompleteBy(currentApplicant)
                        .build(1)
                )
                .withQuestion(newQuestionResource().build())
                .build();
        ApplicantFormInputResource applicantFormInput = newApplicantFormInputResource().build();
        ApplicantFormInputResponseResource applicantResponse = newApplicantFormInputResponseResource().build();
        AssignButtonsViewModel assignButtonsViewModel = new AssignButtonsViewModel();

        when(assignButtonsPopulator.populate(question, question, true)).thenReturn(assignButtonsViewModel);

        TextAreaInputViewModel viewModel = textAreaViewModelPopulator.populate(question, null, question, applicantFormInput, applicantResponse);

        assertThat(viewModel.isComplete(), equalTo(true));
        assertThat(viewModel.getApplication(), equalTo(question.getApplication()));
        assertThat(viewModel.getApplicantSection(), equalTo(null));
        assertThat(viewModel.getAssignButtonsViewModel(), equalTo(assignButtonsViewModel));
    }
}
