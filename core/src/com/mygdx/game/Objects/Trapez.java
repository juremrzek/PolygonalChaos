package com.mygdx.game.Objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.io.Serializable;

public class Trapez extends Polygon implements Serializable {
    private float size;
    private float distance;
    private float position;
    private float startDistance;
    private float startSize;
    private boolean isSelected;
    public Trapez(float size, float distance, int position) {
        this.size = size;
        this.startSize = size;
        this.distance = distance;
        this.startDistance = distance;
        this.position = position;
        isSelected = false;
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
    public void setStartDistance(float distance){
        this.startDistance = distance;
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
    public boolean isSelected(){
        return isSelected;
    }
    public void setSelected(boolean b){
        isSelected = b;
    }
    public void drawOutline(ShapeRenderer sr, Color color){
        sr.setColor(color);
        sr.begin(ShapeRenderer.ShapeType.Line);
        for(int i=0; i<getPoints().length; i+=4){
            sr.line(getPoints()[i], getPoints()[i+1], getPoints()[i+2], getPoints()[i+3]);
        }
        sr.end();
    }
}
