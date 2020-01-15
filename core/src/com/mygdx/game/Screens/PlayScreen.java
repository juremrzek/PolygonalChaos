package com.mygdx.game.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSprite;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.mygdx.game.MyGdxGame;
import com.mygdx.game.Objects.Point;
import com.mygdx.game.Objects.Polygon;
import com.mygdx.game.Objects.Trapez;
import com.badlogic.gdx.math.Intersector;

public class PlayScreen implements Screen {

    private MyGdxGame game;
    private FitViewport viewport;
    private OrthographicCamera camera;
    private Pixmap pixmap;

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

    //variables to create game diversity
    private short numberOfSides;
    private int angle;
    private int rotateSpeed;
    private int scrollSpeed;
    private float tiltRatio;
    private boolean tiltRatioInc;
    private int [] colors;

    private int pointerAngle;
    private int pointerRotationR;
    private int pointerRotationSpeed;

    private Point center;

    public PlayScreen(MyGdxGame game){
        angle = 0;
        numberOfSides = 6;
        rotateSpeed = 2;
        tiltRatio = 1f;
        tiltRatioInc = true;
        scrollSpeed = 2;

        this.game = new MyGdxGame();
        camera = new OrthographicCamera();
        viewport = new FitViewport(1280, 900, camera);

        //Set the center of the screen
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

        trapezi = new Array<>();
        trapezi.add(new Trapez(100, 400));
        trapezi.add(new Trapez(100, 800));

        //Set the colors for the game - these can be changed later (gradient)
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
            drawTrapez(t, t.getDistance(), t.getSize(), angle, 0xFFFFFFFF);
            t.setDistance(t.getDistance()-scrollSpeed);
        }
        drawEquilateralPolygon(middleHexagon[1], numberOfSides, middleHexagon[1].getR()+10, center.x, center.y, colors[0], angle);
        drawEquilateralPolygon(middleHexagon[0], numberOfSides, middleHexagon[0].getR(), center.x, center.y, colors[1], angle);
        drawEquilateralPolygon(pointer,3, pointer.getR(), pointer.getCenterX(), pointer.getCenterY(), colors[0], pointerAngle);
        update(Gdx.graphics.getDeltaTime());
    }
    private void update(float delta){
        if(Gdx.input.isKeyPressed(Input.Keys.ESCAPE))
            dispose();
        //Press F to make fullscreen (to do - put it as an option)
        if(Gdx.input.isKeyPressed(Input.Keys.F)) {
            if(game.fullscreen) {
                Gdx.graphics.setWindowedMode((int)viewport.getWorldWidth(), (int)viewport.getWorldHeight());
                game.fullscreen = false;
            }
            else {
                Graphics.Monitor primaryMonitor = Gdx.graphics.getPrimaryMonitor();
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode(primaryMonitor));
                game.fullscreen = true;
            }
        }

        boolean collided = false;
        for(int i=0; i<trapezi.size; i++){
            if(Intersector.intersectPolygons(new FloatArray(trapezi.get(i).getPoints()), new FloatArray(pointer.getPoints()))){
                collided = true;
            }
        }
        if(!collided){
            if(Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)){
                pointerAngle += pointerRotationSpeed;
            }
            if(Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)){
                pointerAngle -= pointerRotationSpeed;
            }
        }

        //move the pointer based on pointerAngle
        pointer.setCenter(center.x + (float)(pointerRotationR * Math.cos(Math.toRadians(pointerAngle))),
                center.y + (float)(pointerRotationR * Math.sin(Math.toRadians(pointerAngle)))/tiltRatio);
        pointerAngle += rotateSpeed;
        if(pointerAngle > 360){
            pointerAngle = 0;
        }

        if(angle < 360)
            angle+=rotateSpeed;
        else
            angle = 0;

        /*if(!(tiltRatio < 3 && tiltRatio >= 1)){ //tilt the screen up and down
            tiltRatioInc = !tiltRatioInc;
        }
        if(tiltRatioInc)
            tiltRatio+=0.01f;
        else
            tiltRatio-=0.01f;*/
    }
    private void drawBackground(){
        for(int i=0; i<numberOfSides; i++) {
            Polygon bgTriangle = new Polygon(new float[3], new float[3]);
            for (int j = 1; j < 3; j++) {
                bgTriangle.xPoints[0] = viewport.getWorldWidth() / 2f;
                bgTriangle.yPoints[0] = viewport.getWorldHeight() / 2f;
                bgTriangle.xPoints[j] = bgTriangle.xPoints[0] + tiltRatio*(float) Math.cos(Math.toRadians(angle + (360f / numberOfSides) * (i + j))) * 9999;
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
    private void drawEquilateralPolygon(Polygon p, int n, int r, float x, float y, int color, int startAngle){
        for(int i=0; i<p.xPoints.length; i++) {
            p.xPoints[i] = (float) (x + Math.cos(Math.toRadians(startAngle + (360f / p.xPoints.length) * i)) * r);
            p.yPoints[i] = (float) (y + Math.sin(Math.toRadians(startAngle + (360f / p.xPoints.length) * i)) * r / tiltRatio);
        }
        drawPolygon(p.getPoints(), color);
    }
    private void drawTrapez(Polygon p, int distance, int size, int startAngle, int color){
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
    }
}
