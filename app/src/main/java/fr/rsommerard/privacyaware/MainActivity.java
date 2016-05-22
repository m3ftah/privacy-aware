package fr.rsommerard.privacyaware;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import fr.rsommerard.privacyaware.wifidirect.WiFiDirectManager;
import fr.rsommerard.privacyaware.wifidirect.device.DeviceManager;

public class MainActivity extends AppCompatActivity {

    private DeviceManager mDeviceManager;
    private WiFiDirectManager mWiFiDirectManager;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDeviceManager = DeviceManager.getInstance(this);

        mWiFiDirectManager = WiFiDirectManager.getInstance(this);

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

        Button printDevicesButton = (Button) findViewById(R.id.button_print_devices);
        assert printDevicesButton != null;
        printDevicesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,
                        WiFiDirect.devicesListToString(mDeviceManager.getAllDevices()),
                        Toast.LENGTH_SHORT).show();
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
