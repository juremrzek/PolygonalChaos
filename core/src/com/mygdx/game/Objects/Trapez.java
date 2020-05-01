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
    private boolean isDragging;
    public Trapez(float size, float distance, int position) {
        this.size = size;
        this.startSize = size;
        this.distance = distance;
        this.startDistance = distance;
        this.position = position;
        isSelected = false;
        isDragging = false;
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
    public boolean isDragging(){
        return isDragging;
    }
    public void setDragging(boolean b){
        isDragging = b;
    }

    public void drawOutline(ShapeRenderer sr, Color color){
        sr.setColor(color);
        sr.begin(ShapeRenderer.ShapeType.Line);
        for(int i=0; i<getPoints().length/2; i++){
            if(i<getPoints().length/2-1)
                sr.rectLine(getXPoints()[i], getYPoints()[i], getXPoints()[i+1], getYPoints()[i+1], 1);
            else
                sr.rectLine(getXPoints()[i], getYPoints()[i], getXPoints()[0], getYPoints()[0], 1);
        }
        sr.end();
    }
}
