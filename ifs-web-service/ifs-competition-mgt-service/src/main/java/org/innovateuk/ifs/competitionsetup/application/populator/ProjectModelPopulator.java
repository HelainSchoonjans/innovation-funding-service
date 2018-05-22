package org.innovateuk.ifs.competitionsetup.application.populator;

import org.innovateuk.ifs.competition.resource.CompetitionResource;
import org.innovateuk.ifs.competition.resource.CompetitionSetupSubsection;
import org.innovateuk.ifs.competitionsetup.core.populator.CompetitionSetupSubsectionModelPopulator;
import org.innovateuk.ifs.competitionsetup.core.viewmodel.CompetitionSetupSubsectionViewModel;
import org.innovateuk.ifs.competitionsetup.application.viewmodel.ProjectViewModel;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * populates the model for the initial details competition setup section.
 */
@Service
public class ProjectModelPopulator implements CompetitionSetupSubsectionModelPopulator {

    @Override
	public CompetitionSetupSubsection sectionToPopulateModel() {
		return CompetitionSetupSubsection.PROJECT_DETAILS;
	}

	@Override
	public CompetitionSetupSubsectionViewModel populateModel(CompetitionResource competitionResource, Optional<Long> objectId) {
        return new ProjectViewModel();
    }
}