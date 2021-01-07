/*
 * Copyright (c) 2017 Voxvalley, http://voxvalley.com/
 * All Right Reserved,
 *  <p>
 * NOTICE:  All information contained herein is, and remains the property of
 * Voxvalley and its suppliers, if any.
 *
 */

package com.vx.ui.contacts;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.app.dvoip.R;
import com.vx.core.android.contacts.ContactsDetailsModel;
import com.vx.ui.Home;
import com.vx.ui.dialpad.DialerFragment;
import com.vx.utils.PreferenceProvider;

public class ContactsDetailsAdapter extends BaseAdapter {

    ArrayList<ContactsDetailsModel> contactsdetailsList;
    Context mcontext;
    String contacttype;
    private static LayoutInflater inflater = null;
    PreferenceProvider prefprovider;

    public ContactsDetailsAdapter(Context context,
                                  ArrayList<ContactsDetailsModel> contactsdetailsarray, String contactType) {
        mcontext = context;
        contacttype = contactType;
        contactsdetailsList = contactsdetailsarray;
        prefprovider = PreferenceProvider.getPrefInstance(context);

        inflater = (LayoutInflater) mcontext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);


    }

    @Override
    public int getCount() {
        if (contactsdetailsList != null && contactsdetailsList.size() > 0) {
            return contactsdetailsList.size();
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

        TextView phonenumber_tv;
        TextView contacttype_tv;
        LinearLayout listitem_linear;
        ImageView call_image;
        ImageView chat_image;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Holder holder = new Holder();

        View rowView;

        rowView = inflater.inflate(R.layout.contactdetails_list_item, null);

        holder.phonenumber_tv = (TextView) rowView
                .findViewById(R.id.contacts_dtails_phonenumber_tv);
        holder.contacttype_tv = (TextView) rowView
                .findViewById(R.id.contacts_dtails_phonenumber_type_tv);

        holder.call_image = (ImageView) rowView
                .findViewById(R.id.contact_details_call_img);

        holder.chat_image = (ImageView) rowView
                .findViewById(R.id.contact_details_chat_img);

        holder.listitem_linear = (LinearLayout) rowView.findViewById(R.id.listitem_linear);

        final ContactsDetailsModel cModel = contactsdetailsList.get(position);

        holder.phonenumber_tv.setText(cModel.getContactNumber());

        if (contacttype.equals("app")) {
            holder.chat_image.setVisibility(View.VISIBLE);
        } else {
            holder.chat_image.setVisibility(View.INVISIBLE);
        }

        String type = cModel.getContactType();

        holder.contacttype_tv.setText(cModel.getContactType());

        try {
            if (type.equals("2") && type != null) {
                holder.contacttype_tv.setText(mcontext.getResources().getString(R.string.mobile));
            } else if (type.equals("1") && type != null) {
                holder.contacttype_tv.setText(mcontext.getResources().getString(R.string.home));
            } else if (type.equals("3") && type != null) {
                holder.contacttype_tv.setText(mcontext.getResources().getString(R.string.work));
            } else if (type.equals("4") && type != null) {
                holder.contacttype_tv.setText(mcontext.getResources().getString(R.string.fax));
            } else if (type.equals("5") && type != null) {
                holder.contacttype_tv.setText(mcontext.getResources().getString(R.string.fax));
            } else if (type.equals("6") && type != null) {
                holder.contacttype_tv.setText(mcontext.getResources().getString(R.string.pager));
            } else if (type.equals("7") && type != null) {
                holder.contacttype_tv.setText(mcontext.getResources().getString(R.string.other));
            } else if (type.equals("8") && type != null) {
                holder.contacttype_tv.setText("Callback");
            } else if (type.equals("0") && type != null) {
                holder.contacttype_tv.setText("Custom");

            } else {
                holder.contacttype_tv.setText("Number");

            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        holder.listitem_linear.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                    if (Home.mViewPager != null) {
                        Home.mViewPager.setCurrentItem(1);
                    }
                }

                if (DialerFragment.dialerPhoneNumberEditText != null) {
                    DialerFragment.dialerPhoneNumberEditText.setText(cModel.getContactNumber());
                    DialerFragment.dialerPhoneNumberEditText.setSelection(DialerFragment.dialerPhoneNumberEditText.getText().length());
                }

                ((Activity) mcontext).finish();
            }
        });
        return rowView;
    }
}

