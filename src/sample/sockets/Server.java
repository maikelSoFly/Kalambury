package sample.sockets;

//
//     Project name: Kalambury
//
//     Created by maikel on 16.04.2017.
//     Copyright © 2017 Mikołaj Stępniewski. All rights reserved.
//


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import sample.models.CanvasPoint;
import sample.models.ClientThread;
import sample.models.ControlMessage;

import java.io.ObjectOutputStream;
import java.util.concurrent.ThreadLocalRandom;
import java.net.*;
import java.io.IOException;
import java.util.*;


public class Server implements Runnable {
    final private int portNumber;
    private ServerSocket serverSocket;
    private Set<ClientThread> clientThreadHashSet;
    private int roundNumber;
    private ClientThread drawingClient;
    private ObservableList<CanvasPoint> pointsArray = FXCollections.observableArrayList();
    private String word;
    private String[] wordsArray = {"Dom", "Pies", "Szafa", "Paszport", "Java",
                                   "Ogień", "Malarz", "Legia", "Telewizor", "Śpiewać", "Miasto", "Narty",
                                   "Ostry jak brzytwa", "Złota rączka", "Ikea", "Juwenalia"};
    private int wordsArraySize = 16;

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
        int randomNum = ThreadLocalRandom.current().nextInt(0, wordsArraySize);
        this.word = this.wordsArray[randomNum];
        System.out.println(word);
    }


    public void broadcast(ClientThread clientThreadFrom, CanvasPoint cp) throws IOException {
        if (!pointsArray.contains(cp))
            pointsArray.add(cp);

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
            pointsArray.clear();
            if(checkEndOfRound()) roundNumber++;
            selectDrawingClient();
            sendWord();
        }
    }

    private void sendDrawingPermission(ClientThread thread) throws IOException {
        ObjectOutputStream oos = thread.getOos();
        oos.writeObject(new ControlMessage(2));
        oos.flush();
        thread.setRoundsActive(roundNumber);
    }

    private void selectDrawingClient() throws IOException  {
        for(ClientThread thread : clientThreadHashSet) {
            if (!thread.getClientSocket().isClosed() && thread.getRoundsActive() < roundNumber && thread != drawingClient) {
                drawingClient = thread;
                sendDrawingPermission(thread);
                break;
            }
        }
    }

    private boolean checkEndOfRound() {
        for(ClientThread thread : clientThreadHashSet) {
            if(!thread.getClientSocket().isClosed() && thread.getRoundsActive() != roundNumber)
                return false;
        }
        return true;
    }

    private void sendArrayToClient(ClientThread clientThread) throws IOException {
        for(CanvasPoint pt : pointsArray) {
            ObjectOutputStream oos = clientThread.getOos();
            oos.writeObject(pt);
            oos.flush();
        }
    }

    public void checkGuess(ClientThread clientThread, String guess) throws IOException {
        System.out.println(guess +" from "+ clientThread.getClientSocket().getRemoteSocketAddress());
        guess = guess.toLowerCase();
        word = word.toLowerCase();

        if(guess.equals(word)) {
            ObjectOutputStream oos = clientThread.getOos();
            oos.writeObject(new ControlMessage(1));
            broadcast(clientThread, new ControlMessage(1));
            sendWord();
        }
    }

    private void sendWord() throws IOException {
        int randomNum = ThreadLocalRandom.current().nextInt(0, wordsArraySize);
        this.word = this.wordsArray[randomNum];
        System.out.println(word);
        ObjectOutputStream oos = drawingClient.getOos();
        oos.writeObject(word);
        oos.flush();
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
                    sendWord();
                }

                if(pointsArray.size() != 0) {
                    sendArrayToClient(ct);
                }

            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }
}