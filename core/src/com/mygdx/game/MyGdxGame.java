package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.mygdx.game.Screens.*;

public class MyGdxGame extends Game {
	public boolean fullscreen;
	public Graphics.Monitor primaryMonitor;
	@Override
	public void create () {
		fullscreen = false;
		primaryMonitor = Gdx.graphics.getPrimaryMonitor();
		if(fullscreen)
			Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode(primaryMonitor));
		setScreen(new MenuScreen(this));
	}

	@Override
	public void render () {
		super.render();
	}
	
	@Override
	public void dispose (){
		System.exit(0);
	}

}
