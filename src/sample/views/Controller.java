package sample.views;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import sample.Main;
import sample.models.*;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;


public class Controller implements Initializable {
    @FXML
    private Canvas canvas;
    @FXML
    private ColorPicker colorPicker;
    @FXML
    private Label lblSize;
    @FXML
    private Label lblConnectionState;
    @FXML
    private Label lblTurn;

    private GraphicsContext gc;
    private BufferedImage bi;

    private Main mainApp;
    private IntegerProperty size;
    private Color color;
    private ObservableList<CanvasPoint> pointsArray = FXCollections.observableArrayList();
    private int amountOfPointsInLastSet;
    private int lastIndex;

    //Sockets
    final private String serverIpAddress = "192.168.1.5";
    final private short portNumber = 4444;
    private Socket clientSocket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private boolean isConnected;
    private boolean isMyTurn;


    public Controller() {
        this.size = new SimpleIntegerProperty(4);
        this.amountOfPointsInLastSet = 0;
        this.color = Color.GREEN;
        this.isConnected = false;
        this.isMyTurn = false;
        this.lastIndex = 0;
        this.clientSocket = null;
        this.bi = new BufferedImage(400, 400, BufferedImage.TYPE_INT_RGB);
        this.size.addListener((observable, oldValue, newValue) -> {
            lblSize.setText(newValue.toString());
        });
//        this.pointsArray.addListener((ListChangeListener<CanvasPoint>) c -> {
//            while(c.next()) {
//                if(c.wasAdded()) {
//                    System.out.println("Added point. Size: " + pointsArray.size() +" "+ pointsArray.get(pointsArray.size()-1).isBreaking());
//                }
//                else if(c.wasRemoved()) {
//                    System.out.println("Removed points. Size: " +pointsArray.size());
//                }
//            }
//        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.drawImage(SwingFXUtils.toFXImage(bi, null), 0,0 );
        gc.setLineCap(StrokeLineCap.ROUND);
        createCanvasHandlers();
        lblSize.setText(size.getValue().toString());
        color = colorPicker.getValue();
        colorPicker.setPrefWidth(45);
        colorPicker.valueProperty().addListener((observable, oldValue, newValue) -> {
            color = newValue;
        });
    }

    @FXML
    private void handleConnect() throws IOException {
        InetAddress host = null;
        try {
            host = InetAddress.getByName(serverIpAddress);
        } catch (UnknownHostException e) {
            System.out.println("Host not found");
        }

        try {
            clientSocket = new Socket(host, portNumber);
            clientSocket.setReuseAddress(true);
            System.out.println("You are now connected to: " + host.getHostAddress());
            lblConnectionState.setText("Connected to: " + host.getHostAddress());
            isConnected = true;

            oos = new ObjectOutputStream( clientSocket.getOutputStream());
            ois = new ObjectInputStream(clientSocket.getInputStream());

            Thread inputThread = new Thread() {
                public void run() {
                    try {
                        while (true) {
                            Object obj = ois.readObject();
                            if (obj instanceof CanvasPoint) {
                                CanvasPoint cp = (CanvasPoint) obj;
                                if (!pointsArray.contains(cp))
                                    pointsArray.add(cp);
                                //else System.out.println("Point Duplicate");
                                if (cp.isBreaking())
                                    drawFromPointsArray();
                            } else if (obj instanceof ControlMessage) {
                                System.out.println("Got control message");
                                ControlMessage cm = (ControlMessage) obj; //TODO
                                switch (cm.getMessage()) {
                                    case 0:
                                        break;
                                    case 1:
                                        handleClear();
                                        break;
                                    case 2: {
                                        isMyTurn = true;
                                        Platform.runLater(new Runnable() {
                                            @Override
                                            public void run() {
                                                lblTurn.setText("Your turn!");
                                            }
                                        });
                                        break;
                                    }
                                    default:
                                        break;
                                }
                            }
                        }
                    } catch (IOException | ClassNotFoundException ex) {
                        ex.printStackTrace();
                    }
                }
            };
            inputThread.start();

        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("Server not found");
            lblConnectionState.setText("Server not found");
        }
    }

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

