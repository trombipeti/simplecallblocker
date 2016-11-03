package com.trombipeti.simplecallblocker;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.trombipeti.simplecallblocker.adapter.ContactAdapter;
import com.trombipeti.simplecallblocker.model.Contact;

public class NumberAddFragment extends DialogFragment {

    public static final int REQUEST_PICK_CONTACT = 1;

    public static final String TAG = "NumberAddFragment";

    private INumberAddFragment listener;


    private EditText etNumber;
    private EditText etName;
    private Button btnSelectContact;
    private Button btnSave;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (getTargetFragment() != null) {
            try {
                listener = (INumberAddFragment) getTargetFragment();
            } catch (ClassCastException ce) {
                Log.e(TAG,
                        "Target Fragment does not implement fragment interface!");
            } catch (Exception e) {
                Log.e(TAG, "Unhandled exception!");
                e.printStackTrace();
            }
        } else {
            try {
                listener = (INumberAddFragment) activity;
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
        View root = inflater.inflate(R.layout.addnumber, container, false);

        getDialog().setTitle(R.string.add_profile);

        etName = (EditText) root.findViewById(R.id.block_name_edittext);
        etNumber = (EditText) root.findViewById(R.id.phone_number_edittext);

        btnSelectContact = (Button) root.findViewById(R.id.btn_select_contact);

        btnSelectContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent, REQUEST_PICK_CONTACT);
            }
        });

        btnSave = (Button) root.findViewById(R.id.btn_save_number);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etName.getText().toString().trim().equalsIgnoreCase("")) {
                    etName.setError(getResources().getString(R.string.error_empty_name));
                } else if (etNumber.getText().toString().trim().equalsIgnoreCase("")) {
                    etNumber.setError(getString(R.string.error_empty_number));
                } else {
                    if (listener != null) {
                        listener.onNumberAdded(etName.getText().toString(), etNumber.getText().toString());
                    }
                    dismiss();
                }
            }
        }
    );

    return root;

}

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_PICK_CONTACT:
                    Uri contactUri = data.getData();
                    if (contactUri != null) {
                        Cursor c = null;
                        Cursor addrCur = null;
                        try {
                            ContentResolver cr = getActivity().getContentResolver();
                            c = cr.query(contactUri, null, null, null, null);
                            if (c != null && c.moveToFirst()) {
                                String id = c.getString(c.getColumnIndex(BaseColumns._ID));
                                String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                                Log.d("ProfileCreate", "name: " + name);
                                String contactNumber = null;
                                Cursor cursorPhone = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                        null,

                                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,

                                        null,
                                        null);

                                if (cursorPhone.moveToFirst()) {
                                    contactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                }
                                Log.d("ProfileCreate", "contactNumber: " + contactNumber);
                                if (name != null) {
                                    etName.setText(name);
                                }
                                if (contactNumber != null) {
                                    etNumber.setText(contactNumber);
                                }
                                cursorPhone.close();
                            }
                        } finally {
                            if (c != null) {
                                c.close();
                            }
                            if (addrCur != null) {
                                addrCur.close();
                            }
                        }
                    }

                    break;
                default:
                    break;
            }
        }
    }


    public void setListener(INumberAddFragment aListener) {
        listener = aListener;
    }

public interface INumberAddFragment {
    public void onNumberAdded(String name, String number);
}
}
