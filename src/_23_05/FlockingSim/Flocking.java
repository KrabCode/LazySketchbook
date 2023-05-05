package _23_05.FlockingSim;

import processing.core.PApplet;
import com.krab.lazy.*;
import processing.core.PGraphics;
import processing.core.PVector;

public class Flocking extends PApplet {
    LazyGui gui;
    PGraphics pg;
    CameraGrid2D world;
    PVector playerPos;
    PVector playerSpd;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
        fullScreen(P2D);
    }

    @Override
    public void setup() {
        Utils.setupSurface(this, surface);
        gui = new LazyGui(this);
        pg = createGraphics(width, height, P2D);
        world = new CameraGrid2D(this);
        frameRate(144);
    }

    @Override
    public void draw() {
        pg.beginDraw();
        pg.colorMode(HSB,1,1,1,1);
        updatePlayer();
        updateFlock();
        drawBackground();
        world.updateCamera(gui, pg, playerPos);
        world.drawGridAroundPlayer(gui, pg);
        pg.translate(playerPos.x, playerPos.y);
        pg.scale(gui.slider("entity zoom", 1));
        drawFlock();
        drawPlayer();
        pg.endDraw();
        image(pg, 0, 0);
    }

    private void updateFlock() {

    }

    private void updatePlayer(){
        gui.pushFolder("player");
        playerPos = gui.plotXY("playerPos");
        playerSpd = gui.plotXY("playerSpd");
        gui.plotSet("playerPos", PVector.add(playerPos, playerSpd));
        gui.popFolder();
    }

    private void drawPlayer() {
        pg.noStroke();
        pg.fill(1,1,1);
        pg.ellipse(0, 0, 60, 60);
    }

    private void drawFlock() {
        gui.pushFolder("flock");
        gui.popFolder();
    }

    private void drawBackground() {
        pg.fill(gui.colorPicker("background").hex);
        pg.noStroke();
        pg.rectMode(CORNER);
        pg.rect(0,0,width,height);
    }

}
