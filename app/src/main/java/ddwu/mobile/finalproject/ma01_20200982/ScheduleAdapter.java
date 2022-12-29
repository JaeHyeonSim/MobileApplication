package ddwu.mobile.finalproject.ma01_20200982;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ScheduleAdapter extends ArrayAdapter<ScheduleDto> {
    private Context context;
    private int layout;
    private ArrayList<ScheduleDto> scheduleList;
    private LayoutInflater layoutInflater;

    public ScheduleAdapter(Context context, int layout, ArrayList<ScheduleDto> scheduleList) {
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

            TextView tvScheduleName = convertView.findViewById(R.id.tvScheduleName);
            TextView tvMeetDate = convertView.findViewById(R.id.tvMeetDate);

            tvScheduleName.setText(scheduleList.get(position).getName());
            tvMeetDate.setText(scheduleList.get(position).getMeet_date().toString());
        }

        return convertView;
    }

}
