package org.springsource.examples.spring31.services;


import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Service to manage {@link User users } that in turn can manage {@link Customer customers}.
 *
 * @author Josh Long
 */
@SuppressWarnings("unchecked")
@Service
@Transactional
public class UserService {

    // todo
    // - create users
    // - authenticate users

    public static final String USER_CACHE_REGION = "users";

    private EntityManager entityManager;

    @PersistenceContext
    public void setEntityManger(EntityManager em) {
        this.entityManager = em;
    }

    /**
     * Loads a given CRM user by its ID and its password
     *
     * @param email username
     * @param password password
     * @return the authenticated user or <CODE>null</CODE>
     */
    public User login(String email, String password) {
        User user = loginByUsername(email);
        if (user.getPassword().equalsIgnoreCase(password)) {
            return user;
        }
        return null;
    }

    public User createUser(String email, String pw) {
        // first make sure it doesnt already exist
        assert StringUtils.hasText(email) : "the 'email' can't be null";
        assert StringUtils.hasText(pw) : "the 'password' can't be null";
        assert loginByUsername(email) == null : "there is already an existing User with the email '" + email + "'";

        User user = new User();
        user.setEmail(email);
        user.setPassword(pw);
        entityManager.persist(user);
        entityManager.refresh(user);
        return user;
    }

    /**
     * Loads a given CRM user by its ID. All other Spring Security methods ultimately call this one.
     *
     * @param user the user to retrieve
     * @return the {@link User user }
     */
    @Cacheable(value = USER_CACHE_REGION)
    public User loginByUsername(String user) {
        return entityManager.createQuery("select u from  " + User.class.getName() + " u where u.email = :email", User.class)
                .setParameter("email", user)
                .getResultList()
                .iterator().next();
    }


    public void removeUser (long userId) {
       // todo
    }
}

