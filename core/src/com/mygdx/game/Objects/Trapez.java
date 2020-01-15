package com.mygdx.game.Objects;

public class Trapez extends Polygon {
    private int size;
    private int distance;
    public Trapez(int size, int distance) {
        this.size = size;
        this.distance = distance;
    }
    public int getSize(){
        return size;
    }
    public int getDistance(){
        return distance;
    }
    public void setSize(int size){
        this.size = size;
    }
    public void setDistance(int distance){
        this.distance = distance;
    }
}
