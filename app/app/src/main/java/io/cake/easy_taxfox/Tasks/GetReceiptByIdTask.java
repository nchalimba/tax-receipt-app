package io.cake.easy_taxfox.Tasks;

import android.app.Activity;
import io.cake.easy_taxfox.Entities.Receipt;
import io.cake.easy_taxfox.Database.TaxFoxDatabase;

/***
 * This class performs the selection of a receipt by a given id from the database
 */
public class GetReceiptByIdTask implements Runnable{

    private TaxFoxDatabase db;
    private Activity activityContext;
    private GetReceiptByIdResultListener listener;
    private int id;

    public GetReceiptByIdTask(TaxFoxDatabase db, Activity activityContext, GetReceiptByIdResultListener listener, int id) {
        this.db = db;
        this.activityContext = activityContext;
        this.listener = listener;
        this.id = id;
    }

    @Override
    public void run() {
        final Receipt receipt = db.receiptDao().getReceiptById(id);
        activityContext.runOnUiThread(() -> listener.onResult(receipt));
    }

    public interface GetReceiptByIdResultListener{
        void onResult(Receipt receipt);
    }
}
