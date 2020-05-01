package com.mygdx.game.Objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;

public class Icon{
    private float x;
    private float y;
    private Texture texture;
    private float r;
    Color color;
    boolean selected;
    public Icon(float x, float y, float r, String src, Color color){
        this.x = x;
        this.y = y;
        this.r = r;
        this.texture = new Texture(src);
        this.color = color;
        selected = false;
    }
    public float getX(){
        return x;
    };
    public float getY(){
        return y;
    }
    public float getR(){
        return r;
    }
    public Texture getTexture(){
        return texture;
    }
    public Color getColor(){
        return color;
    }
    public void setColor(Color color){
        this.color = color;
    }
    public boolean isSelected(){
        return selected;
    }
    public void setSelected(boolean b){
        selected = b;
    }
    public boolean intersects(Point p){
        return p.distanceFrom(new Point(this.x, this.y)) < this.r;
    }
}
