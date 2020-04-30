package com.mygdx.game.Objects;

import com.badlogic.gdx.utils.FloatArray;

public class Polygon {

    public float[] xPoints;
    public float[] yPoints;;
    private int r;
    private float centerX;
    private float centerY;
    private FloatArray floatArray;
    public Polygon(float[] xPoints, float[] yPoints, int r){
        this.xPoints = xPoints;
        this.yPoints = yPoints;
        this.r = r;
        floatArray = new FloatArray();
    }
    public Polygon(float[] xPoints, float[] yPoints){
        this.xPoints = xPoints;
        this.yPoints = yPoints;
    }
    public Polygon(int r){
        this.r = r ;
    }
    public Polygon(){}
    public float[] getPoints(){ //merge xPoints and yPoints into one table (so it is suitable for polygon collisions and drawing)
        float[] combinedPoints = new float[xPoints.length + yPoints.length];
        for(int i=0; i<combinedPoints.length; i+=2) {
            combinedPoints[i]=xPoints[i/2];
            combinedPoints[i+1]=yPoints[i/2];
        }
        return combinedPoints;
    }
    public int getR(){
        return r;
    }
    public void setCenter(float x, float y){
        this.centerX = x;
        this.centerY = y;
    }
    public float getCenterX(){
        return centerX;
    }
    public float getCenterY(){
        return centerY;
    }
    public Point getCenter(){
        return new Point(centerX, centerY);
    }
    public FloatArray getFloatArray(){
        return new FloatArray(getPoints());
    }
    public float[] getXPoints(){
        return xPoints;
    }
    public float[] getYPoints(){
        return yPoints;
    }
}
