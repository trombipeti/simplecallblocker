package com.trombipeti.simplecallblocker.adapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.trombipeti.simplecallblocker.MainActivity;
import com.trombipeti.simplecallblocker.R;
import com.trombipeti.simplecallblocker.model.BlockProfile;
import com.trombipeti.simplecallblocker.model.BlockProfilesSingleton;
import com.trombipeti.simplecallblocker.model.Contact;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ContactAdapter extends BaseAdapter {

    private BlockProfile profile;

    private ArrayList<Boolean> needRecreateView = new ArrayList<>();

    private Context context;

    private String PROFILE_FILE_NAME;

    public ContactAdapter(Context context, int selectedProfile) {
        profile = BlockProfilesSingleton.Instance().get(selectedProfile);
        this.context = context;
        PROFILE_FILE_NAME = profile.getName().replace(" ", "_") + ".xml";
        loadContacts();
    }

    public String getSaveFileName() {
        return PROFILE_FILE_NAME;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return profile.isEnabled();
    }

    @Override
    public boolean isEnabled(int position) {
        return areAllItemsEnabled();
    }

    @Override
    public int getCount() {
        return profile.getContactsNum();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    private void doRemoveContact(Contact c) {
        profile.removeContact(c);
        needRecreateView.clear();
        for (int cnt = 0; cnt < profile.getContactsNum(); ++cnt) {
            needRecreateView.add(true);
        }
        try {
            saveContacts();
        } catch (IOException e) {
            e.printStackTrace();
        }
        notifyDataSetChanged();
        Intent i = new Intent();
        i.setAction(MainActivity.DATACHANGE_BROADCAST);
        context.sendBroadcast(i);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null || (needRecreateView.size() > position && needRecreateView.get(position))) {
            Log.d("ContactAdapter.getView", "Creating new view for position " + position);
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.contactview, null);
            TextView tvNumber = (TextView) (convertView.findViewById(R.id.contact_number_textview));
            tvNumber.setText(profile.get(position).getPhoneNumber());
            TextView tvName = (TextView) (convertView.findViewById(R.id.contact_name_textview));
            tvName.setText(profile.get(position).getName());
            if (needRecreateView.size() > 0) {
                needRecreateView.set(position, false);
            }
            ImageButton btnDel = (ImageButton) convertView.findViewById(R.id.del_contact_btn);

            final Dialog.OnClickListener dialogListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        doRemoveContact(profile.get(position));
                    }
                    dialog.dismiss();
                }
            };

            btnDel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(context)
                            .setMessage(context.getString(R.string.confirm_remove_number))
                            .setPositiveButton(context.getString(R.string.yes), dialogListener)
                            .setNegativeButton(context.getString(R.string.no), dialogListener)
                            .show();

                }
            });
        }
        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        return R.layout.contactview;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return profile.getContactsNum() == 0;
    }

    public void addContact(Contact aContact) {
        profile.addContact(aContact);
        needRecreateView.add(true);
        for (int i = 0; i < needRecreateView.size(); ++i) {
            needRecreateView.set(i, true);
        }
        new Runnable() {
            @Override
            public void run() {
                try {
                    saveContacts();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.run();
        Intent i = new Intent();
        i.setAction(MainActivity.DATACHANGE_BROADCAST);
        context.sendBroadcast(i);
    }

    public void loadContacts() {
        profile.clear();
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();

            FileInputStream fIn = context.openFileInput(PROFILE_FILE_NAME);

            parser.setInput(fIn, "UTF-8");

            int eventType = parser.getEventType();
            boolean in_profile = false;
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (parser.getName().equalsIgnoreCase("profile")) {
                            in_profile = true;
                        } else if (parser.getName().equalsIgnoreCase("contact") && in_profile) {
                            String name = parser.getAttributeValue("", "name");
                            String number = parser.getAttributeValue("", "number");
                            profile.addContact(new Contact(name, number));
                        }
                        break;
                    default:
                        break;
                }
                eventType = parser.next();
            }

        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            /* Ignore */
            // e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Intent i = new Intent();
        i.setAction(MainActivity.DATACHANGE_BROADCAST);
        context.sendBroadcast(i);
    }

    public void saveContacts() throws IOException {
        XmlSerializer xmlSerializer = Xml.newSerializer();
        FileOutputStream fOut = context.openFileOutput(PROFILE_FILE_NAME, Context.MODE_PRIVATE);

        xmlSerializer.setOutput(fOut, "UTF-8");

        xmlSerializer.startDocument("UTF-8", true);
        xmlSerializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

        xmlSerializer.startTag("", "profile");
        xmlSerializer.attribute("", "name", profile.getName());
        for (int i = 0; i < profile.getContactsNum(); ++i) {
            xmlSerializer.startTag("", "contact");
            xmlSerializer.attribute("", "name", profile.get(i).getName());
            xmlSerializer.attribute("", "number", profile.get(i).getPhoneNumber());
            xmlSerializer.endTag("", "contact");
        }

        xmlSerializer.endTag("", "profile");
        xmlSerializer.endDocument();
        Intent i = new Intent();
        i.setAction(MainActivity.DATACHANGE_BROADCAST);
        context.sendBroadcast(i);
    }

    public BlockProfile getProfile() {
        return profile;
    }
}
