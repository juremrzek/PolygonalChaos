package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.mygdx.game.Screens.PlayScreen;

public class MyGdxGame extends Game {
	public boolean fullscreen;
	@Override
	public void create () {
		fullscreen = false;
		setScreen(new PlayScreen(this));
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
