package com.vinay.vinplayer.activities;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.vinay.vinplayer.R;
import com.vinay.vinplayer.helpers.WiFiDirectBroadcastReceiver;

import java.util.ArrayList;
import java.util.List;

public class WifiTest extends AppCompatActivity {

    private final IntentFilter intentFilter = new IntentFilter();
    Channel mChannel;
    WiFiDirectBroadcastReceiver receiver;
    WifiP2pManager mManager;
    TextView textView;
    WifiP2pManager.PeerListListener peerListListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_test);

        textView= (TextView) findViewById(R.id.text);
        //  Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);


        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);

        receiver=new WiFiDirectBroadcastReceiver(mManager,mChannel,this,peerListListener);


        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        // Device is ready to accept incoming connections from peers.
                        Toast.makeText(getApplicationContext(), "P2P group creation started.",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reason) {
                        Toast.makeText(getApplicationContext(), "P2P group creation failed. Retry.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


        findViewById(R.id.connect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        textView.append("discovered");

                        final List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();


                        peerListListener= new WifiP2pManager.PeerListListener() {
                            @Override
                            public void onPeersAvailable(WifiP2pDeviceList peerList) {

                                List<WifiP2pDevice> refreshedPeers = (List<WifiP2pDevice>) peerList.getDeviceList();
                                if (!refreshedPeers.equals(peers)) {
                                    peers.clear();
                                    peers.addAll(refreshedPeers);

                                    // If an AdapterView is backed by this data, notify it
                                    // of the change.  For instance, if you have a ListView of
                                    // available peers, trigger an update.
                                   // ((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
                                   textView.append(peers.toString());
                                    // Perform any other updates needed based on the new list of
                                    // peers connected to the Wi-Fi P2P network.
                                }

                                if (peers.size() == 0) {
                                    Log.d("wifi", "No devices found");
                                    return;
                                }
                            }
                        };
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                       textView.append("no one found");
                    }
                });
            }
        });

    }

// Other methods omitted for brevity

    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        // Do something in response to the boolean you are supplied
        Toast.makeText(getApplicationContext(),isWifiP2pEnabled+"",Toast.LENGTH_SHORT).show();
        Log.d("wifi",isWifiP2pEnabled+"");
    }
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, intentFilter);

    }
    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

}