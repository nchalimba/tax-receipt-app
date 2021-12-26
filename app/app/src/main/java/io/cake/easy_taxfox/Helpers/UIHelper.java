package io.cake.easy_taxfox.Helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;
import java.util.Calendar;
import java.util.GregorianCalendar;

/***
 * This class provides common methods for UI elements
 */
public class UIHelper {

    /**
     * This method creates a toast.
     * @param context
     * @param message
     */
    public static void showToast(Context context, String message) {
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, message, duration);
        toast.show();
    }

    /**
     * This method creates a new DatePickerDialog.
     * @return DatePickerDialog
     */
    public static DatePickerDialog createDatePickerDialog(Activity activityContext, DatePickerDialog.OnDateSetListener listener) {
        GregorianCalendar today = new GregorianCalendar();
        int day = today.get(Calendar.DAY_OF_MONTH);
        int month = today.get(Calendar.MONTH);
        int year = today.get(Calendar.YEAR);
        return new DatePickerDialog(activityContext, listener, year, month, day);
    }

    /**
     * This method creates altertdialog with a negative button.
     * @param activityContext
     * @param title
     * @param message
     * @param positiveText
     * @param negativeText
     * @param positiveListener
     * @param negativeListener
     */
    public static void showAlertDialog(Activity activityContext, String title, String message, String positiveText,
                                       String negativeText, DialogInterface.OnClickListener positiveListener,
                                       DialogInterface.OnClickListener negativeListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(activityContext);
        builder.setTitle(title).setMessage(message);
        builder.setPositiveButton(positiveText, positiveListener);
        builder.setNegativeButton(negativeText, negativeListener);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * This mehtod creates altertdialog without a negative button.
     * @param activityContext
     * @param title
     * @param message
     * @param positiveText
     * @param positiveListener
     */
    public static void showAlertDialog(Activity activityContext, String title, String message, String positiveText, DialogInterface.OnClickListener positiveListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(activityContext);
        builder.setTitle(title).setMessage(message);
        builder.setPositiveButton(positiveText, positiveListener);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
