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
import com.mygdx.game.Objects.Point;
import com.mygdx.game.Objects.Polygon;
import com.mygdx.game.Objects.Trapez;

public class CreditsScreen implements Screen {

    private MyGdxGame game;
    private Point center;
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
    private GlyphLayout layout;

    public CreditsScreen(MyGdxGame game, float angle, Color[] colors, SpriteBatch batch, BitmapFont font, ShapeRenderer sr){
        this.game = game;
        this.angle = angle;
        this.batch = batch;
        this.font = font;
        this.sr = sr;
        this.colors = colors;
    }

    @Override
    public void show() {
        numberOfSides = 6;
        camera = new OrthographicCamera();
        viewport = new FitViewport(1280, 900, camera);
        center = new Point(viewport.getWorldWidth()/2, viewport.getWorldHeight()/2);
        sr = new ShapeRenderer();

        Trapez tempTrapez = new Trapez(100, 100, 1);
        initTrapez(tempTrapez, tempTrapez.getStartDistance(), tempTrapez.getSize(), angle);
        pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        ect = new EarClippingTriangulator();
        polyBatch = new PolygonSpriteBatch();
        pixmap.fill();
        textureSolid = new Texture(pixmap);
        PolygonRegion polyReg = new PolygonRegion(new TextureRegion(textureSolid), tempTrapez.getPoints(), ect.computeTriangles(tempTrapez.getPoints()).toArray());
        poly = new PolygonSprite(polyReg);
        layout = new GlyphLayout();
        for(Color c:colors){
            System.out.println(c.toString());
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        drawBackground();
        update(delta);
    }
    public void update(float dt){
        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.Q)){
            dispose();
            game.setScreen(new MenuScreen(game, batch, font, sr));
        }
        drawCenteredText("credits", 52, viewport.getWorldHeight() - 30, colors[0]);
        drawCenteredText("made by: jure mrzek", 38, viewport.getWorldHeight() - 160, colors[0]);
        drawCenteredText("mentor: tomaz mavri", 38, viewport.getWorldHeight() - 280, colors[0]);
        drawCenteredText("idea from super hexagon", 38, viewport.getWorldHeight() - 400, colors[0]);
        drawCenteredText("thanks for playing", 38, viewport.getWorldHeight() - 640, colors[0]);
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
                bgTriangle.xPoints[0] = center.x;
                bgTriangle.yPoints[0] = -100;
                bgTriangle.xPoints[j] = bgTriangle.xPoints[0] + (float) Math.cos(Math.toRadians(angle + (360f / numberOfSides) * (i + j))) * 9999; //edges of background triangles need to be very far away from the center to cover the whole screen
                bgTriangle.yPoints[j] = bgTriangle.yPoints[0] + (float)(Math.sin(Math.toRadians(angle + (360f / numberOfSides) * (i + j))) * 9999);
            }
            Color tempColor;
            if(numberOfSides%2 == 0) {
                if (i % 2 == 0)
                    tempColor = colors[2];
                else
                    tempColor = colors[3];
            }
            else{
                if (i % 5 == 0 && i % 3 == 0)
                    tempColor = colors[4];
                else if(i % 2 == 0)
                    tempColor = colors[3];
                else
                    tempColor = colors[2];
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
    }
}
