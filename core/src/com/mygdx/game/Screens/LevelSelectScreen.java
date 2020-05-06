package com.mygdx.game.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.scenes.scene2d.actions.ColorAction;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.mygdx.game.MyGdxGame;
import com.mygdx.game.Objects.*;

import java.io.FileInputStream;
import java.io.ObjectInputStream;

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
    private Color[] currColorSet;
    private ColorAction[] colorActions;
    private float textWidth;

    private Level[] levels;
    private Level displayedLevel;
    private int displayedLevelIndex;

    public LevelSelectScreen(MyGdxGame game, float angle){
        this.game = game;
        this.angle = angle;
    }
    @Override
    public void show() {
        //getLevels();
        importLevels();
        //levels[0] = new Level("Superhexogner", 5, 0.45f, "Some Song lmao ded dd");
        //levels[1] = new Level("oklmao 2", 4, 0.8f, "Galaxy collapse");
        //levels[2] = new Level("Some other name", 8, 0.113135f, "For the love of god");
        displayedLevelIndex = 0;
        displayedLevel = levels[displayedLevelIndex];
        numberOfSides = displayedLevel.getNumberOfSides();


        camera = new OrthographicCamera();
        viewport = new FitViewport(1280, 900, camera);
        batch = new SpriteBatch();
        font = new BitmapFont(Gdx.files.internal("fonts/font.fnt"));
        font.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        center = new Point(viewport.getWorldWidth()/2, viewport.getWorldHeight()/2);
        sr = new ShapeRenderer();

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

        currColorSet = ColorSets.YELLOW_BLACK;
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
        middleHexagon = new Polygon[2];
        for(int i=0; i<middleHexagon.length; i++) {
            middleHexagon[i] = new Polygon(new float[numberOfSides], new float[numberOfSides], 70);
            middleHexagon[i].setCenter(center.x, center.y);
        }

        GlyphLayout layout = new GlyphLayout(font, "a ");
        textWidth = layout.width/2;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        drawBackground();
        sr.setColor(Color.BLACK);
        sr.setProjectionMatrix(camera.combined);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.rect(0, viewport.getWorldHeight()-720, viewport.getWorldWidth(), viewport.getWorldHeight()-310);
        sr.setColor(colors[0]);
        sr.triangle(120, center.y-410, 120, center.y-290, 60, center.y-350);
        sr.triangle(viewport.getWorldWidth()-120, center.y-410, viewport.getWorldWidth()-120, center.y-290, viewport.getWorldWidth()-60, center.y-350);
        sr.end();
        drawEquilateralPolygon(middleHexagon[1], numberOfSides, middleHexagon[0].getR()+10, center.x, center.y-120, colorActions[0].getColor(), angle);
        drawEquilateralPolygon(middleHexagon[0], numberOfSides, middleHexagon[0].getR(), center.x, center.y-120, colorActions[1].getColor(), angle);
        String s = "Levels";
        drawText(s, 52, center.x-textWidth*s.length()*(52f/64)/2, viewport.getWorldHeight()-30, colors[0]);
        s = displayedLevel.getName();
        drawText(s, 64, center.x-textWidth*s.length()/2, viewport.getWorldHeight()-180, colors[0]);
        s = "progress: "+displayedLevel.getProgress();
        drawText(s, 32, center.x-textWidth*s.length()/4, viewport.getWorldHeight()-320, colors[0]);
        s = "song: "+displayedLevel.getSongName();
        drawText(s, 32, center.x-textWidth*s.length()/4, viewport.getWorldHeight()-380, colors[0]);
        update(delta);
    }

    public void update(float dt){
        displayedLevel = levels[displayedLevelIndex];
        numberOfSides = displayedLevel.getNumberOfSides();
        for(int i=0; i<middleHexagon.length; i++) {
            middleHexagon[i] = new Polygon(new float[numberOfSides], new float[numberOfSides], 70);
            middleHexagon[i].setCenter(center.x, center.y);
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)){
            game.setScreen(new MenuScreen(game));
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)){
            game.setScreen(new PlayScreen(game, displayedLevel));
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.A) || Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            displayedLevelIndex--;
            if(displayedLevelIndex<0)
                displayedLevelIndex = levels.length-1;
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.D) || Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            displayedLevelIndex++;
            if(displayedLevelIndex>levels.length-1)
                displayedLevelIndex = 0;
        }
        angle+=0.1f;
        angle%=360;
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
                bgTriangle.yPoints[0] = -100;
                bgTriangle.xPoints[j] = bgTriangle.xPoints[0] + (float) Math.cos(Math.toRadians(angle + (360f / numberOfSides) * (i + j))) * 9999; //edges of background triangles need to be very far away from the center to cover the whole screen
                bgTriangle.yPoints[j] = bgTriangle.yPoints[0] + (float)(Math.sin(Math.toRadians(angle + (360f / numberOfSides) * (i + j))) * 9999);
            }
            Color tempColor;
            if (i % 2 == 0)
                tempColor = colorActions[2].getColor();
            else
                tempColor = colorActions[3].getColor();
            drawPolygon(bgTriangle.getPoints(), tempColor);
        }
    }
    private void importLevels(){
        FileHandle fh = Gdx.files.internal("core/assets/levels");
        FileHandle[] fileNames = fh.list();
        levels = new Level[fileNames.length];
        for(int i=0; i<fileNames.length; i++){
            try {
                FileInputStream fin = new FileInputStream(fileNames[i].toString());
                ObjectInputStream ois = new ObjectInputStream(fin);
                Level level = (Level) ois.readObject();
                ois.close();
                levels[i] = level;
            }catch(Exception e){
                e.printStackTrace();
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
    public void drawText(String text, float size, float x, float y, Color color) {
        batch.begin();
        batch.setProjectionMatrix(camera.combined);
        font.setColor(color);
        font.getData().setScale(size/64); //The non-scaled font is size of 64px, we scale it based on size
        font.draw(batch, text, x, y);
        batch.end();
    }

    @Override
    public void dispose() {
        game.dispose();
        batch.dispose();
        font.dispose();
        sr.dispose();
    }
}
