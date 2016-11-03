package com.trombipeti.simplecallblocker;

import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.ITelephony;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TelephonyReflection {

    private static ITelephony telephonyService;
    private static boolean Inited;

    public static void init(TelephonyManager telephonyManager) throws ClassNotFoundException,
            SecurityException, NoSuchMethodException, IllegalArgumentException,
            IllegalAccessException, InvocationTargetException,
            InstantiationException {

        Class clazz = Class.forName(telephonyManager.getClass().getName());
        Method method = clazz.getDeclaredMethod("getITelephony");
        method.setAccessible(true);
        telephonyService = (ITelephony) method.invoke(telephonyManager);

        Inited = true;
    }

    public static void endPhoneCall(final TelephonyManager telephonyManager) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException, ClassNotFoundException, RemoteException {


        Runtime runtime = Runtime.getRuntime();
        try {
            Log.d("ENDCALL", "service call phone 3 \n");
            runtime.exec("service call phone 3 \n");
        } catch (Exception exc) {
            if(! Inited) {
                TelephonyReflection.init(telephonyManager);
            }
            telephonyService.endCall();
            Log.e("ENDCALL", exc.getMessage());
            exc.printStackTrace();
        }
    }
}
