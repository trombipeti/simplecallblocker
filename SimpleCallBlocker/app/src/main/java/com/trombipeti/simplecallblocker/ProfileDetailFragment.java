package com.trombipeti.simplecallblocker;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;

import com.trombipeti.simplecallblocker.adapter.ContactAdapter;
import com.trombipeti.simplecallblocker.model.BlockProfilesSingleton;
import com.trombipeti.simplecallblocker.model.Contact;

public class ProfileDetailFragment extends Fragment implements NumberAddFragment.INumberAddFragment {

    public static final String ARG_SECTION_NUMBER = "section_number";

    private int mSelectedProfile = 0;

    public int getSelectedProfile() {
        return mSelectedProfile;
    }

    public ProfileDetailFragment() {

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            if (getArguments() != null) {
                mSelectedProfile = getArguments().getInt(MainActivity.KEY_SELECTED_PROFILE);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile_detail, container, false);
        final CheckedTextView toggleDisabled = (CheckedTextView) rootView.findViewById(R.id.porfile_toggle_checkbox);
        toggleDisabled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleDisabled.setChecked(!toggleDisabled.isChecked());
                BlockProfilesSingleton.Instance().modifyEnabled(mSelectedProfile, !toggleDisabled.isChecked());
                Intent i = new Intent();
                i.setAction(MainActivity.DATACHANGE_BROADCAST);
                getActivity().sendBroadcast(i);
            }
        });

        final CheckedTextView toggleBlockAll = (CheckedTextView) rootView.findViewById(R.id.blockall_toggle_checkbox);
        toggleBlockAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleBlockAll.setChecked(!toggleBlockAll.isChecked());
                BlockProfilesSingleton.Instance().modifyAllBlock(mSelectedProfile, toggleBlockAll.isChecked());
                Intent i = new Intent();
                i.setAction(MainActivity.DATACHANGE_BROADCAST);
                getActivity().sendBroadcast(i);
            }
        });

        toggleDisabled.setChecked(! BlockProfilesSingleton.Instance().get(mSelectedProfile).isEnabled());
        toggleBlockAll.setChecked(BlockProfilesSingleton.Instance().get(mSelectedProfile).isAllBlock());

        ListView lvContacts = (ListView) rootView.findViewById(R.id.contacts_listview);
        lvContacts.setAdapter(new ContactAdapter(getActivity(), mSelectedProfile));
        Button addContactBtn = (Button) rootView.findViewById(R.id.add_profile_btn);
        addContactBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            NumberAddFragment createFragment = new NumberAddFragment();
                createFragment.setTargetFragment(ProfileDetailFragment.this, 0);
                FragmentManager fm = getFragmentManager();
                createFragment.setListener(ProfileDetailFragment.this);
                createFragment.show(fm, ProfileCreateFragment.TAG);;
            }
        });
        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((MainActivity) activity).onSectionAttached(
                getArguments().getInt(ARG_SECTION_NUMBER));
    }

    @Override
    public void onNumberAdded(String name, String number) {
        ListView lvContacts = (ListView) getActivity().findViewById(R.id.contacts_listview);
        ContactAdapter adapter = ((ContactAdapter)lvContacts.getAdapter());
        adapter.addContact(new Contact(name, number));
        adapter.notifyDataSetChanged();
    }
}
