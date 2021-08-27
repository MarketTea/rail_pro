package com.railprosfs.railsapp.ui_support;

import com.railprosfs.railsapp.dialog.JobPickerDialog;

public interface FragmentTalkBack extends JobPickerDialog.JobPickerListener {
    void setTimePicker(String totalTime, String startTime, String endTime, int id);
    void setSignatureImage(String imageName, int id);
    void setPictureOnClick(int position);
    void unlockOrientation();
    void lockOrientation();
    void simplePickerResponse(int source, int selection);
    void simpleConfirmResponse(int message);
    void simpleListResponse(int source, int selection);
}
