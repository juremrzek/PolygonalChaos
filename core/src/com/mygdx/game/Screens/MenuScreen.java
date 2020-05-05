package com.mygdx.game.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.scenes.scene2d.actions.ColorAction;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.mygdx.game.MyGdxGame;
import com.mygdx.game.Objects.*;

public class MenuScreen implements Screen {
    private MyGdxGame game;
    private int numberOfSides;
    private float angle;
    private Color[] colors;
    private Color[] newColors;
    private Color[] currColorSet;
    private int colorSetIndex;
    private int numberOfColorSets;
    private Color[][] colorSets;
    private ColorAction[] colorActions;
    private Point center;
    private long startTime;
    private String[] options;
    private int optionFlag;
    private float textWidth;
    private Trapez menuTrapez;

    //To draw polygons(for bg)
    private Pixmap pixmap;
    private PolygonSpriteBatch polyBatch;
    private Texture textureSolid;
    private EarClippingTriangulator ect;
    private PolygonSprite poly;

    private SpriteBatch batch;
    private BitmapFont font;
    private OrthographicCamera camera;
    private FitViewport viewport;

    public MenuScreen(MyGdxGame game){
        this.game = game;
    }

    @Override
    public void show() {
        numberOfSides = 6;
        angle = 0;
        startTime = System.nanoTime();
        optionFlag = 0;
        options = new String[5];
        options[0] = "play";
        options[1] = "create";
        options[2] = "options";
        options[3] = "credits";
        options[4] = "exit";

        camera = new OrthographicCamera();
        viewport = new FitViewport(1280, 900, camera);
        batch = new SpriteBatch();
        font = new BitmapFont(Gdx.files.internal("fonts/font.fnt"));
        font.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        center = new Point(viewport.getWorldWidth()/2, viewport.getWorldHeight()/2);
        menuTrapez = new Trapez(150, 400, numberOfSides/4);
        initTrapez(menuTrapez, menuTrapez.getDistance(), menuTrapez.getSize(), 60);

        //objects to draw polygons
        pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        ect = new EarClippingTriangulator();
        polyBatch = new PolygonSpriteBatch();
        pixmap.fill();
        textureSolid = new Texture(pixmap);
        PolygonRegion polyReg = new PolygonRegion(new TextureRegion(textureSolid), menuTrapez.getPoints(), ect.computeTriangles(menuTrapez.getPoints()).toArray());
        poly = new PolygonSprite(polyReg);

        numberOfColorSets = 8;
        colorSets = new Color[numberOfColorSets][];
        colorSets[0] = ColorSets.BROWN;
        colorSets[1] = ColorSets.YELLOW_BLACK;
        colorSets[2] = ColorSets.GREEN;
        colorSets[3] = ColorSets.PURPLE;
        colorSets[4] = ColorSets.CYAN;
        colorSets[5] = ColorSets.WHITE_GRAY;
        colorSets[6] = ColorSets.YELLOW;
        colorSets[7] = ColorSets.PINK;

        colorSetIndex = 0;
        currColorSet = colorSets[colorSetIndex];
        colors = new Color[currColorSet.length];
        for(int i=0; i<colors.length; i++){
            colors[i] = new Color(currColorSet[i]);
        }
        colorSetIndex++;
        currColorSet = colorSets[colorSetIndex];
        newColors = new Color[currColorSet.length];
        for(int i=0; i<colors.length; i++){
            newColors[i] = new Color(currColorSet[i]);
        }
        colorSetIndex++;
        colorActions = new ColorAction[currColorSet.length];
        for(int i=0; i<colorActions.length; i++){
            colorActions[i] = new ColorAction();
            colorActions[i].setColor(colors[i]);
            colorActions[i].setDuration(1);
            colorActions[i].setEndColor(newColors[i]);
        }
        GlyphLayout layout = new GlyphLayout(font, "a a");
        textWidth = layout.width/3;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        for(ColorAction colorAction : colorActions) {
            colorAction.act(delta);
        }
        drawBackground();
        drawMenuContent();
        update(delta);
    }
    public void update(float dt){
        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)){
            game.dispose();
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.LEFT)){
            optionFlag--;
            if(optionFlag < 0)
                optionFlag = options.length-1;
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)){
            optionFlag++;
            optionFlag %= options.length;
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)){
            switch(optionFlag){
                case 0: game.setScreen(new LevelSelectScreen(game, angle));
                break;
                case 1: game.setScreen(new EditorScreen(game));
                break;
                case 4: game.dispose();
                break;
            }
        }
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
        if(getPassedTime() > 2000000000f){ //one seconds - 1000000000 nanoseconds (we change colors every 2s)
            startTime = System.nanoTime();
            changeColor();
        }
        angle+=0.1f;
        angle %= 360;
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
    private void drawBackground(){
        for(int i=0; i<numberOfSides; i++) {
            Polygon bgTriangle = new Polygon(new float[3], new float[3]);
            for (int j = 1; j < 3; j++) {
                bgTriangle.xPoints[0] = center.x;
                bgTriangle.yPoints[0] = -100;
                bgTriangle.xPoints[j] = bgTriangle.xPoints[0] + (float) Math.cos(Math.toRadians(angle + (360f / numberOfSides) * (i + j))) * 9999; //edges of background triangles need to be very far away from the center to cover the whole screen
                bgTriangle.yPoints[j] = bgTriangle.yPoints[0] + (float)(Math.sin(Math.toRadians(angle + (360f / numberOfSides) * (i + j))) * 9999);
            }
            Color tempColor;
            if (i % 2 == 0)
                tempColor = colors[2];
            else
                tempColor = colors[3];
            drawPolygon(bgTriangle.getPoints(), tempColor);
        }
    }

    public long getPassedTime() {
        return System.nanoTime() - startTime;
    }

    private void changeColor(){
        currColorSet = colorSets[colorSetIndex];
        newColors = new Color[currColorSet.length];
        for(int i=0; i<colors.length; i++){
            newColors[i] = new Color(currColorSet[i]);
        }
        for(int i=0; i<colorActions.length; i++){
            colorActions[i].restart();
            colorActions[i].setEndColor(newColors[i]);
        }
        if(colorSetIndex >= numberOfColorSets-1)
            colorSetIndex = 0;
        else
            colorSetIndex++;
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
    private void drawEquilateralPolygon(Polygon p, int n, int r, float x, float y, Color color, float startAngle){
        for(int i=0; i<n; i++) {
            double segment = Math.toRadians(startAngle + (360f / n) * i);
            p.xPoints[i] = (float) (x + Math.cos(segment) * r);
            p.yPoints[i] = (float) (y + Math.sin(segment) * r);
        }
        drawPolygon(p.getPoints(), color);
    }

    private void drawText(String text, float size, float x, float y, Color color){
        batch.begin();
        batch.setProjectionMatrix(camera.combined);
        font.setColor(color);
        font.getData().setScale(size/64);
        font.draw(batch, text, x, y);
        batch.end();
    }


    private void drawMenuContent(){
        drawText("POLYGONAL", 105, 100, viewport.getWorldHeight()-120, colors[0]);
        drawText("CHAOS", 64, center.x-185, viewport.getWorldHeight()-240, colors[0]);
        drawPolygon(menuTrapez.getPoints(), colors[0]);
        drawText(options[optionFlag], 46, center.x-textWidth*options[optionFlag].length()*(46f/64)/2f, viewport.getWorldHeight()-560, colors[3]);
        drawText("press space to select", 38, 175, 100, colors[0]);
        Polygon pointer = new Polygon(new float[3], new float[3], 30);
        drawEquilateralPolygon(pointer, 3, pointer.getR(), center.x-335, center.y-140, colors[0], 180);
        pointer = new Polygon(new float[3], new float[3], 30);
        drawEquilateralPolygon(pointer, 3, pointer.getR(), center.x+335, center.y-140, colors[0], 0);
    }

    @Override
    public void dispose() {
        pixmap.dispose();
        polyBatch.dispose();
        textureSolid.dispose();
        batch.dispose();
        font.dispose();
    }
}
