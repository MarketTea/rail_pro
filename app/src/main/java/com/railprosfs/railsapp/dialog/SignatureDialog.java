package com.railprosfs.railsapp.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.railprosfs.railsapp.R;
import com.railprosfs.railsapp.SignatureMainLayout;
import com.railprosfs.railsapp.ui_support.FragmentTalkBack;

import static com.railprosfs.railsapp.utility.Constants.IN_SIGN_ID;

public class SignatureDialog extends DialogFragment {
    private int ID;
    private FragmentTalkBack mCallback;
    private final String TAG = "SignatureDialog";
    private SignatureMainLayout sigLayout;

    public SignatureDialog() {
        setRetainInstance(true);
    }

    public void setmCallback(FragmentTalkBack mCallback) {
        this.mCallback = mCallback;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            ID = getArguments().getInt(IN_SIGN_ID);
        }
        Log.d(TAG, "OnCreate");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.signaturedialog, container, false);
        mCallback.lockOrientation();
        sigLayout = v.findViewById(R.id.signature_layout);
        sigLayout.exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Got Non-Empty String No Picture was Saved
                mCallback.unlockOrientation();
                if(sigLayout.PICTURE_NAME != null && !sigLayout.PICTURE_NAME.equals("")) {
                    mCallback.setSignatureImage(sigLayout.PICTURE_NAME, ID);
                }
                dismiss();
            }
        });
        sigLayout.saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallback.unlockOrientation();
                sigLayout.saveImg();
                mCallback.setSignatureImage(sigLayout.PICTURE_NAME, ID);
                dismiss();
            }
        });
        Log.d(TAG, "OnCreateView");
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "OnResume");
        if(getDialog().getWindow() != null) {
            ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
            params.width = ViewGroup.LayoutParams.MATCH_PARENT;
            getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "OnAttach");
        try {
            if(mCallback == null) {
                mCallback = (FragmentTalkBack) context;
            }
        }catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement TotalTimePickerListener");
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
//        Bitmap bp = sigLayout.signatureView.getSignature();
    }

    @Override
    public void onDestroyView() {
        Dialog dialog = getDialog();
        // handles https://code.google.com/p/android/issues/detail?id=17423
        if (dialog != null && getRetainInstance()) {
            dialog.setDismissMessage(null);
        }
        super.onDestroyView();
    }
}
