package org.innovateuk.ifs.status.viewmodel;

import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.internal.InternalProjectSetupRow;
import org.innovateuk.ifs.project.internal.ProjectSetupStage;

import java.util.List;

import static org.innovateuk.ifs.project.internal.ProjectSetupStage.BANK_DETAILS;

public abstract class BaseCompetitionStatusTableViewModel {

    private final long competitionId;
    private final String competitionName;
    private final List<ProjectSetupStage> columns;
    private final List<InternalProjectSetupRow> rows;
    private final boolean canExportBankDetails;
    private final boolean isLoan;

    public BaseCompetitionStatusTableViewModel(CompetitionResource competitionResource, List<InternalProjectSetupRow> rows, boolean projectFinanceUser) {
        this.competitionId = competitionResource.getId();
        this.competitionName = competitionResource.getName();
        this.columns = competitionResource.getProjectSetupStages();
        this.rows = rows;
        this.canExportBankDetails = projectFinanceUser && columns.contains(BANK_DETAILS);
        this.isLoan = competitionResource.isLoan();
    }

    public abstract String getEmptyTableText();

    public long getCompetitionId() {
        return competitionId;
    }

    public String getCompetitionName() {
        return competitionName;
    }

    public List<ProjectSetupStage> getColumns() {
        return columns;
    }

    public List<InternalProjectSetupRow> getRows() {
        return rows;
    }

    public boolean isCanExportBankDetails() {
        return canExportBankDetails;
    }

    public boolean isIsLoan() {
        return isLoan;
    }
}
