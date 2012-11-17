package org.springsource.examples.spring31.services;


import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * @author Josh Long
 */
@SuppressWarnings("unchecked")
@Service
@Transactional
public class UserService implements UserDetailsService, ClientDetailsService {

    private EntityManager entityManager;

    @PersistenceContext
    public void setEntityManger(EntityManager em) {
        this.entityManager = em;
    }


    // todo
    @Override
    public ClientDetails loadClientByClientId(String clientId) throws OAuth2Exception {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    // todo
    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
