package com.trombipeti.simplecallblocker;


import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;

public class ProfileCreateFragment extends DialogFragment {

    public static final String TAG = "ProfileCreateFragment";

    private EditText etProfileName;
    private Button btnSaveProfile;
    private CheckedTextView chktvBlock;

    private IProfileCreateFragment listener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (getTargetFragment() != null) {
            try {
                listener = (IProfileCreateFragment) getTargetFragment();
            } catch (ClassCastException ce) {
                Log.e(TAG,
                        "Target Fragment does not implement fragment interface!");
            } catch (Exception e) {
                Log.e(TAG, "Unhandled exception!");
                e.printStackTrace();
            }
        } else {
            try {
                listener = (IProfileCreateFragment) activity;
            } catch (ClassCastException ce) {
                Log.e(TAG,
                        "Parent Activity does not implement fragment interface!");
            } catch (Exception e) {
                Log.e(TAG, "Unhandled exception!");
                e.printStackTrace();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.createprofile, container, false);

        getDialog().setTitle(R.string.add_profile);

        etProfileName = (EditText)root.findViewById(R.id.profile_name_edittext);
        btnSaveProfile = (Button)root.findViewById(R.id.profile_save_btn);
        chktvBlock = (CheckedTextView)root.findViewById(R.id.block_all_checkedtv);

        btnSaveProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(etProfileName.getText().toString().trim().equalsIgnoreCase("")) {
                    etProfileName.setError(getResources().getString(R.string.error_empty_name));
                } else {
                    if(listener != null) {
                        listener.onProfileCreated(etProfileName.getText().toString(), chktvBlock.isChecked());
                    }

                    dismiss();
                }

            }
        });

        chktvBlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chktvBlock.setChecked( ! chktvBlock.isChecked());
            }
        });

        return root;

    }

    public void setListener(IProfileCreateFragment aListener) {
        listener = aListener;
    }

    public interface IProfileCreateFragment {
        public void onProfileCreated(String profileName, boolean block);
    }
}
