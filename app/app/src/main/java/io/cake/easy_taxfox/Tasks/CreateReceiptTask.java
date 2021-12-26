package io.cake.easy_taxfox.Tasks;

import android.app.Activity;
import io.cake.easy_taxfox.Entities.Receipt;
import io.cake.easy_taxfox.Database.TaxFoxDatabase;

/***
 * This class performs the insertion of a receipt into the database
 */
public class CreateReceiptTask implements Runnable{

    private TaxFoxDatabase db;
    private Receipt receipt;
    private Activity activityContext;
    private CreateReceiptResultListener listener;

    public CreateReceiptTask(TaxFoxDatabase db, Receipt receipt, Activity activityContext, CreateReceiptResultListener listener) {
        this.db = db;
        this.receipt = receipt;
        this.activityContext = activityContext;
        this.listener = listener;
    }

    @Override
    public void run() {
        final long receiptId = db.receiptDao().createReceipt(receipt);
        activityContext.runOnUiThread(() -> listener.onResult(receiptId));
    }

    public interface CreateReceiptResultListener{
        void onResult(long receiptId);
    }
}
