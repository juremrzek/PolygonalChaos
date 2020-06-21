package com.mygdx.game.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.scenes.scene2d.actions.ColorAction;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.mygdx.game.MyGdxGame;
import com.mygdx.game.Objects.*;

import java.io.*;

public class LevelSelectScreen implements Screen {
    private MyGdxGame game;
    private Point center;
    private Polygon[] middleHexagon;

    private Pixmap pixmap;
    private PolygonSprite poly;
    private PolygonSpriteBatch polyBatch;
    private Texture textureSolid;
    private EarClippingTriangulator ect;

    private SpriteBatch batch;
    private BitmapFont font;
    private OrthographicCamera camera;
    private FitViewport viewport;
    private ShapeRenderer sr;

    private int numberOfSides;
    private float angle;
    private Color[] colors;
    private Color[] newColors;
    private String[] currColorSet;
    private ColorAction[] colorActions;

    private Level[] levels;
    private Level displayedLevel;
    private int displayedLevelIndex;
    private GlyphLayout layout;
    private Sound moveSound;

    public LevelSelectScreen(MyGdxGame game, float angle, SpriteBatch batch, BitmapFont font, ShapeRenderer sr){
        this.game = game;
        this.angle = angle;
        this.batch = batch;
        this.font = font;
        this.sr = sr;
    }
    @Override
    public void show() {
        //getLevels();
        importLevels();
        displayedLevelIndex = 0;
        if(levels.length != 0) {
            displayedLevel = levels[displayedLevelIndex];
            numberOfSides = displayedLevel.getNumberOfSides();
        }
        else{
            numberOfSides = 12;
        }


        camera = new OrthographicCamera();
        viewport = new FitViewport(1280, 900, camera);
        center = new Point(viewport.getWorldWidth()/2, viewport.getWorldHeight()/2);
        sr = new ShapeRenderer();
        moveSound = Gdx.audio.newSound(Gdx.files.internal("sounds/menumove.wav"));

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

        currColorSet = displayedLevel.getColorSet();
        colors = new Color[currColorSet.length];
        for(int i=0; i<colors.length; i++){
            colors[i] = new Color(ColorSets.getColorFromHex(currColorSet[i]));
        }
        newColors = new Color[currColorSet.length];
        for(int i=0; i<newColors.length; i++){
            newColors[i] = new Color(ColorSets.getColorFromHex(currColorSet[i]));
        }
        colorActions = new ColorAction[currColorSet.length];
        for(int i=0; i<colorActions.length; i++){
            colorActions[i] = new ColorAction();
            colorActions[i].setColor(colors[i]);
            colorActions[i].setDuration(0.2f);
            colorActions[i].setEndColor(newColors[i]);
        }
        middleHexagon = new Polygon[2];
        for(int i=0; i<middleHexagon.length; i++) {
            middleHexagon[i] = new Polygon(new float[numberOfSides], new float[numberOfSides], 70);
            middleHexagon[i].setCenter(center.x, center.y);
        }
        layout = new GlyphLayout();
    }

    @Override
    public void render(float delta) {
        for (ColorAction ca : colorActions) {
            ca.act(delta);
        }
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        drawBackground();
        sr.setColor(colorActions[1].getColor());
        sr.setProjectionMatrix(camera.combined);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.rect(0, viewport.getWorldHeight()-720, viewport.getWorldWidth(), viewport.getWorldHeight()-310);
        sr.setColor(colorActions[0].getColor());
        sr.triangle(120, center.y-410, 120, center.y-290, 60, center.y-350);
        sr.triangle(viewport.getWorldWidth()-120, center.y-410, viewport.getWorldWidth()-120, center.y-290, viewport.getWorldWidth()-60, center.y-350);
        sr.end();
        if(levels.length != 0) {
            drawEquilateralPolygon(middleHexagon[1], numberOfSides, middleHexagon[0].getR() + 10, center.x, center.y - 120, colorActions[0].getColor(), angle);
            drawEquilateralPolygon(middleHexagon[0], numberOfSides, middleHexagon[0].getR(), center.x, center.y - 120, colorActions[1].getColor(), angle);
            drawCenteredText("play", 52, viewport.getWorldHeight() - 30, colorActions[0].getColor());
            drawCenteredText(displayedLevel.getName(), 64, viewport.getWorldHeight() - 180, colorActions[0].getColor());
            if(displayedLevel.getProgress() >= 100)
                drawCenteredText("progress: completed", 32, viewport.getWorldHeight() - 305, colorActions[0].getColor());
            else
                drawCenteredText("progress: " + (int)displayedLevel.getProgress()+"%", 32, viewport.getWorldHeight() - 305, colorActions[0].getColor());
            drawCenteredText("song: " + displayedLevel.getSongName(), 32, viewport.getWorldHeight() - 365, colorActions[0].getColor());

            double levelLength = Math.floor(displayedLevel.getDuration() * 10) / 10;
            String[] time = (levelLength + "").split("\\.");
            int seconds = Integer.parseInt(time[0]);
            int tenths = Integer.parseInt(time[1]);
            String levelLengthString;
            if (seconds < 10)
                levelLengthString = "0" + seconds;
            else
                levelLengthString = "" + seconds;
            levelLengthString += "." + tenths;

            drawCenteredText("length: "+ levelLengthString+" seconds", 32, viewport.getWorldHeight() - 425, colorActions[0].getColor());
        }
        else
            drawCenteredText("No levels yet", 64, viewport.getWorldHeight()/2+200, colorActions[0].getColor());
        update(delta);
    }

