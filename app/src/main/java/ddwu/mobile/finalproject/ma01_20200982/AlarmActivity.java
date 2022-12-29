package ddwu.mobile.finalproject.ma01_20200982;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CalendarView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class AlarmActivity extends AppCompatActivity implements Serializable {

    private final String TAG = "AlarmActivity";

    PendingIntent sender = null;
    AlarmManager alarmManager = null;

    private CalendarView calendarView;
    private ListView lvSchedules = null;
    private ScheduleAdapter listAdapter;
    private ArrayList<ScheduleDto> scheduleList;

    public static Map<Integer, boolean[]> selecetedItems = new HashMap<Integer, boolean[]>();

    ScheduleDB db;
    ScheduleDao dao;

    private CompositeDisposable mDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        createNotificationChannel();

        scheduleList = new ArrayList<ScheduleDto>();

        lvSchedules = (ListView)findViewById(R.id.lv_schedule_alarm);
        listAdapter = new ScheduleAdapter(this, R.layout.custom_schedule_view, scheduleList);

        lvSchedules.setAdapter(listAdapter);

        db = ScheduleDB.getDatabase(this);
        dao = db.scheduleDao();

        calendarView = findViewById(R.id.calendarView);

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int day) {
                listAdapter.clear();

                String selectedDay = year + "-" + (month + 1) + "-" + day;
                Flowable<List<ScheduleDto>> resultSchedules = dao.findScheduleToday(Date.valueOf(selectedDay));
                mDisposable.add (
                        resultSchedules.subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(schedules -> {
                                            listAdapter.addAll(schedules);
                                        },
                                        throwable -> Log.d(TAG, "error", throwable))
                );
            }
        });

        lvSchedules.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                ScheduleDto schedule = scheduleList.get(pos);
                int key = (int)scheduleList.get(pos).getSchedule_id() * 10000;
                if (!selecetedItems.containsKey(key)) {
                    selecetedItems.put(key,new boolean[]{false, false, false, false});
                }
                boolean[] changed = selecetedItems.get(key);
                AlertDialog.Builder builder = new AlertDialog.Builder(AlarmActivity.this);
                builder.setTitle(R.string.dialog_set_alarm)
                        .setMultiChoiceItems(R.array.alarms, selecetedItems.get(key),
                                new DialogInterface.OnMultiChoiceClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i, boolean isChecked) {
                                        changed[i] = isChecked;
                                        selecetedItems.put(key, changed);
                                    }
                                })
                        .setPositiveButton(R.string.dialog_set_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int pos) {
//                                int[] alarm = getResources().getIntArray(R.array.alarms);
                                for (int i = 0; i < selecetedItems.get(key).length; i++) {
                                    if (selecetedItems.get(key)[i]) {
                                        Intent intent = new Intent(AlarmActivity.this, MyBroadcastReceiver.class);
                                        sender = PendingIntent.getBroadcast(AlarmActivity.this, key + i, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                                        Time time = new Time(schedule.getTime_hour(), schedule.getTime_minute(), 0);
                                        switch (i) {
                                            case 0:
                                                time.setMinutes(time.getMinutes() - 30);
                                                break;
                                            case 1:
                                                time.setHours(time.getHours() - 1);
                                                break;
                                            case 2:
                                                time.setHours(time.getHours() - 12);
                                                break;
                                            case 3:
                                                time.setHours(time.getHours() - 24);
                                                break;
                                        }

                                        String scheduleDate = schedule.getMeet_date().toString();
                                        int year = Integer.parseInt(scheduleDate.split("-")[0]);
                                        int month = Integer.parseInt(scheduleDate.split("-")[1]);
                                        int day = Integer.parseInt(scheduleDate.split("-")[2]);

                                        Calendar calendar = Calendar.getInstance();
                                        calendar.setTimeInMillis(System.currentTimeMillis());
                                        calendar.set(year, month, day,
                                                time.getHours(), time.getMinutes(), time.getSeconds());
                                        Log.d(TAG, calendar.toString());

                                        // 알람 등록
                                        alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), sender);
                                    } else {
                                        Intent intent = new Intent(AlarmActivity.this, MainActivity.class);
                                        PendingIntent pIntent = PendingIntent.getActivity(AlarmActivity.this, key + i, intent, 0);
                                        alarmManager.cancel(pIntent);
                                    }
                                }
                            }
                        })
                        .setNegativeButton(R.string.dialog_cancel, null)
                        .setCancelable(false)
                        .show();
            }
        });

        SharedPreferences sharedPreferences = getSharedPreferences("shared", MODE_PRIVATE);
        Log.d(TAG, sharedPreferences.getAll().toString());
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        SharedPreferences sharedPreferences = getSharedPreferences("shared", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("First", "First");
        for (Map.Entry<Integer, boolean[]> pair : selecetedItems.entrySet()) {
            Integer key = pair.getKey();
            boolean[] items = pair.getValue();
            editor.putInt("key", key);
//            editor.putBoolean()
        }
        editor.commit();
    }

//    public void setStringArrayPref(Context context, String key, boolean[] values) {
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
//        SharedPreferences.Editor editor = prefs.edit();
//        JSONArray arr = new JSONArray();
//        Gson gson =new GsonBuilder().create();
//        for (int i = 0; i < values.length; i++) {
//            String string = gson.toJson(values[i]);
//            arr.put(string);
//        }
//        if (!(values.length == 0)) {
//            editor.putString(key, arr.toString());
//        } else {
//            editor.putString(key, null);
//        }
//        editor.apply();
//    }

//    public boolean[] getStringArrayPref_item(Context context, String key) {
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
//        String json = prefs.getString(key, null);
//        boolean[] OrderDatas = new boolean[4];
//        Gson gson =new GsonBuilder().create();
//        if (json != null) {
//            try {
//                JSONArray a = new JSONArray(json);
//                for (int i = 0; i < a.length(); i++) {
//                    boolean[] orderData = gson.fromJson(a.get(i).toString(), boolean[]);
//                    OrderDatas.add(orderData);
//                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//        return OrderDatas;
//    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnToday:
                calendarView.setDate(System.currentTimeMillis());
                break;
        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);       // strings.xml 에 채널명 기록
            String description = getString(R.string.channel_description);       // strings.xml에 채널 설명 기록
            int importance = NotificationManager.IMPORTANCE_DEFAULT;    // 알림의 우선순위 지정
            NotificationChannel channel = new NotificationChannel(getString(R.string.CHANNEL_ID), name, importance);    // CHANNEL_ID 지정
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);  // 채널 생성
            notificationManager.createNotificationChannel(channel);
        }
    }



}
