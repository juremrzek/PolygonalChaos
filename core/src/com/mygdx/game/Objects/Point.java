package com.mygdx.game.Objects;

public class Point {
    public float x;
    public float y;
    public Point(float x, float y){
        this.x = x;
        this.y = y;
    }
    public Point(){

    }
    public float distanceFrom(Point p){
        return (float)Math.sqrt((this.x-p.x)*(this.x-p.x) + (this.y-p.y)*(this.y-p.y));
    }
}
