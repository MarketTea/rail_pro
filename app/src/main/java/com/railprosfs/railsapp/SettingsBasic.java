package com.railprosfs.railsapp;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import com.railprosfs.railsapp.data.dto.Actor;
import com.railprosfs.railsapp.dialog.SimpleConfirmDialog;
import static com.railprosfs.railsapp.utility.Constants.KY_SIMPLE_CONFIRM_FRAG;

/**
 *  The very basic fragment to display settings.  See the Settings
 *  activity for all the important stuff.
 */
public class SettingsBasic extends PreferenceFragmentCompat {

    private Actor mUser;

    public SettingsBasic() {}

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String s) {
        mUser = new Actor(this.getContext());
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.settings);
        Preference dialogPreference = getPreferenceScreen().findPreference("1007");
        dialogPreference.setEnabled(false);
        Preference rolePreference = getPreferenceScreen().findPreference("1002");
        if(mUser != null) {
            rolePreference.setSummary(mUser.roleStr(this.getContext()));
            if (mUser.isAdmin()) {
                dialogPreference.setEnabled(true);
                dialogPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        simpleConfirmRequest(R.string.title_confirm_Sandbox, R.string.msg_confirm_sandbox, true);
                        return true;
                    }
                });
            }
        }
    }

    /**
     *  The basic fragment support for showing a simple confirmation dialog.
     * @param title     The resource id of a string to use as a title displayed on the dialog.
     * @param message   The resource id of confirmation message to display to the user.
     */
    private void simpleConfirmRequest(int title, int message, boolean showCancel){
        // Set up the fragment.
        FragmentManager mgr = null;
        try {
            mgr = getActivity().getSupportFragmentManager();
        } catch (Exception ex) { return; }
        Fragment fragment = mgr.findFragmentByTag(KY_SIMPLE_CONFIRM_FRAG);
        if (fragment != null) { // Clear out the previous use.
            mgr.beginTransaction().remove(fragment).commit();
        }
        // Launch the confirmation.
        DialogFragment submitFrag = new SimpleConfirmDialog(title, message, !showCancel);
        submitFrag.show(mgr, KY_SIMPLE_CONFIRM_FRAG);
    }
}
