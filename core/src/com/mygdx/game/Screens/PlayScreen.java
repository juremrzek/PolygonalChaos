package com.mygdx.game.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.mygdx.game.MyGdxGame;
import com.mygdx.game.Objects.*;
import com.badlogic.gdx.math.Intersector;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

public class PlayScreen implements Screen {

    private float dt; //deltatime

    private MyGdxGame game;
    private FitViewport viewport;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private BitmapFont font;
    private ShapeRenderer sr;

    //Objects to draw polygons
    private Pixmap pixmap;
    private PolygonSprite poly;
    private PolygonSpriteBatch polyBatch;
    private Texture textureSolid;
    private EarClippingTriangulator ect;

    //Objects that exist on the screen
    private Polygon[] middleHexagon; //Hexagon in the middle of the screen (2 of them because the bigger one is the outline)
    private Polygon pointer;
    private Array<Trapez> trapezi; //Obstacles (Array is Libgdx's equivalent to ArrayList)
    private Trapez tlast;

    private Level level;
    private int numberOfSides;
    private float angle;
    private int rotateSpeed;
    private float scrollSpeed;
    private Color[] colors;
    private String[] currColorSet;
    private float levelTimestamp;
    private long startTime; //time when the screen was shown
    private float timestampSpeed;
    private long seconds;
    private long milliseconds;

    private float pointerAngle;
    private float pointerRotationR;
    private float pointerRotationSpeed;

    private Point center;
    private Music music;

    private boolean dead;
    private long deathTime;
    private Sound deathSound;
    private Sound playSound;
    private Sound winSound;
    private boolean canPlayWinSound;

    public PlayScreen(MyGdxGame game, Level level, SpriteBatch batch, BitmapFont font, ShapeRenderer sr){
        this.game = game;
        this.level = level;
        this.batch = batch;
        this.font = font;
        this.sr = sr;
    }

