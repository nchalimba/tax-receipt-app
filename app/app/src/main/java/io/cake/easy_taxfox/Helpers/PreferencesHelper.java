package io.cake.easy_taxfox.Helpers;

import android.app.Activity;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import io.cake.easy_taxfox.Config.AppConfig;

/**
 * This Helper provides all the methods for the shared preferences.
 * */
public class PreferencesHelper {

    /**
     * This method returns a boolean if the darkmode is currently setted for the given activity context.
     * @param activityContext
     * @return
     */
    private static boolean isDarkTheme(Activity activityContext){
        SharedPreferences sharedPreferences = activityContext.getSharedPreferences(AppConfig.SHARED_PREFERENCES_NAME, activityContext.MODE_PRIVATE);
        return sharedPreferences.getBoolean(AppConfig.PREFERENCES_DARKMODE_KEY, AppConfig.DEFAULT_DARKMODE_ACTIVE);
    }

    /**
     * This method sets the dark or lightmode for the given activity context.
     * @param activityContext
     */
    public static void setTheme(AppCompatActivity activityContext){
        if(isDarkTheme(activityContext)){
            activityContext.getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }else{
            activityContext.getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

    }
}
