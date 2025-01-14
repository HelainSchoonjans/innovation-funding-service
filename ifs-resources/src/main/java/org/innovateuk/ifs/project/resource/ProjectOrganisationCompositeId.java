package org.innovateuk.ifs.project.resource;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;


public class ProjectOrganisationCompositeId implements Serializable {

    private Long projectId;

    private Long organisationId;

    public static ProjectOrganisationCompositeId id(Long projectId, Long organisationId){
        return new ProjectOrganisationCompositeId(projectId, organisationId);
    }

    public ProjectOrganisationCompositeId() {}

    public ProjectOrganisationCompositeId(Long projectId, Long organisationId) {

        this.projectId = projectId;
        this.organisationId = organisationId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public Long getOrganisationId() {
        return organisationId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public void setOrganisationId(Long organisationId) {
        this.organisationId = organisationId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ProjectOrganisationCompositeId that = (ProjectOrganisationCompositeId) o;

        return new EqualsBuilder()
                .append(projectId, that.projectId)
                .append(organisationId, that.organisationId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(projectId)
                .append(organisationId)
                .toHashCode();
    }
}
