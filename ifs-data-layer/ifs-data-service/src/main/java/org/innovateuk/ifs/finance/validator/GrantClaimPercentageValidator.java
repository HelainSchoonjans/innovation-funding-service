package org.innovateuk.ifs.finance.validator;

import org.innovateuk.ifs.finance.domain.ApplicationFinance;
import org.innovateuk.ifs.finance.domain.ApplicationFinanceRow;
import org.innovateuk.ifs.finance.domain.FinanceRow;
import org.innovateuk.ifs.finance.repository.ApplicationFinanceRowRepository;
import org.innovateuk.ifs.finance.resource.cost.GrantClaimPercentage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import static org.innovateuk.ifs.commons.error.ValidationMessages.rejectValue;

/**
 * This class validates the GrantClaim.
 */
@Component
public class GrantClaimPercentageValidator implements Validator {

    @Autowired
    private ApplicationFinanceRowRepository financeRowRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return GrantClaimPercentage.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        GrantClaimPercentage response = (GrantClaimPercentage) target;

        if(response.getPercentage() == null) {
            rejectValue(errors, "percentage", "org.hibernate.validator.constraints.NotBlank.message");
            return;
        }

        FinanceRow cost = financeRowRepository.findById(response.getId()).get();
        ApplicationFinance applicationFinance = ((ApplicationFinanceRow)cost).getTarget();
        Integer max = applicationFinance.getMaximumFundingLevel();
        if (max == null) {
            rejectValue(errors, "percentage", "validation.grantClaimPercentage.maximum.not.defined");
            return;
        }

        if (response.getPercentage() > max) {
            rejectValue(errors, "percentage", "validation.finance.grant.claim.percentage.max", max);
        } else if(response.getPercentage() < 0) {
            rejectValue(errors, "percentage", "validation.field.percentage.max.value.or.higher", 0);
        }
    }

}
