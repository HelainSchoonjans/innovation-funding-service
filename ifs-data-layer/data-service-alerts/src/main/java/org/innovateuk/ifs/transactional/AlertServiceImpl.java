package org.innovateuk.ifs.transactional;

import org.innovateuk.ifs.alert.resource.AlertResource;
import org.innovateuk.ifs.alert.resource.AlertType;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.domain.Alert;
import org.innovateuk.ifs.mapper.AlertMapper;
import org.innovateuk.ifs.repository.AlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static java.time.ZonedDateTime.now;
import static org.innovateuk.ifs.commons.error.CommonErrors.notFoundError;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.util.CollectionFunctions.simpleMap;
import static org.innovateuk.ifs.util.EntityLookupCallbacks.find;

/**
 * Transactional and secured service providing operations around {@link Alert} data.
 */
@Service
public class AlertServiceImpl extends RootTransactionalService implements AlertService {

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private AlertMapper alertMapper;

    @Override
    public ServiceResult<List<AlertResource>> findAllVisible() {
        return serviceSuccess(simpleMap(alertRepository.findAllVisible(now()), alertMapper::mapToResource));
    }

    @Override
    public ServiceResult<List<AlertResource>> findAllVisibleByType(AlertType type) {
        return serviceSuccess(simpleMap(alertRepository.findAllVisibleByType(type, now()), alertMapper::mapToResource));
    }

    @Override
    public ServiceResult<AlertResource> findById(Long id) {
        return find(alertRepository.findById(id), notFoundError(Alert.class, id)).andOnSuccessReturn(alertMapper::mapToResource);
    }

    @Override
    @Transactional
    public ServiceResult<AlertResource> create(AlertResource alertResource) {
        Alert saved = alertRepository.save(alertMapper.mapToDomain(alertResource));
        return serviceSuccess(alertMapper.mapToResource(saved));
    }

    @Override
    @Transactional
    public ServiceResult<Void> delete(Long id) {
        alertRepository.deleteById(id);
        return serviceSuccess();
    }

    @Override
    @Transactional
    public ServiceResult<Void> deleteAllByType(AlertType type) {
        alertRepository.deleteByType(type);
        return serviceSuccess();
    }
}
