package com.railprosfs.railsapp.data.dto;

import android.content.Context;

import com.railprosfs.railsapp.R;

import java.io.Serializable;

/**
 *  Represents information about any people in the application.  Not all fields
    need apply to all types of users.  For example, there is much less info
    populated for a coworker or suborbinate.  To help separate these differences
    out, the Actor class does subclass from here for the actual user of the App.

    This class can be used as an in-memory store of any user(s). I am guessing
    that in a future iteration of the App there will be more than just the
    one person signed in that is of concern, although there is not much done
    with coworkers/suborbinates at the start of the project.

    This class implements Serializable to allow easy movement between screens
    using intents.  This means it cannot contain things that are not serializable.
 */
public class Worker extends BaseDTO {
    private static final long serialVersionUID = 5264124489360369638L; // Required for serialization.

    public int workId = 0;         // The fieldworker id of the user on the server.
    public String userId = "";      // The user id (GUID) on the server.
    public String unique = "";      // The unique name, e.g. email addr.
    public String display = "";     // The display name.
    public int role = 0;            // The role of the worker.
    public int railroad = 0;        // The primary railroad associated with the worker.
    public String properties = "";  // All the railroads associated with the worker.
    public String tag = "";         // Area to store temporary identifier.
    public boolean primary = false; // When true, this account is the primary user.

    // Constructors
    public Worker(){}

    // Helper methods
    public String workIdStr(){ return Integer.toString(workId); }
    public boolean isEmail(){ return unique.contains("@"); }
    public String cleanUnique(){ return isEmail() ? unique : unique.replaceAll("[^0-9]", ""); }
    public boolean isStaff(){ return role > 1; }
    public boolean isAdmin(){ return role == 4; }

    public String roleStr(Context context){
        switch (role){
            case 1:
                return context.getResources().getString(R.string.roleRWIC);
            case 2:
                return context.getResources().getString(R.string.roleSup);
            case 3:
                return context.getResources().getString(R.string.roleMgr);
            case 4:
                return context.getResources().getString(R.string.roleAdm);
            default:
                return context.getResources().getString(R.string.unknown);

        }
    }

}
