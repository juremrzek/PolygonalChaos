package com.mygdx.game.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.mygdx.game.MyGdxGame;
import com.mygdx.game.Objects.Point;
import com.mygdx.game.Objects.Polygon;
import com.mygdx.game.Objects.Trapez;
import com.badlogic.gdx.math.Intersector;
import com.mygdx.game.Objects.Vector;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

public class PlayScreen implements Screen {

    private float dt; //deltatime

    private MyGdxGame game;
    private FitViewport viewport;
    private OrthographicCamera camera;
    private Pixmap pixmap;
    private SpriteBatch batch;
    private BitmapFont font;
    private ShapeRenderer sr;

    //Objects to draw polygons
    private PolygonSprite poly;
    private PolygonSpriteBatch polyBatch;
    private Texture textureSolid;
    private EarClippingTriangulator ect;
    private PolygonRegion polyReg;

    //Objects that exist on the screen
    private Polygon[] middleHexagon; //Hexagon in the middle of the screen (2 of them because the bigger one is the outline)
    private Polygon pointer;
    private Array<Trapez> trapezi; //Obstacles (Array is Libgdx's equivalent to ArrayList)
    private Trapez tlast;

    //variables to create game diversity
    private short numberOfSides;
    private float angle;
    private float rotateSpeed;
    private float scrollSpeed;
    private float tiltRatio;
    private boolean tiltRatioInc;
    private int [] colors;
    private float levelTimestamp;
    private long startTime; //time when the screen was shown
    private float timeStampSpeed;
    long seconds;
    long milliseconds;
    String levelName;

    private float pointerAngle;
    private float pointerRotationR;
    private float pointerRotationSpeed;

    private Point center;
    private Music music;

    public PlayScreen(MyGdxGame game){
        angle = 0;
        numberOfSides = 3;
        tiltRatio = 1;
        tiltRatioInc = true;
        startTime = System.nanoTime();
        seconds = 0;
        milliseconds = 0;
        levelName = "TestLevel";
        levelTimestamp = 0;

        //variables that scale with delta
        rotateSpeed = 200;
        scrollSpeed = 200;

        this.game = game;
        camera = new OrthographicCamera();
        viewport = new FitViewport(1280, 900, camera);

        batch = new SpriteBatch();
        font = new BitmapFont(Gdx.files.internal("fonts/font.fnt"));
        font.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        sr = new ShapeRenderer();
        sr.setColor(Color.BLACK);

        File f = new File("core/assets/levels/" + levelName + ".lvl");
        if(f.exists())
            importLevel(f);
        tlast = trapezi.get(trapezi.size - 1);
        //timeStampSpeed = tlast.getDistance()/getLevelLength();
        timeStampSpeed = scrollSpeed/tlast.getStartDistance();
        System.out.println(timeStampSpeed);

        music = Gdx.audio.newMusic(Gdx.files.internal("music/shaman_gravity.mp3"));
        //music.setVolume(0.1f);
        //music.play();

        //Set the center of the screen as a point
        center = new Point(viewport.getWorldWidth()/2, viewport.getWorldHeight()/2);

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
            pointer.yPoints[i] = (float) (pointer.getCenterY() + Math.sin(Math.toRadians(pointerAngle + (360f / pointer.xPoints.length) * i)) * pointer.getR() / tiltRatio);
        }

