package org.innovateuk.ifs.euinvite.transactional;

import org.innovateuk.ifs.commons.error.Error;
import org.innovateuk.ifs.commons.service.ServiceResult;
import org.innovateuk.ifs.eugrant.domain.EuContact;
import org.innovateuk.ifs.eugrant.domain.EuFunding;
import org.innovateuk.ifs.eugrant.domain.EuGrant;
import org.innovateuk.ifs.eugrant.domain.EuOrganisation;
import org.innovateuk.ifs.eugrant.repository.EuGrantRepository;
import org.innovateuk.ifs.notifications.resource.Notification;
import org.innovateuk.ifs.notifications.resource.NotificationTarget;
import org.innovateuk.ifs.notifications.resource.SystemNotificationSource;
import org.innovateuk.ifs.notifications.resource.UserNotificationTarget;
import org.innovateuk.ifs.notifications.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.util.Collections.singletonList;
import static org.innovateuk.ifs.commons.error.CommonFailureKeys.GENERAL_NOT_FOUND;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceFailure;
import static org.innovateuk.ifs.commons.service.ServiceResult.serviceSuccess;
import static org.innovateuk.ifs.notifications.resource.NotificationMedium.EMAIL;

@Service
public class EuInviteServiceImpl implements EuInviteService {

    @Autowired
    private EuGrantRepository euGrantRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private SystemNotificationSource systemNotificationSource;

    @Override
    @Transactional
    public ServiceResult<Void> sendInvites(List<UUID> euGrantIds) {
        euGrantIds
                .forEach(id -> sendInvite(id)
                        .andOnSuccessReturnVoid(EuGrant::markNotificationSent)
                );

        return serviceSuccess();
    }

    private ServiceResult<EuGrant> sendInvite(UUID euGrantId) {

        Optional<EuGrant> euGrantOpt = euGrantRepository.findById(euGrantId);

        if(!euGrantOpt.isPresent()) {
            return serviceFailure(new Error(GENERAL_NOT_FOUND));
        }
        
        EuGrant euGrant = euGrantOpt.get();
        EuFunding euFunding = euGrant.getFunding();
        EuOrganisation euOrganisation = euGrant.getOrganisation();
        EuContact euContact = euGrant.getContact();
        NotificationTarget recipient = new UserNotificationTarget(
                euContact.getName(),
                euContact.getEmail()
        );

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");

        Map<String, Object> notificationArguments = new HashMap<>();
        notificationArguments.put("referenceCode", euGrant.getShortCode());
        notificationArguments.put("organisationType", euOrganisation.getOrganisationType());
        notificationArguments.put("registeredName", euOrganisation.getName());
        notificationArguments.put("registrationNumber", euOrganisation.getCompaniesHouseNumber());
        notificationArguments.put("fullName", euContact.getName());
        notificationArguments.put("jobTitle", euContact.getJobTitle());
        notificationArguments.put("email", euContact.getEmail());
        notificationArguments.put("telephone", euContact.getTelephone());
        notificationArguments.put("grantAgreementNumber", euFunding.getGrantAgreementNumber());
        notificationArguments.put("actionType", euFunding.getActionType().getName());
        notificationArguments.put("participationIdentificationCode", euFunding.getParticipantId());
        notificationArguments.put("projectName", euFunding.getProjectName());
        notificationArguments.put("startDate", euFunding.getProjectStartDate().format(formatter));
        notificationArguments.put("endDate", euFunding.getProjectEndDate().format(formatter));
        notificationArguments.put("fundingAmount", euFunding.getFundingContribution());
        notificationArguments.put("inviteUrl", "https://apply-for-innovation-funding.service.gov.uk/competition/339/overview");

        Notification notification = new Notification(
                systemNotificationSource,
                singletonList(recipient),
                Notifications.INVITE_EU_REGISTRANT,
                notificationArguments
        );

        return notificationService.sendNotificationWithFlush(notification, EMAIL)
                .andOnSuccessReturn(() -> euGrant);
    }

    private enum Notifications {
        INVITE_EU_REGISTRANT
    }
}
