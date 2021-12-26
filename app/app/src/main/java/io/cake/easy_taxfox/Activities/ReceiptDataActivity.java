package io.cake.easy_taxfox.Activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.rendering.PDFRenderer;
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.cake.easy_taxfox.Helpers.CalendarHelper;
import io.cake.easy_taxfox.Config.AppConfig;
import io.cake.easy_taxfox.Config.Log;
import io.cake.easy_taxfox.Database.TaxFoxDatabaseHelper;
import io.cake.easy_taxfox.Entities.Receipt;

import io.cake.easy_taxfox.Helpers.PreferencesHelper;

import io.cake.easy_taxfox.R;
import io.cake.easy_taxfox.Helpers.UIHelper;
import io.cake.easy_taxfox.VisionApi.ReceiptPrediction;

/***
 * This activity gets triggered for creating a new receipt.
 */
public class ReceiptDataActivity extends NavigationActivity implements DatePickerDialog.OnDateSetListener {

    private TextView titleInput;
    private TextView amountInput;
    private TextView dateInput;
    private Spinner spinnerAnnex;
    private Spinner spinnerSection;
    private ImageButton imgSave;
    private ImageButton imgDelete;
    private ImageView imgView;
    private ImageButton imgInfo;
    private TaxFoxDatabaseHelper dbHelper;
    private Receipt receipt;
    private ArrayAdapter<String> sectionsAdapter;
    private List<String> sections;
    private Map<String, String[]> annexSections;
    private int businessYear;
    private int year;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PreferencesHelper.setTheme(ReceiptDataActivity.this);
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_receipt_data, frameLayout);
        setTitle(R.string.receiptTitle);
        initUi();
        int id = initExtras();
        initAdapter();
        PDFBoxResourceLoader.init(getApplicationContext());
        initReceipt(id);

        initSharedPreference();
    }


    /**
     * This method asks the user to confirm exiting the activity when the back button is pressed
     */
    @Override
    public void onBackPressed() {
        if(receipt.getTitle() == null){
            String title = getResources().getString(R.string.receiptExitDialogTitle);
            String message = getResources().getString(R.string.receiptExitDialogMessage);
            String positive = getResources().getString(R.string.exit);
            String negative = getResources().getString(R.string.cancel);
            UIHelper.showAlertDialog(ReceiptDataActivity.this, title, message,
                    positive, negative,
                    (dialog, which) -> dbHelper.deleteReceipt(receipt,
                            () -> {
                                UIHelper.showToast(ReceiptDataActivity.this, getResources().getString(R.string.receiptDeleteSuccess));
                                ReceiptDataActivity.super.onBackPressed();
                            }),
                    (dialog, which) -> UIHelper.showToast(ReceiptDataActivity.this, getResources().getString(R.string.aborted)));
        }else{
            super.onBackPressed();
        }
    }

    /**
     * This method reads the relevant shared preference (business year)
     */
    private void initSharedPreference() {
        SharedPreferences sharedPreferences = getSharedPreferences(AppConfig.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        businessYear = Integer.valueOf(sharedPreferences.getString(AppConfig.PREFERENCES_BUSINESS_YEAR_KEY, AppConfig.DEFAULT_BUSINESS_YEAR));
    }

    /***
     * This method initializes the UI elements
     */
    private void initUi() {
        titleInput = findViewById(R.id.receiptDataDescriptionET);
        amountInput = findViewById(R.id.receiptDataSumET);
        dateInput = findViewById(R.id.receiptDataDateET);
        spinnerAnnex = findViewById(R.id.receiptDataSpinnerCategory);
        spinnerSection = findViewById(R.id.receiptDataSpinnerSubCategory);
        imgSave = findViewById(R.id.receiptDataImgSave);
        imgDelete = findViewById(R.id.receiptDataImgDelete);
        imgInfo = findViewById(R.id.receiptDataImgInfo);
        imgInfo.setOnClickListener(v -> showAnnexInformation());
        imgSave.setOnClickListener(v -> receiptValidation());
        imgView = findViewById(R.id.receiptDataImgPreview);
        imgDelete.setOnClickListener(v -> initDeleteAlert());
        dateInput.setOnClickListener(v -> UIHelper.createDatePickerDialog(ReceiptDataActivity.this, ReceiptDataActivity.this).show());
        spinnerAnnex.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                changeSections();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                return;
            }
        });
    }

    /**
     * This method shows the information of the annexes in a alert dialog
     */
    private void showAnnexInformation() {
        StringBuilder builder = new StringBuilder();
        String[] annexesLong = getResources().getStringArray(R.array.annexes_long);
        for (int i = 0; i < annexesLong.length; i++) {
            builder.append(annexesLong[i]);
            builder.append("\n");
            builder.append("\n");
        }
        String title = getResources().getString(R.string.receiptInfoDialogTitle);
        String positive = getResources().getString(R.string.ok);
        UIHelper.showAlertDialog(ReceiptDataActivity.this, title, builder.toString(), positive, (dialog, which) -> {
        });
    }

    /***
     * This method initializes the adapter for the section spinner
     */
    private void initAdapter() {
        Map<String, String[]> annexSections = AppConfig.getAnnexSections(ReceiptDataActivity.this);
        sections = new ArrayList<>();
        sections.addAll(Arrays.asList(annexSections.get(spinnerAnnex.getSelectedItem().toString())));
        sectionsAdapter = new ArrayAdapter<>(ReceiptDataActivity.this, android.R.layout.simple_spinner_item, sections);
        spinnerSection.setAdapter(sectionsAdapter);
    }

    /***
     * This method changes the section options according to the current annex
     */
    private void changeSections() {
        annexSections = AppConfig.getAnnexSections(ReceiptDataActivity.this);
        sections.clear();
        sections.addAll(Arrays.asList(annexSections.get(spinnerAnnex.getSelectedItem().toString())));
        sectionsAdapter.notifyDataSetChanged();
    }

    /***
     * This method extracts the intent extras and returns the id, provided in the extras
     * @return the receipt id
     */
    private int initExtras() {
        Intent intent = getIntent();
        if (intent.hasExtra(AppConfig.EXTRA_RECEIPT_PREDICTION)) {
            ReceiptPrediction receiptPrediction = intent.getParcelableExtra(AppConfig.EXTRA_RECEIPT_PREDICTION);
            if(Math.abs(receiptPrediction.getSum() - AppConfig.DEFAULT_RECEIPT_PREDICTION_AMOUNT) < AppConfig.EPSILON_THRESHOLD){
                amountInput.setText("");
            }else{
                amountInput.setText(Double.toString(receiptPrediction.getSum()));
            }

            titleInput.setText(receiptPrediction.getTitle());
        } else {
            amountInput.setText("");
            titleInput.setText(AppConfig.DEFAULT_RECEIPT_PREDICTION_TITLE);
        }
        return (int) intent.getLongExtra(AppConfig.EXTRA_RECEIPT_ID, AppConfig.DEFAULT_RECEIPT_ID);
    }

    /***
     * This method gets the Receipt for a given id and updates the UI accordingly
     * @param id the given receipt id
     */
    private void initReceipt(int id) {
        dbHelper = TaxFoxDatabaseHelper.getInstance(ReceiptDataActivity.this);
        dbHelper.getReceiptById(id, receipt -> {
            this.receipt = receipt;
            if (receipt.getTitle() != null) {
                titleInput.setText(receipt.getTitle());
            }
            if (receipt.getAmount() != null) {
                amountInput.setText(receipt.getAmount().toString());
            }
            if (receipt.getReceiptDate() != null) {
                year = CalendarHelper.getYear(receipt.getReceiptDate());
                dateInput.setText(AppConfig.DATE_FORMAT.format(receipt.getReceiptDate()));
            }
            if (receipt.getAnnex() != null) {
                int index = getSelectionIndex(receipt.getAnnex(), getResources().getStringArray(R.array.annexes_short));
                spinnerAnnex.setSelection(index);
                changeSections();
            }
            if (receipt.getSection() != null) {
                String[] items = annexSections.get(spinnerAnnex.getSelectedItem().toString());
                int index = getSelectionIndex(receipt.getSection(), items);
                spinnerSection.setSelection(index);
            }
            if (receipt != null && receipt.getFilePath() != null) {
                try {
                    createBitmapFromPDF(receipt.getFilePath() + "/" + receipt.getFileName());
                } catch (IOException e) {
                    e.printStackTrace();
                    UIHelper.showToast(ReceiptDataActivity.this, getResources().getString(R.string.receiptPreviewError));
                }
            }
        });
    }

    /**
     * This method loads the first page of the receipts pdf file
     * @param pdfPath
     * @throws IOException
     */
    public void createBitmapFromPDF(String pdfPath) throws IOException {
        File file = new File(pdfPath);
        PDDocument pd = PDDocument.load(file);
        PDFRenderer pr = new PDFRenderer (pd);
        Bitmap bitmap = pr.renderImageWithDPI(0, AppConfig.SCALE_FACTOR);
        imgView.setImageBitmap(bitmap);
    }

    /***
     * This method gets the index of a text in an array of strings
     * @param text
     * @param items
     * @return
     */
    private int getSelectionIndex(String text, String[] items) {
        for (int i = 0; i < items.length; i++) {
            if (items[i].equals(text)) {
                return i;
            }
        }
        UIHelper.showToast(ReceiptDataActivity.this, "Anlage nicht in Dropdown gefunden");
        return 0;
    }

    /***
     * This method validates the whether the user filled all fields
     * @return
     */
    private boolean isValid() {
        if (spinnerAnnex.getSelectedItem() == null) {
            Log.d(AppConfig.RECEIPT_DATA_TAG, "Anlage leer");
        } else if (spinnerSection.getSelectedItem() == null) {
            Log.d(AppConfig.RECEIPT_DATA_TAG, "Abschnitt leer");
        } else if (titleInput.getText().toString().isEmpty()) {
            Log.d(AppConfig.RECEIPT_DATA_TAG, "Titel leer");
        } else if (amountInput.getText().toString().isEmpty()) {
            Log.d(AppConfig.RECEIPT_DATA_TAG, "Betrag leer");
        } else if (dateInput.getText().toString().isEmpty()) {
            Log.d(AppConfig.RECEIPT_DATA_TAG, "Datum leer");
        }
        return spinnerAnnex.getSelectedItem() != null && spinnerSection.getSelectedItem() != null &&
                !titleInput.getText().toString().isEmpty() && !amountInput.getText().toString().isEmpty() &&
                !dateInput.getText().toString().isEmpty();
    }

    /***
     * This method gets and validates the receipt date, given by the user
     * @return the receipt date
     */
    private Date getDate() {
        String dateString = dateInput.getText().toString();
        Date receiptDate;
        try {
            receiptDate = new SimpleDateFormat(AppConfig.DATE_INPUT_FORMAT).parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            UIHelper.showToast(ReceiptDataActivity.this, getResources().getString(R.string.receiptWrongDateFormat));
            return null;
        }
        if (year != businessYear) {
            String title = getResources().getString(R.string.receiptYearDialogTitle);
            String message = getResources().getString(R.string.receiptYearDialogMessage);
            String positive = getResources().getString(R.string.yes);
            String negative = getResources().getString(R.string.cancel);
            UIHelper.showAlertDialog(ReceiptDataActivity.this, title, message, positive, negative, (dialog, which) -> saveReceipt(receiptDate),
                    (dialog, which) -> UIHelper.showToast(ReceiptDataActivity.this, getResources().getString(R.string.aborted)));
            return null;
        }
        return receiptDate;
    }

    /***
     * This method shows the alert dialog for deletion
     */
    private void initDeleteAlert() {
        String title = getResources().getString(R.string.receiptDeleteDialogTitle);
        String message = getResources().getString(R.string.receiptDeleteDialogMessage);
        String positive = getResources().getString(R.string.delete);
        String negative = getResources().getString(R.string.cancel);
        UIHelper.showAlertDialog(ReceiptDataActivity.this, title, message, positive, negative,
                (dialog, which) -> deleteReceipt(), (dialog, which) -> UIHelper.showToast(ReceiptDataActivity.this, getResources().getString(R.string.aborted))
        );
    }

    /***
     * This method deletes the given receipt
     */
    private void deleteReceipt() {
        dbHelper.deleteReceipt(receipt,
                () -> {
                    UIHelper.showToast(ReceiptDataActivity.this, getResources().getString(R.string.receiptDeleteSuccess));
                    Intent intent = new Intent(ReceiptDataActivity.this, OverviewActivity.class);
                    startActivity(intent);
                });
    }

    /***
     * This method calls validation methods before saving the receipt
     */
    private void receiptValidation() {
        //validation
        if (!isValid()) {
            UIHelper.showToast(this, "Bitte fülle alle Felder aus");
            return;
        }
        Date receiptDate = getDate();
        if (receiptDate != null) {
            saveReceipt(receiptDate);
        }
    }

    /***
     * This method saves/updates the current receipt to the database
     * @param receiptDate the date of the receipt
     */
    private void saveReceipt(Date receiptDate) {
        String annex = spinnerAnnex.getSelectedItem().toString();
        String section = spinnerSection.getSelectedItem().toString();
        String title = titleInput.getText().toString();
        Double amount = Double.valueOf(amountInput.getText().toString());
        receipt.setAnnex(annex);
        receipt.setBusinessYear(businessYear);
        receipt.setSection(section);
        receipt.setTitle(title);
        receipt.setAmount(amount);
        receipt.setReceiptDate(receiptDate);
        dbHelper.updateReceipt(receipt);
        Intent intent = new Intent(ReceiptDataActivity.this, OverviewActivity.class);
        startActivity(intent);
    }

    /**
     * This method sets the date of the edittext
     * @param view
     * @param year
     * @param month
     * @param dayOfMonth
     */
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        this.year = year;
        DateFormat df = DateFormat.getDateInstance(DateFormat.DEFAULT,
                Locale.GERMANY);
        GregorianCalendar date = new GregorianCalendar(year, month, dayOfMonth);
        String dateString = df.format(date.getTime());
        month++;
        dateInput.setText(dateString);
    }
}