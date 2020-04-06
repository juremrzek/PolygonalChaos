package com.mygdx.game.Objects;

public class Trapez extends Polygon {
    private float size;
    private float distance;
    private float position;
    public Trapez(int size, int distance, int position) {
        this.size = size;
        this.distance = distance;
        this.position = position;
    }
    public float getSize(){
        return size;
    }
    public float getDistance(){
        return distance;
    }
    public void setSize(float size){
        this.size = size;
    }
    public void setDistance(float distance){
        this.distance = distance;
    }
    public void setPosition(float position){
        this.position = position;
    }
    public float getPosition(){
        return position;
    }
}
