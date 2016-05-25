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

        Button startDiscoveryButton = (Button) findViewById(R.id.button_start_discovery);
        assert startDiscoveryButton != null;
        startDiscoveryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,
                        "Start discovery",
                        Toast.LENGTH_SHORT).show();

                mWiFiDirectManager.start();
            }
        });

        Button stopDiscoveryButton = (Button) findViewById(R.id.button_stop_discovery);
        assert stopDiscoveryButton != null;
        stopDiscoveryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,
                        "Stop discovery",
                        Toast.LENGTH_SHORT).show();

                mWiFiDirectManager.stop();
            }
        });
    }

    @Override
    protected void onDestroy() {
        Log.i(WiFiDirect.TAG, "onDestroy()");
        mWiFiDirectManager.stop();
        super.onDestroy();
    }


}
