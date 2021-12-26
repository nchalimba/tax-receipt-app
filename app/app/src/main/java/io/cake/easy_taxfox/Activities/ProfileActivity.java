package io.cake.easy_taxfox.Activities;



import android.os.Bundle;

import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import io.cake.easy_taxfox.Config.AppConfig;
import io.cake.easy_taxfox.Entities.Customer;
import io.cake.easy_taxfox.Database.TaxFoxDatabaseHelper;
import io.cake.easy_taxfox.Helpers.PreferencesHelper;
import io.cake.easy_taxfox.R;
import io.cake.easy_taxfox.Helpers.UIHelper;

/***
 * This activity controls the main logic for managing the customer profiles.
 */
public class ProfileActivity extends NavigationActivity {

    private TextView userNameText;
    private EditText firstName;
    private EditText lastName;
    private ImageButton saveButton;
    private TaxFoxDatabaseHelper dbHelper;
    private boolean isCustomerCreated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PreferencesHelper.setTheme(ProfileActivity.this);
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_profile, frameLayout);
        setTitle(R.string.profileTitle);
        initProfileUI();
        initDbHelper();
    }

    @Override
    protected void onResume() {
        super.onResume();
        navigationView.getMenu().getItem(getResources().getInteger(R.integer.profileNavigationIndex)).setChecked(true);
        updateProfileUI();
    }

    /**
     * This method updates the ui elements in the profile Activity.
     */
    private void updateProfileUI() {
        dbHelper.getCustomerById(AppConfig.DEFAULT_CUSTOMER_ID, customer -> {
            if(customer !=null){
                isCustomerCreated = true;
                firstName.setText(customer.getFirstName());
                lastName.setText(customer.getLastName());
                StringBuilder builder = new StringBuilder(customer.getFirstName()).append(" ").append(customer.getLastName());
                userNameText.setText(builder.toString());
            }
        });
    }

    /***
     * This method initializes the UI elements
     */
    private void initDbHelper(){
        dbHelper = TaxFoxDatabaseHelper.getInstance(ProfileActivity.this);
    }

    /***
     * This method initialized the main UI components.
     */
    private void initProfileUI(){
        userNameText = findViewById(R.id.profileUserNameText);
        firstName = findViewById(R.id.profileFirstNameET);
        lastName = findViewById(R.id.profileLastNameET);
        saveButton = findViewById(R.id.profileImgSaveProfile);
        saveButton.setOnClickListener(v -> saveProfile());
    }

    /**
     * This method saves the customer profile to the database.
     */
    private void saveProfile(){
        if (!validateInput()){
            UIHelper.showToast(ProfileActivity.this, getResources().getString(R.string.profileInvalid));
            return;
        }
        String firstNameString = firstName.getText().toString();
        String lastNameString = lastName.getText().toString();
        Customer customer = new Customer(firstNameString, lastNameString);
        if(isCustomerCreated){
            customer.setCustomerId(AppConfig.DEFAULT_CUSTOMER_ID);
        }
        dbHelper.createOrUpdateCustomer(customer);
        userNameText.setText(new StringBuilder(firstNameString).append(" ").append(lastNameString).toString());

        UIHelper.showToast(ProfileActivity.this, getResources().getString(R.string.profileSaved));
    }

    /**
     * This method validates the first- and lastname of the user input and checks if it is filled or empty.
     * @return
     */
    private boolean validateInput(){
        return !firstName.getText().toString().isEmpty() && !lastName.getText().toString().isEmpty();
    }
}
