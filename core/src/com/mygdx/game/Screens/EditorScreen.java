package com.mygdx.game.Screens;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.mygdx.game.MyGdxGame;
import com.mygdx.game.Objects.*;

import java.io.*;

public class EditorScreen extends InputAdapter implements Screen {

    private float dt; //deltatime
    private String levelName;

    private MyGdxGame game;
    private FitViewport viewport;
    private OrthographicCamera camera;
    private Pixmap pixmap;
    private SpriteBatch batch;
    private BitmapFont font;
    private ShapeRenderer sr;
    private InputProcessor mouseListener;

    //Objects to draw polygons
    private PolygonSprite poly;
    private PolygonSpriteBatch polyBatch;
    private Texture textureSolid;
    private EarClippingTriangulator ect;
    private PolygonRegion polyReg;

    //Objects that exist on the screen
    private Polygon[] middleHexagon; //Hexagon in the middle of the screen (2 of them because the bigger one is the outline)
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
    private float timestampSpeed;
    private float progressBarWidth;
    private float progressBarHeight;
    private boolean movingBar;
    private boolean movingTrapez;
    private float distanceFromProgressBarToMouse;
    private float distanceFromTrapezToMouse;
    private boolean placing;
    private boolean dragging;
    private float sizeOfNewTrapez;

    private Point center;
    private Point mouse;
    private Circle progressIndicator;
    private Music music;

