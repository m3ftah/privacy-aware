package fr.rsommerard.privacyaware;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button startServiceButton = (Button) findViewById(R.id.button_start_service);
        startServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService();
            }
        });

        Button stopServiceButton = (Button) findViewById(R.id.button_stop_service);
        stopServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService();
            }
        });
    }

    private void startService() {
        Log.d(TAG, "startService P2PService");
        Toast.makeText(this, "P2PService started", Toast.LENGTH_SHORT).show();
        startService(new Intent(this, P2PService.class));
    }

    private void stopService() {
        Log.d(TAG, "stopService P2PService");
        Toast.makeText(this, "P2PService stoped", Toast.LENGTH_SHORT).show();
        stopService(new Intent(this, P2PService.class));
    }
}
