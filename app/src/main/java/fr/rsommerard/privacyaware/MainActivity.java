package fr.rsommerard.privacyaware;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;

import fr.rsommerard.privacyaware.wifidirect.WiFiDirectManager;
import fr.rsommerard.privacyaware.wifidirect.device.DeviceManager;
import fr.rsommerard.privacyaware.wifidirect.exception.NotStartedException;

public class MainActivity extends AppCompatActivity {

    private WiFiDirectManager mWiFiDirectManager;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            mWiFiDirectManager = new WiFiDirectManager(this);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        Button startButton = (Button) findViewById(R.id.button_start);
        assert startButton != null;
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,
                        "Start",
                        Toast.LENGTH_SHORT).show();

                mWiFiDirectManager.start(MainActivity.this);
            }
        });

        Button stopButton = (Button) findViewById(R.id.button_stop);
        assert stopButton != null;
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,
                        "Stop",
                        Toast.LENGTH_SHORT).show();

                mWiFiDirectManager.stop(MainActivity.this);
            }
        });

        Button connectButton = (Button) findViewById(R.id.button_connect);
        assert connectButton != null;
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,
                        "Connect",
                        Toast.LENGTH_SHORT).show();

                try {
                    mWiFiDirectManager.connect();
                } catch (NotStartedException e) {
                    // Nothing
                }
            }
        });

        Button disconnectButton = (Button) findViewById(R.id.button_disconnect);
        assert disconnectButton != null;
        disconnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,
                        "Disconnect",
                        Toast.LENGTH_SHORT).show();

                mWiFiDirectManager.disconnect();
            }
        });
    }

    @Override
    protected void onDestroy() {
        Log.i(WiFiDirect.TAG, "onDestroy()");
        mWiFiDirectManager.stop(MainActivity.this);
        super.onDestroy();
    }


}
