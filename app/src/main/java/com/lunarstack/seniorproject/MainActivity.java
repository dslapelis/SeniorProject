package com.lunarstack.seniorproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.Strategy;

import java.util.ArrayList;
import java.util.Random;

import static com.google.android.gms.nearby.connection.Payload.fromBytes;

/** A class that connects to Nearby Connections and provides convenience methods and callbacks. */
public class MainActivity extends ConnectionsActivity {

    private final String TAG = "com.lunarstack.com.seniorproject.MainActivity";
    private static final Strategy STRATEGY = Strategy.P2P_CLUSTER;
    private static final String SERVICE_ID =
            "com.lunarstack.com.seniorproject.SERVICE_ID";
    /**
     * The state of the app. As the app changes states, the UI will update and advertising/discovery
     * will start/stop.
     */
    private State mState = State.UNKNOWN;
    private String mName;

    private EditText mSendEditText;
    private Button mSendButton;
    private ListView mMessagesListView;
    private Button mUninstallButton;

    private String mDestructCode;

    private ArrayList<Message> mMessages;
    private MessagesListAdapter mMessagesListAdapter;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSendEditText = (EditText) findViewById(R.id.sendEditText);
        mSendButton = (Button) findViewById(R.id.sendButton);
        mMessagesListView = (ListView) findViewById(R.id.messagesList);
        mUninstallButton = (Button) findViewById(R.id.uninstallButton);

        mMessages = new ArrayList<>();
        //mMessagesListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mMessages);
        mMessagesListAdapter = new MessagesListAdapter(mMessages, getApplicationContext());
        mMessagesListView.setAdapter(mMessagesListAdapter);
        mMessagesListView.setDivider(null);

        mUninstallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uninstall();
            }
        });

        /**
         * onClickListener for sending messages.
         */
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String temp = mSendEditText.getText().toString();
                // if there is text to send, send it to our peer
                if(!temp.isEmpty()) {
                    // initialize a buffer to read in the first character of the edit box...if it is
                    // an exclamation point we will read the following text in as the self destruct
                    // value.
                    if(temp.charAt(0) == '!') {
                        mDestructCode = temp.substring(1, temp.length()); // set our destruct code
                        Message newMessage =
                                new Message(("***ALERT*** You've set your self-destruct code" +
                                        " to be \"" + mDestructCode + "\""), 1);
                        mMessages.add(newMessage);
                        mMessagesListAdapter.notifyDataSetChanged();

                        // clear textbox
                        mSendEditText.setText("");

                        // focuses on the bottom of the list
                        jumpToBottom();
                        return;
                    }

                    /**
                     * Convert string to byte array for transmission
                     */
                    byte[] array = temp.getBytes();
                    send(fromBytes(array));
                    mSendEditText.setText("");

                    Message newMessage = new Message(temp, 0);

                    /**
                     * Update the messages array list
                     */
                    mMessages.add(newMessage);
                    mMessagesListAdapter.notifyDataSetChanged();

                    /**
                     * Focuses the view on the bottom of the list
                     * to see the latest messages
                     */
                    jumpToBottom();
                }
            }
        });


        /**
         * Generates a random device name and starts advertising
         */
        mName = generateRandomName() + "_covert";
        setState(State.SEARCHING);
    }


    /**
     * Case for when a new endpoint is discovered -- automatically connects
     * @param endpoint
     */
    @Override
    protected void onEndpointDiscovered(Endpoint endpoint) {
        Log.d(TAG, endpoint.getName());
        // We found an advertiser!
        stopDiscovering();
        connectToEndpoint(endpoint);
    }

    /**
     * Immediately accept all incoming connections
     * @param endpoint
     * @param connectionInfo
     */
    @Override
    protected void onConnectionInitiated(Endpoint endpoint, ConnectionInfo connectionInfo) {
        // A connection to another device has been initiated! We'll use the auth token, which is the
        // same on both devices, to pick a color to use when we're connected. This way, users can
        // visually see which device they connected with.
        // We accept the connection immediately.
        acceptConnection(endpoint);
    }

    /**
     * If we are connected to an endpoint, change our state.
     * @param endpoint
     */
    @Override
    protected void onEndpointConnected(Endpoint endpoint) {
        Toast.makeText(
                this, getString(R.string.toast_connected, endpoint.getName()), Toast.LENGTH_SHORT)
                .show();
        setState(State.CONNECTED);
    }

    /**
     * If we disconnect from an endpoint, change our state.
     * @param endpoint
     */
    @Override
    protected void onEndpointDisconnected(Endpoint endpoint) {
        Toast.makeText(
                this, getString(R.string.toast_disconnected, endpoint.getName()), Toast.LENGTH_SHORT)
                .show();
        setState(State.SEARCHING);
    }

    /**
     * If our connection fails, try advertising for a new peer again.
     * @param endpoint
     */
    @Override
    protected void onConnectionFailed(Endpoint endpoint) {
        if (getState() == State.SEARCHING) {
            startDiscovering();
        }
    }

    /**
     * If we receive data from an endpoint, check to see if it contains our self destruct phrase.
     * If it does, uninstall the app. Otherwise, add it to our list and relay it to the peers we
     * are connected to (except for the peer who sent it).
     * @param endpoint The sender.
     * @param payload The data.
     */
    @Override
    protected void onReceive(Endpoint endpoint, Payload payload) {
        if(payload.getType() == Payload.Type.BYTES) {
            String message = new String(payload.asBytes());

            if(message.equals(mDestructCode)) {
                uninstall();
                return;
            }

            relay(payload, endpoint);

            Message newMessage = new Message(message, 1);
            mMessages.add(newMessage);
            mMessagesListAdapter.notifyDataSetChanged();
            jumpToBottom();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        setState(State.SEARCHING);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopAllEndpoints();
        wipe();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopAllEndpoints();
        wipe();
    }

    @Override
    protected String getName() {
        return mName;
    }

    @Override
    protected String getServiceId() {
        return SERVICE_ID;
    }

    @Override
    protected Strategy getStrategy() {
        return STRATEGY;
    }

    private void onStateChanged(State state) {
        switch(state) {
            case SEARCHING:
                disconnectFromAllEndpoints();
                startDiscovering();
                startAdvertising();
                break;
            case CONNECTED:
                //stopAdvertising();
                //startDiscovering();
                break;
            case UNKNOWN:
                stopAllEndpoints();
                break;

            default:
                // no-op
                break;
        }
    }

    /**
     * Generates our random device name.
     * @return
     */
    private static String generateRandomName() {
        String name = "";
        Random random = new Random();
        for (int i = 0; i < 5; i++) {
            name += random.nextInt(10);
        }
        return name;
    }

    /**
     * The state has changed.
     * @param state The new state.
     */
    private void setState(State state) {
        if (mState == state) {
            Log.d(TAG, "State set to " + state + " but already in that state");
            return;
        }

        logD("State set to " + state);
        mState = state;
        ((TextView) findViewById(R.id.stateTextView)).setText(mName + " -- " + state);
        onStateChanged(state);
    }

    /** @return The current state. */
    private State getState() {
        return mState;
    }

    /**
     * Focuses on the bottom of the list, as the default focus is the top...but our messages
     * are added to the bottom.
     */
    private void jumpToBottom() {
        // focuses on the bottom of the list
        mMessagesListView.setSelection(mMessagesListAdapter.getCount()-1);
    }

    /**
     * Uninstalls the app from the device.
     */
    private void uninstall() {
        wipe();

        Intent intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE);
        intent.setData(Uri.parse("package:com.lunarstack.seniorproject"));
        startActivity(intent);
    }

    private void wipe() {
        // clears every single message -- setting memory to null
        for (Message message : mMessages) {
            message.setMessage(null);
            message.setStatus(-1);
        }

        mMessages.clear();
        mMessagesListAdapter.notifyDataSetChanged();
    }


    /** States that the UI goes through. */
    public enum State {
        UNKNOWN,
        SEARCHING,
        CONNECTED
    }
}