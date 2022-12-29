package ddwu.mobile.finalproject.ma01_20200982;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class ScheduleActivity extends AppCompatActivity {

    private final String TAG = "ScheduleActivity";
    final static int INSERT_ACTIVITY_CODE = 200;
    final static int UPDATE_ACTIVITY_CODE = 201;

    private ListView lvSchedules = null;
    private ListView lvScheduleToday = null;

    private ScheduleAdapter listAdapter;
    private ScheduleTodayAdapter todayAdapter;

    private ArrayList<ScheduleDto> scheduleList;
    private ArrayList<ScheduleDto> scheduleToday;

    ScheduleDB db;
    ScheduleDao dao;

    private CompositeDisposable mDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        scheduleList = new ArrayList<ScheduleDto>();
        scheduleToday = new ArrayList<ScheduleDto>();

        lvSchedules = (ListView)findViewById(R.id.lvSchedules);
        lvScheduleToday = (ListView)findViewById(R.id.lvScheduleToday);
        listAdapter = new ScheduleAdapter(this, R.layout.custom_schedule_view, scheduleList);
        todayAdapter = new ScheduleTodayAdapter(this, R.layout.custom_today_view, scheduleToday);

        lvSchedules.setAdapter(listAdapter);
        lvScheduleToday.setAdapter(todayAdapter);

        db = ScheduleDB.getDatabase(this);
        dao = db.scheduleDao();

        lvSchedules.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                Intent intent = new Intent(ScheduleActivity.this, UpdateScheduleActivity.class);

                ScheduleDto schedule = (ScheduleDto)lvSchedules.getAdapter().getItem(pos);
                intent.putExtra("schedule", schedule);

                startActivityForResult(intent, UPDATE_ACTIVITY_CODE);
                onResume();
            }
        });

        lvSchedules.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ScheduleActivity.this);
                builder.setTitle(R.string.dialog_remove_title)
                        .setMessage("'" + scheduleList.get(pos).getName() + "'\n" + getString(R.string.dialog_remove_message))
                        .setPositiveButton(R.string.dialog_remove_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ScheduleDto schedule = (ScheduleDto)lvSchedules.getAdapter().getItem(pos);
                                Completable deleteResult = dao.deleteSchedule(schedule);
                                mDisposable.add(
                                        deleteResult.subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(() -> Log.d(TAG, "Delete success: "),
                                                        throwable -> Log.d(TAG, "error"))
                                );
                            }
                        })
                        .setNegativeButton(R.string.dialog_cancel, null)
                        .setCancelable(false)
                        .show();
                return true;
            }
        });

        Switch aSwitch = (Switch) findViewById(R.id.switch_before);
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    listAdapter.clear();
                    Flowable<List<ScheduleDto>> resultSchedules = dao.findAllSchedules();
                    mDisposable.add (
                            resultSchedules.subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(schedules -> {
//                                                for (ScheduleDto aSchedule : schedules) {
//                                                    Log.i(TAG, aSchedule.toString());
//                                                }
                                                listAdapter.clear();
                                                listAdapter.addAll(schedules);
                                            },
                                            throwable -> Log.d(TAG, "error", throwable))
                    );
                } else {
                    listAdapter.clear();
                    onResume();
                }
            }
        });

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add_schedule:
                Intent intent = new Intent(this, InsertScheduleActivity.class);
                if (intent != null) startActivity(intent);
                break;
//            case R.id.btnKakaolink:
//                try {
//                    KakaoLink kakaoLink = KakaoLink.getKakaoLink(getApplicationContext());
//                    KakaoTalkLinkMessageBuilder messageBuilder = kakaoLink.createKakaoTalkLinkMessageBuilder();
//                    messageBuilder.addText("카카오톡으로 공유해요.");
//                    kakaoLink.sendMessage(messageBuilder,getApplicationContext());
//                } catch (KakaoParameterException e) {
//                    e.printStackTrace();
//                }
//                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
        String today = date.format(new Date(System.currentTimeMillis()));
        Flowable<List<ScheduleDto>> resultSchedules = dao.findSchedulesAfter(Date.valueOf(today));
        mDisposable.add (
                resultSchedules.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(schedules -> {
//                                    for (ScheduleDto aSchedule : schedules) {
//                                        Log.i(TAG, aSchedule.toString());
//                                    }
                                    listAdapter.clear();
                                    listAdapter.addAll(schedules);
                                },
                                throwable -> Log.d(TAG, "error", throwable))
        );

        Flowable<List<ScheduleDto>> resultTodaySchedules = dao.findScheduleToday(Date.valueOf(today));
        mDisposable.add (
                resultTodaySchedules.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(schedules -> {
                                    for (ScheduleDto aSchedule : schedules) {
                                        Log.i(TAG, aSchedule.toString());
                                    }
                                    todayAdapter.clear();
                                    todayAdapter.addAll(schedules);
                                },
                                throwable -> Log.d(TAG, "error", throwable))
        );

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDisposable.clear();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case INSERT_ACTIVITY_CODE:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this, "생성 성공", Toast.LENGTH_SHORT).show();
                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "생성 취소", Toast.LENGTH_SHORT).show();
                }
                break;

            case UPDATE_ACTIVITY_CODE:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this, "수정 성공", Toast.LENGTH_SHORT).show();
                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "수정 취소", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

}
