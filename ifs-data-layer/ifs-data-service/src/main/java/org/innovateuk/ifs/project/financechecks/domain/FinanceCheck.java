package org.innovateuk.ifs.project.financechecks.domain;

import org.innovateuk.ifs.organisation.domain.Organisation;
import org.innovateuk.ifs.project.core.domain.Project;

import javax.persistence.*;

import static javax.persistence.CascadeType.ALL;

@Entity
public class FinanceCheck {
    public static final String FINANCE_CHECK_COSTS_DESCRIPTION = "Finance check costs for partner for a project";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projectId", referencedColumnName = "id", nullable = false)
    private Project project;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisationId", referencedColumnName = "id", nullable = false)
    private Organisation organisation;

    @OneToOne(cascade = ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "costGroupId", referencedColumnName = "id", nullable = false)
    private CostGroup costGroup;

    public FinanceCheck(Project project, CostGroup costGroup) {
        this.project = project;
        this.costGroup = costGroup;
    }

    public FinanceCheck() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public CostGroup getCostGroup() {
        return costGroup;
    }

    public void setCostGroup(CostGroup costGroup) {
        this.costGroup = costGroup;
    }

    public Organisation getOrganisation() {
        return organisation;
    }

    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
    }
}
