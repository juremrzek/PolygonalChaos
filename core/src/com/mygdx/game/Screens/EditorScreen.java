package com.mygdx.game.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.actions.ColorAction;
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.mygdx.game.MyGdxGame;
import com.mygdx.game.Objects.*;

import java.awt.color.*;

import java.io.*;

public class EditorScreen extends InputAdapter implements Screen {

    private float dt; //deltatime
    private String levelName;
    private String songName;

    private MyGdxGame game;
    private FitViewport viewport;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private BitmapFont font;
    private ShapeRenderer sr;
    private Icon[] icons;

    //Objects to draw polygons
    private Pixmap pixmap;
    private PolygonSpriteBatch polyBatch;
    private Texture textureSolid;
    private EarClippingTriangulator ect;
    private PolygonSprite poly;

    //Objects that exist on the screen
    private Polygon[] middleHexagon; //Hexagon in the middle of the screen (2 of them because the bigger one is the outline)
    private Array<Trapez> trapezi; //Obstacles (Array is Libgdx's equivalent to ArrayList)
    private Trapez tlast;

    //variables to create game diversity
    private int numberOfSides;
    private float angle;
    private float scrollSpeed;
    private Color[] colors;
    private Color[] newColors;
    private Color[] currColorSet;
    private ColorAction [] colorActions;

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
    private boolean deleting;
    private boolean settings;
    private float sizeOfNewTrapez;
    private Vector2 mouse;
    private Vector3 mouseIn3D;

    private Point center;
    private Circle progressIndicator;
    private Music music;

    public EditorScreen(MyGdxGame game){
        this.game = game;
    }

    @Override
    public void show() {
        angle = 0;
        numberOfSides = 8;
        movingBar = false;
        movingTrapez = false;

        sizeOfNewTrapez = 100;
        levelName = "Anothaone";
        songName = "ddropp";
        Gdx.input.setInputProcessor(this);

        scrollSpeed = 200;
        camera = new OrthographicCamera();
        viewport = new FitViewport(1280, 900, camera);
        icons = new Icon[]{new Icon(60, viewport.getWorldHeight()-60, 35, "icons/drag.png", Color.BLACK),
                new Icon(160, viewport.getWorldHeight()-60, 35, "icons/draw.png", Color.BLACK),
                new Icon(260, viewport.getWorldHeight()-60, 35, "icons/delete.png", Color.BLACK),
                new Icon(viewport.getWorldWidth()-80, 60, 35, "icons/settings.png", Color.BLACK)};

        batch = new SpriteBatch();
        font = new BitmapFont(Gdx.files.internal("fonts/font.fnt"));
        font.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        sr = new ShapeRenderer();
        sr.setColor(Color.BLACK);
        music = Gdx.audio.newMusic(Gdx.files.internal("music/shaman_gravity.mp3"));
        //music.setVolume(0.1f);
        //music.play();
        center = new Point(viewport.getWorldWidth()/2, viewport.getWorldHeight()/2);

        Trapez tempTrapez = new Trapez(100, 100, 1);
        initTrapez(tempTrapez, tempTrapez.getStartDistance(), tempTrapez.getSize(), angle);
        //objects to draw polygons
        pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        ect = new EarClippingTriangulator();
        polyBatch = new PolygonSpriteBatch();
        pixmap.fill();
        textureSolid = new Texture(pixmap);
        PolygonRegion polyReg = new PolygonRegion(new TextureRegion(textureSolid), tempTrapez.getPoints(), ect.computeTriangles(tempTrapez.getPoints()).toArray());
        poly = new PolygonSprite(polyReg);

        //camera.unproject can only take Vector3 as an argument so we need to add this variable here
        mouseIn3D = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(mouseIn3D);
        mouse = new Vector2(mouseIn3D.x, mouseIn3D.y);

        progressBarWidth = 600f;
        progressBarHeight = 20f;
        progressIndicator = new Circle(center.x-progressBarWidth/2, viewport.getWorldHeight()-50+progressBarHeight/2, progressBarHeight*1.1f);
        middleHexagon = new Polygon[2];
        for(int i=0; i<middleHexagon.length; i++) {
            middleHexagon[i] = new Polygon(new float[numberOfSides], new float[numberOfSides], 70);
            middleHexagon[i].setCenter(center.x, center.y);
        }
        trapezi = new Array<>();

        //Set the colors for the game - these can be changed later
        currColorSet = ColorSets.BROWN;
        colors = new Color[currColorSet.length];
        for(int i=0; i<colors.length; i++){
            colors[i] = new Color(currColorSet[i]);
        }
        newColors = new Color[currColorSet.length];
        for(int i=0; i<newColors.length; i++){
            newColors[i] = new Color(currColorSet[i]);
        }

        colorActions = new ColorAction[currColorSet.length];
        for(int i=0; i<colorActions.length; i++){
            colorActions[i] = new ColorAction();
            colorActions[i].setColor(colors[i]);
            colorActions[i].setDuration(1);
            colorActions[i].setEndColor(newColors[i]);
        }
    }

