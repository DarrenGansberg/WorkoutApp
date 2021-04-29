package com.darrenganberg.workoutapp;

import androidx.annotation.NonNull;

import java.util.Vector;

//Utility class
//Merely defined in this file for purposes of demo app. Should be extracted and defined
//in its own .java file.
public class Time {

    long interval;
    UNITS units;

    public enum UNITS {Milliseconds}

    //duration = a period of time measured in milliseconds
    public Time(long interval, UNITS units) throws UnsupportedOperationException {

        if (units == UNITS.Milliseconds) {
            this.units = UNITS.Milliseconds;
            this.interval = interval;
            return;
        }

        throw new UnsupportedOperationException();

    }

    public void addMilliseconds(long interval) throws UnsupportedOperationException {
        if (this.units == UNITS.Milliseconds)
        {
            this.interval += interval;
            return;
        }
        throw new UnsupportedOperationException();
    }

    public long millisPerHour() {
        // milliSecondsPerSecond (1000) * SecondsPerMin (60) * MinsPerHour (60)
        return 3600000;
    }

    public long millisPerMin() {
        // milliSecondsPerSecond (1000) * SecondsPerMin (60)
        return 60000;
    }

    public long millisPerSecond() {
        // milliSecondsPerSecond (1000)
        return 1000;
    }

    public long millisPerDecisecond() {
        return 100;
    }

    public long millisPerCentisecond() {
        return 10;
    }

    //Assumes interval value measured in milliseconds
    public long getHoursInInterval(long interval) {
        return (interval / millisPerHour());
    }

    //Assumes interval value measured in milliseconds
    public long getMinutesInInterval(long interval) {
        return (interval / millisPerMin());
    }

    //Assumes interval value measured in milliseconds
    public long getSecondsInInterval(long interval) {
        return (interval / millisPerSecond());
    }

    //Assumes interval value measured in milliseconds
    public long getDecisecondsInInterval(long interval) {
        return (interval / millisPerDecisecond());
    }

    //Assumes duration a value of time measured in milliseconds
    public long getCentisecondsInInterval(long interval) {
        return (interval / millisPerCentisecond());
    }

    @NonNull
    @Override
    public String toString() {
        Vector<Long> decomposedTime = decomposeTime();
        StringBuilder sb = new StringBuilder();
        for (int i = 0, elements = decomposedTime.size(); i < elements; i++) {
            if (i > 0) {
                if (i == elements - 1) {
                    sb.append(".");
                } else {
                    sb.append(":");
                }
            }
            long value = decomposedTime.elementAt(i);
            if (value < 10) {
                sb.append("0");
            }
            sb.append(value);
        }
        return sb.toString();
    }

    private Vector<Long> decomposeTime() {
        Vector<Long> time = new Vector<>();
        long rem = interval;
        time.add(getHoursInInterval(rem));
        rem = rem - (time.elementAt(0) * millisPerHour());

        time.add(getMinutesInInterval(rem));
        rem = rem - (time.elementAt(1) * millisPerMin());

        time.add(getSecondsInInterval(rem));
        rem = rem - (time.elementAt(2) * millisPerSecond());

        time.add(getCentisecondsInInterval(rem));
        rem = rem - (time.elementAt(3) * millisPerCentisecond());
        return time;
    }
}