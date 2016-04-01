package com.mygdx.dungen.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.mygdx.dungen.GameMap;
import com.mygdx.dungen.MapGenDebugRenderer;

/**
 * @author leonard
 *         Date: 13/2/2016
 */
public class MapGenDebugScreen implements Screen {

    private MapGenDebugRenderer mapGenDebugRenderer;

    public MapGenDebugScreen(GameMap map) {
        mapGenDebugRenderer = new MapGenDebugRenderer(map);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        mapGenDebugRenderer.render(delta);
    }

    @Override
    public void resize(int width, int height) {
        mapGenDebugRenderer.resize(width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        mapGenDebugRenderer.dispose();
    }
}
