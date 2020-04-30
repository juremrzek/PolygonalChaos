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
    public float getDistanceFromTrapezSideToCenter(Trapez t, Point center){
        Vector v1 = new Vector(new Point(t.getXPoints()[0], t.getYPoints()[0]), center);
        Vector v2 = new Vector(new Point(t.getXPoints()[1], t.getYPoints()[1]), center);
        Vector mouseVector = new Vector(center, this);
        float beta = (float)(Math.PI - v1.getAngle(v2))/2;
        float alpha = (float)(Math.PI-mouseVector.getAngle(v1));
        float gamma = (float)(Math.PI - beta - alpha);
        return (float)(mouseVector.getLength()/Math.sin(beta) * Math.sin(gamma));
    }
}
