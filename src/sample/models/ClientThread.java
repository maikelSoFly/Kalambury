package sample.models;

import sample.sockets.Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Set;

//
//     Project name: Kalambury
//
//     Created by maikel on 25.04.2017.
//     Copyright © 2017 Mikołaj Stępniewski. All rights reserved.
//

public class ClientThread extends Thread {
    private Socket clientSocket;
    private Set<ClientThread> clientThreadHashSet;
    private Server server;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private int roundsActive;

    public ClientThread(Socket clientSocket, Set<ClientThread> clientThreadHashSet, Server server) {
        this.clientThreadHashSet = clientThreadHashSet;
        this.clientSocket = clientSocket;
        this.server = server;
        this.roundsActive = 0;
        try {
            this.oos = new ObjectOutputStream(clientSocket.getOutputStream());
            this.ois = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while(true) {

                Object obj = ois.readObject();
                if(obj instanceof CanvasPoint) {
                    server.broadcast(this, (CanvasPoint) obj);
                }
                else if(obj instanceof ControlMessage) {
                    server.broadcast(this, (ControlMessage) obj);
                }

            }
        } catch (ClassNotFoundException | IOException ex) {
            ex.printStackTrace();
        }
    }


    //GETTERS & SETTERS
    public Socket getClientSocket() {
        return clientSocket;
    }
    public void setClientSocket(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }
    public Set<ClientThread> getClientThreadHashSet() {
        return clientThreadHashSet;
    }
    public void setClientThreadHashSet(Set<ClientThread> clientThreadHashSet) {
        this.clientThreadHashSet = clientThreadHashSet;
    }
    public ObjectInputStream getOis() {
        return ois;
    }
    public void setOis(ObjectInputStream ois) {
        this.ois = ois;
    }
    public ObjectOutputStream getOos() {
        return oos;
    }
    public void setOos(ObjectOutputStream oos) {
        this.oos = oos;
    }
    public int getRoundsActive() {
        return roundsActive;
    }
    public void setRoundsActive(int roundsActive) {
        this.roundsActive = roundsActive;
    }
}
