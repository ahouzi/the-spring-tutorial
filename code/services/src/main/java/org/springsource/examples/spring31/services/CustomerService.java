package org.springsource.examples.spring31.services;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@SuppressWarnings("unchecked")
@Service
@Transactional
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CustomerService {

    static private final String CUSTOMERS_REGION = "customers";

    private EntityManager entityManager;

    @PersistenceContext
    public void setEntityManger(EntityManager em) {
        this.entityManager = em;
    }

    public Customer createCustomer(String firstName, String lastName, Date signupDate) {
        Customer customer = new Customer();
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setSignupDate(signupDate);
        entityManager.persist(customer);
        return customer;
    }

    public Collection<Customer> search(String name) {
        String sqlName = ("%" + name + "%").toLowerCase();
        String sql = "select c.* from customers c where (LOWER( c.firstName ) LIKE :fn OR LOWER( c.lastName ) LIKE :ln)";
        return entityManager.createNativeQuery(sql, Customer.class)
                .setParameter("fn", sqlName)
                .setParameter("ln", sqlName)
                .getResultList();
    }


    @Transactional(readOnly = true)
    public List<Customer> getAllCustomers() {
        return entityManager.createQuery("SELECT * FROM " + Customer.class.getName()).getResultList();
    }


    @Cacheable(CUSTOMERS_REGION)
    @Transactional(readOnly = true)
    public Customer getCustomerById(Long id) {
        return entityManager.find(Customer.class, id);
    }

    @CacheEvict(CUSTOMERS_REGION)
    public void deleteCustomer(Long id) {
        Customer customer = getCustomerById(id);
        entityManager.remove(customer);
    }

    @CacheEvict(value = CUSTOMERS_REGION, key = "#id")
    public void updateCustomer(Long id, String fn, String ln, Date birthday) {
        Customer customer = getCustomerById(id);
        customer.setLastName(ln);
        customer.setSignupDate(birthday);
        customer.setFirstName(fn);
        entityManager.merge(customer);
    }
}
