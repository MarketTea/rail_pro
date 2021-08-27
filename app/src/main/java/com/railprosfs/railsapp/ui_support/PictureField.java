package com.railprosfs.railsapp.ui_support;

import android.net.Uri;

public class PictureField {
    public Uri pictureURI;
    public String description;
    public int rotation;

    public PictureField(Uri pictureURI, String description, int rotation) {
        this.pictureURI = pictureURI;
        this.description = description;
        this.rotation = rotation;
    }

}
