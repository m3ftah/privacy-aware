package fr.rsommerard.privacyaware.wifidirect.connection;

import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import fr.rsommerard.privacyaware.WiFiDirect;
import fr.rsommerard.privacyaware.dao.Data;
import fr.rsommerard.privacyaware.data.DataManager;

public class Passive extends Thread implements Runnable {

    private final Socket mSocket;
    private final DataManager mDataManager;

    public Passive(final Socket socket, final DataManager dataManager) {
        mSocket = socket;
        mDataManager = dataManager;
    }

    @Override
    public void run() {
        try {
            waitAndCheck(Protocol.HELLO);
            sendMessage(Protocol.HELLO);
            mSocket.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            closeSocket();
        }
    }

    private void closeSocket() {
        try {
            mSocket.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void waitAndCheck(final String message) throws IOException, ClassNotFoundException {
        ObjectInputStream objectInputStream = new ObjectInputStream(mSocket.getInputStream());
        String received = (String) objectInputStream.readObject();

        Log.d(WiFiDirect.TAG, received + " received");

        if (!message.equals(received)) {
            closeSocket();
        }
    }

    private void sendMessage(final String message) throws IOException {
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(mSocket.getOutputStream());
        objectOutputStream.writeObject(message);
        objectOutputStream.flush();

        Log.d(WiFiDirect.TAG, message + " sent");
    }
}
