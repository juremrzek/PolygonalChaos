package com.mygdx.game.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.scenes.scene2d.actions.ColorAction;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.mygdx.game.MyGdxGame;
import com.mygdx.game.Objects.ColorSets;
import com.mygdx.game.Objects.Point;
import com.mygdx.game.Objects.Polygon;

public class LevelSelectScreen implements Screen {
    private MyGdxGame game;
    private Point center;
    private PolygonSprite poly;
    private EarClippingTriangulator ect;
    private PolygonRegion polyReg;
    private Polygon[] middleHexagon;

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

    public LevelSelectScreen(MyGdxGame game, float angle){
        this.game = game;
        this.angle = angle;
    }

    @Override
    public void show() {
        numberOfSides = 4;
        camera = new OrthographicCamera();
        viewport = new FitViewport(1280, 900, camera);
        batch = new SpriteBatch();
        font = new BitmapFont(Gdx.files.internal("fonts/font.fnt"));
        font.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        center = new Point(viewport.getWorldWidth()/2, viewport.getWorldHeight()/2);
        sr = new ShapeRenderer();
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
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.rect(0, viewport.getWorldHeight()-720, viewport.getWorldWidth(), viewport.getWorldHeight()-310);
        sr.setColor(colors[0]);
        sr.triangle(120, center.y-410, 120, center.y-290, 60, center.y-350);
        sr.triangle(viewport.getWorldWidth()-120, center.y-410, viewport.getWorldWidth()-120, center.y-290, viewport.getWorldWidth()-60, center.y-350);
        sr.end();
        drawEquilateralPolygon(middleHexagon[1], numberOfSides, middleHexagon[0].getR()+10, center.x, center.y-120, colorActions[0].getColor(), angle);
        drawEquilateralPolygon(middleHexagon[0], numberOfSides, middleHexagon[0].getR(), center.x, center.y-120, colorActions[1].getColor(), angle);
        drawText("LEVELS", 52, center.x-textWidth*"Levels".length()*(52f/64)/2, viewport.getWorldHeight()-30, colors[0]);
        String s = "supernova";
        drawText(s, 64, center.x-textWidth*s.length()/2, viewport.getWorldHeight()-180, colors[0]);
        s = "progress: 80%";
        drawText(s, 32, center.x-textWidth*s.length()/4, viewport.getWorldHeight()-320, colors[0]);
        s = "song: supernova";
        drawText(s, 32, center.x-textWidth*s.length()/4, viewport.getWorldHeight()-380, colors[0]);
        update(delta);
    }

    public void update(float dt){
        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)){
            game.setScreen(new MenuScreen(game));
        }
        angle+=0.1f;
        angle%=360;
    }

    @Override
    public void resize(int width, int height) {

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
    private void drawPolygon(float [] points, Color color){
        //To draw polygons(for bg)
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture textureSolid = new Texture(pixmap);
        EarClippingTriangulator ect = new EarClippingTriangulator();
        PolygonRegion polyReg = new PolygonRegion(new TextureRegion(textureSolid), points, ect.computeTriangles(points).toArray());
        PolygonSprite poly = new PolygonSprite(polyReg);
        poly.setOrigin(25, 25);
        PolygonSpriteBatch polyBatch = new PolygonSpriteBatch();
        polyBatch.begin();
        polyBatch.draw(polyReg, 2, 2);
        polyBatch.end();
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
