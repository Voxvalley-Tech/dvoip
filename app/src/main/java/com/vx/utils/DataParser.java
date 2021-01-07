/*
 * Copyright (c) 2017 Voxvalley, http://voxvalley.com/
 * All Right Reserved,
 *  <p>
 * NOTICE:  All information contained herein is, and remains the property of
 * Voxvalley and its suppliers, if any.
 *
 */

package com.vx.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Base64;
import android.util.Log;
import com.app.dvoip.R;
import com.github.siyamed.shapeimageview.BuildConfig;
import com.vx.core.android.db.AccountsDB;
import com.vx.core.android.db.DataBaseHelper;
import com.vx.core.android.db.OPXMLDAO;
import com.vx.core.android.db.OpxmlBean;
import com.vx.core.android.getaccounts.ProfileData;
import com.vx.core.android.utils.MethodHelper;

import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * This class contains all OPXML related operations
 */
@SuppressLint("NewApi")
public class DataParser {
    private static PreferenceProvider mPrefs;
    private static String TAG = "DataParser";
    public static String ip = "";
    public static String registrarIp = "";
    public static String header = "";
    public static String footer = "";
    public static String balance = "";
    public static String vpn = "off";
    public static String oldkey = "";
    public static String newkey = "000-000-000";
    public static String prefix = "000-000-000";
    public static String en_pref = "off";
    public static String key = "000-000-000";
    public static String size = "80";
    public static String rereg = "1800";
    public static String sprt = "12345";
    public static String rtrp = "";
    public static String keep = "5";
    public static String send_logs = "off";
    public static String fip = "";
    public static String fports = "1050;1150";

    public static String fports2 = "2100;3200";

    public static String fip2_gprs = "192.169.71.18";
    public static String fports2_gprs = "2100;3200";

    public static String compact_header = "off";

    public static String bal_udp_ip = "";
    public static String api_url = "";
    public static String api_usr = "";
    public static String api_pwd = "";
    public static String ads_url = "";
    public static String ads_status = "";
    public static String push_url = "";
    public static String sms = "";
    public static String filetransferurl = "";
    public static String signup_url = "";
    public static String webcdr = "";
    public static String OPXMLUerName = "";
    public static String encryption_type = "";
    public static String modern_key = "Thisismykey";
    public static String modern_level = "L4";
    public static String modern_algo = "Rainbow";
    public static String modern_matrix = "38,102,10,12,45,124,45,45,45,78,85,95,65,65,25,65";
    public static String modern_msize = "16";
    public static String modern_prefix = "003-007";
    // String response="";

    public static String brandPin;
    public String userName, passWord;
    private int UDP_SERVER_PORT;
    protected static int value;

    public final static int OPXML_WRONG_BRAND_PIN = 0;
    public final static int OPXML_SUCCESS = 5;
    public final static int OPXML_INACTIVE_BRAND_PIN = 2;
    public final static int OPXML_SERVER_ERROR = 3;
    public final static int OPXML_BALANCE_NOT_FOUND = 4;
    public final static int OPXML_OTHER_ERROR = 6;
    public final static int UNKNOWN_HOST_EXCEPTION = 7;

    public static String balance_completeLink;
    public static String bal_url = "";
    public static String helper_IP = "1.2.4.5";
    public static String ps1 = "";
    public static String ps2 = "";

    public static String cid = "";
    public static int index = 0;
    public static String email = "";

	/*public DataParser(String profileName, String sipUserName, String password) {
        this.brandPin = profileName;
		this.userName = sipUserName;
		this.passWord = password;
	}*/

    public static String getBalance(String url, String userName, String pwd, Context context) {

        balance = "";
        PreferenceProvider preferenceProvider = new PreferenceProvider(context);
        if (url.startsWith("http")) {
            balance = getvalueforbalance(url, userName, "");


        } else {

            try {
                byte[] data = Base64.decode(url, Base64.DEFAULT);
                String val = new String(data);

                System.out.println("val=" + val);
                String balance_url =
                        val.replace("u1s1er", userName);
                balance_url = balance_url.replace("zz", "&");

                balance_url = balance_url.replace("pwd", pwd);
                DatagramSocket ds = null;

                ds = new DatagramSocket();


                byte[] m = Base64.encode(balance_url.trim().getBytes(), Base64.DEFAULT);

                String[] bal_ip = bal_udp_ip.split(";");
                InetAddress aHost = InetAddress.getByName(bal_ip[0]);
                // InetAddress aHost = InetAddress.getByName("68.233.253.55");

                //String port_range="11460;11499";
                String port_range = bal_ip[1];
                String ports[] =
                        port_range.split("-");
                int startport = Integer.parseInt(ports[0]);
                int range = Integer.parseInt(ports[1].trim()) - startport;
                int idx =
                        new Random().nextInt(range);
                int serverPort = startport + idx;//


                DatagramPacket request = new DatagramPacket(m, m.length, aHost,
                        serverPort);
                ds.setSoTimeout(30000);
                ds.send(request);
                byte[] buffer
                        = new byte[ds.getReceiveBufferSize()];

                DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
                ds.receive(reply);

                byte[] resbytes = new byte[reply.getLength()];
                System.arraycopy(reply.getData(), 0, resbytes, 0, reply.getLength());
                balance = new String(resbytes);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (balance != null) {
            try {
                DecimalFormat dtime = new DecimalFormat(
                        "0.000");
                balance = dtime.format(Double
                        .parseDouble(balance));

            } catch (Throwable e) {

                if (balance != null && balance.contains("No such an account")) {
                    e.printStackTrace();
                }
            }
        }
      preferenceProvider.setPrefString(PreferenceProvider.BALANCE_VALUE, balance);
        return balance;

    }

    static String getvalueforbalance(String balance_link2, String userName2,
                                     String pwd) {

        balance = "";
        StringBuffer balance_value = new StringBuffer();

        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {

            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] certs, String authType) {
            }
        }};
        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection
                    .setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
        HostnameVerifier hv = new HostnameVerifier() {
            public boolean verify(String urlHostName, SSLSession session) {
                // Log.i(TAG,"Warning: URL Host: " + urlHostName +
                // " vs. "+ session.getPeerHost());
                /*return true;*/
                HostnameVerifier hv= HttpsURLConnection.getDefaultHostnameVerifier();

                return hv.verify(urlHostName, session);
            }
        };

