package fr.rsommerard.privacyaware;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class P2PService extends Service {

    public final String TAG = "P2PService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
