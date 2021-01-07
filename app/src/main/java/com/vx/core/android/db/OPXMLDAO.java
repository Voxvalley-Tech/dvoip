/*
 * Copyright (c) 2017 Voxvalley, http://voxvalley.com/
 * All Right Reserved,
 *  <p>
 * NOTICE:  All information contained herein is, and remains the property of
 * Voxvalley and its suppliers, if any.
 *
 */

package com.vx.core.android.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class OPXMLDAO extends OPXMLDBDAO {

    private static ArrayList<HashMap<String, String>> provisionBaselist = new ArrayList<HashMap<String, String>>();
    private static ArrayList<HashMap<String, String>> bridgelist = new ArrayList<HashMap<String, String>>();
    private static int index = 0;

    public OPXMLDAO(Context context) {
        super(context);
    }

    public synchronized long saveConfig(OpxmlBean opxmlBean, String tableName) {
        ContentValues values = new ContentValues();

        if (tableName.equals(DataBaseHelper.PROVISION_BASE_TABLE)) {

            values.put(DataBaseHelper.Registrar_IP, opxmlBean.registrarIp);
            values.put(DataBaseHelper.Header, opxmlBean.header);
            values.put(DataBaseHelper.Vpn, opxmlBean.vpn);
            values.put(DataBaseHelper.Balance, opxmlBean.bal_url);
            values.put(DataBaseHelper.Status, opxmlBean.status);
            values.put(DataBaseHelper.Send_Log, opxmlBean.send_logs);
            values.put(DataBaseHelper.Helper_IP, opxmlBean.helper_IP);
            values.put(DataBaseHelper.SmsAPI, opxmlBean.sms);
            values.put(DataBaseHelper.Sms_Status, opxmlBean.sms_Status);
            values.put(DataBaseHelper.Im, opxmlBean.im);
            values.put(DataBaseHelper.Threeway, opxmlBean.threeway);
            values.put(DataBaseHelper.Presence_Status,
                    opxmlBean.Presence_Status);
            values.put(DataBaseHelper.Compact_Status, opxmlBean.compact_header);
            values.put(DataBaseHelper.Expiry_Date, opxmlBean.expiry_Date);
            values.put(DataBaseHelper.P1, opxmlBean.ps1);
            values.put(DataBaseHelper.P2, opxmlBean.ps2);

            values.put(DataBaseHelper.Ip, opxmlBean.ip);
            values.put(DataBaseHelper.Key, opxmlBean.key);
            values.put(DataBaseHelper.Size, opxmlBean.size);
            values.put(DataBaseHelper.Oldkey, opxmlBean.oldkey);
            values.put(DataBaseHelper.Newkey, opxmlBean.newkey);
            values.put(DataBaseHelper.Prefix, opxmlBean.prefix);
            values.put(DataBaseHelper.Enpref, opxmlBean.en_pref);
            values.put(DataBaseHelper.Encryption_Type,
                    opxmlBean.encryption_type);
            values.put(DataBaseHelper.Modern_Key, opxmlBean.modern_key);
            values.put(DataBaseHelper.Modern_Level, opxmlBean.modern_level);
            values.put(DataBaseHelper.Modern_Algo, opxmlBean.modern_algo);
            values.put(DataBaseHelper.Modern_Matrix, opxmlBean.modern_matrix);
            values.put(DataBaseHelper.Modern_Size, opxmlBean.modern_msize);
            values.put(DataBaseHelper.Modern_Prefix, opxmlBean.modern_prefix);
            values.put(DataBaseHelper.Re_Reg, opxmlBean.rereg);
            values.put(DataBaseHelper.Sprt, opxmlBean.sprt);
            values.put(DataBaseHelper.Rtp_Port, opxmlBean.rtrp);
            values.put(DataBaseHelper.Keep, opxmlBean.keep);
            values.put(DataBaseHelper.Fip, opxmlBean.fip);
            values.put(DataBaseHelper.Fports, opxmlBean.fports);
            values.put(DataBaseHelper.successCount, "0");
            values.put(DataBaseHelper.Bal_udp_ip, opxmlBean.bal_udp_ip);
            values.put(DataBaseHelper.FailCount, "0");
            values.put(DataBaseHelper.Webcdr, opxmlBean.webcdr);
        }
        return database.insert(tableName, null, values);
    }

    public synchronized int delete(String tableName) {
        index = 0;
        if (tableName.equals(DataBaseHelper.PROVISION_BASE_TABLE)) {
            provisionBaselist.clear();

        } else if (tableName.equals(DataBaseHelper.PROVISION_TABLE)) {
            bridgelist.clear();
        }
        return database.delete(tableName, null, null);
    }

    public synchronized int delete(String tableName, String whereClause, String[] whereArgs) {
        index = 0;
        if (tableName.equals(DataBaseHelper.PROVISION_BASE_TABLE)) {
            provisionBaselist.clear();

        } else if (tableName.equals(DataBaseHelper.PROVISION_TABLE)) {
            bridgelist.clear();
        }
        return database.delete(tableName, whereClause, whereArgs);
    }

    public synchronized int update(OpxmlBean opxmlBean) {

        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.Registrar_IP, opxmlBean.registrarIp);
        values.put(DataBaseHelper.Vpn, opxmlBean.vpn);
        values.put(DataBaseHelper.Status, opxmlBean.status);
        values.put(DataBaseHelper.Send_Log, opxmlBean.send_logs);
        values.put(DataBaseHelper.Helper_IP, opxmlBean.helper_IP);
        values.put(DataBaseHelper.SmsAPI, opxmlBean.sms);
        values.put(DataBaseHelper.Sms_Status, opxmlBean.sms_Status);
        values.put(DataBaseHelper.Im, opxmlBean.im);
        values.put(DataBaseHelper.Threeway, opxmlBean.threeway);
        values.put(DataBaseHelper.Presence_Status, opxmlBean.Presence_Status);
        values.put(DataBaseHelper.Compact_Status, opxmlBean.compact_header);
        values.put(DataBaseHelper.Expiry_Date, opxmlBean.expiry_Date);
        values.put(DataBaseHelper.Ip, opxmlBean.ip);
        values.put(DataBaseHelper.Key, opxmlBean.key);
        values.put(DataBaseHelper.Size, opxmlBean.size);
        values.put(DataBaseHelper.Oldkey, opxmlBean.oldkey);
        values.put(DataBaseHelper.Newkey, opxmlBean.newkey);
        values.put(DataBaseHelper.Prefix, opxmlBean.prefix);
        values.put(DataBaseHelper.Enpref, opxmlBean.en_pref);
        values.put(DataBaseHelper.Encryption_Type, opxmlBean.encryption_type);
        values.put(DataBaseHelper.Modern_Key, opxmlBean.modern_key);
        values.put(DataBaseHelper.Modern_Level, opxmlBean.modern_level);
        values.put(DataBaseHelper.Modern_Algo, opxmlBean.modern_algo);
        values.put(DataBaseHelper.Modern_Matrix, opxmlBean.modern_matrix);
        values.put(DataBaseHelper.Modern_Size, opxmlBean.modern_msize);
        values.put(DataBaseHelper.Modern_Prefix, opxmlBean.modern_prefix);
        values.put(DataBaseHelper.Re_Reg, opxmlBean.rereg);
        values.put(DataBaseHelper.Sprt, opxmlBean.sprt);
        values.put(DataBaseHelper.Rtp_Port, opxmlBean.rtrp);
        values.put(DataBaseHelper.Keep, opxmlBean.keep);
        values.put(DataBaseHelper.Fip, opxmlBean.fip);
        values.put(DataBaseHelper.Fports, opxmlBean.fports);
        values.put(DataBaseHelper.Bal_udp_ip, opxmlBean.bal_udp_ip);
        values.put(DataBaseHelper.Webcdr, opxmlBean.webcdr);
        return database.update(DataBaseHelper.PROVISION_TABLE, values, null,
                null);
    }

    public synchronized int updateBridgeInfo(OpxmlBean opxmlBean, String whereClause,
                                             String[] whereArgs) {
        bridgelist.clear();
        ContentValues values = new ContentValues();
        /*values.put(DataBaseHelper.Registrar_IP, opxmlBean.registrarIp);
		values.put(DataBaseHelper.Vpn, opxmlBean.vpn);
		values.put(DataBaseHelper.Status, opxmlBean.status);
		values.put(DataBaseHelper.Send_Log, opxmlBean.send_logs);
		values.put(DataBaseHelper.Helper_IP, opxmlBean.helper_IP);
		values.put(DataBaseHelper.SmsAPI, opxmlBean.sms);
		values.put(DataBaseHelper.Sms_Status, opxmlBean.sms_Status);
		values.put(DataBaseHelper.Im, opxmlBean.im);
		values.put(DataBaseHelper.Threeway, opxmlBean.threeway);
		values.put(DataBaseHelper.Presence_Status, opxmlBean.Presence_Status);
		values.put(DataBaseHelper.Compact_Status, opxmlBean.compact_header);
		values.put(DataBaseHelper.Expiry_Date, opxmlBean.expiry_Date);*/
        values.put(DataBaseHelper.Ip, opxmlBean.ip);
        values.put(DataBaseHelper.Key, opxmlBean.key);
        values.put(DataBaseHelper.Size, opxmlBean.size);
        values.put(DataBaseHelper.Oldkey, opxmlBean.oldkey);
        values.put(DataBaseHelper.Newkey, opxmlBean.newkey);
        values.put(DataBaseHelper.Prefix, opxmlBean.prefix);
        values.put(DataBaseHelper.Enpref, opxmlBean.en_pref);
        values.put(DataBaseHelper.Encryption_Type, opxmlBean.encryption_type);
        values.put(DataBaseHelper.Modern_Key, opxmlBean.modern_key);
        values.put(DataBaseHelper.Modern_Level, opxmlBean.modern_level);
        values.put(DataBaseHelper.Modern_Algo, opxmlBean.modern_algo);
        values.put(DataBaseHelper.Modern_Matrix, opxmlBean.modern_matrix);
        values.put(DataBaseHelper.Modern_Size, opxmlBean.modern_msize);
        values.put(DataBaseHelper.Modern_Prefix, opxmlBean.modern_prefix);
        values.put(DataBaseHelper.Re_Reg, opxmlBean.rereg);
        values.put(DataBaseHelper.Sprt, opxmlBean.sprt);
        values.put(DataBaseHelper.Fip, opxmlBean.fip);
        values.put(DataBaseHelper.Fports, opxmlBean.fports);
        values.put(DataBaseHelper.Rtp_Port, opxmlBean.rtrp);
        values.put(DataBaseHelper.Keep, opxmlBean.keep);
        values.put(DataBaseHelper.successCount, "0");
        values.put(DataBaseHelper.FailCount, "0");
        return database.update(DataBaseHelper.PROVISION_TABLE, values,
                whereClause, whereArgs);
    }

    public synchronized ArrayList<HashMap<String, String>> getProvisionRecords(String tableName) {

        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        if (tableName.equals(DataBaseHelper.PROVISION_BASE_TABLE)) {

            setDBRecords(list, tableName);
		
			
			/*if(provisionBaselist.size()!=0){
				list=provisionBaselist;
			}else{
				setDBRecords(list,tableName);
			}*/
        } else if (tableName.equals(DataBaseHelper.PROVISION_TABLE)) {

            if (bridgelist.size() != 0) {
                list = bridgelist;
            } else {

                setDBRecords(list, tableName);
            }
        }
        return list;
    }

    private synchronized void setDBRecords(ArrayList<HashMap<String, String>> list, String tableName) {

        String query = "SELECT  *from " + tableName;

        Cursor cursor = database.rawQuery(query, null);
        // int i=0;

        while (cursor.moveToNext()) {
            // OpxmlBean opxmlBean = new OpxmlBean();
            HashMap<String, String> rowdata = new HashMap<String, String>();
            int count = cursor.getColumnCount();
            for (int i = 0; i < count; ++i) {
                Log.i("OPXMLDAO", cursor.getColumnName(i) + ":"
                        + cursor.getString(i));
                rowdata.put(cursor.getColumnName(i), cursor.getString(i));

            }
            list.add(rowdata);
        }

        if (tableName.equals(DataBaseHelper.PROVISION_BASE_TABLE)) {
            provisionBaselist.clear();
            provisionBaselist.addAll(list);
        } else if (tableName.equals(DataBaseHelper.PROVISION_TABLE)) {
            bridgelist.clear();
            bridgelist.addAll(list);
        }
        cursor.close();
    }

    public HashMap<String, String> getBridgeInfo() {
        Log.i("OPXMLDAO", "Index value=" + index + "bridgelist.size()" + bridgelist.size());
        if (index > (bridgelist.size() - 1)) {
            Log.i("OPXMLDAO", "Index null ");
            //index = 0;
            return null;
        }


        Log.i("OPXMLDAO", "==============================");
        HashMap<String, String> map = bridgelist.get(index);
        Log.i("OPXMLDAO", "==index=" + index);
        Set<String> keys = map.keySet();
        Iterator<String> iterator = keys.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Log.i("OPXMLDAO", " =" + key + "=" + map.get(key));
        }
        Log.i("OPXMLDAO", "==============================");
        return bridgelist.get(index);
    }

    public void incrementIndex() {
        ++index;
    }

    public void reSetIndex() {
        index = 0;
    }


    public long updateFailCount(String ip) {

        bridgelist.clear();
        //reSetIndex();
        ContentValues values = new ContentValues();
        values.put(DataBaseHelper.FailCount, 2);


        long result = database.update(DataBaseHelper.PROVISION_TABLE, values,
                DataBaseHelper.Ip + " = ?",
                new String[]{ip});
        Log.i("OPXMLDAO", "update row ip=" + ip + "count=" + result);
        // long result = database.update(DataBaseHelper.PROVISION_TABLE, values,
        //null,
        //  null);
        return result;
    }

    public long updateSucessCount(String ip) {
        return 0;
    }

    public HashMap<String, String> getProvisionBaseInfo() {
        if (provisionBaselist.size() > 0)
            return provisionBaselist.get(0);
        else
            return null;
    }

    public ArrayList<String> getProvisionIPs() {
        //String[] ips = new String[2];
        ArrayList<String> ips = new ArrayList<String>();
        if (provisionBaselist.size() > 0) {
            HashMap<String, String> rowRecord = provisionBaselist.get(0);
            ips.add(rowRecord.get(DataBaseHelper.P1).toString());
            ips.add(rowRecord.get(DataBaseHelper.P2).toString());
        }
        return ips;
    }

    public String getHelperIP() {

        HashMap<String, String> rowRecord = provisionBaselist.get(0);
        String helperip = rowRecord.get(DataBaseHelper.Helper_IP).toString();
        Log.i("OPXMLDAO", "helper ip=" + helperip);
        return helperip;
    }

}
