package io.cake.easy_taxfox.Activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import android.net.Uri;
import android.os.Bundle;

import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;


import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import io.cake.easy_taxfox.Config.Log;
import io.cake.easy_taxfox.Helpers.CalendarHelper;
import io.cake.easy_taxfox.Config.AppConfig;
import io.cake.easy_taxfox.Entities.Export;
import io.cake.easy_taxfox.Entities.Receipt;
import io.cake.easy_taxfox.Database.TaxFoxDatabaseHelper;
import io.cake.easy_taxfox.Helpers.PdfHelper;
import io.cake.easy_taxfox.Helpers.PreferencesHelper;
import io.cake.easy_taxfox.R;
import io.cake.easy_taxfox.Helpers.UIHelper;

/**
 * This Activity gives the user the opportunity generate Exports. It also provides the option to send an
 * implicit intent for sharing the Exports as PDF-Files.
 */

public class ExportActivity extends NavigationActivity implements DatePickerDialog.OnDateSetListener, PdfHelper.PdfExportResultListener {

    private RadioGroup group;
    private ImageButton btn_export;
    private TextView yearHeader;
    private TextView textDateFrom;
    private TextView textDateUntil;
    private RadioButton btn_all;
    private RadioButton btn_single;
    private CheckBox checkBoxWholeBusinessYear;
    private ProgressBar progressBar;
    private int idAll;
    private int idSingle;
    private int helperId;
    private int businessYear;
    private TaxFoxDatabaseHelper dbHelper;
    private List<Receipt> receiptsList;
    private List<Receipt> filteredList;
    private PdfHelper pdfHelper;
    private TextView receiptCountView;
    private boolean checkedAllPdfs = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PreferencesHelper.setTheme(ExportActivity.this);
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_export, frameLayout);
        setTitle(R.string.exportTitle);
        initUI();
        initDateView();
        initExportBtn();
        initHelpers();
    }

    @Override
    protected void onStart(){
        super.onStart();
        initPreferences();
    }

    @Override
    protected void onResume() {
        super.onResume();
        navigationView.getMenu().getItem(getResources().getInteger(R.integer.exportNavigationIndex)).setChecked(true);
        initSharedPreferences();
        initReceiptData();
        initClickCheckBox();
    }

    /**
     * This method initializes all helpers for the activity.
     */
    private void initHelpers(){
        dbHelper = TaxFoxDatabaseHelper.getInstance(ExportActivity.this);
        pdfHelper = new PdfHelper(ExportActivity.this, ExportActivity.this);
    }

    /**
     * This method gets all the receiptdata for the setted businessyear from the shared preferences.
     */
    private void initReceiptData() {
        dbHelper.getReceiptsByBusinessYear(
                businessYear, receipts -> { receiptsList = receipts;
        });
    }

    /**
     * This method initializes all the sharedPreferences for the ExportActivity.
     */
    private void initPreferences(){
        SharedPreferences sharedPreferences = getSharedPreferences(AppConfig.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        businessYear = Integer.valueOf(sharedPreferences.getString(AppConfig.PREFERENCES_BUSINESS_YEAR_KEY, AppConfig.DEFAULT_BUSINESS_YEAR));
        yearHeader.setText(Integer.toString(businessYear));
        Log.d(AppConfig.EXPORT_TAG, "Geschäftsjahr " + businessYear);
    }

    /***
     * This method initializes a SharedPreference-Objekt to connect the TextView displaying the year to the shared preferences
     */
    private void initSharedPreferences(){
        SharedPreferences sharedPreferences = getSharedPreferences(AppConfig.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        businessYear = Integer.valueOf(sharedPreferences.getString(AppConfig.PREFERENCES_BUSINESS_YEAR_KEY, AppConfig.DEFAULT_BUSINESS_YEAR));
    }

    /**
     * This method initializes the ui elements for this activity.
     */
    private void initUI() {
        group = findViewById(R.id.exportSettingsRadioGroup);
        btn_export = findViewById(R.id.exportButtonExport);
        yearHeader = findViewById(R.id.exportSettingsYear);
        textDateFrom = findViewById(R.id.exportSettingsFromET);
        textDateUntil = findViewById(R.id.exportSettingsUntilET);
        btn_all = findViewById(R.id.exportBtnAllPDF);
        btn_single = findViewById(R.id.exportBtnSinglePDF);
        checkBoxWholeBusinessYear = findViewById(R.id.exportCheckBoxWholeBusinessYear);
        idAll = btn_all.getId();
        idSingle = btn_single.getId();
        receiptCountView = findViewById(R.id.exportReceiptCountView);
        progressBar = findViewById(R.id.exportProgressBar);
        group.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.exportBtnAllPDF){
                checkedAllPdfs = true;
            }else if (checkedId == R.id.exportBtnSinglePDF){
                checkedAllPdfs = false;
            }
        });
    }

    /**
     * This method sets the clicklistener for the export-button depending on the radiobuttons.
     */
    private void initExportBtn() {
        btn_export.setOnClickListener(v -> startExport());
    }

    /**
     * This method sets the radiobutton and helps to indicate which type of export the user wants to have.
     */
    private void startExport() {
        if(filteredList==null || !validateDate() || filteredList.isEmpty()){
            UIHelper.showToast(ExportActivity.this, getResources().getString(R.string.exportInvalidInput));
            return;
        }
        ActivityCompat.requestPermissions(ExportActivity.this, new String[] {
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, AppConfig.REQUEST_STORAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if((requestCode == AppConfig.REQUEST_STORAGE) && hasAllPermissionsGranted(grantResults)){
            try {
                progressBar.setVisibility(View.VISIBLE);
                if(!checkedAllPdfs){
                    pdfHelper.createExportSummaryTask(filteredList);
                }else{
                    pdfHelper.createExportSingleTask(filteredList);
                }
            }catch (Exception e){
                e.printStackTrace();
                UIHelper.showToast(ExportActivity.this, getResources().getString(R.string.exportFailed));
                progressBar.setVisibility(View.GONE);
            }
        }else{
            showAlertDialog();
        }
    }

    /***
     * This method shows an alert dialog if permissions are not granted initially
     */
    private void showAlertDialog(){
        if(shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(ExportActivity.this)
                    .setTitle(getResources().getString(R.string.permissionNeeded))
                    .setMessage(getResources().getString(R.string.permissionExportReason))
                    .setPositiveButton(getResources().getString(R.string.permissionOk), (dialog, which) -> ActivityCompat.requestPermissions(ExportActivity.this, new String[] {
                            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, AppConfig.REQUEST_STORAGE))
                    .setNegativeButton(getResources().getString(R.string.permissionCancel), (dialog, which) -> dialog.dismiss())
                    .create().show();
        }
    }

    /***
     * This method checks if every existing permission was granted
     * @param grantResults
     * @return
     */
    private boolean hasAllPermissionsGranted(@NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    /**
     * This method initializes the datepicker-view and dialog for both date input fields.
     */
    private void initDateView() {
        textDateFrom.setFocusable(false);
        textDateUntil.setFocusable(false);
        textDateFrom.setOnClickListener(v -> initDateDialog(v));
        textDateUntil.setOnClickListener(v -> initDateDialog(v));
    }

    /**
     * This method creates the dialog and shows it.
     * @param v
     */
    private void initDateDialog(View v){
        UIHelper.createDatePickerDialog(this, this).show();
        helperId = v.getId();
    }

    /**
     * This method filters the receiptslist by their date and returns the machting receipts.
     * @return List<Receipts>
     * @throws ParseException
     */
    private ArrayList<Receipt> filterReceipts(String startDate, String endDate) throws ParseException {
        Date start = AppConfig.DATE_FORMAT.parse(startDate);
        start = CalendarHelper.addDays(start, -1);
        Date end = AppConfig.DATE_FORMAT.parse(endDate);
        end = CalendarHelper.addDays(end, 1);
        ArrayList<Receipt> result = new ArrayList<Receipt>();
        for (Receipt receipt: receiptsList) {

            if(receipt.getReceiptDate()!= null && receipt.getReceiptDate().after(start) && receipt.getReceiptDate().before(end)) {
                result.add(receipt);
            }
        }
        return result;
    }

    /**
     * This method updates the filtered List of receipts, depending on the dates provided by the user.
     */
    private void updateReceipts() {
        String startDate = textDateFrom.getText().toString();
        String endDate = textDateUntil.getText().toString();
        if(validateDate()){
            try {
                filteredList = filterReceipts(startDate, endDate);
                receiptCountView.setText(filteredList.size() + " " + getResources().getString(R.string.exportReceiptsChosen));
            } catch (ParseException e) {
                e.printStackTrace();
                UIHelper.showToast(ExportActivity.this, getResources().getString(R.string.exportParsingError));
            }
        }else{
            UIHelper.showToast(ExportActivity.this, getResources().getString(R.string.exportProvideDate));
        }
    }

    /**
     * This method validates the userInput for the datepicker-fields.
     * @return
     */
    private boolean validateDate(){
        return !textDateFrom.getText().toString().isEmpty() && !textDateUntil.getText().toString().isEmpty();
    }

    /**
     * This method sets the text from the datepickerdialogs.
     * @param view
     * @param year
     * @param month
     * @param dayOfMonth
     */
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        checkBoxWholeBusinessYear.setChecked(false);
        DateFormat df = DateFormat.getDateInstance(DateFormat.DEFAULT,
                Locale.GERMANY);
        if(year < businessYear || year > businessYear){
            year = businessYear;
        }
        GregorianCalendar date = new GregorianCalendar(year, month, dayOfMonth);
        String dateString = df.format(date.getTime());
        if(helperId == textDateFrom.getId()) {
            textDateFrom.setText(dateString);
        } else {
            textDateUntil.setText(dateString);
        }
        updateReceipts();
    }

    /**
     * This method initialzies the checkbox an it listener for click-events.
     */
    private void initClickCheckBox(){
        checkBoxWholeBusinessYear.setOnClickListener(v -> {
            if(checkBoxWholeBusinessYear.isChecked()){
                setDefaultDate();
            }
        });
    }

    /**
     * This method sets the default date for checking the checkbox.
     */
    private void setDefaultDate() {
        DateFormat df = DateFormat.getDateInstance(DateFormat.DEFAULT,
                Locale.GERMANY);
        GregorianCalendar dateStart = new GregorianCalendar(businessYear, AppConfig.EXPORT_MIN_MONTH, AppConfig.EXPORT_MIN_DAY);
        String dateStringStart = df.format(dateStart.getTime());
        textDateFrom.setText(dateStringStart);

        GregorianCalendar dateEnd = new GregorianCalendar(businessYear, AppConfig.EXPORT_MAX_MONTH, AppConfig.EXPORT_MAX_DAY);
        String dateStringEnd = df.format(dateEnd.getTime());
        textDateUntil.setText(dateStringEnd);
        updateReceipts();
    }

    /**
     * This method is responsible for the sharing of PDF-File exports. 
     * @param exportList
     */
    private void shareExport (ArrayList<Export> exportList){
        Intent intentShare = new Intent(Intent.ACTION_SEND_MULTIPLE);
        ArrayList<Uri> uriList = new ArrayList<>();
        Uri shareFileUri;
        if(exportList == null){
            UIHelper.showToast(ExportActivity.this, getResources().getString(R.string.exportNoFiles));
        } else {
            for (Export element: exportList) {
                File exportFile = new File(element.getFilePath(), element.getFileName());
                if (!exportFile.exists()){
                    UIHelper.showToast(ExportActivity.this, getResources().getString(R.string.exportNoFiles));
                    return;
                }
                shareFileUri = FileProvider.getUriForFile(getApplication(), getApplication().getPackageName() + ".fileprovider", exportFile);
                uriList.add(shareFileUri);
            }
            intentShare.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intentShare.setType(AppConfig.EXPORT_INTENT_TYPE);
            intentShare.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList);
            pdfHelper.removeCachedExports();
            startActivity(Intent.createChooser(intentShare, getResources().getString(R.string.exportIntentTitle)));
        }
    }

    @Override
    public void onExportResult(ArrayList<Export> exports) {
        progressBar.setVisibility(View.GONE);
        shareExport(exports);
    }
}


