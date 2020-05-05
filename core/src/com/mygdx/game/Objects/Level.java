package com.mygdx.game.Objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;

import java.io.Serializable;

public class Level implements Serializable {
    private String name;
    private int numberOfSides;
    private String[] colorSet;
    private float duration;
    private Object[] trapezi;
    private float scrollSpeed;
    public Level(String name, int numberOfSides, float duration, Object[] trapezi, String[] colorSet, float scrollSpeed){
        this.name = name;
        this.numberOfSides = numberOfSides;
        this.duration = duration;
        this.trapezi = trapezi;
        this.colorSet = colorSet;
        this.scrollSpeed = scrollSpeed;
    }
    public String getName(){
        return name;
    }
    public int getNumberOfSides(){
        return numberOfSides;
    }
    public Object[] getTrapezi() {
        return trapezi;
    }
    public void setTrapezi(Object[] trapezi) {
        this.trapezi = trapezi;
    }
    public float getDuration() {
        return duration;
    }
    public void setDuration(float duration) {
        this.duration = duration;
    }
    public float getScrollSpeed() {
        return scrollSpeed;
    }
    public void setScrollSpeed(float scrollSpeed) {
        this.scrollSpeed = scrollSpeed;
    }
    public String[] getColorSet() {
        return colorSet;
    }
    public void setColorSet(String[] colorSet) {
        this.colorSet = colorSet;
    }
}
