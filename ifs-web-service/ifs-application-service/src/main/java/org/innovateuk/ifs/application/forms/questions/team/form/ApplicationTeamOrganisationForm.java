package org.innovateuk.ifs.application.forms.questions.team.form;

import org.innovateuk.ifs.commons.validation.ValidationConstants;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class ApplicationTeamOrganisationForm {

    @NotBlank(message = "{validation.applicationteam.organisation.name.required}")
    private String organisationName;

    @NotBlank(message = "{validation.standard.name.required}")
    private String name;

    @NotBlank(message = "{validation.applicationteam.email.required}")
    @Email(regexp = ValidationConstants.EMAIL_DISALLOW_INVALID_CHARACTERS_REGEX, message = "{validation.applicationteam.email.format}")
    @Size(max = 254, message = "{validation.applicationteam.email.required}")
    private String email;

    public String getOrganisationName() {
        return organisationName;
    }

    public ApplicationTeamOrganisationForm setOrganisationName(String organisationName) {
        this.organisationName = organisationName;
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
