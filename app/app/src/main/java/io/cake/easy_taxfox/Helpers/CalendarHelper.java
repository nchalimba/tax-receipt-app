package io.cake.easy_taxfox.Helpers;

import java.util.Calendar;
import java.util.Date;

/**
 * This class provides all methods for the calendar setup needed for the filtering the receipts.
 */
public class CalendarHelper {

    /**
     * This method extracts the month out of a date
     * @param date
     * @return
     */
    public static int getMonth(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.MONTH) + 1;
    }

    /**
     * This method extracts the year out of a date
     * @param date
     * @return
     */
    public static int getYear(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.YEAR);
    }

    /**
     * This method adds a given amount of days to a date
     * @param date
     * @param amountDays if negative, it substracts the days
     * @return
     */
    public static Date addDays(Date date, int amountDays){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, amountDays);
        return cal.getTime();
    }
}
