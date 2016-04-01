package com.mygdx.dungen;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.Pixmap;
import com.mygdx.dungen.mapgeneration.MapGenerator;


public class DunGen extends Game {

	public static final String TITLE 	= "Dungeon Generation";
	public static final String VERSION 	= "0.7.0";

    FPSLogger fpsLogger;

	@Override
	public void create () {
		Gdx.app.setLogLevel(Application.LOG_DEBUG);

		Gdx.graphics.setCursor(Gdx.graphics.newCursor(new Pixmap(Gdx.files.internal("ui/cursor.png")), 0, 0));
        fpsLogger = new FPSLogger();

		MapGenerator mapGenerator = new MapGenerator();
		new Thread(mapGenerator).start();
	}

	@Override
	public void render () {
        super.render();

        fpsLogger.log();
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	@Override
	public void pause() {
		super.pause();
	}

	@Override
	public void resume() {
		super.resume();
	}
}
