package ddwu.mobile.finalproject.ma01_20200982;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ScheduleTodayAdapter extends ArrayAdapter<ScheduleDto> {
    private Context context;
    private int layout;
    private ArrayList<ScheduleDto> scheduleList;
    private LayoutInflater layoutInflater;

    public ScheduleTodayAdapter(Context context, int layout, ArrayList<ScheduleDto> scheduleList) {
        super(context, layout, scheduleList);
        this.context = context;
        this.layout = layout;
        this.scheduleList = scheduleList;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return scheduleList.size();
    }

    @Override
    public ScheduleDto getItem(int i) {
        return scheduleList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return scheduleList.get(i).getSchedule_id();
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup viewGroup) {
        final int position = pos;

        if (convertView == null) {
            convertView = layoutInflater.inflate(layout, viewGroup, false);

            ImageView ivThumbnail = convertView.findViewById(R.id.ivThumbnail);
            TextView tvScheduleName = convertView.findViewById(R.id.tvScheduleName);
            TextView tvMeetTime = convertView.findViewById(R.id.tvMeetTime);
            TextView tvAddress = convertView.findViewById(R.id.tvAddress);

            ivThumbnail.setImageResource(R.drawable.sommeet_logo);
            tvScheduleName.setText(scheduleList.get(position).getName());
            tvMeetTime.setText(scheduleList.get(position).getTime_hour() + " : " + scheduleList.get(position).getTime_minute());
            tvAddress.setText(scheduleList.get(position).getPlace_title());
        }

        return convertView;
    }

}
