package org.springsource.examples.spring31.web;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springsource.examples.spring31.services.Customer;
import org.springsource.examples.spring31.services.CustomerService;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * A {@link Controller controller} implementation that exposes a RESTful API to clients
 * of the system.
 *
 * This API is to be secured using Spring Security OAuth as it supports full mutation of all
 * {@link Customer business data}.
 *
 * @author Josh Long
 */
@Controller
public class CustomerApiController {

    private Logger log = Logger.getLogger(getClass());

    @Autowired
    private CustomerService customerService;

    @ResponseBody
    @RequestMapping(value = "/api/crm/search", method = RequestMethod.GET)
    public Collection<Customer> search(@RequestParam("q") String query) throws Exception {
        Collection<Customer> customers = customerService.search(query);
        if (log.isDebugEnabled())
            log.debug(String.format("retrieved %s results for search query '%s'", Integer.toString(customers.size()), query));
        return customers;
    }

    @ResponseBody
    @RequestMapping(value = "/api/crm/customer/{id}", method = RequestMethod.GET)
    public Customer customerById(@PathVariable("id") Long  id) {
        return this.customerService.getCustomerById(id);
    }

    // http://springmvc31.joshlong.micro/crm/customers
    @ResponseBody
    @RequestMapping(value = "/api/crm/customers", method = RequestMethod.GET)
    public List<Customer> customers() {
        return this.customerService.getAllCustomers();
    }

    // http://springmvc31.joshlong.micro/crm/customers
    @ResponseBody
    @RequestMapping(value = "/api/crm/customers", method = RequestMethod.PUT)
    public Long  addCustomer(@RequestParam("firstName") String fn, @RequestParam("lastName") String ln) {
        return customerService.createCustomer(fn, ln, new Date()).getId();
    }


    @ResponseBody
    @RequestMapping(value = "/api/crm/customer/{id}", method = RequestMethod.POST)
    public Long  updateCustomer(@PathVariable("id") Long  id, @RequestBody Customer customer) {
        customerService.updateCustomer(id, customer.getFirstName(), customer.getLastName(), customer.getSignupDate());
        return id;
    }

/*    @ResponseBody
    @RequestMapping(value = "/customer/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Integer deleteCustomer(@PathVariable("id") Integer id) {
        customerService.deleteCustomer(id);
        return id;
    }*/

}             
