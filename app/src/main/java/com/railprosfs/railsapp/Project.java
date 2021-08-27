package com.railprosfs.railsapp;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;

import com.railprosfs.railsapp.data_layout.AssignmentTbl;
import com.railprosfs.railsapp.data_layout.DocumentTbl;
import com.railprosfs.railsapp.data_layout.DwrTbl;
import com.railprosfs.railsapp.data_layout.JobSetupTbl;
import com.railprosfs.railsapp.data.dto.Railroads;
import com.railprosfs.railsapp.dialog.SimpleDisplayDialog;
import com.railprosfs.railsapp.ui_support.ProjectViewModel;
import com.railprosfs.railsapp.utility.ExpClass;

import java.io.File;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import static com.railprosfs.railsapp.utility.Constants.*;

/**
 * The Project screen shows detail about the project / job.  This includes
 * more summary information and a list of DWRs similar to the Schedule screen,
 * but also includes all the related documents.
 *
 * A note about mServiceType.  The service type (Flagging vs Utility) is not
 * really a job level data item, but a shift level item.  That means a job
 * might have multiple service types.  But we are suppose to guess at what
 * type of Job Setup form a person might want (Flagging or Utility) and then
 * allow that in the "New Job Setup" link.  Most of the time we should get it
 * right, but at some point may need to redesign the GUI to make user decide
 * explicitly (instead of guessing for them).
 */
public class Project extends AppCompatActivity {
    private int mJobId = 0;
    private int mPropertyId = 0;
    private String mPropertyCode = "";
    private int mServiceType = RP_FLAGGING_SERVICE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();

        // Remember the Job we are looking at.
        if (savedInstanceState != null) {
            mJobId = savedInstanceState.getInt(IN_JOBID, 0);
            mPropertyId = savedInstanceState.getInt(IN_PROPERTYID, 0);
            mPropertyCode = savedInstanceState.getString(IN_PROPERTY_CODE, "");
        }
        if (extras != null) {
            mJobId = extras.getInt(IN_JOBID, 0);
            mPropertyId = extras.getInt(IN_PROPERTYID, 0);
            mPropertyCode = Railroads.PropertyName(getApplicationContext(), mPropertyId);
        }

        // Set up main view and menu.
        setContentView(R.layout.project);

        // Use a factory to pass in the database key to ViewModel. Could also use a public parameter.
        ProjectViewModel.Factory factory = new ProjectViewModel.Factory(getApplication(), mJobId);
        ProjectViewModel mProjectViewModel = ViewModelProviders.of(this, factory).get(ProjectViewModel.class);

        // These are call backs to avoid DB use on main thread.
        mProjectViewModel.GetJob().observe(this, new Observer<AssignmentTbl>() {
            @Override
            public void onChanged(@Nullable final AssignmentTbl project) {
                if (project != null) {
                    FragmentManager manager = getSupportFragmentManager();
                    JobFragment display = (JobFragment) manager.findFragmentById(R.id.fragJobP);
                    if (display != null) {
                        display.ShowJobInfo(project);
                    }
                    mPropertyId = project.RailroadId;
                    mPropertyCode = Railroads.PropertyName(getApplicationContext(), mPropertyId);
                    mServiceType = project.ServiceType;
                }
            }
        });

        // These are call backs to avoid DB use on main thread.
        mProjectViewModel.GetDwrs().observe(this, new Observer<List<DwrTbl>>() {
            @Override
            public void onChanged(@Nullable final List<DwrTbl> dwrs) {
                if (dwrs != null) {
                    FragmentManager manager = getSupportFragmentManager();
                    DetailFragment display = (DetailFragment) manager.findFragmentById(R.id.fragDetailP);
                    if (display != null) {
                        display.ShowDwrDetail(dwrs, mServiceType);
                    }
                }
            }
        });

        // These are call backs to avoid DB use on main thread.
        mProjectViewModel.GetAllJobSetups().observe(this, new Observer<List<JobSetupTbl>>() {
            @Override
            public void onChanged(@Nullable List<JobSetupTbl> jsetupTbls) {
                if (jsetupTbls != null) {
                    FragmentManager manager = getSupportFragmentManager();
                    DetailFragment display = (DetailFragment) manager.findFragmentById(R.id.fragDetailP);
                    if (display != null) {
                        display.ShowJsDetail(jsetupTbls, mServiceType);
                    }
                }
            }
        });

