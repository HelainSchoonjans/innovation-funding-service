package org.innovateuk.ifs.application.validator;

import org.innovateuk.ifs.application.domain.Application;
import org.innovateuk.ifs.application.domain.FormInputResponse;
import org.innovateuk.ifs.finance.resource.cost.FinanceRowItem;
import org.springframework.validation.BindingResult;
import org.springframework.validation.DataBinder;

public final class ValidatorTestUtil {
	
	private ValidatorTestUtil(){}
	
    public static BindingResult getBindingResult(Application application) {
        return new DataBinder(application).getBindingResult();
    }

    public static BindingResult getBindingResult(FormInputResponse formInputResponse) {
        return new DataBinder(formInputResponse).getBindingResult();
    }

    public static BindingResult getBindingResult(FinanceRowItem costItem) {
        return new DataBinder(costItem).getBindingResult();
    }
}
