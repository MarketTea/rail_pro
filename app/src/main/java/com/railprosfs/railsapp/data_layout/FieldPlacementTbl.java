package com.railprosfs.railsapp.data_layout;


import android.view.View;

import com.railprosfs.railsapp.BR;

import androidx.annotation.NonNull;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class FieldPlacementTbl extends BaseObservable {

    @NonNull
    @PrimaryKey(autoGenerate = true)
    public int Id;                      //Primary Key
    public int TemplateId;
    public String TemplateName;
    public int FieldId;
    public String FieldType;
    public String FieldPrompt;
    public String FieldInstructions;
    public String FieldOptions;
    public boolean Required;
    public int Group;
    public String Note;
    public String Code;


    @Bindable
    public boolean isRequired() {
        return Required;
    }

    public FieldPlacementTbl setRequired(boolean required) {
        Required = required;
        notifyPropertyChanged(BR.required);
        notifyPropertyChanged(BR.fieldPrompt);
        return this;
    }

    @Bindable
    public String getFieldInstructions() {
        if (FieldInstructions == null) {
            return "";
        }
        return FieldInstructions;
    }

    @Bindable
    public int getFieldPromptVisibility() {
        if (FieldPrompt != null && !FieldPrompt.equals("")) {
            return View.VISIBLE;
        } else {
            return View.GONE;
        }
    }

    @Bindable
    public String getFieldPrompt() {
        if (FieldPrompt == null) {
            return "";
        }

        if (Required) {
            return FieldPrompt + "*";
        }
        return FieldPrompt;
    }

    @Bindable
    public int getFieldInstructionVisibility() {
        if (FieldInstructions != null && !FieldInstructions.equals("")) {
            return View.VISIBLE;
        } else {
            return View.GONE;
        }
    }

}
