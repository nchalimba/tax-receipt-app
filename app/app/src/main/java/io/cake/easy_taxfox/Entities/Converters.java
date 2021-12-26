package io.cake.easy_taxfox.Entities;

import androidx.room.TypeConverter;

import java.util.Date;
/**
 * This class is necessary to convert timestamp values from the database.
 */
public class Converters {

    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}
