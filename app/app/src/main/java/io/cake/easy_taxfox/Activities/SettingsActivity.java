package io.cake.easy_taxfox.Activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatDelegate;

import io.cake.easy_taxfox.Config.AppConfig;
import io.cake.easy_taxfox.Config.Log;
import io.cake.easy_taxfox.Helpers.PreferencesHelper;
import io.cake.easy_taxfox.Helpers.UIHelper;
import io.cake.easy_taxfox.R;

public class SettingsActivity extends NavigationActivity{

    private Spinner chooseBusinessYear;
    private Switch toggleDarkmode;
    private Switch toggleVisionApi;
    private String businessYear;
    private boolean isSpinnerTouched = false;
    private boolean isDarkmodeActive;
    private boolean isVisionApiActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PreferencesHelper.setTheme(SettingsActivity.this);
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_settings, frameLayout);
        setTitle(R.string.settingsTitle);
        initSettingsUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        navigationView.getMenu().getItem(getResources().getInteger(R.integer.settingsNavigationIndex)).setChecked(true);
    }

    /***
     * This method initializes the UI elements
     */
    private void initSettingsUI() {
        chooseBusinessYear = findViewById(R.id.spinnerChooseBusinessYear);
        toggleDarkmode = findViewById(R.id.settingsSwitchEnableDarkmode);
        toggleVisionApi = findViewById(R.id.settingsSwitchEnableTextrecognition);
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(SettingsActivity.this,R.array.settingsChooseBusinessYear, android.R.layout.simple_spinner_dropdown_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        chooseBusinessYear.setAdapter(adapter1);

        toggleDarkmode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(buttonView.isPressed()){
                Log.d(AppConfig.SETTINGS_TAG, "Darkmode button");
                darkmodeChanged(isChecked);
            }
        });
        toggleVisionApi.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(buttonView.isPressed()){
                Log.d(AppConfig.SETTINGS_TAG, "Vision button");
                saveData();
            }
        });

        chooseBusinessYear.setOnTouchListener((v, event) -> {
            isSpinnerTouched = true;
            return false;
        });
        chooseBusinessYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            /***
             * This method saves the shared preferences if a spinner value is selected
             * @param parent
             * @param view
             * @param position
             * @param id
             */
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(isSpinnerTouched){
                    Log.d(AppConfig.SETTINGS_TAG, "year spinner");
                    isSpinnerTouched = false;
                    saveData();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        loadData();
        updateViews();
    }

    /***
     * This method gets triggered when the switch for darkmode changes. It toggles between darkmode/lightmode
     * @param isChecked specifies, whether the switch is checked or not
     */
    private void darkmodeChanged(boolean isChecked){
        if(isChecked){
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }else{
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        saveData();
    }

    /***
     * This method saves the currently selected settings to the shared preferences
     */
    private void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences(AppConfig.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(AppConfig.PREFERENCES_BUSINESS_YEAR_KEY, chooseBusinessYear.getSelectedItem().toString());
        editor.putBoolean(AppConfig.PREFERENCES_DARKMODE_KEY, toggleDarkmode.isChecked());
        editor.putBoolean(AppConfig.PREFERENCES_VISION_API_KEY, toggleVisionApi.isChecked());
        editor.commit();
        UIHelper.showToast(SettingsActivity.this, getResources().getString(R.string.settingsSaved));
    }

    /***
     * This method reads the shared preferences and sets the global attributes
     */
    public void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences(AppConfig.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        businessYear = sharedPreferences.getString(AppConfig.PREFERENCES_BUSINESS_YEAR_KEY, AppConfig.DEFAULT_BUSINESS_YEAR);
        isDarkmodeActive = sharedPreferences.getBoolean(AppConfig.PREFERENCES_DARKMODE_KEY, AppConfig.DEFAULT_DARKMODE_ACTIVE);
        isVisionApiActive = sharedPreferences.getBoolean(AppConfig.PREFERENCES_VISION_API_KEY, AppConfig.DEFAULT_VISION_API_ACTIVE);
    }

    /***
     * This method updates the UI according to the shared preferences
     */
    public void updateViews() {
        chooseBusinessYear.setSelection(getBusinessYearIndex());
        toggleDarkmode.setChecked(isDarkmodeActive);
        toggleVisionApi.setChecked(isVisionApiActive);
    }

    /***
     * This method gets the index of the currently loaded business year
     * @return
     */
    public int getBusinessYearIndex(){
        String [] businessYears = getResources().getStringArray(R.array.settingsChooseBusinessYear);
        for (int i = 0; i<businessYears.length; i++){
            if (businessYear.equals(businessYears[i])){
                return i;
            }
        }
        return 0;
    }
}