package com.mygdx.game.Objects;

public class Circle {
    private float x;
    private float y;
    private float r;
    public Circle(float x, float y, float r){
        this.x = x;
        this.y = y;
        this.r = r;
    }
    public Circle(){

    }
    public float getX(){
        return x;
    }
    public float getY(){
        return y;
    }
    public float getR(){
        return r;
    }
    public void setX(float x){
        this.x = x;
    }
    public void setY(float y){
        this.y = y;
    }
    public void setR(float r){
        this.r = r;
    }
    public boolean intersects(Point p){
        return p.distanceFrom(new Point(this.x, this.y)) < this.r;
    }
    public Point getCenter() {
        return new Point(x,y);
    }
}
