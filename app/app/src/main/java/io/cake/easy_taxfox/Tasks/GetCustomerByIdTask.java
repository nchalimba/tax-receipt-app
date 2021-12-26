package io.cake.easy_taxfox.Tasks;

import android.app.Activity;
import io.cake.easy_taxfox.Entities.Customer;
import io.cake.easy_taxfox.Database.TaxFoxDatabase;

/***
 * This class performs the selection of a customer by a given id from the database
 */
public class GetCustomerByIdTask implements Runnable {

    private TaxFoxDatabase db;
    private GetCustomerByIdResultListener listener;
    private Activity activityContext;
    private int id;

    public GetCustomerByIdTask(TaxFoxDatabase db, GetCustomerByIdResultListener listener, Activity activityContext, int id) {
        this.db = db;
        this.listener = listener;
        this.activityContext = activityContext;
        this.id = id;
    }

    @Override
    public void run() {
        final Customer customer = db.customerDao().getCustomerById(id);
        activityContext.runOnUiThread(() -> listener.onResult(customer));
    }

    public interface GetCustomerByIdResultListener{
        void onResult(Customer customer);
    }
}
