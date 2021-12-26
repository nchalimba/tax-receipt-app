package io.cake.easy_taxfox.Config;

import android.app.Activity;
import android.graphics.Color;
import android.os.Environment;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import io.cake.easy_taxfox.R;

/**
 * This class contains all constants, which are necessary for the application configuration.
 */
public class AppConfig {

    //GENERAL
    private static final String PACKAGE_NAME = "io.cake.easy_taxfox";
    public static final int DEFAULT_CUSTOMER_ID = 1;
    public static final boolean IS_DEBUG_MODE = true;
    public static final boolean IS_CAMERA_ACTIVE = false;
    public static final double EPSILON_THRESHOLD = 0.000001d;
    
    //PDF
    private static final String FOLDER_NAME = "EasyTaxFox";
    public static final String FOLDER_PATH = createFolderPath();
    public static final int PDF_TO_BITMAP_DPI = 300;
    public static final int PDF_TO_BITMAP_PAGE_INDEX = 0;
    public static final int SCALE_FACTOR = 60;

    //REQUEST CODES
    public static final int REQUEST_STORAGE_CAMERA_ACCOUNTS = 1;
    public static final int REQUEST_SCANNER_ACTIVITY = 99;
    public static final int REQUEST_STORAGE = 2;
    public static final int REQUEST_CODE_PICK_ACCOUNT = 101;
    public static final int REQUEST_ACCOUNT_AUTHORIZATION = 102;

    //TIME
    public static final String TIME_STAMP_INPUT_FORMAT = "yyyyMMdd_HHmmss";
    public static final DateFormat TIME_STAMP_FORMAT = new SimpleDateFormat(TIME_STAMP_INPUT_FORMAT);
    public static final String DATE_INPUT_FORMAT = "dd.MM.yyyy";
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat(DATE_INPUT_FORMAT);

    //LOG TAGS
    public static final String RECEIPT_DATA_TAG = "Receipt data activity";
    public static final String VISION_API_TAG = "Google Cloud Vision API";
    public static final String DASHBOARD_TAG = "Dashboard activity";
    public static final String EXPORT_TAG = "Export activity";
    public static final String SETTINGS_TAG = "Settings activity";
    public static final String PDF_TAG = "PDF helper";

    //EXTRA KEYS
    public static final String EXTRA_RECEIPT_PREDICTION = PACKAGE_NAME + ".receiptPrediction";
    public static final String EXTRA_RECEIPT_ID = PACKAGE_NAME + ".receiptId";

    //PREFERENCES
    public static final String SHARED_PREFERENCES_NAME = "sharedPrefs";
    public static final String PREFERENCES_BUSINESS_YEAR_KEY = "chooseBusinessYear";
    public static final String PREFERENCES_DARKMODE_KEY = "enableDarkmode";
    public static final String PREFERENCES_VISION_API_KEY = "enableTextrecognition";
    public static final String DEFAULT_BUSINESS_YEAR = "2021";
    public static final boolean DEFAULT_DARKMODE_ACTIVE = false;
    public static final boolean DEFAULT_VISION_API_ACTIVE = true;

    //DATABASE
    public static final String DATABASE_NAME = "TaxFox-db";
    public static final int DATABASE_VERSION = 1;
    public static final boolean DATABASE_EXPORT_SCHEMA = false;

    //DASHBOARD ACTIVITY
    public static final float DASHBOARD_PIECHART_TEXTSIZE = 12f;
    public static final int DASHBOARD_PIECHART_ANIMATION_DURATION = 1400;
    public static final int DASHBOARD_PIECHART_LABEL_TEXTSIZE = 10;
    public static final int DASHBOARD_PIECHART_LABEL_COLOR = Color.BLACK;
    public static final int DASHBOARD_PIECHART_CENTER_TEXTSIZE = 12;

    //RECEIPTDATA ACTIVITY
    public static final int DEFAULT_RECEIPT_ID = 0;

    //NAVIGATION ACTIVITY
    public static final String NAVIGATION_TITLE = "Navigation";

