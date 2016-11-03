package com.trombipeti.simplecallblocker.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.trombipeti.simplecallblocker.MainActivity;
import com.trombipeti.simplecallblocker.TelephonyReflection;
import com.trombipeti.simplecallblocker.model.BlockProfile;
import com.trombipeti.simplecallblocker.model.BlockProfilesSingleton;
import com.trombipeti.simplecallblocker.model.Contact;

import java.lang.reflect.InvocationTargetException;
import java.util.EnumSet;
import java.util.LinkedHashSet;

public class CallListenerService extends Service {

    private final IBinder mBinder = new MyBinder();

    public static final String TAG = "CATEGORY_CALL_LISTENER";

    private TelephonyManager mTelephonyMgr;

    private CallListener mListener = new CallListener();

    private LinkedHashSet<String> numbersToBlock = new LinkedHashSet<>();

    private boolean allBlockActive = false;

    private DataUpdateReceiver mReceiver = new DataUpdateReceiver();


    public CallListenerService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        mTelephonyMgr.listen(mListener, PhoneStateListener.LISTEN_CALL_STATE);

        registerReceiver(mReceiver, new IntentFilter(MainActivity.DATACHANGE_BROADCAST));

        return Service.START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mReceiver);
        mTelephonyMgr.listen(mListener, PhoneStateListener.LISTEN_NONE);
    }

    private void addAllContacts(BlockProfile profile) {
        for(int i = 0;i<profile.getContactsNum(); ++i) {
            numbersToBlock.add(profile.get(i).getPhoneNumber());
        }
    }

    public LinkedHashSet<String> notifyChange() {
        numbersToBlock.clear();
        allBlockActive = false;
        for(int i = 0;i<BlockProfilesSingleton.Instance().size();++i) {
            Log.d("notifyChange", "At " + i + ". profile");
            BlockProfile iterProfile = (BlockProfilesSingleton.Instance().get(i));
            if(iterProfile.isEnabled()) {
                Log.d("notifyChange", "Profil enabled: " + iterProfile.getName());
                if(iterProfile.isAllBlock()) {
                    if( ! allBlockActive) {
                        numbersToBlock.clear();
                        allBlockActive = true;
                    }
                    addAllContacts(iterProfile);
                } else {
                    if( ! allBlockActive) {
                        addAllContacts(iterProfile);
                    }
                }
            }
        }
        Toast.makeText(getApplicationContext(), (allBlockActive ? "Enabling only " : "Blocking ") + numbersToBlock.size() + " numbers", Toast.LENGTH_SHORT).show();
        return numbersToBlock;
    }

    private class CallListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            // TODO Itten kell megkereseni, hogy az incomingNumbert blokkolom-e
            if (state == TelephonyManager.CALL_STATE_RINGING) {
                boolean shouldBlock = false;
                if( (allBlockActive && !numbersToBlock.contains(incomingNumber) ||
                   (!allBlockActive && numbersToBlock.contains(incomingNumber))) ) {
                    shouldBlock = true;
                }
                if(shouldBlock) {
                    try {
                        TelephonyReflection.endPhoneCall(mTelephonyMgr);
                    } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException | InstantiationException | ClassNotFoundException | RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
            Log.d("onCallStateChanged, state: ", Integer.toString(state));
        }
    }

    private class DataUpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("onReceive", "Received action " + intent.getAction());
            if(intent.getAction().equals(MainActivity.DATACHANGE_BROADCAST)) {
                notifyChange();
            }
        }
    }

    public class MyBinder extends Binder {
        public CallListenerService getService() {
            return CallListenerService.this;
        }

    }
}