    @Override
    public void render(float delta) {
        for (ColorAction ca : colorActions) {
            ca.act(delta);
        }
        sr.setProjectionMatrix(camera.combined);
        mouseIn3D.x = Gdx.input.getX();
        mouseIn3D.y = Gdx.input.getY();
        mouseIn3D.z = 0;
        viewport.unproject(mouseIn3D);
        mouse.x = mouseIn3D.x;
        mouse.y = mouseIn3D.y;
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        drawBackground();
        for(Trapez t:trapezi) {
            initTrapez(t, t.getDistance(), t.getSize(), angle + (float) 360 / numberOfSides * t.getPosition());
            if(t.getDistance() < 2000) {
                drawPolygon(t.getPoints(), colorActions[0].getColor());
            }
        }
        for(Trapez t:trapezi){
            if(t.isSelected()){
                t.drawOutline(sr, new Color(0x006400FF));
            }
        }
        drawEquilateralPolygon(middleHexagon[1], numberOfSides, middleHexagon[0].getR()+10, center.x, center.y, colorActions[0].getColor(), angle);
        drawEquilateralPolygon(middleHexagon[0], numberOfSides, middleHexagon[0].getR(), center.x, center.y, colorActions[1].getColor(), angle);
        for(Icon icon:icons)
            if(icon.isSelected())
                icon.setColor(new Color(0x006400FF));
            else
                icon.setColor(Color.BLACK);
        drawInfo();
        drawProgressBar(progressBarWidth, progressBarHeight, Color.BLACK);
        update(Gdx.graphics.getDeltaTime());
    }
    private void update(float delta){
        dt = delta;
        if(trapezi.isEmpty())
            tlast = new Trapez(0, 1000, 0);
        else
            tlast = trapezi.get(trapezi.size - 1);
        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)){
            for(Trapez t:trapezi){
                if(t.getDistance() < 1200 && t.getDistance() > -500){
                    t.setDistance(t.getStartDistance());
                }
            }
            exportLevel();
            game.setScreen(new MenuScreen(game));
        }
        if(Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D))
            progressIndicator.setX(progressIndicator.getX()+timestampSpeed*dt*progressBarWidth);
        if(Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A))
           progressIndicator.setX(progressIndicator.getX()-timestampSpeed*dt*progressBarWidth);

        if(Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            if(!movingBar && progressIndicator.intersects(mouse)){
                movingBar = true;
                distanceFromProgressBarToMouse = mouse.x - progressIndicator.getX();
            }
            //System.out.println(mouse.x + " " + icons[0].getX() +" "+ mouse.distanceFrom(new Point(icons[0].getX(), icons[0].getY())));
            for(int i=0; i<icons.length; i++){
                if(icons[i].intersects(mouse)){
                    for(int j=0; j<icons.length; j++){
                        if(i==j)
                            icons[j].setSelected(!icons[j].isSelected());
                        else
                            icons[j].setSelected(false);
                    }
                }
                dragging = icons[0].isSelected();
                placing = icons[1].isSelected();
                deleting = icons[2].isSelected();
                settings = icons[3].isSelected();
            }
            if(placing && !progressIndicator.intersects(mouse) && !icons[1].intersects(mouse) && !icons[3].intersects(mouse)){
                //We have to find out what position is our new trapez going to have
                boolean intersects = false;
                Trapez nt = new Trapez(sizeOfNewTrapez, (tlast.getStartDistance() + tlast.getStartSize()) * levelTimestamp + center.distanceFrom(mouse), getMousePosition());
                initTrapez(nt, nt.getDistance(), nt.getSize(), angle + (float) 360 / numberOfSides * nt.getPosition());
                float mouseDistance = getDistanceFromTrapezSideToCenter(nt);

                nt.setStartDistance((tlast.getStartDistance() + tlast.getStartSize()) * levelTimestamp + mouseDistance - nt.getSize()/2);
                nt.setDistance(nt.getStartDistance()-((tlast.getStartDistance() + tlast.getStartSize())*levelTimestamp));
                initTrapez(nt, nt.getDistance(), nt.getSize(), angle + (float) 360 / numberOfSides * nt.getPosition());
                for(Trapez t:trapezi){
                    if(nt.getPosition() == t.getPosition() && Intersector.intersectPolygons(nt.getFloatArray(), t.getFloatArray()))
                        intersects = true;
                }
                if(!intersects){
                    if(nt.getStartDistance() > tlast.getStartDistance()){
                        trapezi.add(nt);
                        if(trapezi.size > 1){
                            //if we place trapez after the last one, we extend the length of the whole level
                            progressIndicator.setX(progressBarWidth*tlast.getStartDistance()/nt.getStartDistance()*levelTimestamp+center.x-progressBarWidth/2);
                            levelTimestamp = (progressIndicator.getX()-(center.x-progressBarWidth/2))/((center.x+progressBarWidth/2)-(center.x-progressBarWidth/2));
                        }
                        tlast = nt;
                    }
                    else {
                        trapezi.insert(0, nt);
                    }
                }
            }
            for(int i=0; i<trapezi.size; i++) {
                Trapez t = trapezi.get(i);
                if(Intersector.isPointInPolygon(t.getPoints(), 0, t.getPoints().length, mouse.x, mouse.y)){
                    for(int j=0; j<trapezi.size; j++){
                        if(i==j)
                            trapezi.get(j).setSelected(true);
                        else
                            trapezi.get(j).setSelected(false);
                    }
                }
            }
        }
        if(Gdx.input.isButtonPressed(Input.Buttons.LEFT)){
            if(dragging && !movingBar){
                for(Trapez t:trapezi){
                    if(Intersector.isPointInPolygon(t.getPoints(), 0, t.getPoints().length, mouse.x, mouse.y)){
                        if(!movingTrapez){
                            distanceFromTrapezToMouse = center.distanceFrom(mouse) - t.getDistance();
                            movingTrapez = true;
                            t.setDragging(true);
                        }
                    }
                    if(t.isDragging() && getMousePosition() == t.getPosition()) {
                        t.setStartDistance((tlast.getStartDistance() + tlast.getStartSize()) * levelTimestamp + center.distanceFrom(mouse) - distanceFromTrapezToMouse);
                        t.setDistance(t.getStartDistance()-((tlast.getStartDistance() + tlast.getStartSize())*levelTimestamp));
                    }
                    else{
                        t.setDragging(false);
                    }
                }
            }
            if(deleting && !icons[2].intersects(mouse) && !icons[3].intersects(mouse)) {
                for (int i = 0; i < trapezi.size; i++) {
                    if(Intersector.isPointInPolygon(trapezi.get(i).getPoints(), 0, trapezi.get(i).getPoints().length, mouse.x, mouse.y)) {
                        trapezi.removeIndex(i);
                    }
                }
            }
        }else{
            movingBar = false;
            movingTrapez = false;
            for(Trapez t:trapezi)
                t.setDragging(false);
        }
        if(movingBar){
            progressIndicator.setX(mouse.x-distanceFromProgressBarToMouse);
        }
        if(settings){
            sr.setColor(Color.BLACK);
            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.rect(90, 90, viewport.getWorldWidth()-180, viewport.getWorldHeight()-180);
            sr.setColor(currColorSet[0]);
            sr.rect(100, 100, viewport.getWorldWidth()-200, viewport.getWorldHeight()-200);
            sr.end();
            drawText("oklmao", 64, 200, 200);
        }
        for(Trapez t:trapezi) {
            if(t.getStartDistance() > tlast.getStartDistance()) {
                tlast = t;
                trapezi.removeValue(t, false);
                trapezi.add(t);
            }
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
        if(Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            setNumberOfSides(numberOfSides-1);
            currColorSet = ColorSets.CYAN;
            changeColor();
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            File f = new File("core/assets/levels/" + levelName + ".lvl");
            if(f.exists())
                importLevel(f);
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.FORWARD_DEL)){
            for(int i=0; i<trapezi.size; i++){
                if(trapezi.get(i).isSelected())
                    trapezi.removeIndex(i);
            }
        }
        timestampSpeed = scrollSpeed/tlast.getStartDistance();
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
        levelTimestamp = (progressIndicator.getX()-(center.x-progressBarWidth/2))/((center.x+progressBarWidth/2)-(center.x-progressBarWidth/2));
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
                    tempColor = colorActions[2].getColor();
                else
                    tempColor = colorActions[3].getColor();
            }
            else{
                if (i % 5 == 0 && i % 3 == 0)
                    tempColor = colorActions[4].getColor();
                else if(i % 2 == 0)
                    tempColor = colorActions[3].getColor();
                else
                    tempColor = colorActions[2].getColor();
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
        font.getData().setScale(size/64); //The non-scaled font is size of 64px, we scale it based on size
        font.draw(batch, text, x, y);
        batch.end();
    }
    public void drawProgressBar(float width, float height, Color color){
        sr.setColor(color);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.rect(center.x-width/2, viewport.getWorldHeight()-50, width, height);
        sr.circle(center.x-width/2, viewport.getWorldHeight()-50+height/2, height/2);
        sr.circle(center.x+width/2, viewport.getWorldHeight()-50+height/2, height/2);
        if(progressIndicator.getX() > center.x+width/2)
            progressIndicator.setX(center.x+width/2);
        if(progressIndicator.getX() < center.x-width/2)
            progressIndicator.setX(center.x-width/2);
        sr.circle(progressIndicator.getX(), progressIndicator.getY(), progressIndicator.getR());
        sr.end();
        sr.setColor(Color.WHITE);
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.circle(progressIndicator.getX(), progressIndicator.getY(), progressIndicator.getR());
        sr.end();
    }
    public void drawCircle(float x, float y, float r, Color color){
        sr.setColor(color);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.circle(x, y, r);
        sr.end();
    }

    public void drawInfo() {
        double levelLength = Math.floor(getLevelLength() * 10) / 10;
        String[] time = (levelLength + "").split("\\.");
        int seconds = Integer.parseInt(time[0]);
        int tenths = Integer.parseInt(time[1]);
        String levelLengthString;
        if (seconds < 10)
            levelLengthString = "0" + seconds;
        else
            levelLengthString = "" + seconds;
        levelLengthString += "." + tenths;

        drawScreenBox(true, 280, 50, 40);
        if (seconds < 100) {
            drawScreenBox(true, 200, 100, 80);
            drawText(levelLengthString, 25, viewport.getWorldWidth() - 195, viewport.getWorldHeight() - 55);
        } else {
            drawScreenBox(true, 230, 100, 80);
            drawText(levelLengthString, 25, viewport.getWorldWidth() - 225, viewport.getWorldHeight() - 55);
        }
        drawText("Length:", 28, viewport.getWorldWidth() - 265, viewport.getWorldHeight() - 8);
        drawText("sec", 15, viewport.getWorldWidth() - 65, viewport.getWorldHeight() - 65);

        //Draw icons
        for (Icon icon : icons)
            drawCircle(icon.getX(), icon.getY(), icon.getR(), icon.getColor());
        //drawCircle(icons[3].getX(), icons[3].getY(), icons[3].getR(), icons[3].getColor());
        batch.begin();
        batch.draw(icons[0].getTexture(), icons[0].getX() - 25, icons[0].getY() - 25, 45, 45);
        batch.draw(icons[1].getTexture(), icons[1].getX() - 20, icons[1].getY() - 20, 45, 45);
        batch.draw(icons[2].getTexture(), icons[2].getX() - 22, icons[2].getY() - 22, 45, 45);
        batch.draw(icons[3].getTexture(), icons[3].getX() - 22, icons[3].getY() - 22, 45, 45);
        //batch.draw(icons[3].getTexture(), icons[3].getX()-20, icons[3].getY()-20, 45, 45);
        batch.end();
    }
    private void changeColor(){
        newColors = new Color[currColorSet.length];
        for(int i=0; i<colors.length; i++){
            newColors[i] = new Color(currColorSet[i]);
        }
        for(int i=0; i<colorActions.length; i++){
            colorActions[i].restart();
            colorActions[i].setEndColor(newColors[i]);
        }
    }

    public float getLevelLength(){
        if(trapezi.isEmpty())
            return 0;
        System.out.println(tlast.getStartDistance()/scrollSpeed);
        return tlast.getStartDistance()/scrollSpeed;
    }
    public int getMousePosition(){
        int position;
        Vector mouseVector = new Vector(center, new Point(mouse.x, mouse.y));
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
            Level level = new Level(levelName, numberOfSides, getLevelLength(), trapezi.toArray(), ColorSets.toString(currColorSet), scrollSpeed, songName);
            FileOutputStream fos = new FileOutputStream("core/assets/levels/" + levelName + ".lvl");
            ObjectOutputStream ous = new ObjectOutputStream(fos);
            /*for(Trapez t:trapezi) {
                ous.writeObject(t);
            }*/
            ous.writeObject(level);
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
            tlast = trapezi.get(trapezi.size);
            ois.close();

        }catch(EOFException ignore) { //ta exception nam samo pove, da je konec datoteke
        }catch (Exception ex) {
            ex.printStackTrace();
        }
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
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }
    public float getDistanceFromTrapezSideToCenter(Trapez t){
        Vector v1 = new Vector(new Point(t.getXPoints()[0], t.getYPoints()[0]), center);
        Vector v2 = new Vector(new Point(t.getXPoints()[1], t.getYPoints()[1]), center);
        Vector mouseVector = new Vector(center, new Point(mouse.x, mouse.y));
        float beta = (float)(Math.PI - v1.getAngle(v2))/2;
        float alpha = (float)(Math.PI-mouseVector.getAngle(v1));
        float gamma = (float)(Math.PI - beta - alpha);
        return (float)(mouseVector.getLength()/Math.sin(beta) * Math.sin(gamma));
    }
    private void setNumberOfSides(int numberOfSides){
        this.numberOfSides = numberOfSides;
        for(int i=0; i<middleHexagon.length; i++) {
            middleHexagon[i] = new Polygon(new float[numberOfSides], new float[numberOfSides], 70);
            middleHexagon[i].setCenter(center.x, center.y);
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
    public boolean scrolled(int amount){
        if(amount == 1)
            progressIndicator.setX(progressIndicator.getX()+timestampSpeed*dt*progressBarWidth*15);
        if(amount == -1)
            progressIndicator.setX(progressIndicator.getX()-timestampSpeed*dt*progressBarWidth*15);
        return false;
    }
    @Override
    public boolean mouseMoved(int screenX, int screenY){
        return false;
    }
    @Override
    public void dispose() {
        pixmap.dispose();
        polyBatch.dispose();
        textureSolid.dispose();
        //game.dispose();
        batch.dispose();
        font.dispose();
        sr.dispose();
        //music.stop();
        music.dispose();
    }
}
