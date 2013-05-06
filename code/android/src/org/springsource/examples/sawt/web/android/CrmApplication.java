package org.springsource.examples.sawt.web.android;


import android.content.Context;
import org.springsource.examples.sawt.web.android.model.Customer;
import org.springsource.examples.sawt.web.android.service.*;

import java.util.concurrent.atomic.AtomicReference;

@Deprecated
public class CrmApplication {

    private static final AtomicReference<CrmApplication> instance = new AtomicReference<CrmApplication>();
    private Customer customer;

    // the customer being edited
    private CustomerService customerService;

    public CrmApplication(String url) {
        this.customerService = new CustomerServiceClient(url);
    }

    public static CrmApplication crmApplicationInstance(Context context) {
//        instance.compareAndSet(null, new CrmApplication(context.getString(R.string.base_url)));
//        return instance.get();
        return null;
    }

    public CustomerService getCustomerService() {
        return customerService;
    }

    public Customer getCustomer() {
        return customer;
    }

    public synchronized void setCustomer(Customer customer) {
        this.customer = customer;
    }
}
