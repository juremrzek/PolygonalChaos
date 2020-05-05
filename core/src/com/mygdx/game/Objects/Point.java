package com.mygdx.game.Objects;

import com.badlogic.gdx.math.Vector2;

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
    public float distanceFrom(Vector2 p){
        return (float)Math.sqrt((this.x-p.x)*(this.x-p.x) + (this.y-p.y)*(this.y-p.y));
    }
}
