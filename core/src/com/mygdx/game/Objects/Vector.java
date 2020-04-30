package com.mygdx.game.Objects;

public class Vector {
    public Point p1;
    public Point p2;
    private float x;
    private float y;
    public Vector(Point p1, Point p2) {
        this.p1 = p1;
        this.p2 = p2;
        this.x = p2.x - p1.x;
        this.y = p2.y - p1.y;
    }
    public Vector(){

    }
    public float dotProduct(Vector v){
        return this.x*v.x + this.y*v.y;
    }
    public double getAngle(Vector v){
        if(this.getLength() == 0 || v.getLength() == 0){
            System.out.println("one of the vectors has length of 0");
            return 0;
        }
        if(Math.abs(this.dotProduct(v)/(this.getLength()*v.getLength())) > 1){
            System.out.println("cos value not between -1 and 1");
            System.out.println(this.dotProduct(v)/(this.getLength()*v.getLength()));
            return 0;
        }
        return Math.acos(this.dotProduct(v)/(this.getLength()*v.getLength()));
    }
    public double getLength(){
        return Math.sqrt((p1.x-p2.x)*(p1.x-p2.x) + (p1.y-p2.y)*(p1.y-p2.y));
    }
    public void scale(float scalar){//ex. if scalar was 0.5, it would return a point in the exact middle between two points
        p2.x = p1.x+x*scalar;
        p2.y = p1.y+y*scalar;
    }
}
