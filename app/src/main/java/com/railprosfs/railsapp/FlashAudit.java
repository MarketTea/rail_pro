package com.railprosfs.railsapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.railprosfs.railsapp.data.dto.Actor;
import com.railprosfs.railsapp.data_layout.JobTbl;
import com.railprosfs.railsapp.dialog.SimpleConfirmDialog;
import com.railprosfs.railsapp.dialog.SimpleDisplayDialog;
import com.railprosfs.railsapp.service.FlashAuditSendThread;
import com.railprosfs.railsapp.ui_support.FlashAuditViewModel;
import com.railprosfs.railsapp.ui_support.FragmentTalkBack;
import com.railprosfs.railsapp.ui_support.PictureAdapter;
import com.railprosfs.railsapp.ui_support.PictureField;
import com.railprosfs.railsapp.utility.ExpClass;
import com.railprosfs.railsapp.utility.Functions;
import com.railprosfs.railsapp.utility.KTime;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static com.railprosfs.railsapp.service.WebServiceModels.*;
import static com.railprosfs.railsapp.utility.Constants.*;

/**
 *  This screen allows an RWIC to submit a photo in support of a Flash Audit.
 *  Note: Some of the camera and file code is documented here https://developer.android.com/training/camera/photobasics#java
 *
 */
public class FlashAudit extends AppCompatActivity implements FragmentTalkBack, LocationListener {
    private RecyclerView photoRV;
    private FlashAuditViewModel flashAuditViewModel;
    private List<TextWatcher> mWatchListeners;
    private EditText pictureComment;
    private PictureAdapter mPicAdapter;
    private Actor user;
    private ProgressBar progressBar;
    private TextView errorText;
    private Location location;

    /*
        This handler is used to as a callback mechanism, such that the Thread
        can alert the Activity when the data has been retrieved.
    */
    private static class MsgHandler extends Handler {
        private final WeakReference<FlashAudit> mActivity;

        MsgHandler(FlashAudit activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            FlashAudit activity = mActivity.get();
            if (activity != null) {
                Log.d("MARKET_TEA", "msg flash: " + msg.what);
                switch (msg.what) {
                    case WHAT_FLASH_AUDIT:
                        activity.auditSubmitSuccess();
                        break;
                    default:
                        activity.auditSubmitFaield(msg.arg1);
                        break;
                }
            }
        }
    }

