package org.innovateuk.ifs.application.resource.comparators;

import org.innovateuk.ifs.application.resource.ApplicationSummaryResource;

import java.util.Comparator;

/**
 * Comparator, handling nulls, and using id if number of partners is equal.
 */
public class ApplicationSummaryResourceNumberOfPartnersComparator extends DualFieldComparator<Integer, Long> implements Comparator<ApplicationSummaryResource> {

    @Override
    public int compare(ApplicationSummaryResource resource1, ApplicationSummaryResource resource2) {

        Integer o1Lead = resource1.getNumberOfPartners();
        Integer o2Lead = resource2.getNumberOfPartners();

        Long o1Id = resource1.getId();
        Long o2Id = resource2.getId();

        return compare(o1Lead, o2Lead, o1Id, o2Id);
    }
}
