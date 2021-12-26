package io.cake.easy_taxfox.Tasks;

import android.app.Activity;
import java.util.Arrays;
import java.util.List;
import io.cake.easy_taxfox.Entities.Receipt;
import io.cake.easy_taxfox.Database.TaxFoxDatabase;

/***
 * This class performs the selection of receipts by a given business year from the database
 */
public class GetReceiptsByBusinessYearTask implements Runnable {

    private TaxFoxDatabase db;
    private GetReceiptsByBusinessYearResultListener listener;
    private Activity activityContext;
    private int businessYear;

    public GetReceiptsByBusinessYearTask(TaxFoxDatabase db, GetReceiptsByBusinessYearResultListener listener, Activity activityContext, int businessYear) {
        this.db = db;
        this.listener = listener;
        this.activityContext = activityContext;
        this.businessYear = businessYear;
    }

    @Override
    public void run() {
        final List<Receipt> receipts = Arrays.asList(db.receiptDao().getReceiptsByBusinessYear(businessYear));
        activityContext.runOnUiThread(() -> listener.onResult(receipts));
    }

    public interface GetReceiptsByBusinessYearResultListener{
        void onResult(List<Receipt> receipts);
    }
}
