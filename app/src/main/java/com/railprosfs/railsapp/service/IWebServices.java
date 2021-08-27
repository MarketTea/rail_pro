package com.railprosfs.railsapp.service;

import android.content.Context;

import com.railprosfs.railsapp.utility.ExpClass;

/**
 *  This interface allows for the substitution of a mack api provider.
 */
public interface IWebServices {

    public boolean IsNetwork(Context context);

    public <U> U CallGetApi(String path, Class<U> typeU, String token) throws ExpClass;

    public <T> void CallPostApi(String path, final T input, String token) throws ExpClass;

    public <T, U> U CallPostApi(String path, final T input, Class<U> typeU, String token) throws ExpClass;

    public WebServiceModels.LoginResponse CallLoginApi(String path, String user, String password) throws ExpClass;
}
