package ddwu.mobile.finalproject.ma01_20200982;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MyBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Intent pIntent = new Intent(context, ScheduleActivity.class);
        pIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, pIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,"MY_CHANNEL")
                .setSmallIcon(R.drawable.sommeet_alarm)
                .setContentTitle("알림!")
                .setContentText("일정을 확인하세요.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        int notificationId = 100;
        notificationManager.notify(notificationId, builder.build());

    }
}