        //Set the colors for the game - these can be changed later (fade)
        colors = new int[5];
        colors[0] = 0xfefcfdFF; //middle hexagon outline
        colors[1] = 0x747474FF; //middle hexagon fill
        colors[2] = 0xadabacFF; //lighter background part
        colors[3] = 0x747474FF; //darker background part
        colors[4] = 0x919090FF; //third background color if the number of sides is odd (middle color)
        //colors[4] = 0x747474FF;
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        drawBackground();
        for(Trapez t:trapezi)
            drawTrapez(t, t.getDistance(), t.getSize(), angle + (float) 360 / numberOfSides * t.getPosition(), 0xFFFFFFFF);
        drawEquilateralPolygon(pointer,3, pointer.getR(), pointer.getCenterX(), pointer.getCenterY(), colors[0], pointerAngle);
        drawEquilateralPolygon(middleHexagon[1], numberOfSides, middleHexagon[1].getR()+10, center.x, center.y, colors[0], angle);
        drawEquilateralPolygon(middleHexagon[0], numberOfSides, middleHexagon[0].getR(), center.x, center.y, colors[1], angle);
        drawInfo();
        update(Gdx.graphics.getDeltaTime());
    }
    private void update(float delta){
        dt = delta;
        tlast = trapezi.get(trapezi.size - 1);
        if(levelTimestamp <= 1)
            levelTimestamp +=timeStampSpeed*dt;

        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE))
            dispose();
        //Press F to make fullscreen (to do - put it as an option)
        if(Gdx.input.isKeyPressed(Input.Keys.F)) {
            if(game.fullscreen) {
                Gdx.graphics.setWindowedMode((int)viewport.getWorldWidth(), (int)viewport.getWorldHeight());
                game.fullscreen = false;
            }
            else {
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode(game.primaryMonitor));
                game.fullscreen = true;
            }
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            game.setScreen(new EditorScreen(game));
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
            if(t.getSize() <= 0)
                t.setSize(0);
        }
        boolean collidedLeft = false;
        boolean collidedRight = false;
        for(Trapez t:trapezi){
            if(Intersector.intersectPolygons(t.getFloatArray(), pointer.getFloatArray())){
                Vector pointerVector;
                Vector trapezVector;
                //float trapezAngle = angle+(float)360/numberOfSides*t.getPosition();
                if(pointerRotationR <= t.getDistance()){
                    dispose();
                } //now check if the pointer is colliding with right or left size of trapez
                else if(Intersector.isPointInPolygon(t.getPoints(), 0, t.getPoints().length, pointer.getXPoints()[2], pointer.getYPoints()[2])){
                    collidedRight = true;
                    pointerVector = new Vector(center, new Point(pointer.getXPoints()[2], pointer.getYPoints()[2]));
                    trapezVector = new Vector(new Point(t.getXPoints()[1], t.getYPoints()[1]), new Point(t.getXPoints()[2], t.getYPoints()[2]));
                    pointerAngle += Math.floor(Math.toDegrees(pointerVector.getAngle(trapezVector))/tiltRatio);
                }
                else{
                    collidedLeft = true;
                    pointerVector = new Vector(center, new Point(pointer.getXPoints()[1], pointer.getYPoints()[1]));
                    trapezVector = new Vector(new Point(t.getXPoints()[0], t.getYPoints()[0]), new Point(t.getXPoints()[3], t.getYPoints()[3]));
                    pointerAngle -= Math.floor(Math.toDegrees(pointerVector.getAngle(trapezVector))/tiltRatio);
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
                center.y + (float)(pointerRotationR * Math.sin(Math.toRadians(pointerAngle)))/tiltRatio);
        pointerAngle += rotateSpeed*dt;
        pointerAngle=pointerAngle%360;
        angle+=rotateSpeed*dt;
        angle=angle%360;

        /*if(!(tiltRatio < 2 && tiltRatio >= 1)){ //tilt the screen up and down
            tiltRatioInc = !tiltRatioInc;
        }
        if(tiltRatioInc)
            tiltRatio+=0.3f*dt;
        else
            tiltRatio-=0.3f*dt;*/
        //tiltRatio = 2;
    }
    private void drawBackground(){
        for(int i=0; i<numberOfSides; i++) {
            Polygon bgTriangle = new Polygon(new float[3], new float[3]);
            for (int j = 1; j < 3; j++) {
                bgTriangle.xPoints[0] = viewport.getWorldWidth() / 2f;
                bgTriangle.yPoints[0] = viewport.getWorldHeight() / 2f;
                bgTriangle.xPoints[j] = bgTriangle.xPoints[0] + (float) Math.cos(Math.toRadians(angle + (360f / numberOfSides) * (i + j))) * 9999; //edges of background triangles need to be very far away from the center to cover the whole screen
                bgTriangle.yPoints[j] = bgTriangle.yPoints[0] + (float)(Math.sin(Math.toRadians(angle + (360f / numberOfSides) * (i + j))) * 9999)/tiltRatio;
            }
            int tempColor;
            if(numberOfSides%2 == 0) {
                if (i % 2 == 0)
                    tempColor = colors[2];
                else
                    tempColor = colors[3];
            }
            else{
                if (i % 3 == 0)
                    tempColor = colors[2];
                else if(i % 2 == 0)
                    tempColor = colors[3];
                else
                    tempColor = colors[4];
            }
            drawPolygon(bgTriangle.getPoints(), tempColor);
        }
    }
    private void drawEquilateralPolygon(Polygon p, int n, int r, float x, float y, int color, float startAngle){
        for(int i=0; i<n; i++) {
            double segment = Math.toRadians(startAngle + (360f / n) * i);
            p.xPoints[i] = (float) (x + Math.cos(segment) * r);
            p.yPoints[i] = (float) (y + Math.sin(segment) * r / tiltRatio);
        }
        drawPolygon(p.getPoints(), color);
    }
    private void drawTrapez(Polygon p, double distance, double size, double startAngle, int color){
        p.xPoints = new float[4];
        float x = center.x;
        p.xPoints[0] = (float)(x + distance*Math.cos(Math.toRadians(startAngle)));
        p.xPoints[1] = (float)(x + distance*Math.cos(Math.toRadians(360f/numberOfSides)+Math.toRadians(startAngle)));
        p.xPoints[3] = (float)(x + (distance+size)*Math.cos(Math.toRadians(startAngle)));
        p.xPoints[2] = (float)(x + (distance+size)*Math.cos(Math.toRadians(360f/numberOfSides)+Math.toRadians(startAngle)));

        p.yPoints = new float[4];
        float y = center.y;
        p.yPoints[0] = (float)(y + distance/tiltRatio*Math.sin(Math.toRadians(startAngle)));
        p.yPoints[1] = (float)(y + distance/tiltRatio*Math.sin(Math.toRadians(360f/numberOfSides)+Math.toRadians(startAngle)));
        p.yPoints[3] = (float)(y + (distance+size)/tiltRatio*Math.sin(Math.toRadians(startAngle)));
        p.yPoints[2] = (float)(y + (distance+size)/tiltRatio*Math.sin(Math.toRadians(360f/numberOfSides)+Math.toRadians(startAngle)));

        drawPolygon(p.getPoints(), color);
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
        drawPolygon(background.getPoints(), 0x000000FF);
    }

    public void drawText(String text, float size, float x, float y) {
        batch.setProjectionMatrix(camera.combined); //so that coordinates are relative to camera (to the viewport)
        batch.begin();
        font.getData().setScale(size/64);
        font.draw(batch, text, x, y);
        batch.end();
    }

    public long getPassedTime() { //time that has passed since the beginning of the level (screen)
        long passedTime = System.nanoTime() - startTime;
        return passedTime;
    }

    public void drawInfo(){
        long passedTime = getPassedTime();
        if(levelTimestamp < 1) {
            seconds = passedTime / 1000000000; //conversion from nanoseconds to seconds
            milliseconds = (passedTime - seconds * 1000000000) / 100000000;
        }
        drawScreenBox(true,200, 80, 40);
        drawScreenBox(true,400, 50, 20);
        drawScreenBox(false,300, 50, 20);
        drawText("Level 1", 32, 10, viewport.getWorldHeight()-8);
        drawText("Time:", 30, viewport.getWorldWidth()-390, viewport.getWorldHeight()-8);
        if(seconds >= 10)
            drawText(""+seconds, 48, viewport.getWorldWidth()-185, viewport.getWorldHeight()-15);
        else
            drawText("0"+seconds, 48, viewport.getWorldWidth()-185, viewport.getWorldHeight()-15);
        drawText("."+milliseconds, 20, viewport.getWorldWidth()-65, viewport.getWorldHeight()-43);
    }

    public void importLevel(File levelFile){
        try {
            FileInputStream fin = new FileInputStream(levelFile);
            ObjectInputStream ois = new ObjectInputStream(fin);
            trapezi = new Array<>();
            while(ois.available() != -1){
                Object t = ois.readObject();
                trapezi.add((Trapez)t);
            }
            System.out.println("Level successfully imported");
            ois.close();

        }catch(EOFException ignored){ //ta exception nam samo pove, da je konec datoteke
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally{
            System.out.println(trapezi.size);
        }
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
    private void drawPolygon(float [] points, int color){
        pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        textureSolid = new Texture(pixmap);
        ect = new EarClippingTriangulator();
        polyReg = new PolygonRegion(new TextureRegion(textureSolid), points, ect.computeTriangles(points).toArray());
        poly = new PolygonSprite(polyReg);
        poly.setOrigin(25, 25);
        polyBatch = new PolygonSpriteBatch();
        polyBatch.setProjectionMatrix(camera.combined);
        polyBatch.begin();
        polyBatch.draw(polyReg, 2, 2);
        polyBatch.end();
    }
    @Override
    public void dispose() {
        pixmap.dispose();
        polyBatch.dispose();
        textureSolid.dispose();
        game.dispose();
        batch.dispose();
        font.dispose();
        //music.stop();
        music.dispose();
        sr.dispose();
    }
}
