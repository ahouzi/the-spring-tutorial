package org.springsource.examples.spring31.services;


import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Simple object that administers all customer data. This user is the one on whose behalf modifications to
 * {@link Customer customer data} are made.
 *
 * @author Josh Long
 */
@Entity(name = "USERS")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "user")
    private Set<Customer> customers = new HashSet<Customer>();

    private String email;
    private String password;
    private boolean profilePhotoImported;
    private String profilePhotoExt;
    private boolean enabled;
    private Date signupDate;

    public String getProfilePhotoExt() {
        return profilePhotoExt;
    }

    public void setProfilePhotoExt(String profilePhotoExt) {
        this.profilePhotoExt = profilePhotoExt;
    }

    public boolean isProfilePhotoImported() {
        return profilePhotoImported;
    }

    public void setProfilePhotoImported(boolean profilePhotoImported) {
        this.profilePhotoImported = profilePhotoImported;
    }

    public boolean isEnabled() {
        return enabled;
    }


    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Date getSignupDate() {
        return signupDate;
    }

    public void setSignupDate(Date signupDate) {
        this.signupDate = signupDate;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<Customer> getCustomers() {
        return customers;
    }

    public void setCustomers(Set<Customer> customers) {
        this.customers = customers;
    }
}