    private final MsgHandler mHandler = new MsgHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.flash_audit_layout);

        user = new Actor(this);
        mWatchListeners = new ArrayList<>();
        flashAuditViewModel = new ViewModelProvider(this).get(FlashAuditViewModel.class);

        progressBar = findViewById(R.id.progressbar_horizontal);
        photoRV = findViewById(R.id.recyclePictures);
        pictureComment = findViewById(R.id.inputPictureComment);
        errorText = findViewById(R.id.error_text);

        mPicAdapter = new PictureAdapter(flashAuditViewModel.getmPictureList(), this, this);
        photoRV.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        photoRV.setAdapter(mPicAdapter);


        if (checkMyPermission(MY_PERMISSIONS_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)) {
            //Get Location
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (lm != null) {
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 1, this);
            }
        }
    }

    /**
     *  Simple Confirmation Dialog callback.  The simple confirmation dialog is used by
     *  a number of methods.  This is where the confirmation reply (OK or Cancel) is returned.
     *  The message passed in is used as the key to determine what code needs to run.
     *  NOTE: For confirmations that end up here it means the user pressed OK.
     * @param message   The unique message displayed on the confirmation dialog.
     */
    @Override
    public void simpleConfirmResponse(int message) {

        switch (message) {
            case R.string.msg_confirm_flash:
                submitAudit();
                break;
            case R.string.msg_inform_pictures_max:
                // Informational Only
                break;
        }
    }

    /**
     *  The basic fragment support for showing a simple confirmation dialog.
     * @param title     The resource id of a string to use as a title displayed on the dialog.
     * @param message   The resource id of confirmation message to display to the user.
     */
    private void simpleConfirmRequest(int title, int message, boolean showCancel) {
        // Set up the fragment.
        FragmentManager mgr = getSupportFragmentManager();
        Fragment fragment = mgr.findFragmentByTag(KY_SIMPLE_CONFIRM_FRAG);
        if (fragment != null) { // Clear out the previous use.
            mgr.beginTransaction().remove(fragment).commit();
        }
        // Launch the confirmation.
        DialogFragment submitFrag = new SimpleConfirmDialog(title, message, !showCancel);
        submitFrag.show(mgr, KY_SIMPLE_CONFIRM_FRAG);
    }

    /**
     *  The basic fragment support for showing a simple confirmation dialog.
     * @param title     The resource id of a string to use as a title displayed on the dialog.
     * @param message   The resource id of confirmation message to display to the user.
     */
    private void simpleDisplayRequest(int title, String message) {
        // Set up the fragment.
        FragmentManager mgr = getSupportFragmentManager();
        Fragment fragment = mgr.findFragmentByTag(KY_SIMPLE_DISPLAY_FRAG);
        if (fragment != null) { // Clear out the previous use.
            mgr.beginTransaction().remove(fragment).commit();
        }
        // Launch the confirmation.
        DialogFragment submitFrag = new SimpleDisplayDialog(title, message);
        submitFrag.show(mgr, KY_SIMPLE_DISPLAY_FRAG);
    }

    /*
     *  This confirmation dialog is used to submit the Flash Audit to the backend.
     */
    public void completeAudit(View view) {
        if (mPicAdapter.getItemCount() > 0) {
            simpleConfirmRequest(R.string.title_confirm_flash, R.string.msg_confirm_flash, true);
        } else {
            setErrorText(getString(R.string.flash_audit_error_image));
        }
    }

    /*
     * This informational dialog is used to inform the user that they have reached the max number of photos to upload.
     */
    public void MaxPhotoUploadDialog() {
        simpleConfirmRequest(R.string.title_inform_pictures_max, R.string.msg_inform_pictures_max, false);
    }

    /**
     * Provide a popup dialog to request a permission.  Callback is onRequestPermissionsResult().
     * Could add an explaination dialog, but skipping that for now.
     * @param key           This is the key used to guide the callback code.
     * @param permission    This is the "Manifest.permission.X" where X = permission.
     * @return True if permission already granted.
     */
    private boolean checkMyPermission(int key, String permission) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this, new String[]{permission}, key);

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this, new String[]{permission}, key);
            }
            return false;
        } else {
            return true;
        }
    }

    // Manage permission responses.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_CAMERA:
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    setErrorText(getString(R.string.flash_audit_error_camera));
                }
                break;
            case MY_PERMISSIONS_LOCATION:
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    setErrorText(getString(R.string.flash_audit_error_location));
                } else {
                    LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    if (lm != null) {
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 1, this);
                        }
                    }
                }
                break;
        }
    }

    /*
     *  User presses button to get a picture.
     */
    public void flashPhotoPicker(View view) {
        if (flashAuditViewModel.getmPictureList().size() < IMAGE_FLASH_MAX_PHOTOS) {
            switch (view.getId()) {
                case R.id.picture_audit_button:
                    launchPicturePicker();
                    break;
            }
        } else {
            MaxPhotoUploadDialog();
        }
    }

    /*
     *  Check permissions, create a placeholder file and launch the camera app.
     */
    public void launchPicturePicker() {
        try {
            if (checkMyPermission(MY_PERMISSIONS_CAMERA, Manifest.permission.CAMERA)) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                    File photoFile = Functions.GetTempImageFile(DOC_IMAGE_FLASH, IMAGE_FLASH);
                    flashAuditViewModel.setmPhotoUri(FileProvider.getUriForFile(this, getPackageName() + ".share", photoFile));
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, flashAuditViewModel.getmPhotoUri());
                    startActivityForResult(cameraIntent, KY_CAMERA_RESULT);
                } else {
                    throw new ExpClass(17001, "INFORMATIONAL", "The cameraIntent.resolveActivity failed to find a default camera app to load.", "");
                }
            }
        } catch (Exception ex) {
            ExpClass.LogEX(ex, user.unique + " :" + "launchPicturePicker - General error in method.");
            simpleDisplayRequest(R.string.title_inform_display, getResources().getString(R.string.msg_inform_error_info));
        }
    }

    // Upon Return from Camera.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == KY_CAMERA_RESULT && resultCode == Activity.RESULT_OK) {
            PictureField mNew = new PictureField(flashAuditViewModel.getmPhotoUri(), "", Functions.CalcRotationDegrees(DOC_IMAGE_FLASH, Functions.UriFileName(this, flashAuditViewModel.getmPhotoUri())));
            mPicAdapter.addItem(mNew);
            if (photoRV.getLayoutManager() != null)
                photoRV.getLayoutManager().scrollToPosition(mPicAdapter.getItemCount() - 1);
            photoRV.getParent().requestChildFocus(photoRV, photoRV);
        }
    }

    // Not allowing comments on pictures here, but leave in case they want it later.
    @Override
    public void setPictureOnClick(final int position) {
        if (photoRV.getLayoutManager() != null)
            photoRV.getLayoutManager().scrollToPosition(position);
        final PictureField mField = mPicAdapter.getPictureList().get(position);
        //Remove All Previous Text Listeners
        for (TextWatcher mTextWatcher : mWatchListeners) {
            pictureComment.removeTextChangedListener(mTextWatcher);
        }
        //Create New Text Watcher
        TextWatcher mTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                mPicAdapter.getPictureList().get(position).description = pictureComment.getText().toString();
                mPicAdapter.notifyItemChanged(position);
            }
        };

        pictureComment.setText(mField.description);

        //Add the new listener to the array to be removed later
        mWatchListeners.add(mTextWatcher);
        pictureComment.addTextChangedListener(mTextWatcher);
    }

    // Send the images to the backend.
    public void submitAudit() {

        if (checkMyPermission(MY_PERMISSIONS_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)) {
            if (location == null) {
                //Get Location
                LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (lm != null) {
                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 1, this);
                }
            }
        }

        FlashAuditRequest myRequest = createRequest();
        if (myRequest == null || myRequest.images.length == 0) {
            setErrorText(getString(R.string.flash_audit_error_no_photos));
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        progressBar.setIndeterminate(true);
        setErrorText("");
        FlashAuditSendThread send = new FlashAuditSendThread(this, new Gson(), myRequest, new Messenger(mHandler));
        send.start();
    }

    // Build out something the API can understand.
    private FlashAuditRequest createRequest() {
        try {
            double longitude = 0;
            double latitude = 0;

            if (location != null) {
                longitude = location.getLongitude();
                latitude = location.getLatitude();
                Log.d("MARKET_TEA", "lat: ---" + latitude + "-----lng:-----" + longitude);
            }

            //Setup Request
            FlashAuditRequest temp = new FlashAuditRequest();
            temp.rwic = new FieldWorkerWS();
            temp.rwic.id = user.workId;
            temp.timestamp = KTime.ParseNow(KTime.KT_fmtDate3339fk_xS).toString();
            temp.status = "Submitted";
            temp.images = new FlashAuditImages[flashAuditViewModel.getmPictureList().size()];
            for (int i = 0; i < flashAuditViewModel.getmPictureList().size(); i++) {
                FlashAuditImages tempImage = new FlashAuditImages();
                if (flashAuditViewModel.getmPictureList().get(i) != null) {
                    tempImage.timestamp = KTime.ParseNow(KTime.KT_fmtDate3339fk_xS).toString();
                    tempImage.data = Functions.EncodeImagePictureBase64(DOC_IMAGE_FLASH, Functions.UriFileName(this, flashAuditViewModel.getmPictureList().get(i).pictureURI), IMAGE_COMPRESSION);
                    tempImage.description = flashAuditViewModel.getmPictureList().get(i).description;
                    tempImage.longitude = (float) longitude;
                    tempImage.latitude = (float) latitude;
                    if (tempImage.data.length() > 0) {
                        temp.images[i] = tempImage;
                    }
                }
            }
            return temp;
        } catch (Exception ex) {
            ExpClass.LogEX(ex, user.unique + " :CreateRequest failure.");
            return null;
        }
    }

    public void auditSubmitSuccess() {
        unlockOrientation();
        progressBar.setVisibility(View.INVISIBLE);
        // Leaving screen, so give update in notification bar.
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "default")
                .setSmallIcon(R.drawable.complete)
                .setContentTitle(getResources().getString(R.string.title_confirm_flash))
                .setContentText(getResources().getString(R.string.flash_audit_success))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(0, mBuilder.build());
        this.finish();
    }

    public void auditSubmitFaield(int status) {
        progressBar.setVisibility(View.INVISIBLE);
        String holdFmt = getString(R.string.flash_audit_error_submit);
        setErrorText(String.format(holdFmt, status));
    }

    private void setErrorText(String error) {
        errorText.setVisibility(View.VISIBLE);
        errorText.setText(error);
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
    }

    @Override
    public void unlockOrientation() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }

    @Override
    public void lockOrientation() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void setTimePicker(String totalTime, String startTime, String endTime, int id) {

    }

    @Override
    public void setSignatureImage(String imageName, int id) {

    }

    @Override
    public void simplePickerResponse(int source, int selection){

    }

    @Override
    public void simpleListResponse(int source, int selection){

    }

    @Override
    public void setJobNumber(JobTbl tbl) {

    }

}
