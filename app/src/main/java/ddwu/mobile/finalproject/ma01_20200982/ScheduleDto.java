package ddwu.mobile.finalproject.ma01_20200982;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Time;

@Entity(tableName = "schedule_table")
public class ScheduleDto implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private long schedule_id;
    @ColumnInfo(name="name")
    private String name;
    @ColumnInfo(name="party")
    private int party;
    @ColumnInfo(name="meet_date")
    private Date meet_date;
    @ColumnInfo(name="time_hour")
    private int time_hour;
    @ColumnInfo(name="time_minute")
    private int time_minute;
    @ColumnInfo(name="place_title")
    private String place_title;
    @ColumnInfo(name="address")
    private String address;

    public ScheduleDto() {
    }
    public ScheduleDto(String name) {
        this.name = name;
    }
    public ScheduleDto(String name, int party, Date meet_date, int time_hour, int time_minute, String place_title, String address) {
        this.name = name;
        this.party = party;
        this.meet_date = meet_date;
        this.time_hour = time_hour;
        this.time_minute = time_minute;
        this.place_title = place_title;
        this.address = address;
    }
    public ScheduleDto(long schedule_id, String name, int party, Date meet_date, int time_hour, int time_minute, String place_title, String address) {
        this.schedule_id = schedule_id;
        this.name = name;
        this.party = party;
        this.meet_date = meet_date;
        this.time_hour = time_hour;
        this.time_minute = time_minute;
        this.place_title = place_title;
        this.address = address;
    }

    public long getSchedule_id() {
        return schedule_id;
    }
    public void setSchedule_id(long schedule_id) {
        this.schedule_id = schedule_id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getParty() {
        return party;
    }
    public void setParty(int party) {
        this.party = party;
    }
    public Date getMeet_date() {
        return meet_date;
    }
    public void setMeet_date(Date meet_date) {
        this.meet_date = meet_date;
    }
    public int getTime_hour() {
        return time_hour;
    }
    public void setTime_hour(int time_hour) {
        this.time_hour = time_hour;
    }
    public int getTime_minute() {
        return time_minute;
    }
    public void setTime_minute(int time_minute) {
        this.time_minute = time_minute;
    }
    public String getPlace_title() {
        return place_title;
    }
    public void setPlace_title(String place_title) {
        this.place_title = place_title;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "ScheduleDto{" +
                "schedule_id=" + schedule_id +
                ", name='" + name + '\'' +
                ", party=" + party +
                ", meet_date=" + meet_date +
                ", time_hour=" + time_hour +
                ", time_minute=" + time_minute +
                ", place_title='" + place_title + '\'' +
                ", address='" + address + '\'' +
                '}';
    }

}
