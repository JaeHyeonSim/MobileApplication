package ddwu.mobile.finalproject.ma01_20200982;


import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.sql.Date;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class UpdateScheduleActivity extends Activity {

    private final String TAG = "UpdateScheduleActivity";

    EditText etName;
    EditText etParty;
    EditText etDate;
    EditText etTime;
    EditText etPlaceTitle;

    ListView lvSearchResult;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> addrStringList;
    String resultAddr = "";

    Calendar calendar;
    Time selectedTime;

    ScheduleDB db;
    ScheduleDao dao;
    ScheduleDto schedule_upd;

    Intent intent;

    private final CompositeDisposable mDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_schedule);

        etName = (EditText)findViewById(R.id.etNameUpdate);
        etParty = (EditText)findViewById(R.id.etPartyUpdate);
        etDate = (EditText)findViewById(R.id.etDateUpdate);
        etTime = (EditText)findViewById(R.id.etTimeUpdate);
        etPlaceTitle = (EditText)findViewById(R.id.etPlaceTitleUpdate);

        lvSearchResult = findViewById(R.id.lv_searchResult_update);
        addrStringList = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, addrStringList);
        lvSearchResult.setAdapter(adapter);

        calendar = Calendar.getInstance();
        selectedTime = new Time(0);

        db = ScheduleDB.getDatabase(this);
        dao = db.scheduleDao();

        intent = getIntent();
        schedule_upd = (ScheduleDto)intent.getSerializableExtra("schedule");

        selectedTime.setHours(schedule_upd.getTime_hour());
        selectedTime.setMinutes(schedule_upd.getTime_minute());

        etName.setText(schedule_upd.getName());
        etParty.setText(String.valueOf(schedule_upd.getParty()));
        etDate.setText(schedule_upd.getMeet_date().toString());
        etTime.setText(schedule_upd.getTime_hour() + " : " + schedule_upd.getTime_minute());
        etPlaceTitle.setText(schedule_upd.getPlace_title());

        etDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(UpdateScheduleActivity.this, datePicker,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        etTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(UpdateScheduleActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        etTime.setText(" " + selectedHour + " : " + selectedMinute);
                        selectedTime.setHours(selectedHour);
                        selectedTime.setMinutes(selectedMinute);
                    }
                }, hour, minute, true);
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            }
        });

        lvSearchResult.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                String result = addrStringList.get(pos);
                String placeTitle = result.split(" \\(")[0];
                resultAddr = result.split("\\(")[1].split("\\)")[0];
                etPlaceTitle.setText(placeTitle);
            }
        });
    }

    DatePickerDialog.OnDateSetListener datePicker = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            updateLabel();
        }
    };

    private void updateLabel() {
        String myFormat = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.KOREA);
        etDate.setText(sdf.format(calendar.getTime()));
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_searchPlace_update:
                try {
                    addrStringList = executeReverseGeocoding(etPlaceTitle.getText().toString());
                    Log.d(TAG, addrStringList.toString());
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (addrStringList.size() != 0) {
                    adapter.clear();
                    adapter.addAll(addrStringList);
                }
                break;
            case R.id.btnUpdateScheduleSave:
                String name = etName.getText().toString();
                String party = etParty.getText().toString();
                String date = etDate.getText().toString();
                String placeTitle = etPlaceTitle.getText().toString();
                String address = schedule_upd.getAddress();
                if (!resultAddr.equals("")) {
                    address = resultAddr;
                }

                if (etName.length() == 0 || etParty.length() == 0 || etDate.length() == 0
                        || etTime.length() == 0 || etPlaceTitle.length() == 0) {
                    Toast.makeText(UpdateScheduleActivity.this, "일정 정보를 모두 입력하세요.", Toast.LENGTH_SHORT).show();
                } else {
                    schedule_upd.setName(name);
                    schedule_upd.setParty(Integer.parseInt(party));
                    schedule_upd.setMeet_date(Date.valueOf(date));
                    schedule_upd.setTime_hour(selectedTime.getHours());
                    schedule_upd.setTime_minute(selectedTime.getMinutes());
                    schedule_upd.setPlace_title(placeTitle);
                    schedule_upd.setAddress(address);

                    Completable updateResult = dao.updateSchedule(schedule_upd);
                    mDisposable.add(
                            updateResult.subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(() -> Log.d(TAG, "Update success: "),
                                            throwable -> Log.d(TAG, "error"))
                    );
                    setResult(RESULT_OK);
                    finish();
                }
                break;
            case R.id.btnUpdateScheduleClose:
                setResult(RESULT_CANCELED);
                finish();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDisposable.clear();
    }

    private ArrayList<String> executeReverseGeocoding(String string) throws ExecutionException, InterruptedException {
        if (Geocoder.isPresent()) {
            if (string != null) {
                return new ReverseGeoTask().execute(string).get();
            }
        } else {
            Toast.makeText(this, "No Geocoder", Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    class ReverseGeoTask extends AsyncTask<String, Void, ArrayList<String>> {
        Geocoder geocoder = new Geocoder(UpdateScheduleActivity.this, Locale.getDefault());
        @Override
        protected ArrayList<String> doInBackground(String... strings) {
            List<Address> addresses;
            ArrayList<String> addrString = new ArrayList<String>();
            try {
                addresses = geocoder.getFromLocationName(strings[0], 5);
                String[] splits = strings[0].split(" ");
                if (splits.length > 1) {
                    for (String split : splits) {
                        addresses.add(geocoder.getFromLocationName(split, 1).get(0));
                    }
                }
                for (Address address : addresses) {
                    addrString.add(address.getFeatureName().toString() + " (" + address.getAddressLine(0).toString() + ")");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return addrString;
        }

        @Override
        protected void onPostExecute(ArrayList<String> arrayList) {
            return;
        }
    }

}
