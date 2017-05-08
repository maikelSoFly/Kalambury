package sample.models;

import javafx.scene.paint.Color;

import java.io.Serializable;

//
//     Project name: Kalambury
//
//     Created by maikel on 22.04.2017.
//     Copyright © 2017 Mikołaj Stępniewski. All rights reserved.
//

public class CanvasPoint implements Serializable{
    private int x;
    private int y;
    private int size;
    private java.awt.Color color;
    private boolean isBreaking;
    private int amountOfPointsInThisSet;
    private boolean isSingle;

    public CanvasPoint(double x, double y, int size, Color fx) {
        this.x = (int)x;
        this.y = (int)y;
        this.size = size;
        this.color = new java.awt.Color((float) fx.getRed(),
                (float) fx.getGreen(),
                (float) fx.getBlue(),
                (float) fx.getOpacity());
        this.isBreaking = false;
        this.isSingle = false;
    }
    public CanvasPoint(double x, double y, int size) {
        this.x = (int)x;
        this.y = (int)y;
        this.size = size;

        this.isBreaking = false;
        this.isSingle = false;
    }

    public CanvasPoint() {
        this.x = 0;
        this.y = 0;
        this.size = 0;
        this.color = null;
        this.isBreaking = false;
        this.isSingle = false;
    }



    //GETTERS & SETTERS
    public int getX() {
        return x;
    }
    public void setX(double x) {
        this.x = (int)x;
    }
    public int getY() {
        return y;
    }
    public void setY(double y) {
        this.y = (int)y;
    }
    public int getSize() {
        return size;
    }
    public void setSize(int size) {
        this.size = size;
    }
    public Color getColor() {
        Color color = Color.rgb(this.color.getRed(), this.color.getGreen(), this.color.getBlue());
        return color;
    }
    public void setColor(java.awt.Color color) {
        this.color = color;
    }
    public boolean isBreaking() {
        return isBreaking;
    }
    public void setBreaking(boolean breaking) {
        isBreaking = breaking;
    }
    public int getAmountOfPointsInThisSet() {
        return amountOfPointsInThisSet;
    }
    public void setAmountOfPointsInThisSet(int amountOfPointsInThisSet) {
        this.amountOfPointsInThisSet = amountOfPointsInThisSet;
    }
    public boolean isSingle() {
        return isSingle;
    }
    public void setSingle(boolean single) {
        isSingle = single;
    }
}