        try {
            String balance_url = balance_link2;
            balance_url = balance_url.replace("zz", "&");
            balance_url = balance_url.replace("u1s1er", userName2);

            balance_url = balance_url.replace("pwd", pwd);
            Log.i(TAG, "HTTP getBalance: URL: " + balance_url);

            URL url2 = new URL(balance_url);
            HttpURLConnection connection = null;
            if (balance_url.startsWith("https")) {
                connection = (HttpsURLConnection) url2
                        .openConnection();
                ((HttpsURLConnection) connection).setHostnameVerifier(hv);
            } else {
                connection = (HttpURLConnection) url2
                        .openConnection();
            }
            connection.setConnectTimeout(30000);
            DataInputStream is = new DataInputStream(connection.getInputStream());
            int ch;
            while ((ch = is.read()) != -1) {
                balance_value.append((char) ch);
            }

            Log.i(TAG, "HTTP response code: " + connection.getResponseCode());
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                balance = balance_value.toString().trim();
            }

            /** This below code written for 66666 brandPin balance issue **/
            /*HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response = httpclient.execute(new HttpGet(balance_url));
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                HttpEntity entity = response.getEntity();
                balance = EntityUtils.toString(entity, "UTF-8");
                if (balance == null || balance.isEmpty()) {
                    balance = "";
                }
                balance = balance.trim();

                Log.i(TAG, "HTTP balance response: " + balance);
            }*/
            try {
                if (balance != null && balance.length() > 0) {
                    DecimalFormat dtime = new DecimalFormat(
                            "0.000");
                    balance = dtime.format(Double
                            .parseDouble(balance));
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
            balance = "";
        }

        return balance;
    }


    public static int setOpxmlUDPNew(String url, String UDP_SERVER_PORT2,
                                     String brandpin, Context context) {
        int status = OPXML_SUCCESS;
        try {
            // TODO Auto-generated method stub
            PreferenceProvider mPreferenceProvider = PreferenceProvider.getPrefInstance(context);
            String req_parameter = "";
            String result_parameter = "";
            // Log.v("URL", url + "  " + type);
            JSONObject jObj = null;
            String json_enc = "";

            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {

                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(
                        java.security.cert.X509Certificate[] certs,
                        String authType) {
                }

                public void checkServerTrusted(
                        java.security.cert.X509Certificate[] certs,
                        String authType) {
                }
            }};

            // Install the all-trusting trust manager
            try {
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc
                        .getSocketFactory());
                Log.i(TAG, "Trust manager");
            } catch (Throwable e) {
                e.printStackTrace();
            }

            HostnameVerifier hv = new HostnameVerifier() {
                public boolean verify(String urlHostName, SSLSession session) {
                    // Log.i(TAG,"Warning: URL Host: " + urlHostName +
                    // " vs. "+ session.getPeerHost());
                    /*return true;*/
                    HostnameVerifier hv= HttpsURLConnection.getDefaultHostnameVerifier();

                    return hv.verify(urlHostName, session);
                }
            };

