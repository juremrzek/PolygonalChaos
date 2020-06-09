package com.mygdx.game.Objects;

import java.io.Serializable;

public class Level implements Serializable {
    private String name;
    private int numberOfSides;
    private String[] colorSet;
    private float duration;
    private Object[] trapezi;
    private float scrollSpeed;
    private float progress;
    private String songName;
    public Level(String name, int numberOfSides, float duration, Object[] trapezi, String[] colorSet, float scrollSpeed, String songName, float progress){
        this.name = name;
        this.numberOfSides = numberOfSides;
        this.duration = duration;
        this.trapezi = trapezi;
        this.colorSet = colorSet;
        this.songName = songName;
        this.scrollSpeed = scrollSpeed;
        this.progress = progress;
    }
    public Level(String name, int numberOfSides, float progress, String songName){
        this.name = name;
        this.numberOfSides = numberOfSides;
        this.songName = songName;
        this.progress = progress;
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
    public float getProgress() {
        return progress;
    }
    public void setProgress(float progress) {
        this.progress = progress;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }
}