    public EditorScreen(MyGdxGame game){
        angle = 0;
        numberOfSides = 6;
        tiltRatio = 1;
        tiltRatioInc = true;
        movingBar = false;
        movingTrapez = false;
        placing = true;
        dragging = true;
        sizeOfNewTrapez = 100;
        levelName = "TestLevel";
        Gdx.input.setInputProcessor(this);

        //variables that scale with delta
        rotateSpeed = 0;
        scrollSpeed = 200;
        //levelTimestamp = 0.5f;
        timestampSpeed = 0.2f;

        this.game = game;
        camera = new OrthographicCamera();
        viewport = new FitViewport(1280, 900, camera);

        batch = new SpriteBatch();
        font = new BitmapFont(Gdx.files.internal("fonts/font.fnt"));
        font.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        sr = new ShapeRenderer();
        sr.setColor(Color.BLACK);

        music = Gdx.audio.newMusic(Gdx.files.internal("music/shaman_gravity.mp3"));
        //music.setVolume(0.1f);
        //music.play();

        //Set the center of the screen as a point
        center = new Point(viewport.getWorldWidth()/2, viewport.getWorldHeight()/2);
        mouse = new Point(Gdx.input.getX(), Gdx.input.getX());
        progressBarWidth = 600f;
        progressBarHeight = 20f;
        progressIndicator = new Circle(center.x-progressBarWidth/2, viewport.getWorldHeight()-50+progressBarHeight/2, progressBarHeight*1.1f);

        middleHexagon = new Polygon[2];
        for(int i=0; i<middleHexagon.length; i++) {
            middleHexagon[i] = new Polygon(new float[numberOfSides], new float[numberOfSides], 70);
            middleHexagon[i].setCenter(center.x, center.y);
        }
        trapezi = new Array<>();

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
            initTrapez(t, t.getDistance(), t.getSize(), angle + (float) 360 / numberOfSides * t.getPosition());
            drawPolygon(t.getPoints(), 0xFFFFFFFF);
            if(t.isSelected()){
                t.drawOutline(sr, Color.BLACK);
            }
        }
        drawEquilateralPolygon(middleHexagon[1], numberOfSides, middleHexagon[1].getR()+10, center.x, center.y, colors[0], angle);
        drawEquilateralPolygon(middleHexagon[0], numberOfSides, middleHexagon[0].getR(), center.x, center.y, colors[1], angle);
        drawInfo();
        drawProgressBar(progressBarWidth, progressBarHeight);
        update(Gdx.graphics.getDeltaTime());
    }
    private void update(float delta){
        dt = delta;
        mouse.x = Gdx.input.getX();
        mouse.y = viewport.getWorldHeight()-Gdx.input.getY();
        if(trapezi.isEmpty())
            tlast = new Trapez(0, 1000, 0);
        else
            tlast = trapezi.get(trapezi.size - 1);
        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE))
            dispose();
        if(Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            exportLevel();
            game.setScreen(new PlayScreen(game));
        }
        if(Gdx.input.isKeyPressed(Input.Keys.D))
            progressIndicator.setX(progressIndicator.getX()+timestampSpeed*dt*progressBarWidth);
        if(Gdx.input.isKeyPressed(Input.Keys.A))
            progressIndicator.setX(progressIndicator.getX()-timestampSpeed*dt*progressBarWidth);

        if(Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            if(!movingBar && mouse.distanceFrom(progressIndicator.getCenter()) <= progressIndicator.getR()){
                movingBar = true;
                distanceFromProgressBarToMouse = mouse.x - progressIndicator.getX();
            }
            if(placing && !progressIndicator.intersects(mouse)){
                //We have to find out what position is our new trapez going to have

                boolean intersects = false;

                Trapez nt = new Trapez(sizeOfNewTrapez, (tlast.getStartDistance() + tlast.getStartSize()) * levelTimestamp + mouse.distanceFrom(center), getMousePosition());
                initTrapez(nt, nt.getDistance(), sizeOfNewTrapez, angle + (float) 360 / numberOfSides * nt.getPosition());
                float mouseDistance = mouse.getDistanceFromTrapezSideToCenter(nt, center);

                nt.setStartDistance((tlast.getStartDistance() + tlast.getStartSize()) * levelTimestamp + mouseDistance - nt.getSize()/2);
                initTrapez(nt, nt.getDistance(), sizeOfNewTrapez, angle + (float) 360 / numberOfSides * nt.getPosition());
                for(Trapez t:trapezi){
                    if (nt.getPosition() == t.getPosition() && Intersector.intersectPolygons(nt.getFloatArray(), t.getFloatArray()))
                        intersects = true;
                }
                if(!intersects){
                    if(nt.getStartDistance() > tlast.getStartDistance()){
                        trapezi.add(nt);
                        if(trapezi.size > 1){
                            //if we place trapez after the last one, we extend the length of the whole level
                            progressIndicator.setX(progressBarWidth*tlast.getStartDistance()/nt.getStartDistance()*levelTimestamp+center.x-progressBarWidth/2);
                        }
                    }
                    else
                        trapezi.insert(0, nt);
                }
            }
        }
        if(Gdx.input.isButtonPressed(Input.Buttons.LEFT)){
            if(dragging && !movingBar){
                for(int i=0; i<trapezi.size; i++){
                    Trapez t = trapezi.get(i);
                    if(Intersector.isPointInPolygon(t.getPoints(), 0, t.getPoints().length, mouse.x, mouse.y)){
                        if(!movingTrapez){
                            distanceFromTrapezToMouse = mouse.distanceFrom(center) - t.getDistance();
                            movingTrapez = true;
                            t.setSelected(true);
                        }
                    }
                    if(t.isSelected() && getMousePosition() == t.getPosition()) {
                        t.setStartDistance((tlast.getStartDistance() + tlast.getStartSize()) * levelTimestamp + mouse.distanceFrom(center) - distanceFromTrapezToMouse);
                        t.setDistance((tlast.getStartDistance() + tlast.getStartSize())*levelTimestamp);
                        if(t.getStartDistance() > tlast.getStartDistance()){
                            tlast = t;
                            trapezi.removeValue(t, false);
                            trapezi.add(t);
                        }
                    }
                    else{
                        t.setSelected(false);
                    }
                }
            }
        }else{
            movingBar = false;
            movingTrapez = false;
            for(Trapez t:trapezi)
                t.setSelected(false);
        }
        if(movingBar){
            progressIndicator.setX(mouse.x-distanceFromProgressBarToMouse);
        }
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
        if(Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            File f = new File("core/assets/levels/" + levelName + ".lvl");
            if(f.exists())
                importLevel(f);
        }

        for(int i=0; i<trapezi.size; i++){
            Trapez t = trapezi.get(i);
            t.setDistance(t.getStartDistance() - (tlast.getStartDistance()+tlast.getStartSize())*levelTimestamp);
            if(t.getDistance() <= middleHexagon[1].getR()){
                 float distanceDiff;
                 distanceDiff = Math.abs(t.getDistance()-middleHexagon[1].getR());
                 t.setSize(t.getStartSize()-distanceDiff);
                 t.setDistance(t.getDistance()+distanceDiff);
            }
            else{
                t.setSize(t.getStartSize());
            }
            if(t.getSize() <= 0)
                t.setSize(0);
        }

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
    private void initTrapez(Polygon p, double distance, double size, double startAngle){
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
    public void drawProgressBar(float width, float height){
        sr.setColor(Color.BLACK);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setProjectionMatrix(camera.combined);
        sr.rect(center.x-width/2, viewport.getWorldHeight()-50, width, height);
        sr.circle(center.x-width/2, viewport.getWorldHeight()-50+height/2, height/2);
        sr.circle(center.x+width/2, viewport.getWorldHeight()-50+height/2, height/2);
        if(progressIndicator.getX() > center.x+width/2)
            progressIndicator.setX(center.x+width/2);
        if(progressIndicator.getX() < center.x-width/2)
            progressIndicator.setX(center.x-width/2);
        levelTimestamp = (progressIndicator.getX()-(center.x-width/2))/((center.x+width/2)-(center.x-width/2));
        sr.circle(progressIndicator.getX(), progressIndicator.getY(), progressIndicator.getR());
        sr.end();

        sr.setColor(Color.WHITE);
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.circle(progressIndicator.getX(), progressIndicator.getY(), progressIndicator.getR());
        sr.end();
    }

    public void drawInfo(){
        drawScreenBox(false,200, 50, 40);
        drawText("Editor", 28, 10, viewport.getWorldHeight()-8);
        drawScreenBox(true,280, 50, 40);
        drawScreenBox(true,180, 100, 80);
        drawText("Length:", 28, viewport.getWorldWidth()-265, viewport.getWorldHeight()-8);
        //System.out.println(getLevelLength());
        /*if(seconds >= 10)
            drawText(""+seconds, 48, viewport.getWorldWidth()-185, viewport.getWorldHeight()-15);
        else
            drawText("0"+seconds, 48, viewport.getWorldWidth()-185, viewport.getWorldHeight()-15);
        drawText("."+milliseconds, 20, viewport.getWorldWidth()-65, viewport.getWorldHeight()-43);*/
    }

    public float getLevelLength(){
        if(trapezi.isEmpty())
            return 0;
        else return tlast.getStartDistance()/scrollSpeed;
    }
    public int getMousePosition(){
        int position;
        Vector mouseVector = new Vector(center, mouse);
        Vector v = new Vector(center, new Point(middleHexagon[0].getXPoints()[0], middleHexagon[0].getYPoints()[0]));
        if(mouse.y >= center.y)
            position = (int)((mouseVector.getAngle(v))/(Math.PI*2/numberOfSides));
        else
            position = (int)((-mouseVector.getAngle(v))/(Math.PI*2/numberOfSides));
        if(position == 0 && mouse.y <= center.y) {
            position = numberOfSides - 1;
        }
        if(position < 0)
            position += numberOfSides-1;

        return position;
    }
    public void exportLevel() {
        try {
            FileOutputStream fos = new FileOutputStream("core/assets/levels/" + levelName + ".lvl");
            ObjectOutputStream ous = new ObjectOutputStream(fos);
            for(Trapez t:trapezi) {
                ous.writeObject(t);
            }
            ous.close();
            System.out.println("Level exported to core/assets/levels");
        } catch (Exception e) {
            System.out.println("An error has occurred");
            e.printStackTrace();
        }
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

        }catch(EOFException ignore) { //ta exception nam samo pove, da je konec datoteke
        }catch (Exception ex) {
            ex.printStackTrace();
        }
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
    @Override
    public boolean scrolled(int amount){
        if(amount == 1)
            progressIndicator.setX(progressIndicator.getX()+timestampSpeed*dt*progressBarWidth);
        if(amount == -1)
            progressIndicator.setX(progressIndicator.getX()-timestampSpeed*dt*progressBarWidth);
        return false;
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
        sr.dispose();
        //music.stop();
        music.dispose();
    }
}
