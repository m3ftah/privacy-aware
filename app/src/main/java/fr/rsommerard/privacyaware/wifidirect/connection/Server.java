package fr.rsommerard.privacyaware.wifidirect.connection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import fr.rsommerard.privacyaware.data.DataManager;
import fr.rsommerard.privacyaware.wifidirect.ServiceDiscoveryManager;

public class Server extends Thread implements Runnable {

    private final DataManager mDataManager;

    public Server(final DataManager dataManager) {
        mDataManager = dataManager;
    }

    @Override
    public void run() {
        ServerSocket serverSocket = initializeServerSocket();

        assert serverSocket != null;

        while (checkThreadStatus(serverSocket)) {
            process(serverSocket);
        }
    }

    private void process(final ServerSocket serverSocket) {
        try {
            Socket socket = serverSocket.accept();
            new Passive(socket, mDataManager).start();
        } catch (IOException e) {
            e.printStackTrace();
            interrupt();
        }
    }

    private ServerSocket initializeServerSocket() {
        try {
            return new ServerSocket(54412);
        } catch (IOException e) {
            return null;
        }
    }

    private boolean checkThreadStatus(final ServerSocket serverSocket) {
        if (Thread.currentThread().isInterrupted()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                return false;
            }

            return false;
        }

        return true;
    }
}
