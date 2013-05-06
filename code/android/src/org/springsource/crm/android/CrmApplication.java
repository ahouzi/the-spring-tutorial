package org.springsource.crm.android;

import android.app.Application;

/**
 * main context for the application.
 *
 * @author Josh Long
 */
public class CrmApplication extends Application {

    private CrmApplicationContext crmApplicationContext;

    public void onCreate() {
        super.onCreate();
        this.crmApplicationContext = new CrmApplicationContext(getApplicationContext());
    }

    public CrmApplicationContext getCrmApplicationContext() {
        return this.crmApplicationContext;
    }


/*
    public CustomerService getCustomerService() {
        return customerService;
    }

    private Customer customer;

    // the customer being edited

    public Customer getCustomer() {
        return customer;
    }

    public synchronized void setCustomer(Customer customer) {
        this.customer = customer;
    }

    private CustomerService customerService;

    public CrmApplication(String url) {
        this.customerService = new CustomerServiceClient(url);
    }

    public static CrmApplication crmApplicationInstance(Context context) {
        instance.compareAndSet(null, new CrmApplication(context.getString(R.string.base_uri)));
        return instance.get();
    }

    private static final AtomicReference<CrmApplication> instance = new AtomicReference<CrmApplication>();*/

}
