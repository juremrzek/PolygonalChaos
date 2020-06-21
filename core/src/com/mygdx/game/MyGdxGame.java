package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.mygdx.game.Screens.*;

public class MyGdxGame extends Game {
	private SpriteBatch batch;
	private BitmapFont font;
	private ShapeRenderer sr;
	public boolean fullscreen;
	public Graphics.Monitor primaryMonitor;
	@Override
	public void create () {
		batch = new SpriteBatch();
		font = new BitmapFont(Gdx.files.internal("fonts/font.fnt"));
		font.setColor(1.0f, 1.0f, 1.0f, 1.0f);
		sr = new ShapeRenderer();
		sr.setColor(Color.BLACK);
		fullscreen = true;
		primaryMonitor = Gdx.graphics.getPrimaryMonitor();
		if(fullscreen)
			Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode(primaryMonitor));
		setScreen(new MenuScreen(this, batch, font, sr));
	}

	@Override
	public void render () {
		super.render();
	}
	
	@Override
	public void dispose (){
		batch.dispose();
		font.dispose();
		sr.dispose();
		System.exit(0);
	}

}
