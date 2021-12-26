package io.cake.easy_taxfox.VisionApi;

import android.accounts.Account;
import android.app.Activity;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;

import java.io.IOException;

/***
 * This class fetches the oauth tokem of a given account
 */
public class GetOAuthTokenTask implements Runnable {

    private Activity activityContext;
    private Account account;
    private int requestCode;
    private String scope;
    private GetOAuthTokenResultListener listener;

    public GetOAuthTokenTask(Activity activityContext, Account account, int requestCode, String scope, GetOAuthTokenResultListener listener) {
        this.activityContext = activityContext;
        this.account = account;
        this.requestCode = requestCode;
        this.scope = scope;
        this.listener = listener;
    }

    /***
     * This method fetches the oauth token and calls the callback method
     */
    @Override
    public void run() {
        try {
            String token = fetchToken();
            if (token != null) {
                activityContext.runOnUiThread(() -> {
                    listener.onTokenResult(token);
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
            activityContext.runOnUiThread(() -> listener.onTokenResult(null));
        }
    }

    /***
     * This method gets the oauth access token
     * @return
     * @throws IOException
     */
    private String fetchToken() throws IOException {
        String accessToken;
        try {
            accessToken = GoogleAuthUtil.getToken(activityContext, account, scope);
            GoogleAuthUtil.clearToken(activityContext, accessToken);
            accessToken = GoogleAuthUtil.getToken(activityContext, account, scope);
            return accessToken;
        } catch (UserRecoverableAuthException userRecoverableException) {
            activityContext.startActivityForResult(userRecoverableException.getIntent(), requestCode);
        } catch (GoogleAuthException fatalException) {
            fatalException.printStackTrace();
        }
        return null;
    }

    /***
     * This interface provides a callback method that is called when the token is fetched
     */
    public interface GetOAuthTokenResultListener{
        void onTokenResult(String token);
    }
}
