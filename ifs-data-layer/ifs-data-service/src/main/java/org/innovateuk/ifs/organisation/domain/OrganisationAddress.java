package org.innovateuk.ifs.organisation.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.innovateuk.ifs.address.domain.Address;
import org.innovateuk.ifs.address.domain.AddressType;
import org.innovateuk.ifs.project.bankdetails.domain.BankDetails;

import javax.persistence.*;
import javax.validation.Valid;
import java.util.List;

import static javax.persistence.CascadeType.REMOVE;

/**
 * Resource object to store the address details, from the company, from the companies house api.
 */
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"organisation_id", "address_id"})})
public class OrganisationAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Organisation organisation;
    @Valid
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.PERSIST})
    private Address address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_type_id", referencedColumnName = "id")
    private AddressType addressType;

    @OneToMany(mappedBy = "organisationAddress", fetch = FetchType.LAZY, cascade = REMOVE)
    private List<BankDetails> bankDetails;

    public OrganisationAddress(Organisation organisation, Address address, AddressType addressType) {
        this.organisation = organisation;
        this.address = address;
        this.addressType = addressType;
    }

    public OrganisationAddress() {
        // no-arg constructor
    }

    public List<BankDetails> getBankDetails() {
        return bankDetails;
    }

    public void setBankDetails(List<BankDetails> bankDetails) {
        this.bankDetails = bankDetails;
    }
    
    @JsonIgnore
    public Organisation getOrganisation() {
        return organisation;
    }

    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
    }


    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public AddressType getAddressType() {
        return addressType;
    }

    public void setAddressType(AddressType addressType) {
        this.addressType = addressType;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
