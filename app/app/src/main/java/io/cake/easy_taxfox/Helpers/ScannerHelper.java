package io.cake.easy_taxfox.Helpers;

import androidx.annotation.Nullable;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.scanlibrary.ScanActivity;
import com.scanlibrary.ScanConstants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.cake.easy_taxfox.Config.AppConfig;
import io.cake.easy_taxfox.Entities.Receipt;
import io.cake.easy_taxfox.Database.TaxFoxDatabaseHelper;
import io.cake.easy_taxfox.R;

/**
 * This class provides all methods for the ScannerActivity interactions.
 */
public class ScannerHelper {

    private Activity activityContext;
    private TaxFoxDatabaseHelper dbHelper;
    private ScannerResultListener listener;
    private Bitmap bitmap;
    private int businessYear;

    public ScannerHelper(Activity activityContext, ScannerResultListener listener) {
        this.activityContext = activityContext;
        this.listener = listener;
        initDbHelper();
        initSharedPreference();
    }

    /**
     * This method initializes the databasehelper.
     */
    private void initDbHelper() {
        dbHelper = TaxFoxDatabaseHelper.getInstance(activityContext);
    }


    /**
     * This method creates an intent for using the camera.
     */
    public void openCamera() {
        int preference = ScanConstants.OPEN_CAMERA;
        Intent intent = new Intent(activityContext, ScanActivity.class);
        intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, preference);
        activityContext.startActivityForResult(intent, AppConfig.REQUEST_SCANNER_ACTIVITY);
    }

    /**
     * This method creates an intent for using the camera.
     */
    public void openGallery() {
        int preference = ScanConstants.OPEN_MEDIA;
        Intent intent = new Intent(activityContext, ScanActivity.class);
        intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, preference);
        activityContext.startActivityForResult(intent, AppConfig.REQUEST_SCANNER_ACTIVITY);
    }

    /**
     * This method intercepts the response from the ScannerActiviy.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void scannerActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == AppConfig.REQUEST_SCANNER_ACTIVITY && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getExtras().getParcelable(ScanConstants.SCANNED_RESULT);
            try {
                bitmap = MediaStore.Images.Media.getBitmap(activityContext.getContentResolver(), uri);
                createPdf(bitmap);
                activityContext.getContentResolver().delete(uri, null, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method reads the relevant shared preference (business year)
     */
    private void initSharedPreference() {
        SharedPreferences sharedPreferences = activityContext.getSharedPreferences(AppConfig.SHARED_PREFERENCES_NAME, activityContext.MODE_PRIVATE);
        businessYear = Integer.valueOf(sharedPreferences.getString(AppConfig.PREFERENCES_BUSINESS_YEAR_KEY, AppConfig.DEFAULT_BUSINESS_YEAR));
    }

    /***
     * This method initializes the directory for saving files
     * @return
     */
    public static boolean initDirectory() {
        File folder = new File(AppConfig.FOLDER_PATH);
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdirs();
        }
        return success;
    }

    /**
     * This method creates a PDF File from a bitmap.
     *
     * @param bitmap
     * @throws FileNotFoundException
     */
    private void createPdf(Bitmap bitmap) throws FileNotFoundException {
        if (!initDirectory()) {
            UIHelper.showToast(activityContext, activityContext.getResources().getString(R.string.scanNoDirectoryCreated));
        }
        String pdfPath = AppConfig.FOLDER_PATH;
        Date timeStamp = new Date();
        String fileName = createFileName(timeStamp);
        File file = new File(pdfPath, fileName);
        PdfWriter writer = new PdfWriter(file);
        PdfDocument pdfDocument = new PdfDocument(writer);
        Document document = new Document(pdfDocument);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, AppConfig.SCAN_BITMAP_JPEG_COMPRESS_QUALITY, stream);
        byte[] bitmapData = stream.toByteArray();
        ImageData imageData = ImageDataFactory.create(bitmapData);
        Image image = new Image(imageData);
        document.add(image);
        document.close();
        savePDFPathIntoDatabase(pdfPath, fileName, timeStamp);

    }

    /**
     * This method creates a filename for a PDF-File.
     *
     * @param date
     * @return
     */
    public String createFileName(Date date) {
        String timeStamp = AppConfig.TIME_STAMP_FORMAT.format(date);
        return timeStamp + AppConfig.SCAN_FILE_NAME;
    }

    /**
     * This method saves the filepath of the PDF-File to the database
     * @param pdfPath
     * @param fileName
     * @param timeStamp
     */
    private void savePDFPathIntoDatabase(String pdfPath, String fileName, Date timeStamp) {
        Receipt receipt = new Receipt();
        receipt.setTimeStamp(timeStamp);
        receipt.setFileName(fileName);
        receipt.setFilePath(pdfPath);
        receipt.setBusinessYear(businessYear);
        dbHelper.createReceipt(receipt, receiptId -> {
            listener.onScanFinished(receiptId, bitmap);
        });
    }

    /**
     * This interface provides a callback method that is triggered when the scanlibrary has finished its workflow
     */
    public interface ScannerResultListener {
        void onScanFinished(long receiptId, Bitmap bitmap);
    }


}