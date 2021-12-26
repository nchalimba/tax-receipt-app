package io.cake.easy_taxfox.Database;

import android.app.Activity;

import androidx.room.Room;

import java.util.concurrent.Executors;

import io.cake.easy_taxfox.Config.AppConfig;
import io.cake.easy_taxfox.Entities.Customer;
import io.cake.easy_taxfox.Entities.Export;
import io.cake.easy_taxfox.Entities.Receipt;
import io.cake.easy_taxfox.Tasks.CreateExportTask;
import io.cake.easy_taxfox.Tasks.CreateOrUpdateCustomerTask;
import io.cake.easy_taxfox.Tasks.CreateReceiptTask;
import io.cake.easy_taxfox.Tasks.DeleteReceiptTask;
import io.cake.easy_taxfox.Tasks.GetCustomerByIdTask;
import io.cake.easy_taxfox.Tasks.GetReceiptByIdTask;
import io.cake.easy_taxfox.Tasks.GetReceiptsByBusinessYearTask;
import io.cake.easy_taxfox.Tasks.UpdateReceiptTask;

/***
 * This is class serves as an abstraction layer to the database and handles all database requests.
 */
public class TaxFoxDatabaseHelper {
    private TaxFoxDatabase db;
    private static TaxFoxDatabaseHelper INSTANCE;
    
    private Activity activityContext;

    /***
     * This method creates an instance of the database helper and opens the db connection.
     * @param activityContext
     */
    private TaxFoxDatabaseHelper(Activity activityContext){
        this.activityContext= activityContext;
        initDatabase();
    }


    /***
     * This method is an implementation of the singleton design pattern. It gets the currently instantiated object or creates a new one.
     * @param activityContext
     * @return
     */
    public static TaxFoxDatabaseHelper getInstance(Activity activityContext){
        if(INSTANCE == null) {
            INSTANCE = new TaxFoxDatabaseHelper(activityContext);
        }else {
            setActivityContext(activityContext);
        }
        return INSTANCE;
    }

    /***
     * This method sets the activity context needed for the database.
     * @param activityContext
     */
    private static void setActivityContext(Activity activityContext){
        INSTANCE.activityContext = activityContext;
    }

    /***
     * This method destroys the single instance after closing the db connection.
     */
    public static void destroyInstance(){
        if(INSTANCE != null){
            INSTANCE.db.close();
            INSTANCE = null;
        }

    }

    /***
     * This method initializes the database connection.
     */
    private void initDatabase() {
        db = Room.databaseBuilder(activityContext,
                TaxFoxDatabase.class,
                AppConfig.DATABASE_NAME).build();
    }


    /**
     * This runnable task creates a new export.
     * @param export
     * @param listener
     */
    public void createExport(Export export, CreateExportTask.CreateExportResultListener listener){
        CreateExportTask task = new CreateExportTask(db, activityContext, listener, export);
        Executors.newSingleThreadExecutor().submit(task);
    }

    /**
     * This runnable task creates a new customer or updates the customer data.
     * @param customer
     */
    public void createOrUpdateCustomer(Customer customer){
        CreateOrUpdateCustomerTask task = new CreateOrUpdateCustomerTask(db, customer);
        Executors.newSingleThreadExecutor().submit(task);
    }

    /**
     * This runnable task creates a new receipt.
     * @param receipt
     * @param listener
     */
    public void createReceipt(Receipt receipt, CreateReceiptTask.CreateReceiptResultListener listener){
        CreateReceiptTask task = new CreateReceiptTask(db, receipt, activityContext, listener);
        Executors.newSingleThreadExecutor().submit(task);
    }

    /**
     * This runnable task deletes a receipt.
     * @param receipt
     * @param listener
     */
    public void deleteReceipt(Receipt receipt, DeleteReceiptTask.DeleteReceiptResultListener listener){
        DeleteReceiptTask task = new DeleteReceiptTask(db, receipt, activityContext, listener);
        Executors.newSingleThreadExecutor().submit(task);
    }

    /**
     * This runnable task gets a customer by its id.
     * @param id
     * @param listener
     */
    public void getCustomerById(int id, GetCustomerByIdTask.GetCustomerByIdResultListener listener){
        GetCustomerByIdTask task = new GetCustomerByIdTask(db, listener, activityContext, id);
        Executors.newSingleThreadExecutor().submit(task);
    }

    /**
     * This runnable task gets a receipt by its id.
     * @param id
     * @param listener
     */
    public void getReceiptById(int id, GetReceiptByIdTask.GetReceiptByIdResultListener listener){
        GetReceiptByIdTask task = new GetReceiptByIdTask(db, activityContext, listener,id);
        Executors.newSingleThreadExecutor().submit(task);
    }

    /**
     * This runnable task gets a list of receipts by its businessyear.
     * @param businessYear
     * @param listener
     */
    public void getReceiptsByBusinessYear(int businessYear, GetReceiptsByBusinessYearTask.GetReceiptsByBusinessYearResultListener listener){
        GetReceiptsByBusinessYearTask task = new GetReceiptsByBusinessYearTask(db, listener, activityContext, businessYear);
        Executors.newSingleThreadExecutor().submit(task);
    }

    /**
     * This runnable task updates the data for a specific receipt.
     * @param receipt
     */
    public void updateReceipt(Receipt receipt){
        if (receipt.getReceiptId()!=0){
            UpdateReceiptTask task = new UpdateReceiptTask(db, receipt);
            Executors.newSingleThreadExecutor().submit(task);
        }
    }
}
