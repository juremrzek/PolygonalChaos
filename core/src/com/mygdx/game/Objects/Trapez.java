package com.mygdx.game.Objects;

import java.io.Serializable;

public class Trapez extends Polygon implements Serializable {
    private float size;
    private float distance;
    private float position;
    private float startDistance;
    private float startSize;
    public Trapez(float size, float distance, int position) {
        this.size = size;
        this.startSize = size;
        this.distance = distance;
        this.startDistance = distance;
        this.position = position;
    }
    public Trapez(){

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
    public float getStartDistance() {
        return startDistance;
    }
    public float getStartSize(){
        return startSize;
    }
}
