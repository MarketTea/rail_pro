package com.railprosfs.railsapp.data_layout;

public class Metrics {
    public long Saves;      // Number of times a form is saved.
    public long ScreenTime; // Amount of time (in msec) spent on the screen.

    public Metrics(long saves, long screenTime){
        Saves = saves;
        ScreenTime = screenTime;
    }
    public void AddSave() { Saves++; }
    public void AddScreenTime(long value) { ScreenTime += value; }
}
