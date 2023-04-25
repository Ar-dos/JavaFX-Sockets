package com.example.demo;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.Scanner;

public class Controller implements Initializable {

    private BooleanProperty aPressed = new SimpleBooleanProperty();

    private BooleanProperty dPressed = new SimpleBooleanProperty();

    private BooleanProperty leftPressed = new SimpleBooleanProperty();

    private BooleanProperty rightPressed = new SimpleBooleanProperty();

    private BooleanBinding keyPressed = (aPressed).or(dPressed).or(leftPressed).or(rightPressed);

    private ArrayList<Circle> bulletsPlayer = new ArrayList<>();

    private ArrayList<Circle> bulletsEnemy = new ArrayList<>();

    private int movementVariable = 2;


    @FXML
    private Rectangle shape1;
    @FXML
    private Rectangle shape2;
    @FXML
    private AnchorPane scene;
    @FXML
    private Rectangle health1;
    @FXML
    private Rectangle health2;

    private Socket socket;
    private BufferedInputStream input ;
    private BufferedOutputStream output;

    private int enemyPosition = 170;
    private boolean playerShot = false;
    private boolean enemyShot = false;

    private boolean start = false;

    @FXML
    private TextArea ta;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        movementSetup();
        Bullet b = new Bullet(100,100);

        Thread listen = new Thread ( () -> {
            try {
                // Create a server socket
                Socket socket = new Socket("localhost",8081);
                output = new BufferedOutputStream(socket.getOutputStream());
                input = new BufferedInputStream(socket.getInputStream());
                while (true)
                {

                    byte[] arr = new byte[Integer.BYTES];
                    input.read(arr);
                    int line = 0;
                    for (byte byt : arr) {
                        line = (line << 8) + (byt & 0xFF);
                    }
                    System.out.println(line);
                    if (line == 666)
                        enemyShot = true;
                    else
                        enemyPosition = line;
                }
            }
            catch(IOException ex) {
                ex.printStackTrace();
            }
        });

        listen.start();
        timer.start();

        keyPressed.addListener(((observableValue, aBoolean, t1) -> {
        }));
    }

    AnimationTimer timer = new AnimationTimer() {
        @Override
        public void handle(long timestamp) {

            for (int i = 0; i < bulletsPlayer.size(); i++) {
                bulletsPlayer.get(i).setLayoutY(bulletsPlayer.get(i).getLayoutY() - 2);
                if (bulletsPlayer.get(i).getLayoutY() * -1 > shape2.getLayoutY() + 160 && bulletsPlayer.get(i).getLayoutY() * -1 < shape2.getLayoutY() + 220
                && bulletsPlayer.get(i).getCenterX() > shape2.getLayoutX() && bulletsPlayer.get(i).getCenterX() < shape2.getLayoutX() + 60)
                {
                    health2.setWidth(health2.getWidth() - 20);
                    scene.getChildren().remove(bulletsPlayer.get(i));
                    bulletsPlayer.remove(i);
                    break;
                }
                if (bulletsPlayer.get(i).getLayoutY() < -280) {
                    scene.getChildren().remove(bulletsPlayer.get(i));
                    bulletsPlayer.remove(i);
                }
            }

            for (int i = 0; i < bulletsEnemy.size(); i++) {
                bulletsEnemy.get(i).setLayoutY(bulletsEnemy.get(i).getLayoutY() + 2);
                if (bulletsEnemy.get(i).getLayoutY() > shape1.getLayoutY() - 100 && bulletsEnemy.get(i).getLayoutY()  < shape1.getLayoutY() - 40
                        && bulletsEnemy.get(i).getCenterX() > shape1.getLayoutX() && bulletsEnemy.get(i).getCenterX() < shape1.getLayoutX() + 60)
                {
                    health1.setWidth(health1.getWidth() - 20);
                    scene.getChildren().remove(bulletsEnemy.get(i));
                    bulletsEnemy.remove(i);
                    break;
                }
                if (bulletsEnemy.get(i).getLayoutY() > 280) {
                    scene.getChildren().remove(bulletsEnemy.get(i));
                    bulletsEnemy.remove(i);
                }
            }
            sendCommand((int)shape1.getLayoutX());

            shape2.setLayoutX(enemyPosition);

            if (playerShot) {
                Circle temp = new  Circle(shape1.getLayoutX()+30,shape1.getLayoutY(),10,Color.DODGERBLUE);
                bulletsPlayer.add(temp);
                scene.getChildren().add(temp);
                sendCommand(666);
                playerShot = false;
            }

            if (enemyShot) {
                enemyShoot();
                enemyShot = false;
            }

            if(aPressed.get() && shape1.getLayoutX() > 0) {
                shape1.setLayoutX(shape1.getLayoutX() - movementVariable);
            }

            if(dPressed.get() && shape1.getLayoutX() < 340){
                shape1.setLayoutX(shape1.getLayoutX() + movementVariable);
            }


        }
    };

    public void sendCommand(int command) {
        try {
            output.write(ByteBuffer.allocate(4).putInt(command).array());
            output.flush();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void enemyShoot() {
        Circle temp = new  Circle(shape2.getLayoutX()+30,shape2.getLayoutY() + 60,10,Color.RED);
        bulletsEnemy.add(temp);
        scene.getChildren().add(temp);
    }



    public void movementSetup(){
        scene.setOnKeyPressed(e -> {

            if(e.getCode() == KeyCode.A)
                aPressed.set(true);


            if(e.getCode() == KeyCode.D)
                dPressed.set(true);






         });

        scene.setOnKeyReleased(e ->{

            if(e.getCode() == KeyCode.A)
                aPressed.set(false);



            if(e.getCode() == KeyCode.D)
                dPressed.set(false);


            if (e.getCode() == KeyCode.SPACE)
                playerShot = true;

        });

    }
}
