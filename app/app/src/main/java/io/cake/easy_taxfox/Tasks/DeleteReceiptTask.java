package io.cake.easy_taxfox.Tasks;

import android.app.Activity;
import io.cake.easy_taxfox.Database.TaxFoxDatabase;
import io.cake.easy_taxfox.Entities.Receipt;

/***
 * This class performs the deletion of a receipt in the database
 */
public class DeleteReceiptTask implements Runnable{

    private TaxFoxDatabase db;
    private Receipt receipt;
    private DeleteReceiptResultListener listener;
    private Activity activityContext;

    public DeleteReceiptTask(TaxFoxDatabase db, Receipt receipt, Activity activityContext, DeleteReceiptResultListener listener) {
        this.db = db;
        this.receipt = receipt;
        this.activityContext = activityContext;
        this.listener = listener;
    }

    @Override
    public void run() {
        db.receiptDao().deleteReceipt(receipt);
        activityContext.runOnUiThread(() -> listener.onResult());
    }

    public interface DeleteReceiptResultListener{
        void onResult();
    }
}
