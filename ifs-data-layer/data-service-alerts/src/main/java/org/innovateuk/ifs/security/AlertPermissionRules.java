package org.innovateuk.ifs.security;

import org.innovateuk.ifs.alert.resource.AlertResource;
import org.innovateuk.ifs.commons.security.PermissionRule;
import org.innovateuk.ifs.commons.security.PermissionRules;
import org.innovateuk.ifs.domain.Alert;
import org.innovateuk.ifs.user.resource.UserResource;
import org.springframework.stereotype.Component;

import static org.innovateuk.ifs.util.SecurityRuleUtil.isSystemMaintenanceUser;

/**
 * Provides the permissions around CRUD operations for {@link Alert} resources.
 */
@Component
@PermissionRules
public class AlertPermissionRules {

    @PermissionRule(value = "CREATE", description = "System maintentenance users can create Alerts")
    public boolean systemMaintenanceUserCanCreateAlerts(final AlertResource alertResource, final UserResource user) {
        return isSystemMaintenanceUser(user);
    }

    @PermissionRule(value = "DELETE", description = "System maintentenance users can delete Alerts")
    public boolean systemMaintenanceUserCanDeleteAlerts(final AlertResource alertResource, final UserResource user) {
        return isSystemMaintenanceUser(user);
    }
}