    //EXPORT ACTIVITY
    public static final float[] EXPORT_TABLE_COLUMN_WIDTH = {100f, 100f, 100f, 100f, 100f};
    public static final String EXPORT_TABLE_DATE_HEADER = "Datum";
    public static final String EXPORT_TABLE_ANNEX_HEADER = "Anlage";
    public static final String EXPORT_TABLE_SECTION_HEADER = "Abschnitt";
    public static final String EXPORT_TABLE_TITLE_HEADER = "Bezeichnung";
    public static final String EXPORT_TABLE_AMOUNT_HEADER = "Betrag";
    public static final int EXPORT_IMAGE_QUALITY = 100;
    public static final int EXPORT_IMAGE_HEIGHT = 3508;
    public static final int EXPORT_IMAGE_WIDTH = 2480;
    public static final int EXPORT_MIN_MONTH = 0;
    public static final int EXPORT_MIN_DAY = 1;
    public static final int EXPORT_MAX_MONTH = 11;
    public static final int EXPORT_MAX_DAY = 31;
    public static final String EXPORT_INTENT_TYPE = "application/pdf";
    public static final String EXPORT_FILE_NAME = "_MyTaxfoxExport.pdf";

    //SCANLIBRARY
    public static final int OPEN_CAMERA = 1;
    public static final int OPEN_GALLERY = 2;
    public static final int SCAN_BITMAP_JPEG_COMPRESS_QUALITY = 100;
    public static final String SCAN_FILE_NAME = "_MyTaxfoxScan.pdf";

    //VISION API
    public static final String DEFAULT_RECEIPT_PREDICTION_TITLE = "";
    public static final Double DEFAULT_RECEIPT_PREDICTION_AMOUNT = 0.0;
    public static final int TASK_TIMEOUT = 300;
    public static final String OAUTH_SCOPE = "oauth2:https://www.googleapis.com/auth/cloud-platform";
    public static final String EXTRA_OAUTH_TOKEN = "authtoken";
    public static final int VISION_BITMAP_MAX_DIMENSION = 1024;
    public static final String VISION_API_REQUEST_TYPE_1 = "DOCUMENT_TEXT_DETECTION";
    public static final String VISION_API_REQUEST_TYPE_2 = "TEXT_DETECTION";
    public static final int VISION_MAX_RESULTS = 10;
    public static final int VISION_JPEG_COMPRESS_QUALITY = 90;
    public static final String[] VISION_SUM_KEYWORDS = {"summe", "sum", "betrag", "gesamtbetrag", "total"};
    public static final int VISION_MAX_TITLE_LENGTH = 30;
    private static final String SECTIONS_IDENTIFIER = "sections_";
    private static final String SECTIONS_DEF_TYPE = "array";
    public static final int GEOMETRY_ABOVE_LINE = 1;
    public static final int GEOMETRY_BELOW_LINE = -1;
    public static final int GEOMETRY_ON_LINE = 0;

    /**
     * This method returns a map of all annex sections and their subcategories.
     * @param activityContext
     * @return
     */
    public static  final Map<String, String[]> getAnnexSections(Activity activityContext){
        Map<String, String[]> annexSections = new HashMap<>();
        String[] annexes = activityContext.getResources().getStringArray(R.array.annexes_short);
        for(int i = 0; i<annexes.length; i++){
            int sectionsId = activityContext.getResources().getIdentifier(SECTIONS_IDENTIFIER + i, SECTIONS_DEF_TYPE, activityContext.getPackageName());
            String[] sections = activityContext.getResources().getStringArray(sectionsId);
            annexSections.put(annexes[i], sections);
        }
        annexSections = Collections.unmodifiableMap(annexSections);
        return annexSections;
    }

    /**
     * This method creates and returns the file-/folder-path of the app.
     * @return
     */
    private static final String createFolderPath (){
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString() + "/" + AppConfig.FOLDER_NAME + "/";
        return path;
    }

    /**
     * This method formats a given double to a german currency format.
     * @param amount
     * @return
     */
    public static String formatMoney(Double amount){
        Locale locale = Locale.GERMANY;
        NumberFormat numberFormat = NumberFormat.getCurrencyInstance(locale);
        return numberFormat.format(amount);
    }
}
