package org.springsource.examples.spring31.web;


import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * wrapper object to encapsulate the details of the login <CODE>username</CODE> and <CODE>password</CODE>.
 *
 * @author Josh Long
 *
 */
public class SignInAttempt {
    @NotEmpty
    private String password;

    @Email
    @NotEmpty
    private String username;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}