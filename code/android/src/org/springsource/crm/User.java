package org.springsource.crm;


import org.codehaus.jackson.annotate.JsonProperty;

import java.util.*;

public class User {
    @JsonProperty
    private Long id;
    @JsonProperty
    private java.util.Set<Customer> customers = new HashSet<Customer>();
    @JsonProperty
    private boolean importedFromServiceProvider = false;
    @JsonProperty
    private String firstName;
    @JsonProperty
    private String lastName;
    @JsonProperty
    private String username;
    @JsonProperty
    private String password;
    @JsonProperty
    private boolean profilePhotoImported;
    @JsonProperty
    private String profilePhotoExt;
    @JsonProperty
    private boolean enabled;
    @JsonProperty
    private Date signupDate;


    public User(Long id, Set<Customer> customers, boolean enabled, String firstName, String lastName, String username,
                String password, boolean importedFromServiceProvider, String profilePhotoExt, boolean profilePhotoImported, Date signupDate) {
        this.customers = customers;
        this.enabled = enabled;
        this.firstName = firstName;
        this.id = id;
        this.importedFromServiceProvider = importedFromServiceProvider;
        this.lastName = lastName;
        this.password = password;
        this.profilePhotoExt = profilePhotoExt;
        this.profilePhotoImported = profilePhotoImported;
        this.signupDate = signupDate;
        this.username = username;
    }

    public Set<Customer> getCustomers() {
        return customers;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getFirstName() {
        return firstName;
    }

    public Long getId() {
        return id;
    }

    public boolean isImportedFromServiceProvider() {
        return importedFromServiceProvider;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPassword() {
        return password;
    }

    public String getProfilePhotoExt() {
        return profilePhotoExt;
    }

    public boolean isProfilePhotoImported() {
        return profilePhotoImported;
    }

    public Date getSignupDate() {
        return signupDate;
    }

    public String getUsername() {
        return username;
    }
}
