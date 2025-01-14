package org.innovateuk.ifs.application.forms.saver;

import org.innovateuk.ifs.commons.error.ValidationMessages;
import org.innovateuk.ifs.form.resource.FormInputResource;
import org.innovateuk.ifs.form.resource.QuestionResource;
import org.innovateuk.ifs.form.service.FormInputResponseRestService;
import org.innovateuk.ifs.form.service.FormInputRestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.function.Consumer;

import static org.innovateuk.ifs.commons.error.ErrorConverterFactory.toField;
import static org.innovateuk.ifs.form.resource.FormInputScope.APPLICATION;
import static org.innovateuk.ifs.form.resource.FormInputType.FILEUPLOAD;
import static org.innovateuk.ifs.util.HttpUtils.requestParameterPresent;

/**
 * This Saver will handle save all the non-file questions that are related to the application.
 */
@Service
public class ApplicationQuestionNonFileSaver extends AbstractApplicationSaver {

    @Autowired
    private FormInputResponseRestService formInputResponseRestService;

    @Autowired
    private FormInputRestService formInputRestService;

    public ValidationMessages saveNonFileUploadQuestions(List<QuestionResource> questions,
                                                         HttpServletRequest request,
                                                         Long userId,
                                                         Long applicationId,
                                                         boolean ignoreEmpty) {

        ValidationMessages allErrors = new ValidationMessages();
        questions.forEach(question ->
                {
                    List<FormInputResource> formInputs = formInputRestService.getByQuestionIdAndScope(question.getId(), APPLICATION).getSuccess();
                    formInputs
                            .stream()
                            .filter(formInput -> FILEUPLOAD != formInput.getType())
                            .forEach(saveFormInput(request, userId, applicationId, ignoreEmpty, allErrors));
                }
        );
        return allErrors;
    }

    private Consumer<? super FormInputResource> saveFormInput(HttpServletRequest request,
                                                              Long userId,
                                                              Long applicationId,
                                                              boolean ignoreEmpty,
                                                              ValidationMessages allErrors) {
        return formInput -> {
            String formInputKey = getFormInputKey(formInput.getId());

            requestParameterPresent(formInputKey, request).ifPresent(value -> {
                ValidationMessages errors = formInputResponseRestService.saveQuestionResponse(
                        userId,
                        applicationId,
                        formInput.getId(),
                        value,
                        ignoreEmpty)
                        .getSuccess();
                allErrors.addAll(errors, toField(formInputKey));
            });
        };
    }
}
