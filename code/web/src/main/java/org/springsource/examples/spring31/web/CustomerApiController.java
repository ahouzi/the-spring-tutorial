package org.springsource.examples.spring31.web;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springsource.examples.spring31.services.Customer;
import org.springsource.examples.spring31.services.CustomerService;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * A {@link Controller controller} implementation that exposes a RESTful API to clients
 * of the system.
 * <p/>
 * This API is to be secured using Spring Security OAuth as it supports full mutation of all
 * {@link Customer business data}.
 *
 * @author Josh Long
 */
@Controller
public class CustomerApiController {

    static public final String CUSTOMER_COLLECTION_URL = "/api/crm/{userId}/customers";
    static public final String CUSTOMER_SEARCH = "/api/crm/search";
    static public final String CUSTOMER_COLLECTION_ENTRY_URL = CUSTOMER_COLLECTION_URL + "/{customerId}";

    private Logger log = Logger.getLogger(getClass());

    private CustomerService customerService;

    @Inject
    public void setCustomerService(CustomerService customerService) {
        this.customerService = customerService;
    }

    @ResponseBody
    @RequestMapping(value = CUSTOMER_SEARCH, method = RequestMethod.GET)
    public Collection<Customer> search(@RequestParam("q") String query) throws Exception {
        Collection<Customer> customers = customerService.search(query);
        if (log.isDebugEnabled())
            log.debug(String.format("retrieved %s results for search query '%s'", Integer.toString(customers.size()), query));
        return customers;
    }

    @ResponseBody
    @RequestMapping(value = CUSTOMER_COLLECTION_ENTRY_URL, method = RequestMethod.GET)
    public Customer customerById(@PathVariable("customerId") Long id) {
        return this.customerService.getCustomerById(id);
    }

    @ResponseBody
    @RequestMapping(value = CUSTOMER_COLLECTION_URL, method = RequestMethod.GET)
    public List<Customer> customers(@PathVariable("userId") Long userId) {
        return this.customerService.getAllUserCustomers(userId);
    }

    @ResponseBody
    @RequestMapping(value = CUSTOMER_COLLECTION_URL, method = RequestMethod.POST)
    public Long addCustomer(@PathVariable("userId") Long userId, @RequestParam("firstName") String fn, @RequestParam("lastName") String ln) {
        return customerService.createCustomer(userId, fn, ln, new Date()).getId();
    }


    @ResponseBody
    @RequestMapping(value = CUSTOMER_COLLECTION_ENTRY_URL, method = RequestMethod.PUT)
    public Long updateCustomer(@PathVariable("customerId") Long id, @RequestParam("firstName") String fn, @RequestParam("lastName") String ln) {
        Customer customer = this.customerService.getCustomerById(id);
        customerService.updateCustomer(id, fn, ln, customer.getSignupDate());
        return id;
    }

    @ResponseBody
    @RequestMapping(value = CUSTOMER_COLLECTION_ENTRY_URL, method = RequestMethod.DELETE)
    public Long deleteCustomer(@PathVariable("customerId") Long id) {
        customerService.deleteCustomer(id);
        return id;
    }

}             
