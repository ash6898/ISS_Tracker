package com.example.isstracker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static android.content.Context.ALARM_SERVICE;

public class NotifyRecyclerViewAdapter extends RecyclerView.Adapter<NotifyRecyclerViewAdapter.ViewHolder> {

    private Context context;
    private ArrayList<String> durationList;
    private ArrayList<String> timestampList;
    private long timeleft;
    private PendingIntent sender;
    private AlarmManager am;
    private Intent intent;
    private SharedPreferences switchState;
    private Calendar cal;

    NotifyRecyclerViewAdapter(Context context, ArrayList<String> duration, ArrayList<String> timestamp){
        this.context = context;
        durationList = duration;
        timestampList = timestamp;
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);

        Log.d("cyclee", "onViewDetach");
        if(holder.timer != null){
            holder.timer.cancel();
        }
    }

    @NonNull
    @Override
    public NotifyRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.notify_recycler_view, viewGroup,false);

        switchState = context.getSharedPreferences("switchState", Context.MODE_PRIVATE);

        return new NotifyRecyclerViewAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final NotifyRecyclerViewAdapter.ViewHolder viewHolder, int i) {
        final int position = i;

        viewHolder.duration.setText(durationList.get(i));
        viewHolder.timestamp.setText(timestampList.get(i));

        viewHolder.notify_switch.setChecked(switchState.getBoolean(String.valueOf(position), false));

        if (viewHolder.timer != null) {
            viewHolder.timer.cancel();
        }

        if(viewHolder.notify_switch.isChecked()){
            Log.d("checkk", "checked");
            //SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy\nh:mm a", Locale.ENGLISH);
            Date datee = new Date();

            cal = Calendar.getInstance();
            // add 2 minutes to the calendar object
            cal.setTime(datee);
            cal.add(Calendar.MINUTE, 2+position);

            long timee = cal.getTimeInMillis();

            try{
                viewHolder.timer = new CountDownTimer(timee-System.currentTimeMillis(), 1000*60){
                    @Override
                    public void onTick(long millisecondsUntilFinished) {
                        timeleft = millisecondsUntilFinished;
                        long hrs = TimeUnit.MILLISECONDS.toHours(timeleft);
                        long min = TimeUnit.MILLISECONDS.toMinutes(timeleft) -
                                TimeUnit.HOURS.toMinutes(hrs);

                        Log.d("millii", "in Resume " + position + " " + timeleft/1000);

                        String notifyTxt;
                        if(hrs == 0){
                            if(millisecondsUntilFinished < 1000 * 120){
                                notifyTxt = "Notify in less than 2 minute";
                            }
                            else{
                                notifyTxt = String.format(Locale.ENGLISH,"Notify in %2d minutes", min);
                            }
                        }
                        else{
                            notifyTxt = String.format(Locale.ENGLISH,"Notify in %2d hours %2d minutes", hrs, min);
                        }
                        //String formattedTimer = String.format(Locale.ENGLISH,"%l:%02l", hr,min);
                        viewHolder.notify_txt.setText(notifyTxt);
                    }

                    @Override
                    public void onFinish() {
                        viewHolder.notify_txt.setText("ISS is now above you");
                        viewHolder.notify_switch.setChecked(false);
                        viewHolder.notify_switch.setVisibility(View.INVISIBLE);
                        SharedPreferences.Editor editor = context.getSharedPreferences("switchState", Context.MODE_PRIVATE).edit();
                        editor.putBoolean(String.valueOf(position),false);
                        //Log.d("adapterPos", position+"");
                        editor.apply();
                    }
                };
                viewHolder.timer.start();
            }
            catch (NullPointerException npe){
                Log.d("npe", npe.getMessage());
            }
        }
        else{
            Log.d("checkk", "not checked");
        }

        viewHolder.notify_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){

                    Log.d("runn", "runningggg.....");

                    SharedPreferences.Editor editor = context.getSharedPreferences("switchState", Context.MODE_PRIVATE).edit();
                    editor.putBoolean(String.valueOf(position),true);
                    editor.apply();

                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy\nh:mm a", Locale.ENGLISH);
                    Date date = null;
                    Date date1 = null;
                    try {
                        //date = sdf.parse(timestampList.get(position));
                        date1 = sdf.parse("14-09-2020\n12:35 PM");
                        date = new Date();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    //sdf.getCalendar()
                    // get a Calendar object with current time
                    cal = Calendar.getInstance();
                    // add 5 minutes to the calendar object
                    cal.setTime(date);
                    cal.add(Calendar.MINUTE, 2+position);

                    long time = cal.getTimeInMillis();

                    intent = new Intent(context, NotifyAlarmReceiver.class);
                    intent.putExtra("alarm_message", "notify " + position);
                    // In reality, you would want to have a static variable for the request code instead of 192837
                    sender = PendingIntent.getBroadcast(context, position, intent,    PendingIntent.FLAG_ONE_SHOT);

                    // Get the AlarmManager service
                    am = (AlarmManager) context.getSystemService(ALARM_SERVICE);
                    am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), sender);

                    Log.d("alarmSet", position + " alarmSet");
                    //String timeLeftMsg = timeleft/1000 + "";
                    //viewHolder.notify_txt.setText(timeLeftMsg);

                    viewHolder.timer = new CountDownTimer(time-System.currentTimeMillis(), 1000*60){
                        @Override
                        public void onTick(long millisecondsUntilFinished) {
                            timeleft = millisecondsUntilFinished;
                            long hrs = TimeUnit.MILLISECONDS.toHours(timeleft);
                            long min = TimeUnit.MILLISECONDS.toMinutes(timeleft) -
                                    TimeUnit.HOURS.toMinutes(hrs);

                            Log.d("millii", position + " " + (timeleft/1000));

                            String notifyTxt;
                            if(hrs == 0){
                                if(millisecondsUntilFinished < 1000 * 120){
                                    notifyTxt = "Notify in less than 2 minute";
                                }
                                else{
                                    notifyTxt = String.format(Locale.ENGLISH,"Notify in %2d minutes", min);
                                }
                            }
                            else{
                                notifyTxt = String.format(Locale.ENGLISH,"Notify in %2d hours %2d minutes", hrs, min);
                            }
                            //String formattedTimer = String.format(Locale.ENGLISH,"%l:%02l", hr,min);
                            viewHolder.notify_txt.setText(notifyTxt);
                        }

                        @Override
                        public void onFinish() {
                            viewHolder.notify_txt.setText("ISS is now above you");
                            viewHolder.notify_switch.setChecked(false);
                            viewHolder.notify_switch.setVisibility(View.INVISIBLE);
                            SharedPreferences.Editor editor = context.getSharedPreferences("switchState", Context.MODE_PRIVATE).edit();
                            editor.putBoolean(String.valueOf(position),false);
                            editor.apply();
                        }
                    };
                    try{
                        viewHolder.timer.start();
                    }
                    catch (NullPointerException npe){
                        Log.d("npe", npe.getMessage());
                    }
                }
                else{
                    SharedPreferences.Editor editor = context.getSharedPreferences("switchState", Context.MODE_PRIVATE).edit();
                    editor.putBoolean(String.valueOf(position),false);
                    editor.apply();

                    viewHolder.notify_txt.setText("off");

                    if(viewHolder.timer != null){
                        viewHolder.timer.cancel();
                    }

                    Intent intent = new Intent(context, NotifyAlarmReceiver.class);
                    intent.putExtra("alarm_message", "notify " + position);
                    // In reality, you would want to have a static variable for the request code instead of 192837
                    sender = PendingIntent.getBroadcast(context, position, intent, PendingIntent.FLAG_ONE_SHOT);
                    am = (AlarmManager) context.getSystemService(ALARM_SERVICE);

                    if(am != null) {
                        am.cancel(sender);
                        sender.cancel();
                    }

                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return durationList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        TextView duration, timestamp, notify_txt;
        Switch notify_switch;
        CountDownTimer timer;

        ViewHolder(@NonNull final View itemView) {
            super(itemView);

            duration = itemView.findViewById(R.id.duration_value);
            timestamp = itemView.findViewById(R.id.timestamp_value);
            notify_switch = itemView.findViewById(R.id.notify_switch);
            notify_txt = itemView.findViewById(R.id.notify_txt);
        }

    }
}