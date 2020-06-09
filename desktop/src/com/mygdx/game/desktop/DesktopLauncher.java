package com.mygdx.game.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mygdx.game.MyGdxGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 1280;
		config.height = 900;
		config.title = "Polygonal Chaos";
		config.fullscreen = true;
		config.resizable = true;
		config.addIcon("icons/icon-128.png", Files.FileType.Internal);
		config.addIcon("icons/icon-32.png", Files.FileType.Internal);
		config.addIcon("icons/icon-16.png", Files.FileType.Internal); //LibGDX sam poskrbi, kdaj uporabiti kakšno velikost
		new LwjglApplication(new MyGdxGame(), config);
	}
}
