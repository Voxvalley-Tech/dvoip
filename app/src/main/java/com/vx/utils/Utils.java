package com.vx.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;

import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

public class Utils {

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {

        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    public static String getDate(long milliSeconds, String dateFormat) {
        // Create a DateFormatter object for displaying date in specified
        // format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in
        // milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }



    public static String getContactName(final String remoteParty,
                                        Context mContext) {
        // TODO Auto-generated method stub
        String displayname = null;
        // final SipUri sipUri = new SipUri(remoteParty);
        // if(sipUri.isValid()){
        displayname = remoteParty;
        // }
        Uri mBaseUri, uri;
        String[] projection;
        mBaseUri = Contacts.Phones.CONTENT_FILTER_URL;
        projection = new String[]{android.provider.Contacts.People.NAME};
        try {
            Class<?> c = Class
                    .forName("android.provider.ContactsContract$PhoneLookup");
            mBaseUri = (Uri) c.getField("CONTENT_FILTER_URI").get(mBaseUri);
            projection = new String[]{"display_name"};
        } catch (Exception e) {
            e.printStackTrace();
        }

        uri = Uri.withAppendedPath(mBaseUri, Uri.encode(displayname));

        Cursor cursor = mContext.getContentResolver().query(uri, projection,
                null, null, null);

        String contactName = "";

        if (cursor != null) {

            if (cursor.moveToFirst()) {
                contactName = cursor.getString(0) + "%24%yes";

            } else {
                contactName = displayname + "%24%no";

            }

            cursor.close();
        }

        // cursor = null;

        return contactName;
    }


    public static boolean contactExists(Context context, String number) {
        // / number is the phone number
        Uri lookupUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number));
        String[] mPhoneNumberProjection = {PhoneLookup._ID,
                PhoneLookup.NUMBER, PhoneLookup.DISPLAY_NAME};
        Cursor cur = context.getContentResolver().query(lookupUri,
                mPhoneNumberProjection, null, null, null);
        try {
            if (cur.moveToFirst()) {
                return true;
            }
        } finally {
            if (cur != null)
                cur.close();
        }
        return false;
    }

    public static Bitmap getIMAGEPhoto(Context context, String phoneNumber) {

        // If OS version is more than Lollipop we ask all below permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!PermissionUtils.hasPermissions(context, PermissionUtils.CONTACTSPERMISSION)) {
                return null;
            }
        }

        Uri phoneUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNumber));
        Uri photoUri = null;
        ContentResolver cr = context.getContentResolver();
        try {
            Cursor contact = cr.query(phoneUri,
                    new String[]{ContactsContract.Contacts._ID}, null, null,
                    null);
            if (contact != null)
                if (contact.moveToFirst()) {
                    long userId = contact.getLong(contact
                            .getColumnIndex(ContactsContract.Contacts._ID));
                    photoUri = ContentUris.withAppendedId(
                            ContactsContract.Contacts.CONTENT_URI, userId);

                } else {
                    // Bitmap defaultPhoto =
                    // BitmapFactory.decodeResource(getResources(),
                    // R.drawable.incall_2_normal);
                    return null;
                }
            if (photoUri != null) {


                InputStream input = ContactsContract.Contacts
                        .openContactPhotoInputStream(cr, photoUri);
                if (input != null) {
                    return BitmapFactory.decodeStream(input);
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean getNetwork(Context context) {
         /* Checking the Internet connection*/
        try {
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            boolean is3g = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting();
            boolean isWifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting();

            if (!is3g && !isWifi) {
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    private static final String CHAR_LIST = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHARNUMBER_LIST = "abcdefghijklmnopqrstuvwxyz1234567890";

    /**
     * This method generates random string
     *
     * @return
     */
    public static String generateRandomString(int length, String type) {

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



    public static String encrypt_MD5(String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String convertSecondsToHMmSs(long seconds) {
        long s = seconds % 60;
        long m = (seconds / 60) % 60;
        long h = (seconds / (60 * 60)) % 24;
        return String.format("%d:%02d:%02d", h, m, s);
    }




}
