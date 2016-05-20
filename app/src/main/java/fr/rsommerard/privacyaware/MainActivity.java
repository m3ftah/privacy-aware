package fr.rsommerard.privacyaware;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.List;

import fr.rsommerard.privacyaware.wifidirect.connection.ServiceDiscoveryManager;
import fr.rsommerard.privacyaware.wifidirect.device.DeviceManager;

public class MainActivity extends AppCompatActivity {

    private ServiceDiscoveryManager mServiceDiscoveryManager;
    private DeviceManager mDeviceManager;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WifiP2pManager manager = (WifiP2pManager) this.getSystemService(Context.WIFI_P2P_SERVICE);
        WifiP2pManager.Channel channel = manager.initialize(this, this.getMainLooper(), null);

        WiFiDirect.cleanAllGroupsRegistered(manager, channel);

        mDeviceManager = DeviceManager.getInstance(this);
        mServiceDiscoveryManager = ServiceDiscoveryManager.getInstance(this);

        Button mPrintDevicesButton = (Button) findViewById(R.id.button_print_devices);
        assert mPrintDevicesButton != null;
        mPrintDevicesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Print devices", Toast.LENGTH_SHORT).show();
                WiFiDirect.printDeviceList(mDeviceManager.getAllDevices());
            }
        });

        /*Button mStopDiscoveryButton = (Button) findViewById(R.id.button_stop_discovery);
        assert mStopDiscoveryButton != null;
        mStopDiscoveryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Stop discovery", Toast.LENGTH_SHORT).show();
                mServiceDiscoveryManager.stop();
            }
        });*/
    }

    @Override
    protected void onDestroy() {
        Log.i(WiFiDirect.TAG, "onDestroy()");
        super.onDestroy();
    }


}
