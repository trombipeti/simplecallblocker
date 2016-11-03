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

public class NavigationAdapter extends BaseAdapter {

    private static final String SAVE_FILE_NAME = "blockprofiles.xml";

    private Context context;

    public NavigationAdapter(Context context) {
        this.context = context;
        loadProfiles();
    }

    @Override
    public int getCount() {
        return BlockProfilesSingleton.Instance().size() + 1;
    }

    @Override
    public Object getItem(int position) {
        if(position < BlockProfilesSingleton.Instance().size()) {
            return BlockProfilesSingleton.Instance().get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private String getItemName(Context context, int position) {
        if(position < BlockProfilesSingleton.Instance().size()) {
            return BlockProfilesSingleton.Instance().get(position).getName();
        } else {
            return context.getResources().getString(R.string.title_settings);
        }
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_list_item, null);
            TextView tvName = (TextView)convertView.findViewById(R.id.drawer_item_tv);
            tvName.setText(getItemName(parent.getContext(), position));


        } else {
            TextView tvName = (TextView)convertView.findViewById(R.id.drawer_item_tv);
            if(! tvName.getText().equals(getItemName(parent.getContext(), position))) {
                tvName.setText(getItemName(parent.getContext(), position));
            }
        }
        return convertView;
    }

    public void loadProfiles() {
        BlockProfilesSingleton.Instance().clear();
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();

            FileInputStream fIn = context.openFileInput(SAVE_FILE_NAME);

            parser.setInput(fIn, "UTF-8");

            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if( parser.getName().endsWith("profile")) {
                            String name = parser.getAttributeValue("", "name");
                            boolean enabled = (parser.getAttributeValue("", "enabled").equals(Boolean.toString(true)));
                            boolean block = (parser.getAttributeValue("", "blockall").equals(Boolean.toString(true)));

                            if (parser.getName().equalsIgnoreCase("profile")) {
                                Log.d("loadProfiles", "Profil: " + Boolean.toString(block));
                                BlockProfilesSingleton.Instance().addBlockProfile(new BlockProfile(name, block, enabled));
                            } else if (parser.getName().equalsIgnoreCase("defaultprofile")) {
                                Log.d("loadProfiles", "Alap profil: " + Boolean.toString(block));
                                BlockProfilesSingleton.Instance().setDefaultProfile(new BlockProfile(name, block, enabled));
                            }
                        }
                        break;
                    default:
                        break;
                }
                eventType = parser.next();
            }
        } catch (FileNotFoundException e) {
            /* Ignore this */
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        }
        Intent i = new Intent();
        i.setAction(MainActivity.DATACHANGE_BROADCAST);
        context.sendBroadcast(i);
    }

    public void saveProfiles() throws IOException {
        BlockProfilesSingleton profiles = BlockProfilesSingleton.Instance();
        XmlSerializer xmlSerializer = Xml.newSerializer();
        FileOutputStream fOut = context.openFileOutput(SAVE_FILE_NAME, Context.MODE_PRIVATE);

        xmlSerializer.setOutput(fOut, "UTF-8");

        xmlSerializer.startDocument("UTF-8", true);
        xmlSerializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

        xmlSerializer.startTag("", "blockprofiles");

        xmlSerializer.startTag("", "defaultprofile");
        xmlSerializer.attribute("", "name", profiles.getDefaultProfile().getName());
        xmlSerializer.attribute("", "enabled", Boolean.toString(profiles.getDefaultProfile().isEnabled()));
        xmlSerializer.attribute("", "blockall", Boolean.toString(profiles.getDefaultProfile().isAllBlock()));
        xmlSerializer.endTag("", "defaultprofile");
        for (int i = 1; i < BlockProfilesSingleton.Instance().size(); ++i) {
            xmlSerializer.startTag("", "profile");
            xmlSerializer.attribute("", "name", profiles.get(i).getName());
            xmlSerializer.attribute("", "enabled", Boolean.toString(profiles.get(i).isEnabled()));
            xmlSerializer.attribute("", "blockall", Boolean.toString(profiles.get(i).isAllBlock()));
            xmlSerializer.endTag("", "profile");
        }

        xmlSerializer.endTag("", "blockprofiles");
        xmlSerializer.endDocument();
    }

    public void addProfile(BlockProfile aProfile) {
        BlockProfilesSingleton.Instance().addBlockProfile(aProfile);
        try {
            saveProfiles();
        } catch (IOException e) {
            e.printStackTrace();
        }
        notifyDataSetChanged();
        Intent i = new Intent();
        i.setAction(MainActivity.DATACHANGE_BROADCAST);
        context.sendBroadcast(i);
    }

    public void removeProfile(int index) {
        BlockProfilesSingleton.Instance().removeBlockProfile(index);
        try {
            saveProfiles();
        } catch (IOException e) {
            e.printStackTrace();
        }
        notifyDataSetChanged();
        Intent i = new Intent();
        i.setAction(MainActivity.DATACHANGE_BROADCAST);
        context.sendBroadcast(i);
    }

    public void removeProfile(BlockProfile profile) {
        BlockProfilesSingleton.Instance().removeBlockProfile(profile);
        try {
            saveProfiles();
        } catch (IOException e) {
            e.printStackTrace();
        }
        notifyDataSetChanged();
        Intent i = new Intent();
        i.setAction(MainActivity.DATACHANGE_BROADCAST);
        context.sendBroadcast(i);
    }
}
