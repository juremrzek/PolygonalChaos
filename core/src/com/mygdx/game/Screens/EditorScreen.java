package com.mygdx.game.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.mygdx.game.MyGdxGame;
import com.mygdx.game.Objects.Point;
import com.mygdx.game.Objects.Polygon;
import com.mygdx.game.Objects.Trapez;
import com.badlogic.gdx.math.Intersector;

public class EditorScreen implements Screen {

    private float dt; //deltatime

    private MyGdxGame game;
    private FitViewport viewport;
    private OrthographicCamera camera;
    private Pixmap pixmap;
    private SpriteBatch batch;
    private BitmapFont font;

    //Objects to draw polygons
    private PolygonSprite poly;
    private PolygonSpriteBatch polyBatch;
    private Texture textureSolid;
    private EarClippingTriangulator ect;
    private PolygonRegion polyReg;

    //Objects that exist on the screen
    private Polygon[] middleHexagon; //Hexagon in the middle of the screen (2 of them because the bigger one is the outline)
    private Array<Trapez> trapezi; //Obstacles (Array is Libgdx's equivalent to ArrayList)

    //variables to create game diversity
    private short numberOfSides;
    private float angle;
    private float rotateSpeed;
    private float scrollSpeed;
    private float tiltRatio;
    private boolean tiltRatioInc;
    private int [] colors;
    private long startTime; //time when the screen was shown
    private float levelTimestamp; //from 0 to 1 -> 1 being 100%

    private Point center;
    private Music music;

    public EditorScreen(MyGdxGame game){
        angle = 0;
        numberOfSides = 6;
        tiltRatio = 1;
        tiltRatioInc = true;
        startTime = System.nanoTime();

        this.game = game;
        camera = new OrthographicCamera();
        viewport = new FitViewport(1280, 900, camera);

        batch = new SpriteBatch();
        font = new BitmapFont(Gdx.files.internal("font.fnt"));
        font.setColor(1.0f, 1.0f, 1.0f, 1.0f);

        /*music = Gdx.audio.newMusic(Gdx.files.internal("super_hexagonest.mp3"));
        music.setVolume(0.1f);
        music.play();*/

        //Set the center of the screen as a point
        center = new Point(viewport.getWorldWidth()/2, viewport.getWorldHeight()/2);

        middleHexagon = new Polygon[2];
        for(int i=0; i<middleHexagon.length; i++) {
            middleHexagon[i] = new Polygon(new float[numberOfSides], new float[numberOfSides], 70);
            middleHexagon[i].setCenter(center.x, center.y);
        }

        trapezi = new Array<>();
        trapezi.add(new Trapez(100, 600, 2));
        trapezi.add(new Trapez(100, 800,2));
        trapezi.add(new Trapez(100, 1200,5));
        trapezi.add(new Trapez(100, 1400,0));
        trapezi.add(new Trapez(100, 1800,3));

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
        for(Trapez t:trapezi) {
            drawTrapez(t, t.getDistance(), t.getSize(), angle+(float)360/numberOfSides*t.getPosition(), 0xFFFFFFFF);
        }
        drawEquilateralPolygon(middleHexagon[1], numberOfSides, middleHexagon[1].getR()+10, center.x, center.y, colors[0], angle);
        drawEquilateralPolygon(middleHexagon[0], numberOfSides, middleHexagon[0].getR(), center.x, center.y, colors[1], angle);
        //drawInfo();
        update(Gdx.graphics.getDeltaTime());
    }
    private void update(float delta){
        dt = delta;
        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE))
            dispose();
        //Press F to make fullscreen (to do - put it as an option)
        if(Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            if(game.fullscreen) {
                Gdx.graphics.setWindowedMode((int)viewport.getWorldWidth(), (int)viewport.getWorldHeight());
                game.fullscreen = false;
            }
            else {
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode(game.primaryMonitor));
                game.fullscreen = true;
            }
        }
        ///eeeeeeeeeeeeeeeeeeeeeeeeeeeee
        if(Gdx.input.isKeyPressed(Input.Keys.D)) {
            if(levelTimestamp<1)
                levelTimestamp += 0.001f;
            else
                levelTimestamp = 1;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.A)) {
            if (levelTimestamp > 0)
                levelTimestamp -= 0.001f;
            else
                levelTimestamp = 0;
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            game.setScreen(new PlayScreen(game));
        }

        for(Trapez t:trapezi) {
            t.setDistance(t.getDistance()-scrollSpeed*dt);
        }
        angle+=rotateSpeed*dt;
        angle=angle%360;

        /*if(!(tiltRatio < 3 && tiltRatio >= 1)){ //tilt the screen up and down
            tiltRatioInc = !tiltRatioInc;
        }
        if(tiltRatioInc)
            tiltRatio+=0.1f*dt;
        else
            tiltRatio-=0.1f*dt;*/
    }
    private void drawBackground(){
        for(int i=0; i<numberOfSides; i++) {
            Polygon bgTriangle = new Polygon(new float[3], new float[3]);
            for (int j = 1; j < 3; j++) {
                bgTriangle.xPoints[0] = viewport.getWorldWidth() / 2f;
                bgTriangle.yPoints[0] = viewport.getWorldHeight() / 2f;
                bgTriangle.xPoints[j] = bgTriangle.xPoints[0] + tiltRatio*(float) Math.cos(Math.toRadians(angle + (360f / numberOfSides) * (i + j))) * 9999; //edges of background triangles need to be very far away from the center to cover the whole screen
                bgTriangle.yPoints[j] = bgTriangle.yPoints[0] + (float) Math.sin(Math.toRadians(angle + (360f / numberOfSides) * (i + j))) * 9999;
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
        for(int i=0; i<p.xPoints.length; i++) {
            p.xPoints[i] = (float) (x + Math.cos(Math.toRadians(startAngle + (360f / p.xPoints.length) * i)) * r);
            p.yPoints[i] = (float) (y + Math.sin(Math.toRadians(startAngle + (360f / p.xPoints.length) * i)) * r / tiltRatio);
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
        p.yPoints[0] = (float)(y + distance*Math.sin(Math.toRadians(startAngle)));
        p.yPoints[1] = (float)(y + distance*Math.sin(Math.toRadians(360f/numberOfSides)+Math.toRadians(startAngle)));
        p.yPoints[3] = (float)(y + (distance+size)*Math.sin(Math.toRadians(startAngle)));
        p.yPoints[2] = (float)(y + (distance+size)*Math.sin(Math.toRadians(360f/numberOfSides)+Math.toRadians(startAngle)));

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
        long seconds = passedTime/1000000000; //conversion from nanoseconds to seconds
        long milliseconds = (passedTime-seconds*1000000000)/100000000;
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
    }
}