package com.darrenganberg.workoutapp;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    private class StartButtonHandler implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            onStart(v);
        }
    }

    private class PauseButtonHandler implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            onPause(v);
        }
    }

    private class StopButtonHandler implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            onStop(v);
        }
    }

    public enum Timer { STOPPED, PAUSED, RUNNING }

    private Timer timerMode = Timer.STOPPED;
    long interval = 0;

    AlarmManager am;
    Intent intent;
    PendingIntent pendingIntent;
    IntentFilter intentFilter;
    private BroadcastReceiver receiver = null;
    public static final String CLOCK_TICK = "com.darrenganberg.workoutapp.CLOCK_TICK";

    String lastWorkout = "";
    long lastWorkoutDuration = 0;


    long startTime;
    long currentTime;

    private SharedPreferences appPreferences;


    //Activates a timer that is used by the activity to keep track of the workout time.
    private void activateTimer()
    {
        intent = new Intent(MainActivity.CLOCK_TICK);
        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME, 10,10,pendingIntent);
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getAction().equals(MainActivity.CLOCK_TICK))
                {
                    currentTime = SystemClock.elapsedRealtime();
                    interval = currentTime - startTime;
                    updateTimerText();
                }
            }
        };
        intentFilter = new IntentFilter();
        intentFilter.addAction(MainActivity.CLOCK_TICK);
        registerReceiver(receiver, intentFilter);
        timerMode = Timer.RUNNING;
    }

    private int TimerModeToInteger(Timer mode)
    {
        switch (mode)
        {
            case PAUSED:
                return 1;
            case RUNNING:
                return 2;
            default:
                return 0;
        }
    }

    private Timer IntegerToTimerMode(int mode)
    {
        switch(mode)
        {
            case 1:
                return Timer.PAUSED;
            case 2:
                return Timer.RUNNING;
            default:
                return Timer.STOPPED;
        }
    }

    protected void init()
    {
        registerClickHandlers();
        loadPersistedData();
        updateLastWorkoutText();
        //reactivate the timer if we're running.
        if (timerMode == Timer.RUNNING)
        {
            activateTimer();
        }
    }

    //Retrieves application persisted data relating to the last workout.
    protected void loadPersistedData()
    {
        appPreferences = getSharedPreferences("com.darrenganberg.workoutapp", MODE_PRIVATE);
        lastWorkout = appPreferences.getString("lastWorkout", "");
        lastWorkoutDuration = appPreferences.getLong("lastWorkoutDuration", 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null)
        {
            //restore what needs to be restored
            interval = savedInstanceState.getLong("interval");
            timerMode = IntegerToTimerMode(savedInstanceState.getInt("timerMode", 0));
            startTime = savedInstanceState.getLong("startTime");
            currentTime = savedInstanceState.getLong("currentTime");
        }

        init();
        updateTimerText();
    }

    //Handles a click of the Pause Button in the Activity.
    public void onPause(View view) {

        if (timerMode == Timer.RUNNING)
        {
            am.cancel(pendingIntent);
            unregisterReceiver(receiver);
        }

        timerMode = Timer.PAUSED;

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("interval", interval);
        outState.putInt("timerMode", TimerModeToInteger(timerMode));
        outState.putLong("startTime", startTime);
        outState.putLong("nowTime", currentTime);
    }

    //Handles a click of the Start Button in the Activity.
    public void onStart(View view) {

        AppCompatEditText workoutTypeInput = findViewById(R.id.workout_type_textInput);
        String inputText = workoutTypeInput.getText().toString();
        if (inputText.equals("") || inputText.equals(null) )
        {
            Toast.makeText(this, "Enter workout name before starting", Toast.LENGTH_SHORT).show();
            return;
        }
        if (timerMode != Timer.RUNNING)
        {
            currentTime = SystemClock.elapsedRealtime();
            if (!(timerMode == Timer.PAUSED))
            {
                startTime = currentTime;
            }
            activateTimer();
        }
    }

    //Handles a click of the Stop Button in the Activity.
    public void onStop(View view){

        if (timerMode == Timer.RUNNING)
        {
            am.cancel(pendingIntent);
            unregisterReceiver(receiver);
            receiver = null;
            saveLastWorkout();
            updateLastWorkoutText();

        } else if (timerMode == Timer.STOPPED)
        {
            //causes the visibile time to reset.
            //only performed if the timer is already stopped.
            updateTimerText();
        }

        interval = 0; //reset the interval
        timerMode = Timer.STOPPED;
    }


    //Registers the callbacks/listeners to handle UI element interaction (i.e. clicks)
    protected void registerClickHandlers()
    {
        registerClickHandler(findViewById(R.id.startButton), new StartButtonHandler());
        registerClickHandler(findViewById(R.id.pauseButton), new PauseButtonHandler());
        registerClickHandler(findViewById(R.id.stopButton), new StopButtonHandler());
    }

    protected void registerClickHandler(View v, View.OnClickListener handler)
    {
        v.setOnClickListener(handler);
    }


    //Persist the data for the last workout using shared preferences.
    private void saveLastWorkout()
    {
        AppCompatEditText workoutTypeInput = findViewById(R.id.workout_type_textInput);
        lastWorkout = workoutTypeInput.getText().toString();
        lastWorkoutDuration = interval;
        SharedPreferences.Editor editor = appPreferences.edit();
        editor.putString("lastWorkout", lastWorkout);
        editor.putLong("lastWorkoutDuration", lastWorkoutDuration);
        editor.commit();
    }

    //Update the text view that displays the details relating to the last workout
    protected void updateLastWorkoutText()
    {
        if (lastWorkout.equals("")  && (lastWorkoutDuration == 0))
        {
            AppCompatTextView lastWorkoutView = findViewById(R.id.lastWorkoutTextView);
            lastWorkoutView.setText(R.string.last_workout_duration_default);
        }
        else if (!lastWorkout.equals("") && (!(lastWorkoutDuration == 0)))
        {
            AppCompatTextView lastWorkoutView = findViewById(R.id.lastWorkoutTextView);
            String outputText = getString(R.string.last_workout_duration);
            Time t = new Time(lastWorkoutDuration, Time.UNITS.Milliseconds);
            outputText = String.format(outputText, t.toString(), lastWorkout);
            lastWorkoutView.setText(outputText);
        }
    }

    //Update the TextView that displays the workout time.
    private void updateTimerText()
    {
        AppCompatTextView timerTextView = findViewById(R.id.workoutTimeTextView);
        Time t = new Time(this.interval, Time.UNITS.Milliseconds);
        timerTextView.setText(t.toString());
    }





}







