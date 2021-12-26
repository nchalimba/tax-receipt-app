package io.cake.easy_taxfox.Tasks;

import android.app.Activity;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import io.cake.easy_taxfox.Entities.Export;
import io.cake.easy_taxfox.Entities.Receipt;
import io.cake.easy_taxfox.Helpers.PdfHelper;

/***
 * This class performs the summary pdf export
 */
public class CreatePdfExportSummaryTask implements Runnable{

    private List<Receipt> receipts;
    private PdfHelper helper;
    private PdfHelper.PdfExportResultListener listener;
    private Activity activityContext;

    public CreatePdfExportSummaryTask(List<Receipt> receipts, PdfHelper helper,Activity activityContext, PdfHelper.PdfExportResultListener listener) {
        this.receipts = receipts;
        this.helper = helper;
        this.activityContext = activityContext;
        this.listener = listener;
    }

    @Override
    public void run() {
        try {
            helper.createExportSummary(receipts, () -> {});
            ArrayList<Export> exports = helper.getChachedExport();
            activityContext.runOnUiThread(() -> listener.onExportResult(exports));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
