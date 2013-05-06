package org.springsource.crm;

/**
 * handles common, application-wide API calls.
 *
 *
 * @author Josh Long
 */
public  interface Crm {
    UserTemplate userOperations() ;
    CustomerTemplate customerOperations() ;

}
