/**
 * Copyright (c) 2017 Voxvalley, http://voxvalley.com/
 * All Right Reserved,
 * <p>
 * NOTICE:  All information contained herein is, and remains the property of
 * Voxvalley and its suppliers, if any.
 */

package com.vx.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * This class provides singleton instance for shared preference.
 */
public class PreferenceProvider {

    private static SharedPreferences sharedPref;
    private static PreferenceProvider preferenceProvider;
    private Editor mEdit;
    public static final String BALANCE_URL = "BalanceUrl";
    public static final String APP_HEADER = "AppHeader";
    public static final String APP_FOOTER = "AppFooter";
    public static final String BALANCE_VALUE = "BalanceValue";
    public static final String WEB_CDR_URL="WebCdrUrl";
    public static final String ISCALLLOGSUPDATED= "CallLogsUpdateWhenAppKill";

    public static PreferenceProvider getPrefInstance(Context applicationContext) {

        if (preferenceProvider == null) {
            preferenceProvider = new PreferenceProvider(applicationContext);

        }

        return preferenceProvider;

    }

    public PreferenceProvider(Context context){

        sharedPref = context.getSharedPreferences(Constants.SHARED_PREFERENCE_MOSIP,
                Context.MODE_PRIVATE);

    }

    public void setPrefString(String key, String val) {

        mEdit = sharedPref.edit();
        mEdit.putString(key, val);
        mEdit.commit();
    }

    public void setPrefBoolean(String key, boolean val) {

        mEdit = sharedPref.edit();
        mEdit.putBoolean(key, val);
        mEdit.commit();
    }

    public void setPrefInt(String key, int val) {

        mEdit = sharedPref.edit();
        mEdit.putInt(key, val);
        mEdit.commit();
    }

    public void setPrefFloat(String key, float val) {

        mEdit = sharedPref.edit();
        mEdit.putFloat(key, val);
        mEdit.commit();
    }

    public String getPrefString(String key) {

        return sharedPref.getString(key, "");
    }

    public boolean getPrefBoolean(String key) {

        return sharedPref.getBoolean(key, false);
    }

    public int getPrefInt(String key) {

        return sharedPref.getInt(key, 0);
    }

    public float getPrefFloat(String key) {

        return sharedPref.getFloat(key, 1);
    }

    public int getPrefInt(String key, int i) {
        return sharedPref.getInt(key, i);
    }

}
