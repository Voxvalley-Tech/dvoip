/*
 * Copyright (c) 2017 Voxvalley, http://voxvalley.com/
 * All Right Reserved,
 *  <p>
 * NOTICE:  All information contained herein is, and remains the property of
 * Voxvalley and its suppliers, if any.
 *
 */

package com.vx.ui.recents;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.dvoip.R;
import com.vx.core.android.recents.RecentsModel;

public class RecentsDetailsAdapter extends BaseAdapter {

    ArrayList<RecentsModel> recentsList;
    Context mcontext;
    SharedPreferences sharedpreferences;

    private static LayoutInflater inflater = null;

    public RecentsDetailsAdapter(Context context,
                                 ArrayList<RecentsModel> recentsarray) {

        mcontext = context;
        recentsList = recentsarray;
        inflater = (LayoutInflater) mcontext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        sharedpreferences = mcontext.getSharedPreferences("MoSIP",
                Context.MODE_PRIVATE);

    }

    @Override
    public int getCount() {

        if (recentsList != null && recentsList.size() > 0) {
            return recentsList.size();
        } else {
            return 0;
        }

    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class Holder {
        TextView name_tv;
        TextView number_tv;
        TextView date_tv;
        TextView duration_tv;
        TextView calltype_tv;
        ImageView calltype_img;
        ImageView recents_profilepic_imageview, info_img;
        View divider;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Holder holder = new Holder();
        View rowView;

        rowView = inflater.inflate(R.layout.adapter_recent_details_item, null);
        holder.name_tv = (TextView) rowView
                .findViewById(R.id.recents_name_textview);
        holder.number_tv = (TextView) rowView
                .findViewById(R.id.recents_number_tv);
        holder.date_tv = (TextView) rowView
                .findViewById(R.id.recents_date_tv);
        holder.duration_tv = (TextView) rowView
                .findViewById(R.id.recents_duration_tv);

        holder.calltype_tv = (TextView) rowView.findViewById(R.id.recents_calltype_tv);
        holder.info_img = (ImageView) rowView.findViewById(R.id.info_img);
        holder.info_img.setVisibility(View.GONE);
        holder.recents_profilepic_imageview = (ImageView) rowView.findViewById(R.id.recents_profilepic_imageview);
        holder.recents_profilepic_imageview.setVisibility(View.GONE);
        holder.calltype_img = (ImageView) rowView.findViewById(R.id.recents_calltype_img);
        holder.divider = (View) rowView.findViewById(R.id.divider);


        RecentsModel cModel = recentsList.get(position);

        holder.calltype_tv.setVisibility(View.GONE);

        if((cModel.getCalltype()).equals("5"))
        {
            holder.calltype_img.setImageResource(R.drawable.reject);
            //holder.calltype_tv.setText("Reject:");
            //holder.calltype_tv.setTextColor(mcontext.getResources().getColor(R.color.recents_listitem_reject));


        }else if((cModel.getCalltype()).equals("3"))
        {
            holder.calltype_img.setImageResource(R.drawable.missed);
            //holder.calltype_tv.setText("Missed:");
            //holder.calltype_tv.setTextColor(mcontext.getResources().getColor(R.color.recents_listitem_missed));



        }
        else if((cModel.getCalltype()).equals("2"))
        {
            holder.calltype_img.setImageResource(R.drawable.outgoing);
            //holder.calltype_tv.setText("Outgoing:");
            //holder.calltype_tv.setTextColor(mcontext.getResources().getColor(R.color.recents_listitem_outgoing));


        }
        else if((cModel.getCalltype()).equals("1"))
        {
            holder.calltype_img.setImageResource(R.drawable.incoming);
            //holder.calltype_tv.setText("Incoming:");
            //holder.calltype_tv.setTextColor(mcontext.getResources().getColor(R.color.recents_listitem_income));


        }

        holder.name_tv.setSelected(true);
        holder.name_tv.setText(cModel.getName());

        try {
            holder.duration_tv.setText(convertSecondsToHMmSs(Long.parseLong(cModel.getDuration())));
        } catch (Exception e) {
            holder.duration_tv.setText(cModel.getDuration());
            e.printStackTrace();
        }


        //	holder.duration_tv.setText(cModel.getDuration());
        holder.number_tv.setText(cModel.getNumber());
       /* holder.number_tv.setVisibility(View.GONE);
        holder.name_tv.setVisibility(View.GONE);*/
        boolean is24fromat = DateFormat.is24HourFormat(mcontext);

        SimpleDateFormat formatter = null;

        try {


            if (is24fromat) {
                formatter = new SimpleDateFormat(
                        "dd/MM/yyyy HH:mm:ss");
            } else {

                formatter = new SimpleDateFormat(
                        "dd/MM/yyyy hh:mm:ss a");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        Date date = null;
        try {

            Calendar cal1 = Calendar.getInstance();
            cal1.setTimeInMillis(Long.parseLong(cModel.getDate()));

            //SimpleDateFormat formatter = new SimpleDateFormat(
            //		"dd/MM/yyyy HH:mm:ss a");
            String dateString = formatter.format(cal1.getTime());
            holder.date_tv.setText("" + dateString);

            //	date = formatter.parse(cModel.getDate());
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        //String dateString = formatter.format(date);

        //holder.name_tv.setText(cModel.getName());
        // holder.profilepic_tv.setImageResource(imageId[position]);

        return rowView;
    }

    public String convertSecondsToHMmSs(long seconds) {
        long s = seconds % 60;
        long m = (seconds / 60) % 60;
        long h = (seconds / (60 * 60)) % 24;

        if (h > 0) {
            return String.format("%0d:%02d:%02d", h, m, s);
        } else {
            return String.format("%02d:%02d", m, s);
        }
    }


}
