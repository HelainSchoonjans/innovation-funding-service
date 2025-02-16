package org.innovateuk.ifs.project.spendprofile.repository;

import org.innovateuk.ifs.project.spendprofile.domain.SpendProfile;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;
import java.util.Optional;

/**
 * This interface is used to generate Spring Data Repositories.
 * For more info:
 * http://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories
 */
public interface SpendProfileRepository extends PagingAndSortingRepository<SpendProfile, Long> {
    Optional<SpendProfile> findOneByProjectIdAndOrganisationId(Long projectId, Long organisationId);
    List<SpendProfile> findByProjectId(Long projectId);
}
