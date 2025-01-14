package org.innovateuk.ifs.fundingdecision.validator;


import org.innovateuk.ifs.application.domain.Application;
import org.innovateuk.ifs.application.transactional.ApplicationSummaryServiceImpl;
import org.innovateuk.ifs.fundingdecision.domain.FundingDecisionStatus;
import org.springframework.stereotype.Component;

@Component
public class ApplicationFundingDecisionValidator {
    public boolean isValid(Application application) {

        if(!hasBeenSubmitted(application) ||
                (decisionIsSuccessful(application) && decisionNotificationWasSent(application))) {
            return false;
        }

        return true;
    }

    private boolean hasBeenSubmitted(Application application) {
        boolean hasBeenSubmitted = ApplicationSummaryServiceImpl.SUBMITTED_APPLICATION_STATES.contains(application.getApplicationProcess().getProcessState());

        return hasBeenSubmitted;
    }

    private boolean decisionIsSuccessful(Application application) {
        return application.getFundingDecision() != null && application.getFundingDecision().equals(FundingDecisionStatus.FUNDED);
    }

    private boolean decisionNotificationWasSent(Application application) {
        return application.getManageFundingEmailDate() != null;
    }
}
