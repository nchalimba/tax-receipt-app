package io.cake.easy_taxfox.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


import io.cake.easy_taxfox.Config.AppConfig;
import io.cake.easy_taxfox.Config.Log;
import io.cake.easy_taxfox.Database.TaxFoxDatabaseHelper;
import io.cake.easy_taxfox.Entities.Receipt;
import io.cake.easy_taxfox.Helpers.CalendarHelper;
import io.cake.easy_taxfox.Helpers.PreferencesHelper;

import io.cake.easy_taxfox.R;

/***
 * This activity is the starting point of the app. Here, the user can see some data of the receipts
 */
public class DashboardActivity extends ScannerVisionActivity implements AdapterView.OnItemSelectedListener{

    private ImageButton btnTakePicture;
    private ImageButton btnShowGallery;
    private Spinner spinner;
    private PieChart pieChart;
    private TextView tvScanAmounts;
    private TextView tvBusinessYear;
    private ArrayList <String> annexList;
    private ArrayList<Receipt> receiptArrayList;
    private ArrayList<Receipt> receiptsFiltered = new ArrayList<>();
    private TaxFoxDatabaseHelper dbHelper;

    @Override
    protected void onDestroy() {
        TaxFoxDatabaseHelper.destroyInstance();
        Log.d(AppConfig.DASHBOARD_TAG, "Dashboard Activity destroyed");
        super.onDestroy();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        PreferencesHelper.setTheme(DashboardActivity.this);
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_dashboard, frameLayout);
        setTitle(R.string.dashboardTitle);
        initDashboardUI();
        initHelper();
        checkForExistingProfile();
        initPieChart();
    }

    @Override
    protected void onResume() {
        PreferencesHelper.setTheme(DashboardActivity.this);
        super.onResume();
        navigationView.getMenu().getItem(getResources().getInteger(R.integer.dashboardNavigationIndex)).setChecked(true);
        updateUI();
        getAllReceipts();
    }

    /**
     * This method initializes the UI components of the dashboard
     */
    private void initDashboardUI(){
        progressBar = findViewById(R.id.dashboardProgressBar);
        tvScanAmounts = findViewById(R.id.dashboardAmountScans);
        spinner = findViewById(R.id.dashboardMonthSpinner);
        btnShowGallery = findViewById(R.id.dashboardBtnShowGallery);
        btnTakePicture = findViewById(R.id.dashboardBtnTakePicture);
        spinner.setOnItemSelectedListener(DashboardActivity.this);
        tvBusinessYear = findViewById(R.id.dashboardYear);
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
    }

    /***
     * This method initializes the necessary abstraction classes (helpers)
     */
    private void initHelper(){
        dbHelper = TaxFoxDatabaseHelper.getInstance(DashboardActivity.this);
    }

    /***
     * This method calls the necessary helper methods dependent on the request code
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    /***
     * This method calls the according helper method or shows a toast if the permissions were not granted
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    /***
     * This method queries whether a profile exists. If not, the user is redirected directly to the ProfileActivity
     */
    private void checkForExistingProfile(){
        dbHelper.getCustomerById(AppConfig.DEFAULT_CUSTOMER_ID, customer -> {
            if(customer == null){
                Intent intent = new Intent(DashboardActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });
    }


    /***
     * This method processes the results when selecting a certain time period
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        updateDashboard();
    }

    /***
     * This method processes the results when no time period is selected
     * @param parent
     */
    @Override
    public void onNothingSelected(AdapterView<?> parent) {}

    /***
     * This method updates the business year textview
     */
    private void updateUI(){
        tvBusinessYear.setText(Integer.toString(businessYear));
    }


    /***
     * This method processes a database query of all receipts in a selected business year
     */
    public void getAllReceipts(){
        receiptArrayList = new ArrayList<>();
        dbHelper.getReceiptsByBusinessYear(businessYear, receipts -> {
            receiptArrayList = new ArrayList<>(receipts);
            updateDashboard();
        });
    }


    /***
     * This method initializes a pie chart displaying and categorizing all receipts of a certain time period by annexes including the amount
     */
    private void initPieChart(){
        pieChart = findViewById(R.id.activity_main_piechart);
        setupPieChart();
    }

    /***
     * This method initializes the piechart
     */
    private void setupPieChart() {
        pieChart.setDrawHoleEnabled(true);
        pieChart.setUsePercentValues(true);
        pieChart.setEntryLabelTextSize(AppConfig.DASHBOARD_PIECHART_LABEL_TEXTSIZE);
        pieChart.setEntryLabelColor(AppConfig.DASHBOARD_PIECHART_LABEL_COLOR);
        pieChart.setCenterText(getResources().getString(R.string.dashboardPieChartHeading));
        pieChart.setCenterTextSize(AppConfig.DASHBOARD_PIECHART_CENTER_TEXTSIZE);
        pieChart.getDescription().setEnabled(false);
        Legend l = pieChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setEnabled(false);
    }

    /***
     * This method updates the piechart data
     * @param annexes
     * @param amounts
     */
    private void loadPieChartData(String[] annexes, int[] amounts) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        for (int i = 0; i < annexes.length; i++){
            entries.add(new PieEntry(amounts[i], annexes[i]));
        }
        ArrayList<Integer> colors = new ArrayList<>();
        for (int color: ColorTemplate.MATERIAL_COLORS) {
            colors.add(color);
        }
        for (int color: ColorTemplate.VORDIPLOM_COLORS) {
            colors.add(color);
        }
        PieDataSet dataSet = new PieDataSet(entries, getResources().getString(R.string.dashboardPieChartDataLabel));
        dataSet.setColors(colors);
        PieData data = new PieData(dataSet);
        data.setDrawValues(true);
        data.setValueFormatter(new PercentFormatter(pieChart));
        data.setValueTextSize(AppConfig.DASHBOARD_PIECHART_TEXTSIZE);
        data.setValueTextColor(Color.BLACK);
        pieChart.setData(data);
        pieChart.invalidate();
        pieChart.animateY(AppConfig.DASHBOARD_PIECHART_ANIMATION_DURATION, Easing.EaseInOutQuad);
    }


    /***
     * This method filters all receipts of a certain business year by month
     * @return
     */
    private void filterReceipts(){
        int index = spinner.getSelectedItemPosition();
        receiptsFiltered.clear();
        if (index == 0){
            receiptsFiltered.addAll(receiptArrayList);
        }
        for (Receipt receipt: receiptArrayList){
            if (receipt.getReceiptDate() != null){
                int month = CalendarHelper.getMonth(receipt.getReceiptDate());
                if(month == index){
                    receiptsFiltered.add(receipt);
                }
            }
        }
        for(Receipt receipt: receiptsFiltered){
            Log.d(AppConfig.DASHBOARD_TAG, Integer.toString(receipt.getBusinessYear())) ;
        }
    }

    /***
     * This method extracts the annexes of the filtered receipts list and serves as a data source for the pie chart
     * @return
     */
    private String[] getAnnexes(){
        annexList = new ArrayList<>();
        for(Receipt receipt: receiptsFiltered){
            if(receipt.getAnnex() != null){
                String annex = receipt.getAnnex();
                annexList.add(annex);
            }
        }
        Set<String> set = new HashSet<>(annexList);
        annexList.clear();
        annexList.addAll(set);
        String[] annexes = new String[annexList.size()];
        annexes = annexList.toArray(annexes);
        return annexes;
    }

    /***
     * This method extracts the amounts of the filtered receipts list and serves as a data source for the pie chart
     */
    private int[] getAmounts(String[] annexArray){
        int[] amountArray = new int[annexArray.length];
        for(Receipt receipt: receiptsFiltered){
            for(int i = 0; i < annexArray.length; i++){
                if(receipt.getAnnex() != null && receipt.getAnnex().equals(annexArray[i])){
                    amountArray[i] = (int) (amountArray[i] + receipt.getAmount());
                }
            }
        }
        return amountArray;
    }

    /***
     * This method updates the dashboard components as soon as the spinner value changes
     */
    private void updateDashboard(){
        filterReceipts();
        String[] annexes = getAnnexes();
        int[] amounts = getAmounts(annexes);
        loadPieChartData(annexes, amounts);
        updateScanAmount();
    }

    /***
     * This method sets the TextView of all receipts ina selected business year based
     */
    private void updateScanAmount(){
        int countScans = receiptsFiltered.size();
        tvScanAmounts.setText(String.valueOf(countScans));
    }

}