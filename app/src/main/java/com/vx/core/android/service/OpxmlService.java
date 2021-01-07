package com.vx.core.android.service;

import android.app.Dialog;
import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.vx.core.android.db.AccountsDB;
import com.vx.core.android.db.DataBaseHelper;
import com.vx.core.android.db.OPXMLDAO;
import com.vx.core.android.getaccounts.ProfileData;
import com.vx.ui.SplashActivity;
import com.vx.utils.Config;
import com.vx.utils.DataParser;
import com.vx.utils.PreferenceProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class OpxmlService extends IntentService {

    private SharedPreferences sp;
    private PreferenceProvider prefs;
    private Dialog popup_dialog;
    AccountsDB sipdb;
    private String username;
    private String password;
    private HashMap<String, String> oldbridgeInfo;

    String ip;
    String portRange;
    String brandpin;
    public int status;

    public OpxmlService() {
        super("opxmlservice");
        // TODO Auto-generated constructor stub
    }

    public OpxmlService(String name) {
        super(name);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void onHandleIntent(Intent paramIntent) {

        prefs = PreferenceProvider.getPrefInstance(getApplicationContext());
        username = prefs.getPrefString("login_username");
        password = prefs.getPrefString("login_password");
        brandpin = prefs.getPrefString("login_brandpin");
        sipdb = new AccountsDB(this);
        try {
            sp = getApplicationContext().getSharedPreferences("opxmllink",
                    MODE_PRIVATE);

            OPXMLDAO opxmldao2 = new OPXMLDAO(getApplicationContext());
            opxmldao2.getProvisionRecords(DataBaseHelper.PROVISION_BASE_TABLE);
            oldbridgeInfo = opxmldao2.getProvisionBaseInfo();
            opxmldao2.close();
            boolean ishitsuccess = false;
            out: for (int i = 0; i < 3; ++i) {

                ishitsuccess = false;

                // status =
                // DataParser.GetstaticData(username,password,sipdb,prefs);
                Log.i("Opxmlservice", "OPXML hot from Service");


                    // this.brandpin=brandpin;
                    // String[]
                    // ipPorts=SplashActivity.iplist.get(j).toString().split(":");
                    ip = Config.PRIMARY_OPXML_URL;// ipPorts[0];
                    portRange = "";// ipPorts[1];
                    status = DataParser.setOpxmlUDPNew(ip, portRange,
                            prefs.getPrefString("login_brandpin"),getApplicationContext());
                    if (status == 5) {
                        DataParser.setstaticData(username, password, sipdb,
                                prefs);
                    }
                    if (status != 3 && status != 0) {
                        ishitsuccess = true;
                        break out;
                    }


            }

            if (status == 5) {
                sendAlertBroadCast(status, false);
                assignUrl();
            } else {
                OPXMLDAO opxmldao = new OPXMLDAO(getApplicationContext());
                ArrayList<HashMap<String, String>> records = opxmldao
                        .getProvisionRecords(DataBaseHelper.PROVISION_BASE_TABLE);
                if (records.size() != 0 && status!=0) {
                    opxmldao.delete(DataBaseHelper.PROVISION_BASE_TABLE, null,
                            null);
                }
                opxmldao.close();
                showServerErrorMsg(status, false);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void assignUrl() {

        // isfip2=false;

        OPXMLDAO opxmldao = new OPXMLDAO(getApplicationContext());
        opxmldao.getProvisionRecords(DataBaseHelper.PROVISION_BASE_TABLE);
        HashMap<String, String> bridgeInfo = opxmldao.getProvisionBaseInfo();
        opxmldao.close();
        if (bridgeInfo != null) {
            String registrarIp = bridgeInfo.get(DataBaseHelper.Registrar_IP);
            String vpn = bridgeInfo.get(DataBaseHelper.Vpn);
            String ip = bridgeInfo.get(DataBaseHelper.Ip);
            String oldkey = bridgeInfo.get(DataBaseHelper.Oldkey);
            String newkey = bridgeInfo.get(DataBaseHelper.Newkey);
            Log.i("OpxmlService", "newkey=" + newkey);

            String en_pref = bridgeInfo.get(DataBaseHelper.Enpref);

            String key = bridgeInfo.get(DataBaseHelper.Key);
            String prefix = bridgeInfo.get(DataBaseHelper.Prefix);
            newkey = bridgeInfo.get(DataBaseHelper.Newkey);
            String size = bridgeInfo.get(DataBaseHelper.Size);
            String rereg = bridgeInfo.get(DataBaseHelper.Re_Reg);
            String sprt = bridgeInfo.get(DataBaseHelper.Sprt);
            String keep = bridgeInfo.get(DataBaseHelper.Keep);
            String fip = bridgeInfo.get(DataBaseHelper.Fip);
            String fports = bridgeInfo.get(DataBaseHelper.Fports);
            String encryption_type = bridgeInfo
                    .get(DataBaseHelper.Encryption_Type);
            // Toast.makeText(getApplicationContext(),
            // "values are before changed", 100).show();
            if (oldbridgeInfo != null)
                if (!ip.equals(oldbridgeInfo.get(DataBaseHelper.Ip))
                        || !registrarIp.equals(oldbridgeInfo
                        .get(DataBaseHelper.Registrar_IP))
                        || !vpn.endsWith(oldbridgeInfo.get(DataBaseHelper.Vpn))
                        || !size.equals(oldbridgeInfo.get(DataBaseHelper.Size))
                        || !encryption_type.equals(oldbridgeInfo
                        .get(DataBaseHelper.Encryption_Type))
                        || !prefix.equals(oldbridgeInfo
                        .get(DataBaseHelper.Prefix))
                        || !en_pref.equals(oldbridgeInfo
                        .get(DataBaseHelper.Enpref))
                        || !newkey.equals(oldbridgeInfo
                        .get(DataBaseHelper.Newkey))) {
                    // Toast.makeText(getApplicationContext(),
                    // "values are changed", 100).show();

                    updateProfileDB(username, password, sipdb, prefs,
                            bridgeInfo);
                    sendAlertBroadCast(status, true);
                }

        }

    }

    private void sendAlertBroadCast(int status, boolean isUpdate) {

        if (status == DataParser.OPXML_WRONG_BRAND_PIN
                || status == DataParser.OPXML_INACTIVE_BRAND_PIN || isUpdate) {
            invokeBroadCast(status, isUpdate);
        }

    }

    private void invokeBroadCast(int status, boolean isUpdate) {
        Intent intent = new Intent(getApplicationContext().getPackageName()
                + ".alertReceiver");

        intent.putExtra("status", "" + status);
        intent.putExtra("opxmlupdate", isUpdate);
        sendBroadcast(intent);
    }

    private void showServerErrorMsg(int status, boolean isUpdate) {

        invokeBroadCast(status, isUpdate);
    }

    public void updateProfileDB(String username, String password,
                                AccountsDB sipdb, PreferenceProvider prefs,
                                HashMap<String, String> bridgeInfo) {

        Random r = new Random();
        String st_rtpPortRange = "2048-9999";
        int Low = Integer.parseInt(st_rtpPortRange.split("-")[0]);
        int High = Integer.parseInt(st_rtpPortRange.split("-")[1]);
        int rtp_port = r.nextInt(High - Low) + Low;
        // rtrp = "" + rtp_port;
        prefs.setPrefString("rtrp", "" + rtp_port);

        prefs.setPrefString("switchip",
                bridgeInfo.get(DataBaseHelper.Registrar_IP));

        sipdb.open();

        sipdb.removeAll();

        sipdb.close();

        ArrayList<ProfileData> profiledatalist = new ArrayList<ProfileData>();

        ProfileData profiledata = new ProfileData();

        profiledata.setACCID(0);
        profiledata.setDISPLAYNAME("vox");
        profiledata.setSIPDOMAIN(""
                + bridgeInfo.get(DataBaseHelper.Registrar_IP));
        profiledata.setDOMAINPROXY("" + bridgeInfo.get(DataBaseHelper.Ip));
        profiledata.setSENDSIPKEEPALIVES(""
                + bridgeInfo.get(DataBaseHelper.Keep));
        profiledata.setAUDIOCODECSLISTBASIC("");
        profiledata.setKEEPALIVEEXP("" + bridgeInfo.get(DataBaseHelper.Re_Reg));
        profiledata.setAUTHNAME("");
        profiledata.setCALLERID("");
        profiledata.setUSERNAME("" + username);
        profiledata.setPASSWORD("" + password);

        profiledata.setXMPPDOMAIN("");

        profiledata.setXMPPFILETRANSDOMAIN("");
        profiledata.setXMPPPROXYPROTOCOL("");
        profiledata.setXMPPPROXYHOST("");
        profiledata.setXMPPUSERNAME("");
        profiledata.setXMPPPASSWORD("");
        profiledata.setXMPPPROXYUSERNAME("");
        profiledata.setXMPPPROXYPASSWORD("");
        profiledata.setSTUNSERVER("");
        profiledata.setVIDEOCODECSLIST("");
        profiledata.setVMACCESSCODE("");
        profiledata.setVMACCOUNT("");
        profiledata.setAPIURL("");
        profiledata.setAPIUSER("");
        profiledata.setAPIPASSWORD("");
        profiledata.setPUSHURL("");
        profiledata.setPREMAUDIOCODECSLIST("");

        profiledatalist.add(profiledata);

        sipdb.open();

        sipdb.addAccount(profiledatalist.get(0));

        sipdb.close();

        // return 5;

    }

}
