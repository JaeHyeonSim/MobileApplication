package ddwu.mobile.finalproject.ma01_20200982;

import androidx.room.TypeConverter;

import java.sql.Date;
import java.sql.Time;

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