    @Override
    public void show() {
        angle = 0;
        startTime = System.nanoTime();
        seconds = 0;
        milliseconds = 0;
        levelTimestamp = 0;
        switch(level.getName()){
            case "First level": rotateSpeed = 50;
            break;
            case "Another level": rotateSpeed = 75;
            break;
            case "Level3": rotateSpeed = 100;
            break;
            case "Final level": rotateSpeed = 125;
            break;
            default: rotateSpeed = 70;
        }
        scrollSpeed = 200;
        dead = false;
        canPlayWinSound = true;

        camera = new OrthographicCamera();
        viewport = new FitViewport(1280, 900, camera);
        //Set the center of the screen as a point
        center = new Point(viewport.getWorldWidth()/2, viewport.getWorldHeight()/2);

        //Set the colors for the game - these can be changed later
        currColorSet = level.getColorSet();
        colors = new Color[currColorSet.length];
        for(int i=0; i<colors.length; i++){
            colors[i] = new Color(ColorSets.getColorFromHex(currColorSet[i]));
        }
        font.setColor(colors[0]);

        trapezi = new Array<>();
        this.numberOfSides = level.getNumberOfSides();
        for (Object o : level.getTrapezi()) {
            trapezi.add((Trapez)o);
        }

        tlast = trapezi.get(trapezi.size - 1);
        for(Trapez t:trapezi) {
            t.setDistance(t.getStartDistance() - (tlast.getStartDistance() + tlast.getStartSize()) * levelTimestamp);
            initTrapez(t, t.getDistance(), t.getSize(), angle + (float) 360 / numberOfSides * t.getPosition());
        }
        //objects to draw polygons
        pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        ect = new EarClippingTriangulator();
        polyBatch = new PolygonSpriteBatch();
        pixmap.fill();
        textureSolid = new Texture(pixmap);
        PolygonRegion polyReg = new PolygonRegion(new TextureRegion(textureSolid), tlast.getPoints(), ect.computeTriangles(tlast.getPoints()).toArray());
        poly = new PolygonSprite(polyReg);
        timestampSpeed = scrollSpeed/tlast.getStartDistance();

        music = Gdx.audio.newMusic(Gdx.files.internal("music/"+level.getSongName()+".mp3"));
        music.setVolume(0.4f);
        music.play();

        deathSound = Gdx.audio.newSound(Gdx.files.internal("sounds/die.wav"));
        playSound = Gdx.audio.newSound(Gdx.files.internal("sounds/play.wav"));
        winSound = Gdx.audio.newSound(Gdx.files.internal("sounds/win.wav"));
        playSound.play(0.6f);

        middleHexagon = new Polygon[2];
        for(int i=0; i<middleHexagon.length; i++) {
            middleHexagon[i] = new Polygon(new float[numberOfSides], new float[numberOfSides], 70);
            middleHexagon[i].setCenter(center.x, center.y);
        }
        pointer = new Polygon(new float[3], new float[3], 13);
        pointer.setCenter(center.x, center.y);
        pointerRotationR = middleHexagon[0].getR()+25;
        pointerAngle = 0;
        pointerRotationSpeed = 10;
        for(int i=0; i<pointer.xPoints.length; i++) {
            pointer.xPoints[i] = (float) (pointer.getCenterX() + Math.cos(Math.toRadians(pointerAngle + (360f / pointer.xPoints.length) * i)) * pointer.getR());
            pointer.yPoints[i] = (float) (pointer.getCenterY() + Math.sin(Math.toRadians(pointerAngle + (360f / pointer.xPoints.length) * i)) * pointer.getR());
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        drawBackground();
        for(Trapez t:trapezi){
            if(t.getDistance() < 2000 && t.getDistance() > -500){
                initTrapez(t, t.getDistance(), t.getSize(), angle + (float) 360 / numberOfSides * t.getPosition());
                drawPolygon(t.getPoints(), colors[0]);
            }
        }
        drawEquilateralPolygon(pointer,3, pointer.getR(), pointer.getCenterX(), pointer.getCenterY(), colors[0], pointerAngle);
        drawEquilateralPolygon(middleHexagon[1], numberOfSides, middleHexagon[1].getR()+10, center.x, center.y, colors[0], angle);
        drawEquilateralPolygon(middleHexagon[0], numberOfSides, middleHexagon[0].getR(), center.x, center.y, colors[1], angle);
        drawInfo();
        if(!dead)
            update(Gdx.graphics.getDeltaTime());
        else
            endGame(Gdx.graphics.getDeltaTime());
    }
    private void update(float delta){
        dt = delta;

        if(!trapezi.isEmpty())
            tlast = trapezi.get(trapezi.size - 1);
        if(levelTimestamp <= 1)
            levelTimestamp +=timestampSpeed*dt;

        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.Q)){
            deathTime = System.nanoTime();
            deathSound.play(0.4f);
            dead = true;
        }
        for(int i=0; i<trapezi.size; i++){
            Trapez t = trapezi.get(i);
            t.setDistance(t.getStartDistance() - (tlast.getStartDistance()+tlast.getStartSize())*levelTimestamp);
            float distanceDiff;

            if(t.getDistance() <= middleHexagon[1].getR()){
                distanceDiff = Math.abs(t.getDistance()-middleHexagon[1].getR());
                t.setSize(t.getStartSize()-distanceDiff);
                t.setDistance(t.getDistance()+distanceDiff);
            }
            else {
                t.setSize(t.getStartSize());
            }
        }
        for(int i=0; i<trapezi.size; i++){
            if(trapezi.get(i).getSize() <= 0)
                trapezi.removeIndex(i);
        }
        //check collision
        boolean collidedLeft = false;
        boolean collidedRight = false;
        for(Trapez t:trapezi){
            if(t.getDistance() < 1000 && Intersector.intersectPolygons(t.getFloatArray(), pointer.getFloatArray())){
                Vector pointerVector;
                Vector trapezVector;
                if(pointerRotationR <= t.getDistance()){
                    //System.out.println("game over");
                    dead = true;
                    deathTime = System.nanoTime();
                    deathSound.play(0.4f);
                }
                //now check if the pointer is colliding with right or left size of trapez by checking which edge of the pointer is trapez colliding with
                if(Intersector.isPointInPolygon(t.getPoints(), 0, t.getPoints().length, pointer.getXPoints()[2], pointer.getYPoints()[2])){
                    collidedRight = true;
                    pointerVector = new Vector(center, new Point(pointer.getXPoints()[2], pointer.getYPoints()[2]));
                    trapezVector = new Vector(new Point(t.getXPoints()[1], t.getYPoints()[1]), new Point(t.getXPoints()[2], t.getYPoints()[2]));
                    pointerAngle += Math.floor(Math.toDegrees(pointerVector.getAngle(trapezVector)));
                }
                if(Intersector.isPointInPolygon(t.getPoints(), 0, t.getPoints().length, pointer.getXPoints()[1], pointer.getYPoints()[1])){
                    collidedLeft = true;
                    pointerVector = new Vector(center, new Point(pointer.getXPoints()[1], pointer.getYPoints()[1]));
                    trapezVector = new Vector(new Point(t.getXPoints()[0], t.getYPoints()[0]), new Point(t.getXPoints()[3], t.getYPoints()[3]));
                    pointerAngle -= Math.floor(Math.toDegrees(pointerVector.getAngle(trapezVector)));
                }
            }
        }
        if ((Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) && !collidedLeft) {
            pointerAngle += pointerRotationSpeed;
        }
        if ((Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) && !collidedRight) {
            pointerAngle -= pointerRotationSpeed;
        }
        //move the pointer based on pointerAngle
        pointer.setCenter(center.x + (float)(pointerRotationR * Math.cos(Math.toRadians(pointerAngle))),
                center.y + (float)(pointerRotationR * Math.sin(Math.toRadians(pointerAngle))));
        pointerAngle += rotateSpeed*dt;
        pointerAngle %= 360;
        angle+=rotateSpeed*dt;
        angle %= 360;
    }
    private void endGame(float dt){
        if(getPassedTime(deathTime) > 1600000000){
            exportLevel();
            dispose();
            game.setScreen(new LevelSelectScreen(game, angle, batch, font, sr));
        }

        pointerAngle += rotateSpeed*dt;
        pointerAngle %= 360;
        angle+=rotateSpeed*dt;
        angle %= 360;
        pointer.setCenter(center.x + (float)(pointerRotationR * Math.cos(Math.toRadians(pointerAngle))),
                center.y + (float)(pointerRotationR * Math.sin(Math.toRadians(pointerAngle))));
        if(Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)){
            exportLevel();
            dispose();
            game.setScreen(new PlayScreen(game, level, batch, font, sr));
        }
        fadeMusic(dt);
    }
    private void fadeMusic(float dt){
        music.setVolume(music.getVolume()-dt/2);
        if(music.getVolume() <= 0.1f)
            music.setVolume(0.1f);
    }

    private void drawBackground(){
        for(int i=0; i<numberOfSides; i++) {
            Polygon bgTriangle = new Polygon(new float[3], new float[3]);
            for (int j = 1; j < 3; j++) {
                bgTriangle.xPoints[0] = viewport.getWorldWidth() / 2f;
                bgTriangle.yPoints[0] = viewport.getWorldHeight() / 2f;
                bgTriangle.xPoints[j] = bgTriangle.xPoints[0] + (float) Math.cos(Math.toRadians(angle + (360f / numberOfSides) * (i + j))) * 9999; //edges of background triangles need to be very far away from the center to cover the whole screen
                bgTriangle.yPoints[j] = bgTriangle.yPoints[0] + (float)(Math.sin(Math.toRadians(angle + (360f / numberOfSides) * (i + j))) * 9999);
            }
            Color tempColor;
            if(numberOfSides%2 == 0) {
                if (i % 2 == 0)
                    tempColor = colors[2];
                else
                    tempColor = colors[3];
            }
            else{
                if (i % 5 == 0 && i % 3 == 0)
                    tempColor = colors[4];
                else if(i % 2 == 0)
                    tempColor = colors[3];
                else
                    tempColor = colors[2];
            }
            drawPolygon(bgTriangle.getPoints(), tempColor);
        }
    }
    private void drawEquilateralPolygon(Polygon p, int n, int r, float x, float y, Color color, float startAngle){
        for(int i=0; i<n; i++) {
            double segment = Math.toRadians(startAngle + (360f / n) * i);
            p.xPoints[i] = (float) (x + Math.cos(segment) * r);
            p.yPoints[i] = (float) (y + Math.sin(segment) * r);
        }
        drawPolygon(p.getPoints(), color);
    }
    private void initTrapez(Polygon p, double distance, double size, double startAngle){
        p.xPoints = new float[4];
        p.xPoints[0] = (float)(center.x + distance*Math.cos(Math.toRadians(startAngle)));
        p.xPoints[1] = (float)(center.x + distance*Math.cos(Math.toRadians(360f/numberOfSides)+Math.toRadians(startAngle)));
        p.xPoints[3] = (float)(center.x + (distance+size)*Math.cos(Math.toRadians(startAngle)));
        p.xPoints[2] = (float)(center.x + (distance+size)*Math.cos(Math.toRadians(360f/numberOfSides)+Math.toRadians(startAngle)));

        p.yPoints = new float[4];
        p.yPoints[0] = (float)(center.y + distance*Math.sin(Math.toRadians(startAngle)));
        p.yPoints[1] = (float)(center.y + distance*Math.sin(Math.toRadians(360f/numberOfSides)+Math.toRadians(startAngle)));
        p.yPoints[3] = (float)(center.y + (distance+size)*Math.sin(Math.toRadians(startAngle)));
        p.yPoints[2] = (float)(center.y + (distance+size)*Math.sin(Math.toRadians(360f/numberOfSides)+Math.toRadians(startAngle)));
    }

    //draws a box on a side of the screen (left or right) - boxOffset tells the difference between top and bottom lines of a trapezium
    public void drawScreenBox(boolean right, float boxWidth, float boxHeight, float boxOffset) {
        Polygon background;
        float[] backgroundPointsX = new float[4];
        float[] backgroundPointsY = new float[4];
        if(right) {
            backgroundPointsX[0] = viewport.getWorldWidth() - (boxWidth + boxOffset);
            backgroundPointsX[1] = viewport.getWorldWidth();
            backgroundPointsX[2] = viewport.getWorldWidth();
            backgroundPointsX[3] = viewport.getWorldWidth() - boxWidth;
        }
        else {
            backgroundPointsX[0] = -10;
            backgroundPointsX[1] = boxWidth + boxOffset;
            backgroundPointsX[2] = boxWidth;
            backgroundPointsX[3] = -10;
        }
        backgroundPointsY[0] = viewport.getWorldHeight();
        backgroundPointsY[1] = viewport.getWorldHeight();
        backgroundPointsY[2] = viewport.getWorldHeight() - boxHeight;
        backgroundPointsY[3] = viewport.getWorldHeight() - boxHeight;
        background = new Polygon(backgroundPointsX, backgroundPointsY);
        drawPolygon(background.getPoints(), new Color(0x000000FF));
    }

    public void drawText(String text, float size, float x, float y) {
        batch.begin();
        batch.setProjectionMatrix(camera.combined);
        font.getData().setScale(size/64);
        font.draw(batch, text, x, y);
        batch.end();
    }

    public long getPassedTime(long startTime) { //time that has passed since the beginning of the level (screen)
        return System.nanoTime() - startTime;
    }

    public void drawInfo(){
        long passedTime = getPassedTime(startTime);
        if(levelTimestamp < 1 && !dead) {
            seconds = passedTime / 1000000000; //conversion from nanoseconds to seconds
            milliseconds = (passedTime - seconds * 1000000000) / 100000000;
        }else if(canPlayWinSound && !dead){
            deathTime = System.nanoTime();
            dead = true;
            winSound.play(0.8f);
            canPlayWinSound = false;
        }
        drawScreenBox(false,300, 50, 20);
        String secondsString;
        if(seconds >= 10)
            secondsString = ""+seconds;
        else
            secondsString = "0"+seconds;

        if(seconds < 100){
            drawScreenBox(true,400, 50, 20);
            drawScreenBox(true,200, 80, 40);
            drawText(secondsString, 48, viewport.getWorldWidth()-185, viewport.getWorldHeight()-15);
            drawText("Time:", 30, viewport.getWorldWidth()-390, viewport.getWorldHeight()-8);
        }
        else{
            drawScreenBox(true,460, 50, 20);
            drawScreenBox(true,260, 80, 40);
            drawText(secondsString, 48, viewport.getWorldWidth()-245, viewport.getWorldHeight()-15);
            drawText("Time:", 30, viewport.getWorldWidth()-450, viewport.getWorldHeight()-8);
        }
        drawText("."+milliseconds, 20, viewport.getWorldWidth()-65, viewport.getWorldHeight()-43);
        drawText("Level 1", 32, 10, viewport.getWorldHeight()-8);
    }
    public float getLevelLength(){
        if(trapezi.isEmpty())
            return 0;
        else return tlast.getStartDistance()/scrollSpeed;
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }
    public void exportLevel() {
        try {
            if(levelTimestamp*100 > level.getProgress())
                level.setProgress(levelTimestamp*100);
            FileHandle fileHandle= Gdx.files.local("levels/"+level.getName()+".lvl");
            FileOutputStream fos = new FileOutputStream(fileHandle.file());
            ObjectOutputStream ous = new ObjectOutputStream(fos);
            ous.writeObject(level);
            fos.close();
            ous.close();
            System.out.println("Level exported");
        } catch (Exception e) {
            System.out.println("An error has occurred");
            e.printStackTrace();
        }
    }
    private void drawPolygon(float [] points, Color color){
        pixmap.setColor(color);
        pixmap.fill();
        textureSolid = new Texture(pixmap);
        PolygonRegion polyReg = new PolygonRegion(new TextureRegion(textureSolid), points, ect.computeTriangles(points).toArray());
        poly.setRegion(polyReg);
        poly.setOrigin(25, 25);
        polyBatch.setProjectionMatrix(camera.combined);
        polyBatch.begin();
        polyBatch.draw(polyReg, 2, 2);
        polyBatch.end();
        textureSolid.dispose();
    }
    @Override
    public void dispose() {
        pixmap.dispose();
        polyBatch.dispose();
        textureSolid.dispose();
        music.stop();
        music.dispose();
    }
}
