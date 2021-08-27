package com.railprosfs.railsapp;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.railprosfs.railsapp.data_layout.AssignmentTbl;
import com.railprosfs.railsapp.data.dto.Railroads;
import com.railprosfs.railsapp.utility.ExpClass;
import com.railprosfs.railsapp.utility.ExpParseToCalendar;
import com.railprosfs.railsapp.utility.KTime;

import java.util.regex.PatternSyntaxException;

import static com.railprosfs.railsapp.utility.Constants.*;

/**
 *  This fragment holds the Job description.
 */
public class JobFragment extends Fragment {

    private TextView mJobNbr;
    private TextView mDateRange;
    private TextView mProperty;
    private TextView mSubdivision;
    private TextView mJobDesc;
    private TextView mAddedNotes;
    private TextView mEquipment;
    private TextView mDistanceFromTrack;
    private TextView mRoadmaster;
    private TextView mPermit;
    private TextView mMilePost;
    private TextView mSupervisor;
    private LinearLayout mSupervisorUX;
    private TextView mContact;
    private TextView mLocation;
    private ImageView mLocationIcon;


    public JobFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Different layouts based on orientation
        if (Configuration.ORIENTATION_LANDSCAPE == getResources().getConfiguration().orientation) {
            return inflater.inflate(R.layout.project_fragment_job_land, container, false);
        }
        else {
            return inflater.inflate(R.layout.project_fragment_job, container, false);
        }
    }

    /*
        Upon first run, load up a Job description.  Android will keep text around
        even with a context change, so no need for any special restore logic.
    */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeAndSetupWidgets(view);
        AssignmentTbl job = GetSafeJob();
        ShowJobInfo(job);
    }

    private void initializeAndSetupWidgets(@NonNull View view) {
        mJobNbr = view.findViewById(R.id.txtJobNbrJT);
        mDateRange = view.findViewById(R.id.dateRangeJT);
        mProperty = view.findViewById(R.id.txtPropertyJT);
        mSubdivision = view.findViewById(R.id.txtSubdivisionJT);
        mJobDesc = view.findViewById(R.id.jobDescJT);
        mAddedNotes = view.findViewById(R.id.txtAddedNotesJT);
        mEquipment = view.findViewById(R.id.txtEquipmentJT);
        mDistanceFromTrack = view.findViewById(R.id.txtDistanceFromTrackJT);
        mRoadmaster = view.findViewById(R.id.txtRoadmasterJT);
        mPermit = view.findViewById(R.id.txtPermitJT);
        mMilePost = view.findViewById(R.id.txtMilePost);
        mSupervisor = view.findViewById(R.id.txtSupervisorJT);
        mSupervisorUX = view.findViewById(R.id.rowSupervisorJT);
        mContact = view.findViewById(R.id.txtContactJT);
        mLocation = view.findViewById(R.id.txtLocationJT);
        mLocationIcon = view.findViewById(R.id.imgHomeMapLinkJT);
    }

    /*
        The Display method is mostly called by the activity to update the description.
    */
    public void ShowJobInfo(AssignmentTbl job) {
        try {
            if (mJobNbr != null) {
                mJobNbr.setText(BlankForNull(job.JobNumber));
                try {
                    mDateRange.setText(String.format(DASHED_RANGE,
                            KTime.ParseToFormat(job.JobStartDate, KTime.KT_fmtDate3339k, KTime.UTC_TIMEZONE, KTime.KT_fmtDateShrtMiddle, KTime.UTC_TIMEZONE),
                            KTime.ParseToFormat(job.JobEndDate, KTime.KT_fmtDate3339k, KTime.UTC_TIMEZONE, KTime.KT_fmtDateShrtMiddle, KTime.UTC_TIMEZONE)));
                } catch (ExpParseToCalendar expParseToCalendar) {
                    mDateRange.setText(R.string.blank);
                }
                mProperty.setText(Railroads.PropertyName(getContext(),job.RailroadId));
                mSubdivision.setText(BlankForNull(job.Subdivision));
                mJobDesc.setText(BlankForNull(job.JobDescription));
                mAddedNotes.setText(BlankForNull(job.Notes));
                mEquipment.setText(BlankForNull(job.EquipmentDescription));
                mDistanceFromTrack.setText(BlankForNull(job.DistanceFromTracks));
                mRoadmaster.setText(BlankForNull(job.TrackSupervisor));
                mPermit.setText(BlankForNull(job.PermitNumber));
                mMilePost.setText(BlankForNull(job.MilePostStart));
                mSupervisor.setText(BlankForNull(job.SupervisorRP));
                if(BlankForNull(job.SupervisorRP).equalsIgnoreCase(SUPERVISOR_JOB)){
                    mSupervisorUX.setVisibility(View.VISIBLE);
                    mSupervisor.setText(R.string.yes);
                } else {
                    mSupervisorUX.setVisibility(View.GONE);
                }
                mContact.setText(ContactsDisplay(BlankForNull(job.FieldContactName), BlankForNull(job.FieldContactPhone), BlankForNull(job.FieldContactEmail)));
                mLocation.setText(BlankForNull(job.LocationName));
                mLocation.setTag(BlankForNull(job.LocationLink));
                mLocationIcon.setTag(BlankForNull(job.LocationLink));
            }
        } catch (Exception ex) {
            ExpClass.LogEX(ex, KEVIN_SPEAKS);
        }
    }

    // Unpack the contacts and format them as desired.  To see the data getting saved,
    // look in Functions.BuildAssignment().
    private String ContactsDisplay(String names, String phones, String emails) {
        try {
            // In Portrait mode, oddly enough, there is more horizontal space.
            String DELIMIT_DISPLAY = "\n"; // landscape stack
            String DELIMIT_ROWEND = "\n\n";
            if (Configuration.ORIENTATION_PORTRAIT == getResources().getConfiguration().orientation) {
                DELIMIT_DISPLAY = " :: ";  // portrait flat
                DELIMIT_ROWEND = "\n\n";
            }

            // Final product
            StringBuilder TheContacts = new StringBuilder();

            // The contact information is packed into three fields.
            String[] allNames = {names};
            String[] allPhones = {phones};
            String[] allEmails = {emails};
            try {
                allNames = names.split(DELIMIT_CONTACTS_REG);
                allPhones = phones.split(DELIMIT_CONTACTS_REG);
                allEmails = emails.split(DELIMIT_CONTACTS_REG);
            } catch (PatternSyntaxException px){
                /* This is expected for earlier versions of data, just use raw data. */
            }

            for (int i = 0; i < allNames.length; i++) {
                String holdPhone = "";
                String holdEmail = "";
                if (allPhones.length > i && !allPhones[i].equalsIgnoreCase(DELIMIT_CONTACTS_PH)) {
                    holdPhone = DELIMIT_DISPLAY + allPhones[i].replaceFirst("(\\d{3})(\\d{3})(\\d+)", "($1) $2-$3");
                }
                if (allEmails.length > i && !allEmails[i].equalsIgnoreCase(DELIMIT_CONTACTS_PH)) {
                    holdEmail = DELIMIT_DISPLAY + allEmails[i];
                }
                if((i+1)!=allNames.length) { // do not need the last new line.
                    TheContacts.append(allNames[i]).append(holdPhone).append(holdEmail).append(DELIMIT_ROWEND);
                } else {
                    TheContacts.append(allNames[i]).append(holdPhone).append(holdEmail);
                }
            }
            return TheContacts.toString();

        } catch (Exception ex){
            ExpClass.LogEX(ex, "ContactsDisplay");
            return "";
        }
    }

    // We rely on callbacks to get real data, but need something until they occur.
    private AssignmentTbl GetSafeJob(){
        AssignmentTbl holdJob = new AssignmentTbl();
        holdJob.JobNumber = "Unknown";
        holdJob.Subdivision = "";
        holdJob.JobDescription = "";
        holdJob.Notes = "";
        holdJob.EquipmentDescription = "";
        holdJob.DistanceFromTracks = "";
        holdJob.TrackSupervisor = "";
        holdJob.PermitNumber = "";
        holdJob.MilePostStart = "";
        holdJob.SupervisorRP = "";
        holdJob.FieldContactName = "";
        holdJob.FieldContactPhone = "";
        holdJob.FieldContactEmail = "";
        holdJob.LocationName = "";
        holdJob.LocationLink = "";
        holdJob.JobStartDate = "";
        holdJob.JobEndDate = "";
        return holdJob;
    }

    private String BlankForNull(String value){ return value == null ? "" : value; }
}