    public void update(float dt){
        if(levels.length != 0) {
            displayedLevel = levels[displayedLevelIndex];
            numberOfSides = displayedLevel.getNumberOfSides();
            currColorSet = displayedLevel.getColorSet();
            changeColor();
        }
        for(int i=0; i<middleHexagon.length; i++) {
            middleHexagon[i] = new Polygon(new float[numberOfSides], new float[numberOfSides], 70);
            middleHexagon[i].setCenter(center.x, center.y);
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.Q)){
            dispose();
            game.setScreen(new MenuScreen(game, batch, font, sr));
        }
        if((Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) && levels.length != 0){
            dispose();
            game.setScreen(new PlayScreen(game, displayedLevel, batch, font, sr));
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.A) || Gdx.input.isKeyJustPressed(Input.Keys.LEFT) && levels.length != 0) {
            displayedLevelIndex--;
            if(displayedLevelIndex<0)
                displayedLevelIndex = levels.length-1;
            moveSound.play(0.5f);
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.D) || Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) && levels.length != 0) {
            displayedLevelIndex++;
            if(displayedLevelIndex>levels.length-1)
                displayedLevelIndex = 0;
            moveSound.play(0.5f);
        }
        angle+=0.1f;
        angle%=360;
    }
    private void changeColor(){
        newColors = new Color[currColorSet.length];
        for(int i=0; i<colors.length; i++){
            newColors[i] = new Color(ColorSets.getColorFromHex(currColorSet[i]));
        }
        for(int i=0; i<colorActions.length; i++){
            colorActions[i].restart();
            colorActions[i].setEndColor(newColors[i]);
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
    private void initTrapez(Polygon p, double distance, double size, double startAngle){
        p.xPoints = new float[4];
        p.xPoints[0] = (float)(center.x + distance*Math.cos(Math.toRadians(startAngle)));
        p.xPoints[1] = (float)(center.x + distance*Math.cos(Math.toRadians(360f/numberOfSides)+Math.toRadians(startAngle)));
        p.xPoints[3] = (float)(center.x + (distance+size)*Math.cos(Math.toRadians(startAngle)));
        p.xPoints[2] = (float)(center.x + (distance+size)*Math.cos(Math.toRadians(360f/numberOfSides)+Math.toRadians(startAngle)));

        p.yPoints = new float[4];
        p.yPoints[0] = (float)(-100 + distance*Math.sin(Math.toRadians(startAngle)));
        p.yPoints[1] = (float)(-100 + distance*Math.sin(Math.toRadians(360f/numberOfSides)+Math.toRadians(startAngle)));
        p.yPoints[3] = (float)(-100 + (distance+size)*Math.sin(Math.toRadians(startAngle)));
        p.yPoints[2] = (float)(-100 + (distance+size)*Math.sin(Math.toRadians(360f/numberOfSides)+Math.toRadians(startAngle)));
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
    private void importLevels(){
        FileHandle fh = Gdx.files.local("levels");
        FileHandle[] files = fh.list();
        Level[] templevels = new Level[files.length];
        levels = new Level[files.length];
        if(templevels.length == 0){
            return;
        }
        for(int i=0; i<files.length; i++){
            try {
                FileInputStream fin = new FileInputStream(files[i].toString());
                ObjectInputStream ois = new ObjectInputStream(fin);
                Level level = (Level) ois.readObject();
                ois.close();
                templevels[i] = level;
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        for (Level templevel : templevels) {
            switch (templevel.getName()) {
                case "First level":
                    levels[0] = templevel;
                    break;
                case "Another level":
                    levels[1] = templevel;
                    break;
                case "Level3":
                    levels[2] = templevel;
                    break;
                case "Final level":
                    levels[3] = templevel;
                    break;
            }
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
    public void drawCenteredText(String s, float size, float y, Color color){
        batch.begin();
        batch.setProjectionMatrix(camera.combined);
        font.setColor(color);
        font.getData().setScale(size/64);
        layout.setText(font, s);
        font.draw(batch, s, center.x-layout.width/2, y);
        batch.end();
    }

    @Override
    public void dispose() {
        pixmap.dispose();
        polyBatch.dispose();
        textureSolid.dispose();
        moveSound.dispose();
    }
}
