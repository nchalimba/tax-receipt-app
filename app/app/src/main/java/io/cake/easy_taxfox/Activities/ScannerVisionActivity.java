package io.cake.easy_taxfox.Activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.cake.easy_taxfox.Config.AppConfig;
import io.cake.easy_taxfox.Config.Log;
import io.cake.easy_taxfox.R;
import io.cake.easy_taxfox.Helpers.ScannerHelper;
import io.cake.easy_taxfox.Helpers.UIHelper;
import io.cake.easy_taxfox.VisionApi.GetOAuthTokenTask;
import io.cake.easy_taxfox.VisionApi.ReceiptPrediction;
import io.cake.easy_taxfox.VisionApi.VisionApiHelper;

/***
 * This activity controls the main logic flow of the scanlibrary and the vision api
 */
public class ScannerVisionActivity extends NavigationActivity implements  ScannerHelper.ScannerResultListener, GetOAuthTokenTask.GetOAuthTokenResultListener, VisionApiHelper.VisionApiResultListener{

    protected int businessYear;
    protected int scanMethod;
    private boolean isVisionApiActive;
    private String accessToken;
    private long scannedReceiptId = 0;
    private ScannerHelper scannerHelper;
    private VisionApiHelper visionApiHelper;
    protected ProgressBar progressBar;
    protected ImageButton scanVisionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initHelper();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initSharedPreferences();
    }

    /***
     * This method requests the relevant permissions dependend on whether the vision api is active or not
     */
    protected void requestRequiredPermissions() {
        if(!AppConfig.IS_CAMERA_ACTIVE && scanMethod == AppConfig.OPEN_CAMERA){
            UIHelper.showToast(ScannerVisionActivity.this, getResources().getString(R.string.scanCameraInactive));
            return;
        }
        String permissions[];
        if(isVisionApiActive){
            permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.GET_ACCOUNTS};
        }else{
            permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        }
        requestPermissions(permissions, AppConfig.REQUEST_STORAGE_CAMERA_ACCOUNTS);
    }

    /***
     * This method calls the according helper method or shows a dialog if the permissions were not granted
     * @param requestCode
     * @param permissions
     * @param grantResults
     */

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if((requestCode == AppConfig.REQUEST_STORAGE_CAMERA_ACCOUNTS) && hasAllPermissionsGranted(grantResults)){
            if(isVisionApiActive){
                if (checkSelfPermission(Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_GRANTED){
                    visionApiHelper.getAuthToken();
                }else{
                    UIHelper.showToast(ScannerVisionActivity.this, getResources().getString(R.string.permissionDenied));
                }
            }else if(scanMethod == AppConfig.OPEN_CAMERA){
                scannerHelper.openCamera();
            }else if(scanMethod == AppConfig.OPEN_GALLERY){
                scannerHelper.openGallery();
            }else{
                UIHelper.showToast(this, getResources().getString(R.string.scanNoMethodSelected));
            }
        }else{
            showAlertDialog();
        }
    }

    /***
     * This method checks if every existing permission was granted
     * @param grantResults
     * @return
     */
    protected boolean hasAllPermissionsGranted(@NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    /***
     * This method shows an alert dialog if permissions are not granted initially
     */
    private void showAlertDialog(){
        if(shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) ||
                shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) ||
                shouldShowRequestPermissionRationale(Manifest.permission.GET_ACCOUNTS)) {
            new AlertDialog.Builder(ScannerVisionActivity.this)
                    .setTitle(getResources().getString(R.string.permissionNeeded))
                    .setMessage(getResources().getString(R.string.permissionScannerReason))
                    .setPositiveButton(getResources().getString(R.string.permissionOk), (dialog, which) -> requestRequiredPermissions())
                    .setNegativeButton(getResources().getString(R.string.permissionCancel), (dialog, which) -> dialog.dismiss())
                    .create().show();
        }
    }

    /***
     * This method initializes a SharedPreference-Objekt to connect the TextView displaying the year to the shared preferences
     */
    private void initSharedPreferences(){
        SharedPreferences sharedPreferences = getSharedPreferences(AppConfig.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        businessYear = Integer.valueOf(sharedPreferences.getString(AppConfig.PREFERENCES_BUSINESS_YEAR_KEY, AppConfig.DEFAULT_BUSINESS_YEAR));
        isVisionApiActive = sharedPreferences.getBoolean(AppConfig.PREFERENCES_VISION_API_KEY, true);
    }

    /***
     * This method initializes the necessary abstraction classes (helpers)
     */
    private void initHelper(){
        scannerHelper = new ScannerHelper(ScannerVisionActivity.this, this);
        visionApiHelper = new VisionApiHelper(ScannerVisionActivity.this, this);
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
        if(visionApiHelper.isVisionApiActivityResult(requestCode)){
            visionApiHelper.VisionApiActivityResult(requestCode, resultCode, data);
        }else{
            if(progressBar != null && isVisionApiActive){
                progressBar.setVisibility(View.VISIBLE);
            }
            if(scanVisionButton != null && isVisionApiActive){
                scanVisionButton.setEnabled(false);
            }
            scannerHelper.scannerActivityResult(requestCode, resultCode, data);
        }
        if(resultCode == RESULT_CANCELED){
            if(progressBar != null && isVisionApiActive) {
                progressBar.setVisibility(View.GONE);
            }
            if(scanVisionButton != null && isVisionApiActive){
                scanVisionButton.setEnabled(true);
            }
        }
    }

    /***
     * This method starts the receiptDataActivity with the receipt id
     * @param receiptId
     */
    protected void startReceiptDataActivity(long receiptId) {
        Intent intent = new Intent(ScannerVisionActivity.this, ReceiptDataActivity.class);
        intent.putExtra(AppConfig.EXTRA_RECEIPT_ID, receiptId);
        startActivity(intent);
    }

    /***
     * This method gets triggered as soon as the Scan is finished. It either calls the Vision API or sends an inent with just the id
     * @param receiptId (the id of the newly created receipt)
     * @param bitmap The bitmap of the scan (relevant for the google vision api)
     */
    @Override
    public void onScanFinished(long receiptId, Bitmap bitmap) {
        if(isVisionApiActive){
            scannedReceiptId = receiptId;
            visionApiHelper.callVisionApi(bitmap, accessToken, ScannerVisionActivity.this);
        }else{
            startReceiptDataActivity(receiptId);
        }
    }

    /***
     * This method starts the according scanner method (camera / gallery) after receiving the oAuth Token
     * @param token
     */
    @Override
    public void onTokenResult(String token) {
        accessToken = token;
        if (scanMethod == AppConfig.OPEN_CAMERA){
            scannerHelper.openCamera();
        }else if(scanMethod == AppConfig.OPEN_GALLERY){
            scannerHelper.openGallery();
        }else{
            UIHelper.showToast(ScannerVisionActivity.this, getResources().getString(R.string.scanNoMethodSelected));
        }
    }

    /***
     * This method starts the receiptDataActivity with the predictions retrieved from the google vision api and the receipt id
     * @param receiptPrediction
     */
    @Override
    public void onVisionApiResult(ReceiptPrediction receiptPrediction) {
        if (progressBar != null && isVisionApiActive){
            progressBar.setVisibility(View.GONE);
        }
        if(scanVisionButton != null && isVisionApiActive){
            scanVisionButton.setEnabled(true);
        }
        Intent intent = new Intent(ScannerVisionActivity.this, ReceiptDataActivity.class);
        intent.putExtra(AppConfig.EXTRA_RECEIPT_PREDICTION, receiptPrediction);
        intent.putExtra(AppConfig.EXTRA_RECEIPT_ID, scannedReceiptId);
        startActivity(intent);
    }
}
