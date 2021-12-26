package io.cake.easy_taxfox.Tasks;

import io.cake.easy_taxfox.Entities.Receipt;
import io.cake.easy_taxfox.Database.TaxFoxDatabase;

/***
 * This class performs the update of a receipt from the database
 */
public class UpdateReceiptTask implements Runnable {

    private TaxFoxDatabase db;
    private Receipt receipt;

    public UpdateReceiptTask(TaxFoxDatabase db, Receipt receipt) {
        this.db = db;
        this.receipt = receipt;
    }

    @Override
    public void run() {
        db.receiptDao().updateReceipt(receipt);
    }
}
