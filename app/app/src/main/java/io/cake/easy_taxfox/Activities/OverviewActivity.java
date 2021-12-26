package io.cake.easy_taxfox.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;

import io.cake.easy_taxfox.Config.AppConfig;
import io.cake.easy_taxfox.Config.Log;
import io.cake.easy_taxfox.Database.TaxFoxDatabaseHelper;
import io.cake.easy_taxfox.Entities.Receipt;
import io.cake.easy_taxfox.Helpers.CalendarHelper;
import io.cake.easy_taxfox.Helpers.PreferencesHelper;
import io.cake.easy_taxfox.Others.ReceiptListAdapter;
import io.cake.easy_taxfox.R;

/**
 * This activity shows the customer an overview about his/hers receipts.
 */
public class OverviewActivity extends ScannerVisionActivity {

    private ImageButton btnTakePicture;
    private ImageButton btnShowGallery;
    private TaxFoxDatabaseHelper dbHelper;
    private ArrayList<Receipt> receiptArrayList;
    private ArrayList<Receipt> receiptsFiltered;
    private ReceiptListAdapter receiptListAdapter;
    private ListView listView;
    private Spinner monthSpinner;
    private TextView businessYearView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PreferencesHelper.setTheme(OverviewActivity.this);
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_overview, frameLayout);
        setTitle(R.string.overviewTitle);
        initOverviewActivityUI();
        initHelper();
        setListener();
        initAdapter();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        navigationView.getMenu().getItem(getResources().getInteger(R.integer.overviewNavigationIndex)).setChecked(true);
        getReceiptsByBusinessYear();
        updateYearUI();
    }

    /***
     * This method initialized the main UI components.
     */
    private void initOverviewActivityUI() {
        btnTakePicture = findViewById(R.id.overviewBtnTakePicture);
        btnShowGallery = findViewById(R.id.overviewBtnShowGallery);
        listView = findViewById(R.id.overviewListViewReceipts);
        monthSpinner = findViewById(R.id.overviewSpinnerMonth);
        businessYearView = findViewById(R.id.overviewTextYear);
        progressBar = findViewById(R.id.overviewProgressBar);
    }

    /***
     * This method initializes the necessary abstraction classes (helpers).
     */
    private void initHelper(){
        dbHelper = TaxFoxDatabaseHelper.getInstance(OverviewActivity.this);
    }

    /***
     * This method sets the listeners for the UI elements.
     */
    public void setListener(){
        btnTakePicture.setOnClickListener(v -> {
            scanMethod = AppConfig.OPEN_CAMERA;
            scanVisionButton = btnTakePicture;
            requestRequiredPermissions();
        });
        btnShowGallery.setOnClickListener(v -> {
            scanMethod = AppConfig.OPEN_GALLERY;
            scanVisionButton = btnShowGallery;
            requestRequiredPermissions();
        });
        monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterReceipts();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    /***
     * This method initializes the relevant perferences.
     */
    private void updateYearUI(){
        businessYearView.setText(Integer.toString(businessYear));
    }

    /***
     * This method calls the necessary helper methods dependent on the request code.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    /***
     * This method calls the according helper method or shows a toast if the permissions were not granted.
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /***
     * This method initializes the custom adapter for the list of receipts.
     */
    private void initAdapter(){
        receiptsFiltered = new ArrayList<>();
        receiptListAdapter = new ReceiptListAdapter(OverviewActivity.this, receiptsFiltered);
        listView.setAdapter(receiptListAdapter);
        listView.setOnItemClickListener((parent, view, position, id) -> startReceiptDataActivity(receiptsFiltered.get(position).getReceiptId()));
    }

    /***
     * This method retrieves all receipts of the current business year from the database.
     */
    private void getReceiptsByBusinessYear(){
        Log.d("Resume", "Fetching receipts");
        receiptArrayList = new ArrayList<>();
        dbHelper.getReceiptsByBusinessYear(businessYear, receipts -> {
            // Gegeben: receipts []
            ArrayList <Receipt> list = new ArrayList<>(receipts);
            this.receiptArrayList.addAll(list);
            filterReceipts();

        });
    }

    /***
     * This method filters the receipts dependent on the selected month.
     */
    private void filterReceipts(){
        int index = monthSpinner.getSelectedItemPosition();
        receiptsFiltered.clear();
        if (index == 0){
            receiptsFiltered.addAll(receiptArrayList);
        }
        for (Receipt receipt: receiptArrayList){
            Log.d("Receipt Jahre", Integer.toString(receipt.getBusinessYear()));
            if (receipt.getReceiptDate() != null){
                int month = CalendarHelper.getMonth(receipt.getReceiptDate());
                if(month == index){
                    receiptsFiltered.add(receipt);
                }
            }
        }
        receiptsFiltered = sortReceipts(receiptsFiltered);
        receiptListAdapter.notifyDataSetChanged();
    }

    /**
     * This method sorts a given arraylist of receipts by receiptdate.
     * @param receipts
     * @return
     */
    private ArrayList<Receipt> sortReceipts(ArrayList<Receipt> receipts){
        Collections.sort(receipts, (o1, o2) -> {
            if(o1.getReceiptDate() == null){
                return (o2.getReceiptDate() == null) ? 0 : -1;
            }
            if(o2.getReceiptDate() == null){
                return 1;
            }
            return o1.getReceiptDate().compareTo(o2.getReceiptDate());
        });
        return receipts;
    }

}