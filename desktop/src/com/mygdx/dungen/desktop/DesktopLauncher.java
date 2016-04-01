package com.mygdx.dungen.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.mygdx.dungen.DunGen;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.vSyncEnabled = false;
		config.fullscreen = false;

		config.height = 540;
		config.width = 960;

		config.title = DunGen.TITLE + " v" + DunGen.VERSION;

		//System.setProperty("org.lwjgl.opengl.Window.undecorated", "true");

		new LwjglApplication(new DunGen(), config);
	}
}
