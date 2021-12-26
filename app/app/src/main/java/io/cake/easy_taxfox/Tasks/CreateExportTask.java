package io.cake.easy_taxfox.Tasks;

import android.app.Activity;
import io.cake.easy_taxfox.Entities.Export;
import io.cake.easy_taxfox.Database.TaxFoxDatabase;

/***
 * This class performs the insertion of a new export into the database
 */
public class CreateExportTask implements Runnable{

    private TaxFoxDatabase db;
    private Activity activityContext;
    private CreateExportResultListener listener;
    private Export export;

    public CreateExportTask(TaxFoxDatabase db, Activity activityContext, CreateExportResultListener listener, Export export) {
        this.db = db;
        this.activityContext = activityContext;
        this.listener = listener;
        this.export = export;
    }

    @Override
    public void run() {
        db.exportDao().createExport(export);
        activityContext.runOnUiThread(() -> listener.onResult());
    }

    public interface  CreateExportResultListener{
        void onResult();
    }
}