        // These are call backs to avoid DB use on main thread.
        mProjectViewModel.GetDocs().observe(this, new Observer<List<DocumentTbl>>() {
            @Override
            public void onChanged(@Nullable final List<DocumentTbl> docs) {
                if (docs != null) {
                    FragmentManager manager = getSupportFragmentManager();
                    DetailFragment display = (DetailFragment) manager.findFragmentById(R.id.fragDetailP);
                    if (display != null) {
                        display.ShowDocDetail(docs, mServiceType);
                    }
                }
            }
        });
    }

    /*
     *  We want to make sure we save off the job key, everything
     *  flows from that.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(IN_JOBID, mJobId);
        outState.putInt(IN_PROPERTYID, mPropertyId);
        outState.putString(IN_PROPERTY_CODE, mPropertyCode);
        super.onSaveInstanceState(outState);
    }

    /*
     *  Restore the state, compliments of onSaveInstanceState().
     *  NOTE: If you do not see something here, often we need to
     *  restore data in the OnCreate().
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    // Look up the job location.
    public void LaunchGMaps(View view) {
        if (view.getTag() != null) {
            String link = view.getTag().toString();
            // Launch Maps.
            if (link.length() > 0) {
                Uri mapUri = Uri.parse(link);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, mapUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                mapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getBaseContext().startActivity(mapIntent);
            } else {
                simpleDisplayRequest(0, getResources().getString(R.string.msg_inform_no_coordinates));
            }
        }
    }

    // Look up job dwr.
    public void ShowDwrDetail(View view) {
        try {
            switch (view.getId()) {
                case R.id.lblDwrStartLink:
                    Intent intent = new Intent(this, DwrEdit.class);
                    Bundle mBundle = new Bundle();
                    mBundle.putInt(IN_JOBID, mJobId);
                    mBundle.putInt(IN_PROPERTYID, mPropertyId);
                    intent.putExtras(mBundle);
                    startActivity(intent);
                    break;
                default:
                    if (view.getTag() != null) {
                        Intent dwrdata = new Intent(this, DwrEdit.class);
                        Bundle dwrbunl = new Bundle();
                        dwrbunl.putInt(IN_DWRID, (int) view.getTag());
                        dwrdata.putExtras(dwrbunl);
                        startActivity(dwrdata);
                    }
                    break;
            }
        } catch (Exception ex) {
            ExpClass.LogEX(ex, this.getClass().getName() + ".ShowDwrInfo");
        }
    }

    public void ShowJsDetail(View view) {
        try {
            switch (view.getId()) {
                case R.id.lblJobSetupLink:
                    Intent intent = new Intent(this, JobSetup.class);
                    Bundle mBundle = new Bundle();
                    mBundle.putInt(IN_PROPERTYID, mPropertyId);
                    mBundle.putString(IN_PROPERTY_CODE, mPropertyCode);
                    mBundle.putInt(IN_JOBID, mJobId);
                    mBundle.putInt(IN_JSID, 0);
                    mBundle.putInt(IN_JOBSETUP_FORM, (int) view.getTag());
                    intent.putExtras(mBundle);
                    startActivity(intent);
                    break;
                default:
                    if (view.getTag() != null) {
                        Intent jsdata = new Intent(this, JobSetup.class);
                        Bundle jsbunl = new Bundle();
                        jsbunl.putInt(IN_PROPERTYID, mPropertyId);
                        jsbunl.putString(IN_PROPERTY_CODE, mPropertyCode);
                        jsbunl.putInt(IN_JOBID, mJobId);
                        jsbunl.putInt(IN_JSID, (int) view.getTag());
                        jsdata.putExtras(jsbunl);
                        startActivity(jsdata);
                    }
                    break;
            }
        } catch (Exception ex) {
            ExpClass.LogEX(ex, this.getClass().getName() + ".ShowJsDetail");
        }
    }

    public void ShowCsDetail(View view){
        try {
            if(view.getId() == R.id.lblCoverSheetLink){
                Intent intent = new Intent(this, JobSetup.class);
                Bundle mBundle = new Bundle();
                mBundle.putInt(IN_PROPERTYID, mPropertyId);
                mBundle.putString(IN_PROPERTY_CODE, mPropertyCode);
                mBundle.putInt(IN_JOBID, mJobId);
                mBundle.putInt(IN_JSID, 0);
                mBundle.putInt(IN_JOBSETUP_FORM, RP_COVER_SERVICE);
                intent.putExtras(mBundle);
                startActivity(intent);
            }
        } catch (Exception ex) {
            ExpClass.LogEX(ex, this.getClass().getName() + ".ShowCsDetail");
        }
    }

    // Launch a Document viewer
    public void LaunchDocument(View view) {
        try {
            String fileName = (String) view.getTag();
            File file = new File(fileName);
            if(!file.exists()) {
                simpleDisplayRequest(0, getResources().getString(R.string.msg_no_file));
                return;
            }
            MimeTypeMap myMime = MimeTypeMap.getSingleton();

            Intent newIntent = new Intent(Intent.ACTION_VIEW);
            Uri uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".share", file);
            newIntent.setDataAndType(uri, getMimeType(uri.getPath()));

            newIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                PackageManager pm = getApplication().getPackageManager();
                if(newIntent.resolveActivity(pm) != null) {
                    startActivity(newIntent);
                }
                else {
                    simpleDisplayRequest(0, getResources().getString(R.string.msg_no_file_handler));
                }
            } catch (ActivityNotFoundException e) {
                simpleDisplayRequest(0, getResources().getString(R.string.msg_no_file_handler) + "(b)");
            }
        } catch (Exception ex) {
            ExpClass.LogEX(ex, this.getClass().getName() + ".LaunchDocument");
        }
    }

    // url = file path or whatever suitable URL you want.
    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    /**
     *  The basic fragment support for showing a simple confirmation dialog. Note that there
     *  is no expected callback (and so NO Response method) from this dialog.
     * @param title     The resource id of a string to use as a title displayed on the dialog.
     * @param message   The resource id of confirmation message to display to the user.
     */
    private void simpleDisplayRequest(int title, String message){
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


}
