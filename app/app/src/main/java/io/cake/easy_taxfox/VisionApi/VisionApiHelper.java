package io.cake.easy_taxfox.VisionApi;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.cake.easy_taxfox.Config.AppConfig;
import io.cake.easy_taxfox.Config.Log;
import io.cake.easy_taxfox.R;
import io.cake.easy_taxfox.Helpers.UIHelper;


/***
 * This class represents an abstraction layer between the Google Cloud Vision API implementation and the activities that use it.
 */
public class VisionApiHelper {

    private Context context;
    private Activity activityContext;
    private Account mAccount;
    private GetOAuthTokenTask.GetOAuthTokenResultListener listener;

    public VisionApiHelper(Context context, GetOAuthTokenTask.GetOAuthTokenResultListener listener) {
        this.context = context;
        activityContext = (Activity) context;
        this.listener = listener;
    }

    /***
     * This method is the entry point for the main vision api functionality and calls the task to process a given bitmap
     * @param bitmap the input receipt
     * @param accessToken the oAuth access token needed to connect to the vision api
     * @param listener
     */
    public void callVisionApi(Bitmap bitmap,String accessToken,VisionApiResultListener listener){
        bitmap = resizeBitmap(bitmap);
        Log.d(AppConfig.VISION_API_TAG, "Calling Vision API");
        VisionApiTask visionApiTask = new VisionApiTask(accessToken, bitmap, activityContext, listener::onVisionApiResult);
        try {
            Executors.newSingleThreadExecutor().submit(visionApiTask).get(AppConfig.TASK_TIMEOUT, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            listener.onVisionApiResult(new ReceiptPrediction(AppConfig.DEFAULT_RECEIPT_PREDICTION_TITLE, AppConfig.DEFAULT_RECEIPT_PREDICTION_AMOUNT));
        }
    }

    /***
     * This method calls an account picker to choose / add a google account needed for the oAuth process
     */
    public void pickUserAccount() {
        String[] accountTypes = new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE};
        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                accountTypes, false, null, null, null, null);
        activityContext.startActivityForResult(intent, AppConfig.REQUEST_CODE_PICK_ACCOUNT);
    }

    /***
     * This method calls a task needed to get the oAuth token for a given google account
     */
    public void getAuthToken() {
        if (mAccount == null) {
            pickUserAccount();
        } else {
            GetOAuthTokenTask oAuthTokenTask = new GetOAuthTokenTask(activityContext, mAccount, AppConfig.REQUEST_ACCOUNT_AUTHORIZATION, AppConfig.OAUTH_SCOPE, listener);
            Executors.newSingleThreadExecutor().submit(oAuthTokenTask);

        }
    }

    /***
     * This method gets triggered when the user returns from choosing a google account or from giving oauth access.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void VisionApiActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == AppConfig.REQUEST_CODE_PICK_ACCOUNT) {
            if (resultCode == Activity.RESULT_OK) {
                String email = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                AccountManager am = AccountManager.get(activityContext);
                Account[] accounts = am.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
                for (Account account : accounts) {
                    if (account.name.equals(email)) {
                        mAccount = account;
                        break;
                    }
                }
                getAuthToken();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                UIHelper.showToast(activityContext, activityContext.getResources().getString(R.string.visionNoAccount));

            }
        } else if (requestCode == AppConfig.REQUEST_ACCOUNT_AUTHORIZATION) {
            if (resultCode == Activity.RESULT_OK) {
                Bundle extra = data.getExtras();
                listener.onTokenResult(extra.getString(AppConfig.EXTRA_OAUTH_TOKEN));
            } else if (resultCode == Activity.RESULT_CANCELED) {
                UIHelper.showToast(activityContext, activityContext.getResources().getString(R.string.visionAuthFailed));
            }
        }
    }

    /***
     * This method validates whether a given activity result should be processed by the vision api helper or not
     * @param requestCode
     * @return
     */
    public boolean isVisionApiActivityResult(int requestCode){
        return requestCode == AppConfig.REQUEST_CODE_PICK_ACCOUNT || requestCode == AppConfig.REQUEST_ACCOUNT_AUTHORIZATION;
    }

    /***
     * This method resizes a given bitmap to better match the requirements of the vision api
     * @param bitmap
     * @return
     */
    public Bitmap resizeBitmap(Bitmap bitmap) {
        int maxDimension = AppConfig.VISION_BITMAP_MAX_DIMENSION;
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }

    /***
     * This interface handles the callback as soon as the vision api process is finished
     */
    public interface VisionApiResultListener{
        void onVisionApiResult(ReceiptPrediction receiptPrediction);
    }

}
