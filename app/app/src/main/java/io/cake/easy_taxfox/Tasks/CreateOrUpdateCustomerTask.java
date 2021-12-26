package io.cake.easy_taxfox.Tasks;

import io.cake.easy_taxfox.Entities.Customer;
import io.cake.easy_taxfox.Database.TaxFoxDatabase;

/***
 * This class performs the insertion or update of a customer into the database
 */
public class CreateOrUpdateCustomerTask implements Runnable {

    private TaxFoxDatabase db;
    private Customer customer;

    public CreateOrUpdateCustomerTask(TaxFoxDatabase db, Customer customer) {
        this.db = db;
        this.customer = customer;
    }

    @Override
    public void run() {
        if (customer.getCustomerId()!=0){
            db.customerDao().updateCustomer(customer);
        }else{
            db.customerDao().createCustomer(customer);
        }
    }
}
