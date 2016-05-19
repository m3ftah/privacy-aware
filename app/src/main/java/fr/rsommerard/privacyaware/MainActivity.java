package fr.rsommerard.privacyaware;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import fr.rsommerard.privacyaware.wifidirect.connection.ServiceDiscoveryManager;

public class MainActivity extends AppCompatActivity {

    private ServiceDiscoveryManager mServiceDiscoveryManager;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mServiceDiscoveryManager = ServiceDiscoveryManager.getInstance(this);

        Button mStartDiscoveryButton = (Button) findViewById(R.id.button_start_discovery);
        assert mStartDiscoveryButton != null;
        mStartDiscoveryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mServiceDiscoveryManager.start();
            }
        });

        Button mStopDiscoveryButton = (Button) findViewById(R.id.button_stop_discovery);
        assert mStopDiscoveryButton != null;
        mStopDiscoveryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mServiceDiscoveryManager.stop();
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mServiceDiscoveryManager.stop();
    }


}
