package com.lunarstack.seniorproject;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;

/**
 * Created by daniel slapelis on 4/16/18.
 */

public class MessagesListAdapter extends ArrayAdapter<Message> {

    Context mContext;
    private ArrayList<Message> mMessagesList;
    private static LayoutInflater mInflater = null;
    final String TAG = "Message Adapter";

    public MessagesListAdapter (ArrayList<Message> list, Context context) {
        super(context, R.layout.row, list);
        this.mContext = context;
        this.mMessagesList = list;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mMessagesList.size();
    }

    @Override
    public Message getItem(int position) {
        return mMessagesList.get(position);
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
        Log.d(TAG, "Message status: " + curr.getStatus());


        TextView message = (TextView) vi.findViewById(R.id.message);
        message.setText(curr.getMessage());

        return vi;
    }
}
