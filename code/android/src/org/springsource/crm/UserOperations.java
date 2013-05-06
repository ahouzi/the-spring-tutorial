package org.springsource.crm;

/**
 * operations for dealing with {@link User user} instances.
 *
 * @author Josh Long
 */
public  interface  UserOperations {
    User self() ;
    User byId(long userId) ;
    User getUserProfile () ;

}
