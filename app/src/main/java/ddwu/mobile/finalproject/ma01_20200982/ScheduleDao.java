package ddwu.mobile.finalproject.ma01_20200982;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.sql.Date;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;

@Dao
public interface ScheduleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Single<Long> insertSchedule(ScheduleDto schedule);

    @Update
    Completable updateSchedule(ScheduleDto schedule);

    @Delete
    Completable deleteSchedule(ScheduleDto schedule);

    @Query("SELECT * FROM schedule_table ORDER BY meet_date")
    Flowable<List<ScheduleDto>> findAllSchedules();

    @Query("SELECT * FROM schedule_table WHERE meet_date = :today")
    Flowable<List<ScheduleDto>> findScheduleToday(Date today);

    @Query("SELECT * FROM schedule_table WHERE meet_date > :today ORDER BY meet_date")
    Flowable<List<ScheduleDto>> findSchedulesAfter(Date today);

    @Query("SELECT * FROM schedule_table WHERE schedule_id = :schedule_id")
    Single<ScheduleDto> findScheduleByScheduleId(long schedule_id);

    @Query("SELECT * FROM schedule_table WHERE name = :name")
    Single<ScheduleDto> findScheduleByName(String name);

    @Query("SELECT * FROM schedule_table WHERE name LIKE :name")
    Flowable<List<ScheduleDto>> searchSchedule(String name);

}
