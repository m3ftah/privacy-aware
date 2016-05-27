package fr.rsommerard.privacyaware.wifidirect.connection;

import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import fr.rsommerard.privacyaware.WiFiDirect;
import fr.rsommerard.privacyaware.data.DataManager;

public class Active extends Thread implements Runnable {
    private static final int SOCKET_TIMEOUT = 5000;

    private final InetAddress mGroupOwnerAddress;
    private final DataManager mDataManager;

    public Active(final InetAddress groupOwnerAddress, final DataManager dataManager) {
        mGroupOwnerAddress = groupOwnerAddress;
        mDataManager = dataManager;
    }

    @Override
    public void run() {
        Socket socket = connect();

        if (socket == null) {
            Log.e(WiFiDirect.TAG, "Cannot open socket with the groupOwner");
            return;
        }

        try {
            sendMessage(socket, Protocol.HELLO);
            waitAndCheck(socket, Protocol.HELLO);
            socket.close();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }

    private void closeSocket(final Socket socket) {
        try {
            socket.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void sendMessage(final Socket socket, final String message) throws IOException {
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        objectOutputStream.writeObject(message);
        objectOutputStream.flush();

        Log.i(WiFiDirect.TAG, message + " sent");
    }

    /*private void sendData(final Socket socket) throws IOException, ClassNotFoundException {
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());

        Data data = mDataManager.getData();

        Data dataCleaned = new Data(null, data.getContent());
        objectOutputStream.writeObject(dataCleaned);
        objectOutputStream.flush();

        Log.d(WiFiDirect.TAG, data + " sent");

        waitAndCheck(socket, Protocol.ACK);
        mDataManager.removeData(data);
    }*/

    private void waitAndCheck(final Socket socket, final String message) throws IOException, ClassNotFoundException {
        ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
        String received = (String) objectInputStream.readObject();

        Log.i(WiFiDirect.TAG, received + " received");

        if (!message.equals(received)) {
            closeSocket(socket);
        }
    }

    private Socket connect() {
        InetSocketAddress inetSocketAddress =
                new InetSocketAddress(mGroupOwnerAddress, 54412);

        try {
            Socket socket = new Socket();
            socket.connect(inetSocketAddress, SOCKET_TIMEOUT);
            return socket;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
