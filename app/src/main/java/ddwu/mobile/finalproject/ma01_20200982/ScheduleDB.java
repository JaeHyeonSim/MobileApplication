package ddwu.mobile.finalproject.ma01_20200982;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {ScheduleDto.class}, version=1)
@TypeConverters({Converters.class})
public abstract class ScheduleDB extends RoomDatabase {
    public abstract ScheduleDao scheduleDao();

    private static volatile ScheduleDB INSTANCE;

    static ScheduleDB getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (ScheduleDB.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    ScheduleDB.class, "schedule_db.db")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
