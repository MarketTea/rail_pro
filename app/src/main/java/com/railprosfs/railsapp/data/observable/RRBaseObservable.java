package com.railprosfs.railsapp.data.observable;

import androidx.databinding.BaseObservable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.Serializable;

/**
 * Created by Son Dinh on 3/7/2016.
 */
public class RRBaseObservable extends BaseObservable {
    public String toJSON(){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }
}