   private void createCanvasHandlers() {
        canvas.setOnMouseDragged((MouseEvent event) -> {
            if(isMyTurn && !event.isStillSincePress()) {
                CanvasPoint point = new CanvasPoint(event.getX(), event.getY(), size.get(), color);
                pointsArray.add(point);

                gc.setStroke(color);
                gc.setLineWidth(size.get());
                if (pointsArray.size() > 1 && !pointsArray.get(pointsArray.size() - 2).isBreaking()) {

                    gc.strokeLine(pointsArray.get(pointsArray.size() - 2).getX(),
                            pointsArray.get(pointsArray.size() - 2).getY(),
                            point.getX(),
                            point.getY());
                } else {
                    amountOfPointsInLastSet = 0;
                }
                point.setAmountOfPointsInThisSet(amountOfPointsInLastSet++);
            }
        });

        canvas.setOnMouseReleased((MouseEvent event) -> {
            if(isMyTurn) {
                if (event.isDragDetect()) {
                    CanvasPoint point = new CanvasPoint(event.getX(), event.getY(), size.get(), color);
                    point.setSingle(true);
                    point.setBreaking(true);
                    point.setAmountOfPointsInThisSet(0);
                    pointsArray.add(point);

                    gc.setFill(point.getColor());
                    gc.fillOval(point.getX(), point.getY(), point.getSize(), point.getSize());
                } else {
                    CanvasPoint point = pointsArray.get(pointsArray.size() - 1);
                    point.setBreaking(true);
                }
                sendPoints();
            }
        });
   }

   private void sendPoints() {
        if(isConnected) {
            try {
                for (int i = lastIndex; i < pointsArray.size(); i++) {
                    oos.writeObject(pointsArray.get(i));
                    oos.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            lastIndex = pointsArray.size() - 1;
        }
   }

   @FXML
   private void handleEndTurn() { //TODO

        try {
            oos.writeObject(new ControlMessage(1));
            oos.flush();
            handleClear();
            lastIndex = 0;
            isMyTurn = false;
            lblTurn.setText("Wait for your turn");
        } catch(IOException e) {
            e.printStackTrace();
       }

   }

   @FXML
    private void handleButtonSizeMinus() {
       size.set(size.get() - 1);
   }

    @FXML
    private void handleButtonSizePlus() {
        size.set(size.get() + 1);
    }

    @FXML
    private void handleClear() {
        pointsArray.clear();
        gc.setFill(Color.BLACK);
        gc.drawImage(SwingFXUtils.toFXImage(bi, null), 0,0 );
    }

    @FXML
    private void handleUndo() throws IndexOutOfBoundsException {
        if(pointsArray.size() > 0) {
            int pointsToRemove;
            pointsToRemove = pointsArray.get(pointsArray.size() - 1).getAmountOfPointsInThisSet()+1;

            int k = pointsArray.size();
            pointsArray.subList(k - pointsToRemove, k).clear();

            drawFromPointsArray();
        } else System.out.println("Points Array is empty");
    }

    private void drawFromPointsArray() {
        gc.setFill(Color.BLACK);
        gc.drawImage(SwingFXUtils.toFXImage(bi, null), 0,0 );

        if(pointsArray.size() > 0) {
            for (CanvasPoint point : pointsArray) {
                if (pointsArray.indexOf(point) >= 1 && !pointsArray.get(pointsArray.indexOf(point) - 1).isBreaking()) {

                    gc.setStroke(point.getColor());
                    gc.setLineWidth(point.getSize());
                    gc.strokeLine(pointsArray.get(pointsArray.indexOf(point) - 1).getX(),
                            pointsArray.get(pointsArray.indexOf(point) - 1).getY(),
                            point.getX(),
                            point.getY());
                } else if(point.isSingle()) {
                    gc.setFill(point.getColor());
                    gc.fillOval(point.getX(), point.getY(), point.getSize(), point.getSize());
                }
            }
        } else System.out.println("Points Array is empty");
    }
}