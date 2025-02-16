package org.innovateuk.ifs.finance.transactional;

import org.innovateuk.ifs.commons.security.NotSecured;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.finance.resource.OrganisationFinancesWithGrowthTableResource;
import org.innovateuk.ifs.finance.resource.OrganisationFinancesWithoutGrowthTableResource;

public interface OrganisationFinanceService {
    @NotSecured(value = "Service should only be calling other services to receive data and should be using their permission rules.", mustBeSecuredByOtherServices = false)
    ServiceResult<OrganisationFinancesWithGrowthTableResource> getOrganisationWithGrowthTable(long applicationId, long organisationId);

    @NotSecured(value = "Service should only be calling other services to receive data and should be using their permission rules.", mustBeSecuredByOtherServices = false)
    ServiceResult<OrganisationFinancesWithoutGrowthTableResource> getOrganisationWithoutGrowthTable(long applicationId, long organisationId);

    @NotSecured(value = "Service should only be calling other services to receive data and should be using their permission rules.", mustBeSecuredByOtherServices = false)
    ServiceResult<Void> updateOrganisationWithGrowthTable(long applicationId, long organisationId, OrganisationFinancesWithGrowthTableResource finances);

    @NotSecured(value = "Service should only be calling other services to receive data and should be using their permission rules.", mustBeSecuredByOtherServices = false)
    ServiceResult<Void> updateOrganisationWithoutGrowthTable(long applicationId, long organisationId, OrganisationFinancesWithoutGrowthTableResource finances);

    @NotSecured(value = "Service should only be calling other services to receive data and should be using their permission rules.", mustBeSecuredByOtherServices = false)
    ServiceResult<Boolean> isShowStateAidAgreement(long applicationId, long organisationId);
}