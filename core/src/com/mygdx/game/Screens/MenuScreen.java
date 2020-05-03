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
    private byte numberOfSides;
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

    //To draw polygons(for bg)
    private Pixmap pixmap;
    private PolygonSprite poly;
    private PolygonSpriteBatch polyBatch;
    private Texture textureSolid;
    private EarClippingTriangulator ect;
    private PolygonRegion polyReg;

    private SpriteBatch batch;
    private BitmapFont font;
    private OrthographicCamera camera;
    private FitViewport viewport;

    public MenuScreen(MyGdxGame game){
        this.game = game;
        numberOfSides = 6;
        angle = 0;
        startTime = System.nanoTime();
        optionFlag = 0;
        options = new String[4];
        options[0] = "play";
        options[1] = "create";
        options[2] = "options";
        options[3] = "credits";

        camera = new OrthographicCamera();
        viewport = new FitViewport(1280, 900, camera);
        batch = new SpriteBatch();
        font = new BitmapFont(Gdx.files.internal("fonts/font.fnt"));
        font.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        center = new Point(viewport.getWorldWidth()/2, viewport.getWorldHeight()/2);

        numberOfColorSets = 7;
        colorSets = new Color[numberOfColorSets][];
        colorSets[0] = ColorSets.ORANGE;
        colorSets[1] = ColorSets.YELLOW_BLACK;
        colorSets[2] = ColorSets.PURPLE;
        colorSets[3] = ColorSets.GREEN;
        colorSets[4] = ColorSets.WHITE_GRAY;
        colorSets[5] = ColorSets.CYAN;
        colorSets[6] = ColorSets.PINK;

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
        colorActions = new ColorAction[6];
        for(int i=0; i<colorActions.length; i++){
            colorActions[i] = new ColorAction();
            colorActions[i].setColor(colors[i]);
            colorActions[i].setDuration(1);
            colorActions[i].setEndColor(newColors[i]);
        }
    }

    @Override
    public void show() {

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
            dispose();
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
                case 0: game.setScreen(new PlayScreen(game));
                break;
                case 1: game.setScreen(new EditorScreen(game));
                break;
            }
        }
        if(getPassedTime() > 2000000000f){ //one seconds - 1000000000 nanoseconds (we change colors every 5s)
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
                bgTriangle.xPoints[0] = viewport.getWorldWidth() / 2f;
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
    private void initTrapez(Polygon p, double distance, double size, double startAngle){
        p.xPoints = new float[4];
        float x = center.x;
        p.xPoints[0] = (float)(x + distance*Math.cos(Math.toRadians(startAngle)));
        p.xPoints[1] = (float)(x + distance*Math.cos(Math.toRadians(360f/numberOfSides)+Math.toRadians(startAngle)));
        p.xPoints[3] = (float)(x + (distance+size)*Math.cos(Math.toRadians(startAngle)));
        p.xPoints[2] = (float)(x + (distance+size)*Math.cos(Math.toRadians(360f/numberOfSides)+Math.toRadians(startAngle)));

        p.yPoints = new float[4];
        float y = -100;
        p.yPoints[0] = (float)(y + distance*Math.sin(Math.toRadians(startAngle)));
        p.yPoints[1] = (float)(y + distance*Math.sin(Math.toRadians(360f/numberOfSides)+Math.toRadians(startAngle)));
        p.yPoints[3] = (float)(y + (distance+size)*Math.sin(Math.toRadians(startAngle)));
        p.yPoints[2] = (float)(y + (distance+size)*Math.sin(Math.toRadians(360f/numberOfSides)+Math.toRadians(startAngle)));
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
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        font.setColor(color);
        font.getData().setScale(size/64); //The non-scaled font is size of 64px, we scale it based on size
        font.draw(batch, text, x, y);
        batch.end();
    }

    private void drawMenuContent(){
        drawText("POLYGONAL", 105, 110, viewport.getWorldHeight()-120, colors[0]);
        drawText("CHAOS", 64, center.x-185, viewport.getWorldHeight()-240, colors[0]);
        Trapez menuTrapez = new Trapez(150, 400, numberOfSides/4);
        initTrapez(menuTrapez, menuTrapez.getDistance(), menuTrapez.getSize(), 60);
        drawPolygon(menuTrapez.getPoints(), colors[0]);
        drawText(options[optionFlag], 48, center.x-options[optionFlag].length()*54/2f, viewport.getWorldHeight()-560, colors[3]);
        drawText("press space to start", 38, 220, 100, colors[0]);
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
        game.dispose();
        batch.dispose();
        font.dispose();
        //sr.dispose();
        //music.stop();
        //music.dispose();
    }
}
