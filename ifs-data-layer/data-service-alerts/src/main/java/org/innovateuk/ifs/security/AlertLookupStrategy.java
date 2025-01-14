package org.innovateuk.ifs.security;

import org.innovateuk.ifs.alert.resource.AlertResource;
import org.innovateuk.ifs.commons.security.PermissionEntityLookupStrategies;
import org.innovateuk.ifs.commons.security.PermissionEntityLookupStrategy;
import org.innovateuk.ifs.mapper.AlertMapper;
import org.innovateuk.ifs.repository.AlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Lookup strategy for {@link AlertResource}, used for permissioning.
 */
@Component
@PermissionEntityLookupStrategies
public class AlertLookupStrategy {

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private AlertMapper alertMapper;

    @PermissionEntityLookupStrategy
    public AlertResource getAlertResource(final Long id){
        return alertMapper.mapToResource(alertRepository.findById(id).orElse(null));
    }

}
