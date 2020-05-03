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
    private float rotateSpeed;
    private Color[] colors;
    private Color[] newColors;
    private Color[] currColorSet;
    private int colorSetIndex;
    private int numberOfColorSets;
    private Color[][] colorSets;
    private ColorAction[] colorActions;
    private Point center;
    private long startTime;

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
        numberOfSides = 12;
        angle = 0;
        startTime = System.nanoTime();

        camera = new OrthographicCamera();
        viewport = new FitViewport(1280, 900, camera);
        batch = new SpriteBatch();
        font = new BitmapFont(Gdx.files.internal("fonts/font.fnt"));
        font.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        center = new Point(viewport.getWorldWidth()/2, viewport.getWorldHeight()/2);

        numberOfColorSets = 4;
        colorSets = new Color[numberOfColorSets][];
        colorSets[0] = ColorSets.yellowBlack;
        colorSets[1] = ColorSets.whiteGray;
        colorSets[2] = ColorSets.red;
        colorSets[3] = ColorSets.orange;

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
            colorActions[i].setDuration(5);
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
        for(int i=0; i<colorActions.length; i++){
            colorActions[i].act(delta);
        }
        drawBackground();
        drawText("POLYGONAL", 105, 110, viewport.getWorldHeight()-100, colors[0]);
        drawText("chaos", 64, center.x-185, viewport.getWorldHeight()-240, colors[0]);
        update(delta);
    }
    public void update(float dt){
        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)){
            dispose();
        }
        angle+=0.1f;
        angle %= 360;

        if(Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            game.setScreen(new EditorScreen(game));
        }
        if(getPassedTime() > 6000000000f){ //one seconds - 1000000000 nanoseconds (we change colors every 6s)
            startTime = System.nanoTime();
            changeColor();
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
        long passedTime = System.nanoTime() - startTime;
        return passedTime;
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

    private void drawText(String text, float size, float x, float y, Color color){
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        font.setColor(color);
        font.getData().setScale(size/64); //The non-scaled font is size of 64px, we scale it based on size
        font.draw(batch, text, x, y);
        batch.end();
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