            try {

                int req_int = 6;
                int res_int = 6;
                int brand_int = 8;
                int user_agent_int = 10;
                try {
                    req_int = DataParser.randInt(4, 7);
                    res_int = DataParser.randInt(4, 7);
                    brand_int = DataParser.randInt(8, 10);
                    user_agent_int = DataParser.randInt(11, 15);
                } catch(Exception e){
                    e.printStackTrace();
                }

                String user_agent = "";
                req_parameter = generateRandomStringhttps(req_int, "AN"); // randInt(6,9)
                result_parameter = generateRandomStringhttps(res_int, "A"); // randInt(6,9)
                String brandpin_key = generateRandomStringhttps(brand_int, "AN"); // 10

                brandpin ="dvoip";
                try {
                    user_agent = DataParser.generateRandomStringhttps(
                            user_agent_int, "AN");
                } catch(Exception e){
                    e.printStackTrace();
                    user_agent = "";
                }
                String urlParameters = req_parameter
                        + "="
                        + result_parameter
                        + "&"
                        + brandpin_key
                        + "="
                        + encryptString(brandpin, req_parameter
                        + result_parameter);// Base64.encodeToString((req_parameter+brandPin+result_parameter).getBytes(),
                StringBuffer balance_value = null; // DEFAULT);//;encryptString(brandPin,req_parameter+result_parameter);
                try {
                    URL urls = new URL(url);
                    HttpsURLConnection connection = (HttpsURLConnection) urls
                            .openConnection();
                    connection.setHostnameVerifier(hv);
                    connection.setRequestProperty("User-Agent", user_agent);
                    connection.setDoOutput(true);
                    connection.setRequestMethod("POST");

                    connection.setRequestProperty("Content-Type",
                            "application/x-www-form-urlencoded");
                    connection.setRequestProperty("charset", "utf-8");
                    connection
                            .setRequestProperty(
                                    "Content-Length",
                                    ""
                                            + Integer.toString(urlParameters
                                            .getBytes().length));
                    connection.setUseCaches(false);
                    connection.setReadTimeout(30000);// set the time out as 30
                    // secs

                    DataOutputStream wr = new DataOutputStream(
                            connection.getOutputStream());
                    wr.writeBytes(urlParameters);
                    wr.flush();
                    wr.close();
                    balance_value = new StringBuffer();
                    DataInputStream is = new DataInputStream(
                            connection.getInputStream());
                    int ch;
                    while ((ch = is.read()) != -1) {
                        balance_value.append((char) ch);
                    }

                    is.close();
                    connection.disconnect();
                } catch(Throwable e) {
                    e.printStackTrace();
                }
                try {
                    json_enc = encryptString(balance_value.toString(),
                            req_parameter + result_parameter);

                    jObj = new JSONObject(json_enc);
                    Log.i(TAG,"response :: " + jObj);

                    if (jObj.has("st")) {
                        status = Integer.parseInt(jObj.getString("st"));
                    } else {
                        status = Integer.parseInt(jObj.getString("status"));
                    }

                    OPXMLDAO opxmldao = null;
                    if (jObj.has("st")) {
                        status = Integer.parseInt(jObj.getString("st"));
                    } else {
                        status = Integer.parseInt(jObj.getString("status"));
                    }

                    opxmldao = new OPXMLDAO(context);
                    ArrayList<HashMap<String, String>> records = opxmldao
                            .getProvisionRecords(DataBaseHelper.PROVISION_BASE_TABLE);
                    if (records.size() != 0) {
                        opxmldao.delete(DataBaseHelper.PROVISION_BASE_TABLE, null,
                                null);
                    }

                    switch (status) {
                        case OPXML_INACTIVE_BRAND_PIN:
                            return OPXML_INACTIVE_BRAND_PIN;

                        case 3:
                            mPreferenceProvider.setPrefString(PreferenceProvider.APP_HEADER, "");
                            mPreferenceProvider.setPrefString(PreferenceProvider.APP_FOOTER, "");
                            return OPXML_WRONG_BRAND_PIN;

                        case 0:
                            return OPXML_SERVER_ERROR;

                        default:
                            break;
                    }
                    status = OPXML_SUCCESS;
                    // if (jObj.getString("status").equals("1"))
                    {

                        // Log.i(TAG,"Status code is O/F : "
                        // + jObj.getString("vpn"));

                        OpxmlBean tempbean = new OpxmlBean();

                        if (jObj.has("i")) {

                            ip = jObj.getString("i");
                            tempbean.ip=jObj.getString("i");
                        } else {

                            ip = jObj.getString("ip");
                            tempbean.ip=jObj.getString("ip");
                        }

                        if (jObj.has("ri")) {
                            registrarIp = jObj.getString("ri");
                            tempbean.registrarIp=jObj.getString("ri");
                        } else {

                            registrarIp = jObj.getString("registrarip");
                            tempbean.registrarIp=jObj.getString("registrarip");
                        }
                        // registrarIp="182.72.244.94:5080";
                        //registrarIp="96.31.69.52:5050";
                        if (jObj.has("h")) {
                            header = jObj.getString("h");
                            tempbean.header=jObj.getString("h");
                        } else {
                            header = jObj.getString("header");
                            tempbean.header=jObj.getString("header");
                        }
                        if (jObj.has("cid")) {
                            cid = jObj.getString("cid");
                            Log.i(TAG, "parseJsonData: after if cid:"+cid);
                        }
                        if (jObj.has("f")) {
                            footer = jObj.getString("f");
                            tempbean.footer=jObj.getString("f");
                        } else {
                            footer = jObj.getString("footer");
                            tempbean.footer=jObj.getString("footer");
                        }
                        if (jObj.has("v")) {
                            vpn = jObj.getString("v");
                            tempbean.vpn=jObj.getString("v");
                            // vpn="off";

                        } else {
                            vpn = jObj.getString("vpn");
                            tempbean.vpn=jObj.getString("vpn");
                            // vpn="off";
                        }
                        //vpn="off";

                        if (jObj.has("o")) {
                            oldkey = jObj.getString("o");
                            tempbean.oldkey=jObj.getString("o");
                        } else {
                            oldkey = jObj.getString("oldkey");
                            tempbean.oldkey=jObj.getString("oldkey");
                        }
                        if (jObj.has("n")) {
                            newkey = jObj.getString("n");
                            tempbean.newkey=jObj.getString("n");
                        } else {
                            newkey = jObj.getString("newkey");
                            tempbean.newkey=jObj.getString("newkey");
                        }
                        // newkey="0-0-0";
                        if (jObj.has("e")) {
                            en_pref = jObj.getString("e");
                            tempbean.en_pref=jObj.getString("e");
                        } else {
                            en_pref = jObj.getString("en_pref");
                            tempbean.en_pref=jObj.getString("en_pref");
                        }
                        if (jObj.has("ky")) {
                            key = jObj.getString("ky");
                            tempbean.key=jObj.getString("ky");
                        } else {
                            key = jObj.getString("key");
                            tempbean.key=jObj.getString("key");
                        }
                        // key="0-0-0";
                        if (jObj.has("p")) {
                            prefix = jObj.getString("p");
                            tempbean.prefix=jObj.getString("p");
                        } else {
                            prefix = jObj.getString("prefix");
                            tempbean.prefix=jObj.getString("prefix");
                        }
                        // prefix="0-0-0";
                        if (jObj.has("s")) {
                            size = jObj.getString("s");
                            tempbean.size=jObj.getString("s");
                        } else {
                            size = jObj.getString("size");
                            tempbean.size=jObj.getString("size");
                        }
                        // size="200";
                        if (jObj.has("r")) {
                            rereg = jObj.getString("r");
                            tempbean.rereg=jObj.getString("r");
                        } else {
                            rereg = jObj.getString("rereg");
                            tempbean.rereg=jObj.getString("rereg");
                        }
                        if (jObj.has("sp")) {
                            sprt = jObj.getString("sp");
                            tempbean.sprt=jObj.getString("sp");
                        } else {
                            sprt = jObj.getString("sprt");
                            tempbean.sprt=jObj.getString("sprt");
                        }
                        if (jObj.has("rp")) {
                            rtrp = jObj.getString("rp");
                            tempbean.rtrp=jObj.getString("rp");
                        } else {
                            rtrp = jObj.getString("rtrp");
                            tempbean.rtrp=jObj.getString("rtrp");
                        }
                        if (jObj.has("k")) {
                            keep = jObj.getString("k");
                            tempbean.keep=jObj.getString("k");
                        } else {
                            keep = jObj.getString("keep");
                            tempbean.keep=jObj.getString("keep");
                        }
                        if (jObj.has("b")) {
                            bal_url = jObj.getString("b");
                            tempbean.bal_url=jObj.getString("b");
                        } else {
                            bal_url = jObj.getString("balance");
                            tempbean.bal_url=jObj.getString("balance");
                        }
                        try {
                            if (jObj.has("sl")) {
                                send_logs = jObj.getString("sl");
                                tempbean.send_logs=jObj.getString("sl");
                                // send_logs="off";
                            } else {
                                send_logs = jObj.getString("sendlog");
                                tempbean.send_logs=jObj.getString("sendlog");
                            }
                        } catch (Exception e) {
                            System.out.println("sendlogs error");
                        }

                        String tempfip = "";
                        if (jObj.has("fi")) {
                            tempfip = jObj.getString("tempfip");
                        } else {
                            tempfip = jObj.getString("fips");
                        }

                        if (tempfip.trim().length() > 0)
                            fip = tempfip;

                        String tempfports = "";
                        if (jObj.has("fp")) {
                            tempfports = jObj.getString("fp");
                        } else {

                            tempfports = jObj.getString("fports");
                        }
                        if (tempfports.trim().length() > 0)
                            fports = tempfports;

                        try {
                            if (jObj.has("c")) {
                                compact_header = jObj.getString("c");
                                tempbean.compact_header=jObj.getString("c");
                            } else {
                                compact_header = jObj.getString("compact");
                                tempbean.compact_header=jObj.getString("compact");
                            }

                        } catch (Exception e) {
                            System.out.println("compact error");
                        }

                        if (jObj.has("bl")) {
                            bal_udp_ip = jObj.getString("bl");
                            tempbean.bal_udp_ip=jObj.getString("bl");
                        }

                        if (jObj.has("api_url")) {
                            api_url = jObj.getString("api_url");
                            tempbean.api_url=jObj.getString("api_url");
                        } else if (jObj.has("au")) {
                            api_url = jObj.getString("au");
                            tempbean.api_url=jObj.getString("au");
                        }

                        if (jObj.has("api_usr")) {
                            api_usr = jObj.getString("api_usr");
                            tempbean.api_usr=jObj.getString("api_usr");
                        } else if (jObj.has("an")) {
                            api_usr = jObj.getString("an");
                            tempbean.api_usr=jObj.getString("an");
                        }

                        if (jObj.has("api_pwd")) {
                            api_pwd = jObj.getString("api_pwd");
                            tempbean.api_pwd=jObj.getString("api_pwd");
                        } else if (jObj.has("ap")) {
                            api_pwd = jObj.getString("ap");
                            tempbean.api_pwd=jObj.getString("ap");
                        }

                        if (jObj.has("ads_url")) {
                            ads_url = jObj.getString("ads_url");
                            tempbean.ads_url=jObj.getString("ads_url");
                        } else if (jObj.has("adu")) {
                            ads_url = jObj.getString("adu");
                            tempbean.ads_url=jObj.getString("adu");
                        }

                        if (jObj.has("ads_status")) {
                            ads_status = jObj.getString("ads_status");
                            tempbean.ads_status=jObj.getString("ads_status");
                        } else if (jObj.has("ads")) {
                            ads_status = jObj.getString("ads");
                            tempbean.ads_status=jObj.getString("ads");
                        }

                        if (jObj.has("push_url")) {
                            push_url = jObj.getString("push_url");
                            tempbean.push_url=jObj.getString("push_url");
                        } else if (jObj.has("pu")) {
                            push_url = jObj.getString("pu");
                            tempbean.push_url=jObj.getString("pu");
                        }
                        if (jObj.has("sms")) {
                            sms = jObj.getString("sms");
                            tempbean.sms=jObj.getString("sms");
                        } else if (jObj.has("sa")) {
                            sms = jObj.getString("sa");
                            tempbean.sms=jObj.getString("sa");
                        }
                        if (jObj.has("filetransferurl")) {
                            filetransferurl = jObj.getString("filetransferurl");
                            tempbean.filetransferurl=jObj.getString("filetransferurl");
                        } else if (jObj.has("ftu")) {
                            filetransferurl = jObj.getString("ftu");
                            tempbean.filetransferurl=jObj.getString("ftu");
                        }

                        if (jObj.has("signup_url")) {
                            signup_url = jObj.getString("signup_url");
                            tempbean.signup_url=jObj.getString("signup_url");
                        } else if (jObj.has("sgu")) {
                            signup_url = jObj.getString("sgu");
                            tempbean.signup_url=jObj.getString("sgu");
                        }
                        if (jObj.has("webcdr")) {
                            webcdr = jObj.getString("webcdr");
                            tempbean.webcdr=jObj.getString("webcdr");
                        } else if (jObj.has("wc")) {
                            webcdr = jObj.getString("wc");
                            tempbean.webcdr=jObj.getString("wc");
                        }
                        if (jObj.has("email")) {
                            email = jObj.getString("email");
                        }
                        try {
                            if (jObj.has("et")) {
                                encryption_type = jObj.getString("et");
                                tempbean.encryption_type=jObj.getString("et");
                            } else {
                                if (jObj.has("encryption_type")) {
                                    encryption_type = jObj
                                            .getString("encryption_type");
                                    tempbean.encryption_type=jObj.getString("encryption_type");
                                }
                            }
                            if (jObj.has("ml")) {
                                modern_key = jObj.getString("mk");
                                tempbean.modern_key=jObj.getString("mk");
                            } else {
                                if (jObj.has("modern_key")) {
                                    modern_key = jObj.getString("modern_key");
                                    tempbean.modern_key=jObj.getString("modern_key");
                                }
                            }
                            if (jObj.has("ml")) {
                                modern_level = jObj.getString("ml");
                                tempbean.modern_level=jObj.getString("ml");
                            } else {
                                if (jObj.has("modern_level")) {
                                    modern_level = jObj
                                            .getString("modern_level");
                                    tempbean.modern_level=jObj.getString("modern_level");
                                }
                            }
                            if (jObj.has("ma")) {
                                modern_algo = jObj.getString("ma");
                                tempbean.modern_algo=jObj.getString("ma");
                            } else {
                                if (jObj.has("modern_algo")) {
                                    modern_algo = jObj.getString("modern_algo");
                                    tempbean.modern_algo=jObj.getString("modern_algo");
                                }
                            }
                            if (jObj.has("mm")) {
                                modern_matrix = jObj.getString("mm");
                                tempbean.modern_matrix=jObj.getString("mm");
                            } else {
                                if (jObj.has("modern_matrix")) {
                                    modern_matrix = jObj
                                            .getString("modern_matrix");
                                    tempbean.modern_matrix=jObj.getString("modern_matrix");
                                }
                            }
                            if (jObj.has("ms")) {
                                modern_msize = jObj.getString("ms");
                                tempbean.modern_msize=jObj.getString("ms");
                            } else {
                                if (jObj.has("modern_msize")) {
                                    modern_msize = jObj
                                            .getString("modern_msize");
                                    tempbean.modern_msize=jObj.getString("modern_msize");
                                }
                            }
                            if (jObj.has("mp")) {
                                modern_prefix = jObj.getString("mp");
                                tempbean.modern_prefix=jObj.getString("mp");
                            } else {
                                if (jObj.has("modern_prefix")) {
                                    modern_prefix = jObj
                                            .getString("modern_prefix");
                                    tempbean.modern_prefix=jObj.getString("modern_prefix");
                                }
                            }
                            mPreferenceProvider.setPrefString(PreferenceProvider.APP_HEADER, header);
                            mPreferenceProvider.setPrefString(PreferenceProvider.APP_FOOTER, footer);
                            mPreferenceProvider.setPrefString(PreferenceProvider.BALANCE_URL, bal_url);
                            mPreferenceProvider.setPrefString(PreferenceProvider.WEB_CDR_URL, webcdr);
                            long count = opxmldao.saveConfig(tempbean,
                                    DataBaseHelper.PROVISION_BASE_TABLE);
                            Log.i("DataParser", "Log base table count=" + count);

                            provisionBaselist = opxmldao
                                    .getProvisionRecords(DataBaseHelper.PROVISION_BASE_TABLE);
                            HashMap<String, String> provisionBaseInfo = opxmldao
                                    .getProvisionBaseInfo();
                            if (provisionBaseInfo != null) {
                                DataParser.setProvisionBaseInfo(provisionBaseInfo);
                            }





                        } catch(Exception e){
                            e.printStackTrace();
                        }

                    }

                } catch(Exception e){
                    status = DataParser.OPXML_SERVER_ERROR;
                    e.printStackTrace();
                }

            } catch(Throwable e) {
                e.printStackTrace();
                status = DataParser.OPXML_SERVER_ERROR;
                Log.e("DataParsing Exception :",
                        "Error:: Exception  " + e.getLocalizedMessage());
            }
        } catch(Throwable e) {
            e.printStackTrace();
        }
        /*
         * if(status==SERVER_ERROR){ status=
		 * setOpxmlUDP2(url,UDP_SERVER_PORT2,brandpin,type); }
		 */
        Log.i(TAG,"status at setopxmludp" + status);
        return status;

    }

    public static String encryptString(String str, String key) {
        Log.i(TAG,"encryption string ::: " + key);
        try {
            StringBuffer sb = new StringBuffer(str);
            int lenStr = str.length();
            int lenKey = key.length();

            // For each character in our string, encrypt it...
            for (int i = 0, j = 0; i < lenStr; i++, j++) {
                if (j >= lenKey)
                    j = 0; // Wrap 'round to beginning of key string.

                // XOR the chars together. Must cast back to char to avoid
                // compile error.

                sb.setCharAt(i, (char) (str.charAt(i) ^ key.charAt(j)));
            }
            return sb.toString();
        } catch(Exception e){
            e.printStackTrace();
            return "error";
        }

    }
    private static final String CHAR_LIST = "bdefghjkmnqstuwxy";
    private static final String CHARNUMBER_LIST = "bdefghjkmnqstuwxy1234567890";
    public static String generateRandomStringhttps(int length, String type) {

        StringBuffer randStr = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = 0;
            if (type.equals("AN")) {
                number = getRandomNumber(CHARNUMBER_LIST);
                char ch = CHARNUMBER_LIST.charAt(number);
                randStr.append(ch);
            } else if (type.equals("A")) {
                number = getRandomNumber(CHAR_LIST);
                char ch = CHAR_LIST.charAt(number);
                randStr.append(ch);
            }
        }
        return randStr.toString();
    }

    public synchronized static int setOpxmlUDP(String url, String UDP_SERVER_PORT2,
                                               String brandpin, Context context) {
        int status = OPXML_SUCCESS;
        brandPin = brandpin;
        try {
            String port = url.split(";")[1];
            url = url.split(";")[0];
            InetAddress aHost2 = InetAddress.getByName(url.substring(8));
            //to // remove // https:// Log.i("DataParser","host ip=" +
            aHost2.getHostAddress().toString();
            url = aHost2.getHostAddress().toString();


            //status=getOpxmlhttpStatus(url,  brandpin, context);
            //	status = setOpxmlUDPnew(url, UDP_SERVER_PORT2, brandpin, context);
            status = getOpxmlUDPStatus(url, port, brandpin, context);
        } catch (UnknownHostException ue) { // Added by Ramesh
            ue.printStackTrace();
            status = UNKNOWN_HOST_EXCEPTION;
        } catch (Exception e) {
            e.printStackTrace();
            status = OPXML_SERVER_ERROR;

        }

        return status;

    }

    static StringBuilder EncryptionKey = new StringBuilder();
    private static ArrayList<HashMap<String, String>> provisionBaselist;
    private static ArrayList<HashMap<String, String>> bridgelist;
    public static int status = 5;

    public static int parseJsonData(String jsonData, Context context) {
        PreferenceProvider preferenceProvider = PreferenceProvider.getPrefInstance(context);
        int status = OPXML_SUCCESS;
        OPXMLDAO opxmldao = null;
        try {

            JSONObject jObj = new JSONObject(jsonData);

            if (jObj.has("st")) {
                status = Integer.parseInt(jObj.getString("st"));
            } else {
                status = Integer.parseInt(jObj.getString("status"));
            }

            opxmldao = new OPXMLDAO(context);
            ArrayList<HashMap<String, String>> records = opxmldao
                    .getProvisionRecords(DataBaseHelper.PROVISION_BASE_TABLE);
            if (records.size() != 0) {
                opxmldao.delete(DataBaseHelper.PROVISION_BASE_TABLE, null,
                        null);
            }


            switch (status) {

                case OPXML_INACTIVE_BRAND_PIN:
                    opxmldao.close();
                    return OPXML_INACTIVE_BRAND_PIN;

                case 3:
                    opxmldao.close();
                    return OPXML_WRONG_BRAND_PIN;

                case 0:
                    opxmldao.close();
                    return OPXML_SERVER_ERROR;

                default:
                    break;

            }
            status = OPXML_SUCCESS;

            OpxmlBean tempbean = new OpxmlBean();

            if (jObj.has("ri")) {
                tempbean.registrarIp = jObj.getString("ri");
            } else {

                tempbean.registrarIp = jObj.getString("registrarip");
            }
            //	registrarIp = tempbean.registrarIp;
            // registrarIp="182.72.244.94:5080";
            //tempbean.registrarIp="204.15.169.44:2109";
            if (jObj.has("h")) {
                tempbean.header = jObj.getString("h");
            } else {
                tempbean.header = jObj.getString("header");
            }
            if (jObj.has("un")) {
                preferenceProvider.setPrefString("login_username", jObj.getString("un"));
            }
            if (jObj.has("cid")) {
                cid = jObj.getString("cid");
                Log.i(TAG, "parseJsonData: after if cid:"+cid);
            }
            //	header = tempbean.header;
            if (jObj.has("f")) {
                tempbean.footer = jObj.getString("f");
                DataParser.footer = jObj.getString("f");
            } else {
                tempbean.footer = jObj.getString("footer");
                DataParser.footer = jObj.getString("footer");
            }
            if (jObj.has("v")) {
                tempbean.vpn = jObj.getString("v");

            } else {
                tempbean.vpn = jObj.getString("vpn");
            }


            try {
                if (jObj.has("sl")) {
                    tempbean.send_logs = jObj.getString("sl");
                    // send_logs="off";
                } else if (jObj.has("sendlog")) {
                    tempbean.send_logs = jObj.getString("sendlog");
                }
                //		send_logs = tempbean.send_logs;
            } catch (Exception e) {
                Log.i("DataParser", "sendlogs error");
            }

			/*	if (jObj.has("ps1")) {
                    tempbean.ps1 = jObj.getString("ps1");
				}
				if (jObj.has("ps2")) {
					tempbean.ps2 = jObj.getString("ps2");
				}
				if (jObj.has("hl")) {
					tempbean.helper_IP = jObj.getString("hl");
				}*/
            if (jObj.has("b")) {
                tempbean.bal_url = jObj.getString("b");
            } else if (jObj.has("balance")) {
                tempbean.bal_url = jObj.getString("balance");
            }
            //tempbean.bal_url = bal_url;

            try {
                if (jObj.has("c")) {
                    tempbean.compact_header = jObj.getString("c");
                } else {
                    tempbean.compact_header = jObj.getString("compact");
                }
                compact_header = tempbean.compact_header;
            } catch (Exception e) {
                Log.i("DataParser", "compact error");
            }

            if (jObj.has("i")) {
                tempbean.ip = jObj.getString("i");
            } else {

                tempbean.ip = jObj.getString("ip");
            }
            // if(i==0){
            // bean.ip="82.72.244.90:5060";
            // }
            // bean.ip="182.72.244.90:5060";
            // ip=bean.ip;

            if (jObj.has("o")) {
                tempbean.oldkey = jObj.getString("o");
            } else {
                tempbean.oldkey = jObj.getString("oldkey");
            }
            //	oldkey = tempbean.oldkey;
            if (jObj.has("n")) {
                tempbean.newkey = jObj.getString("n");
            } else {
                tempbean.newkey = jObj.getString("newkey");
            }
                /*
                 * if(i<1){ bean.newkey="0-0-0"; }else{
				 * bean.newkey="003-085-005"; }
				 */
            //	newkey = tempbean.newkey;
            // newkey="0-0-0";
            if (jObj.has("e")) {
                tempbean.en_pref = jObj.getString("e");
            } else {
                tempbean.en_pref = jObj.getString("en_pref");
            }
            //	en_pref = tempbean.en_pref;
            if (jObj.has("ky")) {
                tempbean.key = jObj.getString("ky");
            } else {
                tempbean.key = jObj.getString("key");
            }
            //	key = tempbean.key;
            // key="0-0-0";
            if (jObj.has("p")) {
                tempbean.prefix = jObj.getString("p");
            } else {
                tempbean.prefix = jObj.getString("prefix");
            }
            //		prefix = tempbean.prefix;
            // 005-096-005
            // prefix="0-0-0";
            if (jObj.has("s")) {
                tempbean.size = jObj.getString("s");
            } else {
                tempbean.size = jObj.getString("size");
            }
            //	size = tempbean.size;
            // size="200";
            if (jObj.has("r")) {
                tempbean.rereg = jObj.getString("r");
            } else {
                tempbean.rereg = jObj.getString("rereg");
            }
            //	rereg = tempbean.rereg;
            if (jObj.has("sp")) {
                tempbean.sprt = jObj.getString("sp");
            } else {
                tempbean.sprt = jObj.getString("sprt");
            }
            //	sprt = tempbean.sprt;
            if (jObj.has("rp")) {
                tempbean.rtrp = jObj.getString("rp");
                rtrp = jObj.getString("rp");
            } else {
                tempbean.rtrp = jObj.getString("rtrp");
                rtrp = jObj.getString("rtrp");
            }
            //	rtrp = tempbean.rtrp;
            if (jObj.has("k")) {
                tempbean.keep = jObj.getString("k");
            } else {
                tempbean.keep = jObj.getString("keep");
            }

            if (jObj.has("bl")) {
                tempbean.bal_udp_ip = jObj.getString("bl");

            }


            String tempfip = "";
            if (jObj.has("fi")) {
                tempfip = jObj.getString("fi");
            } else if (jObj.has("fips")) {
                tempfip = jObj.getString("fips");
            }

            if (tempfip.trim().length() > 0)
                tempbean.fip = tempfip;

            String tempfports = "";
            if (jObj.has("fp")) {
                tempfports = jObj.getString("fp");
            } else if (jObj.has("fports")) {
                tempfports = jObj.getString("fports");
            }
            if (tempfports.trim().length() > 0)
                tempbean.fports = tempfports;

            if (jObj.has("et")) {
                tempbean.encryption_type = jObj.getString("et");
            } else {
                if (jObj.has("encryption_type")) {
                    tempbean.encryption_type = jObj
                            .getString("encryption_type");
                }
            }

            long count = opxmldao.saveConfig(tempbean,
                    DataBaseHelper.PROVISION_BASE_TABLE);
            Log.i("DataParser", "Log base table count=" + count);

            provisionBaselist = opxmldao
                    .getProvisionRecords(DataBaseHelper.PROVISION_BASE_TABLE);
            HashMap<String, String> provisionBaseInfo = opxmldao
                    .getProvisionBaseInfo();
            if (provisionBaseInfo != null) {
                DataParser.setProvisionBaseInfo(provisionBaseInfo);
            }


        } catch (Exception e) {
            status = OPXML_SERVER_ERROR;
            e.printStackTrace();
        }
        if (opxmldao != null)
            opxmldao.close();
        return status;

    }

    public static void setProvisionBaseInfo(
            HashMap<String, String> provisionBaseInfo) {

        registrarIp = provisionBaseInfo.get(DataBaseHelper.Registrar_IP);
        //registrarIp="192.168.4.72:6060";
        header = provisionBaseInfo.get(DataBaseHelper.Header);
        vpn = provisionBaseInfo.get(DataBaseHelper.Vpn);
        //vpn="off";
        send_logs = provisionBaseInfo.get(DataBaseHelper.Send_Log);
        bal_url = provisionBaseInfo.get(DataBaseHelper.Balance);

        helper_IP = provisionBaseInfo.get(DataBaseHelper.Helper_IP);
        ps1 = provisionBaseInfo.get(DataBaseHelper.P1);
        ps2 = provisionBaseInfo.get(DataBaseHelper.P2);

        setBridgeInfo(provisionBaseInfo);
    }

    public static void setBridgeInfo(HashMap<String, String> bridgeInfo) {

        ip = bridgeInfo.get(DataBaseHelper.Ip);
        oldkey = bridgeInfo.get(DataBaseHelper.Oldkey);
        newkey = bridgeInfo.get(DataBaseHelper.Newkey);
        Log.i("DataParser", "newkey=" + newkey);

        en_pref = bridgeInfo.get(DataBaseHelper.Enpref);

        key = bridgeInfo.get(DataBaseHelper.Key);

        prefix = bridgeInfo.get(DataBaseHelper.Prefix);

        size = bridgeInfo.get(DataBaseHelper.Size);
        rereg = bridgeInfo.get(DataBaseHelper.Re_Reg);
        sprt = bridgeInfo.get(DataBaseHelper.Sprt);
        keep = bridgeInfo.get(DataBaseHelper.Keep);
        fip = bridgeInfo.get(DataBaseHelper.Fip);
        fports = bridgeInfo.get(DataBaseHelper.Fports);
        encryption_type = bridgeInfo.get(DataBaseHelper.Encryption_Type);
        // encryption_type="latest";
        modern_key = bridgeInfo.get(DataBaseHelper.Modern_Key);
        modern_level = bridgeInfo.get(DataBaseHelper.Modern_Level);
        modern_algo = bridgeInfo.get(DataBaseHelper.Modern_Algo);
        modern_matrix = bridgeInfo.get(DataBaseHelper.Modern_Matrix);
        modern_msize = bridgeInfo.get(DataBaseHelper.Modern_Size);
        modern_prefix = bridgeInfo.get(DataBaseHelper.Modern_Prefix);
        bal_udp_ip = bridgeInfo.get(DataBaseHelper.Bal_udp_ip);
    }


    public static boolean sendProvisionHit(String brandpin, Context context) {
        boolean ishitsuccess = false;

        // int status;
    /*	int ipSize = SplashActivity.iplist.size();
        for (int j = 0; j < ipSize; ++j) {
			ishitsuccess = false;
			
			String[] ipPorts = SplashActivity.iplist.get(j).toString()
					.split(";");
			String pip = ipPorts[0];
			String portRange = ipPorts[1];

			status = DataParser.setOpxmlUDP(pip, portRange, brandpin, context);
			if (status != 3) {
				ishitsuccess = true;
				break;
			}

		}*/
        return ishitsuccess;
    }


    public static int setOpxmlUDPnew(String url, String UDP_SERVER_PORT2,
                                     String brandpin, Context context) {
        Log.v("URL", url);
        int status = OPXML_SUCCESS;
        JSONObject jObj = null;
        int UDP_SERVER_PORT;
        String response = "";
        String json_enc = "";
        // brandpin="goldenvoiz";
        String ports[] = UDP_SERVER_PORT2.split("-");
        int startport = Integer.parseInt(ports[0]);
        int range = Integer.parseInt(ports[1].trim()) - startport;
        int idx = new Random().nextInt(range);
        idx = idx > 2 ? idx - 2 : idx; // port range last two are not included
        // from server. so we are subtracted the
        // value.
        UDP_SERVER_PORT = startport + idx;// Integer.parseInt(random);

        DatagramSocket ds = null;

        try {
            int sourceport = randInt(1000, 60000);
            // InetAddress sourceAddr = InetAddress.getLocalHost();
            ds = new DatagramSocket(sourceport);
            EncryptionKey.setLength(0);
            String finalvalue = ""; // keyval.reverse().toString() +
            // encryptString(dialername,
            // keyval.reverse().toString()) +
            // padhexValue;
            generate_key(digit, len, diff);
            finalvalue = "m,5," + brandpin + "," + "";// encryptOrDecrypt("5," +
            // brandpin + "," + "");

            finalvalue = "m," + brandpin + "," + "5," + "1.2";

            finalvalue = encryptOrDecrypt(finalvalue);
            StringBuffer plaintext = new StringBuffer(finalvalue); // new
            // StringBuffer(dialername);

            byte[] m = plaintext.toString().getBytes();// encrypt(plaintext.toString(),
            // encryptionKey);
            InetAddress aHost = InetAddress.getByName(url);
            int serverPort = UDP_SERVER_PORT;
            DatagramPacket request = new DatagramPacket(m, m.length, aHost,
                    serverPort);
            ds.setSoTimeout(30000);
            ds.send(request);
            byte[] buffer = new byte[ds.getReceiveBufferSize()];

            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            ds.receive(reply);

            byte[] resbytes = new byte[reply.getLength()];
            System.arraycopy(reply.getData(), 0, resbytes, 0, reply.getLength());
            String res = new String(resbytes);
            String str_received = encryptOrDecrypt(res); // encryptString(res,
            // keyval.toString());
            // //
            // decrypt(resbytes,
            // encryptionKey);

            // Log.i("DataParser","Response:"+str_received);
            str_received.replace("\0", "");

            // str_received= str_received.substring(0,
            // str_received.length()-randvalue);

            Log.i("DataParser", "json response:" + url + "=" + str_received);

            status = parseJsonData(str_received, context);

            ds.disconnect();
            ds.close();

        } catch (SocketException e) {
            response = e.toString();
            e.printStackTrace();
            status = OPXML_SERVER_ERROR;
        } catch (UnknownHostException e) {
            response = e.toString();
            e.printStackTrace();
            status = OPXML_SERVER_ERROR;
        } catch (IOException e) {
            response = e.toString();
            e.printStackTrace();
            status = OPXML_SERVER_ERROR;
        } catch (Exception e) {
            response = e.toString();
            e.printStackTrace();
            status = OPXML_SERVER_ERROR;
        } finally {
            if (ds != null) {
                ds.close();
            }
        }

        return status;
    }

    /**
     * This method generates random numbers
     *
     * @return int
     */
    private static int getRandomNumber(String type) {
        int randomInt = 0;
        Random randomGenerator = new Random();
        randomInt = randomGenerator.nextInt(type.length());
        if (randomInt - 1 == -1) {
            return randomInt;
        } else {
            return randomInt - 1;
        }
    }

    public static int randInt(int min, int max) {
        Random rand = new Random();
        int random = rand.nextInt((max - min) + 1) + min;
        return random;
    }

    public static String getRandomPort(String[] ports) {
        int dx = new Random().nextInt(ports.length);
        return ports[dx];
    }

    public static int setstaticData(String username, String password,
                                    AccountsDB sipdb, PreferenceProvider prefs) {


        Random r = new Random();
        String st_rtpPortRange = "9000-20000";
        int Low = Integer.parseInt(st_rtpPortRange.split("-")[0]);
        int High = Integer.parseInt(st_rtpPortRange.split("-")[1]);
        int rtp_port = r.nextInt(High - Low) + Low;

        if (rtrp == null || rtrp.length() == 0) {
            rtrp = "" + rtp_port;
            prefs.setPrefString("rtrp", "" + rtp_port);
        }

        prefs.setPrefString("switchip", registrarIp);

        sipdb.open();

        sipdb.removeAll();

        sipdb.close();

        ArrayList<ProfileData> profiledatalist = new ArrayList<ProfileData>();

        ProfileData profiledata = new ProfileData();

        profiledata.setACCID(0);
        profiledata.setDISPLAYNAME("vox");
        profiledata.setSIPDOMAIN("" + registrarIp);
        profiledata.setDOMAINPROXY("" + ip);
        profiledata.setSENDSIPKEEPALIVES("" + keep);
        profiledata.setAUDIOCODECSLISTBASIC("");
        profiledata.setKEEPALIVEEXP("" + rereg);
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

        return 5;

    }


    public static int getOpxmlUDPStatus(String url, String UDP_SERVER_PORT2,
                                        String brandpin, Context context) {
        Log.v("URL", url);
        int status = OPXML_SUCCESS;
        JSONObject jObj = null;
        String json_enc = "";
        String response = "";
        //brandpin="goldenvoiz";
        String ports[] = UDP_SERVER_PORT2.split("-");
        int startport = Integer.parseInt(ports[0]);
        int range = Integer.parseInt(ports[1].trim()) - startport;
        int idx = new Random().nextInt(range);
        idx = idx > 2 ? idx - 2 : idx; // port range last two are not included
        // from server. so we are subtracted the
        // value.
        int UDP_SERVER_PORT = startport + idx;// Integer.parseInt(random);

        DatagramSocket ds = null;
        mPrefs = PreferenceProvider.getPrefInstance(context);
        try {
            String deviceId = MethodHelper.getWifiMacAddress(context);
            String userName = mPrefs.getPrefString("login_username");
            int sourceport = randInt(1000, 60000);
            //	InetAddress sourceAddr = InetAddress.getLocalHost();
            ds = new DatagramSocket(sourceport);
            EncryptionKey.setLength(0);
            String finalvalue = ""; //keyval.reverse().toString() + encryptString(dialername, keyval.reverse().toString()) + padhexValue;
            generate_key(digit, len, diff);
            Log.i("DataParser", "Brand pin is " + brandpin);
            //finalvalue = encryptOrDecrypt("5," + brandpin + "," + BuildConfig.VERSION_NAME + "");
            finalvalue = encryptOrDecrypt(userName + "," + "5," + deviceId + "," + brandpin + "," + context.getString(R.string.app_name) + "," + BuildConfig.VERSION_NAME + "");
            Log.i(TAG, "Final value " + finalvalue);
            String mystring = encryptOrDecrypt(finalvalue);
            Log.i(TAG, "Final value " + mystring);
            StringBuffer plaintext = new StringBuffer(finalvalue); // new
            // StringBuffer(dialername);

            byte[] m = plaintext.toString().getBytes();// encrypt(plaintext.toString(),
            // encryptionKey);
            InetAddress aHost = InetAddress.getByName(url);
            int serverPort = UDP_SERVER_PORT;
            DatagramPacket request = new DatagramPacket(m, m.length, aHost,
                    serverPort);
            //ds.setSoTimeout(30000);
            // To avoid delay in OPXML calls time out reduced to 3 seconds
            ds.setSoTimeout(3000);
            ds.send(request);
            byte[] buffer = new byte[ds.getReceiveBufferSize()];

            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            ds.receive(reply);

            byte[] resbytes = new byte[reply.getLength()];
            System.arraycopy(reply.getData(), 0, resbytes, 0, reply.getLength());
            String res = new String(resbytes);
            String str_received = encryptOrDecrypt(res); //encryptString(res, keyval.toString()); // decrypt(resbytes,
            //String str_received =encryptString(res,"AZBYCX"); 							// encryptionKey);

            //Log.i("DataParser",str_received);
            str_received.replace("\0", "");

            // str_received= str_received.substring(0,
            // str_received.length()-randvalue);

            Log.i(TAG, "getOpxmlUDPStatus, Response: " + str_received);

            status = parseJsonData(str_received, context);


            ds.disconnect();
            ds.close();

        } catch (SocketException e) {
            response = e.toString();
            e.printStackTrace();
            status = OPXML_SERVER_ERROR;
        } catch (UnknownHostException e) {
            response = e.toString();
            e.printStackTrace();
            //status = OPXML_SERVER_ERROR;
            status = UNKNOWN_HOST_EXCEPTION; // Added by Ramesh
        } catch (IOException e) {
            response = e.toString();
            e.printStackTrace();
            status = OPXML_SERVER_ERROR;
        } catch (Exception e) {
            response = e.toString();
            e.printStackTrace();
            status = OPXML_SERVER_ERROR;
        } finally {
            if (ds != null) {
                ds.close();
            }
        }

        return status;
    }


    static int digit = 67;
    static int len = 5;
    static int diff = 7;


    static void generate_key(int digit, int len, int diff) {
        int i;
        for (i = 0; i < len; i++) {
            EncryptionKey.append((char) digit);
            digit = digit - diff;
            while (digit > 126) {
                digit = digit - 126 + 32;
            }
        }

        Log.i("DataParser", "encryption key is " + EncryptionKey.toString());

    }


    static String encryptOrDecrypt(String packet) {
        int m, n;
        n = 0;
       // Log.i(TAG, "Final value " + packet);
        int len = packet.length();
        StringBuilder temp = new StringBuilder();
        for (m = 0; m < len; m++) {
            char ch = (char) (packet.charAt(m) ^ EncryptionKey.charAt(n));
            // Log.i("DataParser","ch="+ch);
            temp.append(ch);
            n++;
            if (n == EncryptionKey.length())
                n = 0;
        }


        return temp.toString();
    }

    /**
     * This method will send user feedback to Control panel
     *
     * @param primaryOpxmlUrl
     * @param feedBackString
     * @param context
     */
    public static void sendFeedBack(String primaryOpxmlUrl, String feedBackString, Context context) {
        DatagramSocket ds = null;
        try {
            String port = primaryOpxmlUrl.split(";")[1];
            primaryOpxmlUrl = primaryOpxmlUrl.split(";")[0];
            // commneted for testing
            /************************************************/
            /*InetAddress aHost2 = InetAddress.getByName(primaryOpxmlUrl.substring(8));
            //to // remove // https:// Log.i("DataParser","host ip=" +
            aHost2.getHostAddress().toString();
            primaryOpxmlUrl = aHost2.getHostAddress().toString();*/
            /*************************************************/
            String ports[] = port.split("-");
            int startport = Integer.parseInt(ports[0]);
            int range = Integer.parseInt(ports[1].trim()) - startport;
            int idx = new Random().nextInt(range);
            idx = idx > 2 ? idx - 2 : idx; // port range last two are not included
            // from server. so we are subtracted the
            // value.
            int UDP_SERVER_PORT = startport + idx;// Integer.parseInt(random);
            mPrefs = PreferenceProvider.getPrefInstance(context);
            int sourceport = randInt(3023,3025);
            ds = new DatagramSocket(sourceport);
            EncryptionKey.setLength(0);
            String finalvalue = "";
            generate_key(digit, len, diff);
            finalvalue = encryptOrDecrypt(feedBackString);
            Log.i(TAG, "Final value " + finalvalue);
            String mystring = encryptOrDecrypt(finalvalue);
            Log.i(TAG, "Final value " + mystring);
            StringBuffer plaintext = new StringBuffer(finalvalue); // new
            // StringBuffer(dialername);

            byte[] m = plaintext.toString().getBytes();// encrypt(plaintext.toString(),
            // encryptionKey);
            InetAddress aHost = InetAddress.getByName(primaryOpxmlUrl);
            int serverPort = UDP_SERVER_PORT;
            Log.i(TAG,"Port "+serverPort+"Lenth "+m.length/1024);
            DatagramPacket request = new DatagramPacket(m, m.length, aHost,
                    serverPort);
            //ds.setSoTimeout(30000);
            // To avoid delay in OPXML calls time out reduced to 3 seconds
            ds.setSoTimeout(30000);
            ds.send(request);
            byte[] buffer = new byte[ds.getReceiveBufferSize()];

            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
            ds.receive(reply);

            byte[] resbytes = new byte[reply.getLength()];
            System.arraycopy(reply.getData(), 0, resbytes, 0, reply.getLength());
            String res = new String(resbytes);
            String str_received = encryptOrDecrypt(res); //encryptString(res, keyval.toString()); // decrypt(resbytes,
            str_received.replace("\0", "");
            Log.i(TAG, "FeedBack, Response: " + str_received);
        } catch (UnknownHostException ue) { // Added by Ramesh
            ue.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ds != null) {
                ds.close();
            }
        }
    }
}
