package com.lunarstack.seniorproject;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;

/**
 * Created by danielslapelis on 4/16/18.
 */

public class MessagesListAdapter extends BaseAdapter {

    private Activity mActivity;
    private ArrayList<Message> mMessagesList;
    private static LayoutInflater mInflater = null;

    public MessagesListAdapter (Activity activity, ArrayList<Message> list) {
        this.mActivity = activity;
        this.mMessagesList = list;
        mInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mMessagesList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if (convertView == null) {
            vi = mInflater.inflate(R.layout.row, null);
        }

        Message curr = mMessagesList.get(position);

        // different cases for whether message was sent or received
        if(curr.getStatus() == 0) {
            TextView sent = (TextView) vi.findViewById(R.id.sentText);
            sent.setText(curr.getMessage());
        } else {
            TextView received = (TextView) vi.findViewById(R.id.receivedText);
            received.setText(curr.getMessage());
        }
        return vi;
    }
}
