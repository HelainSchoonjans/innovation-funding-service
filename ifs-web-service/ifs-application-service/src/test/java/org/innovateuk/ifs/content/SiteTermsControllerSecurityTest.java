package org.innovateuk.ifs.content;

import org.innovateuk.ifs.security.BaseControllerSecurityTest;
import org.junit.Test;

public class SiteTermsControllerSecurityTest extends BaseControllerSecurityTest<SiteTermsController> {

    @Override
    protected Class<? extends SiteTermsController> getClassUnderTest() {
        return SiteTermsController.class;
    }

    @Test
    public void newTermsAndConditions() {
        assertAccessDenied(() -> classUnderTest.newTermsAndConditions(null),
                () -> {

                });
    }

    @Test
    public void agreeNewTermsAndConditions() {
        assertAccessDenied(() -> classUnderTest.agreeNewTermsAndConditions(null, null, null, null, null, null),
                () -> {

                });
    }

}
