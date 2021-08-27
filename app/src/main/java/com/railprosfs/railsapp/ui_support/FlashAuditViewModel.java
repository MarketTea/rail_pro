package com.railprosfs.railsapp.ui_support;

import android.net.Uri;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;

public class FlashAuditViewModel extends ViewModel {
    private Uri mPhotoUri;
    private ArrayList<PictureField> mPictureList;

    public FlashAuditViewModel(){
        mPictureList = new ArrayList<>();
    }

    public ArrayList<PictureField> getmPictureList() {
        return mPictureList;
    }

    public Uri getmPhotoUri() { return mPhotoUri; }
    public void setmPhotoUri(Uri value) {
        mPhotoUri = value;
    }

}
