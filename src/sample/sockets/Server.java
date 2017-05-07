package sample.sockets;

//
//     Project name: Kalambury
//
//     Created by maikel on 16.04.2017.
//     Copyright © 2017 Mikołaj Stępniewski. All rights reserved.
//


import sample.models.CanvasPoint;
import sample.models.ClientThread;
import sample.models.ControlMessage;

import java.io.ObjectOutputStream;
import java.net.*;
import java.io.IOException;
import java.util.*;


public class Server implements Runnable {
    final private int portNumber;
    private ServerSocket serverSocket;
    private Set<ClientThread> clientThreadHashSet;
    private int roundNumber;
    private ClientThread drawingClient;

    public static void main(String args[]) throws IOException{
        new Server(4444).run();
    }

    private Server(int portNumber) throws IOException {
        try {
            serverSocket = new ServerSocket(portNumber);
            serverSocket.setReuseAddress(true);
        } catch (IOException e) {
            System.out.println(e.getStackTrace());
        }
        this.portNumber = portNumber;
        this.clientThreadHashSet = Collections.synchronizedSet(new HashSet<ClientThread>());
        this.roundNumber = 1;
    }

    public void broadcast(ClientThread clientThreadFrom, CanvasPoint cp) throws IOException {
        for(ClientThread thread : clientThreadHashSet) {
            if(thread != clientThreadFrom && !thread.getClientSocket().isClosed()) {
                ObjectOutputStream oos = thread.getOos();
                oos.writeObject(cp);
                oos.flush();
            }
        }
    }

    public void broadcast(ClientThread clientThreadFrom, ControlMessage cm) throws IOException {
        for(ClientThread thread : clientThreadHashSet) {
            if(thread != clientThreadFrom && !thread.getClientSocket().isClosed()) {
                ObjectOutputStream oos = thread.getOos();
                oos.writeObject(cm);
                oos.flush();
            }
        }
        if(cm.getMessage() == 1) {
            if(checkEndOfRound()) roundNumber++;
            selectDrawingClient();
        }
    }

    public void sendDrawingPermission(ClientThread thread) throws IOException {
        ObjectOutputStream oos = thread.getOos();
        oos.writeObject(new ControlMessage(2));
        oos.flush();
        thread.setRoundsActive(roundNumber);
    }

    public void selectDrawingClient() throws IOException  {
        for(ClientThread thread : clientThreadHashSet) {
            if (!thread.getClientSocket().isClosed() && thread.getRoundsActive() < roundNumber && thread != drawingClient) {
                drawingClient = thread;
                sendDrawingPermission(thread);
                break;
            }
        }
    }

    public boolean checkEndOfRound() {
        for(ClientThread thread : clientThreadHashSet) {
            if (!thread.getClientSocket().isClosed() && thread.getRoundsActive() < roundNumber) {
                if(thread.getRoundsActive() != roundNumber) return false;
            }
        }
        return true;
    }


    @Override
    public void run() { //MAIN THREAD
        System.out.println("Waiting for clients");
        while(true) {
            try {
                ClientThread ct;
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " +clientSocket.getRemoteSocketAddress());
                clientThreadHashSet.add(ct = new ClientThread(clientSocket, clientThreadHashSet, this));
                System.out.println("Total clients: " +clientThreadHashSet.size());
                ct.start();
                
                if(clientThreadHashSet.size() == 1) {
                    drawingClient = ct;
                    sendDrawingPermission(ct);
                }
            } catch(IOException e) {
                e.printStackTrace();
            }

        }
    }
}