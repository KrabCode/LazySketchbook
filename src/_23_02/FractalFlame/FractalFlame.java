package _23_02.FractalFlame;

import _0_utils.Utils;
import _22_03.PostFxAdapter;
import lazy.LazyGui;
import lazy.ShaderReloader;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.opengl.PShader;

import java.util.ArrayList;

/**
 * The algorithm:
 * Have three non-linear functions that return a new pos based on old pos.
 * Pick random point, iterate 'totalIterations' times, pick a random function for each iteration.
 * Start adding low brightness points to the canvas after 'invisibleIterations'.
 * Blue means that the point hit it at all, red means the number of iters when that point was reached.
 * That creates the histogram.
 * Then render the histogram using logarithmic brightness and iteration count in a shader based filter.
 */



public class FractalFlame extends PApplet {
    private LazyGui gui;
    PGraphics pg;
    ArrayList<ArrayList<PVector>> points = new ArrayList<>();

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    public void settings() {
        fullScreen(P2D);
    }

    public void setup() {
        gui = new LazyGui(this);
        pg = createGraphics(width, height, P2D);
    }

    public void draw() {
        clear();
        pg.beginDraw();
        if (gui.toggle("big red switch/update scene", true)) {
            pg.noStroke();
            if (gui.toggle("background/active", true)) {
                pg.blendMode(PConstants.SUBTRACT);
                pg.fill(gui.colorPicker("background/subtract", 0xFF000000).hex);
                pg.rect(0, 0, width, height);
                pg.blendMode(PConstants.ADD);
                pg.fill(gui.colorPicker("background/add", 0xFF000000).hex);
                pg.rect(0, 0, width, height);
            }

            pg.translate(width / 2f, height / 2f);
            gui.pushFolder("point clusters");
            int emitterCount = gui.sliderInt("cluster count");
            for (int i = 0; i < emitterCount; i++) {
                if (i >= points.size()) {
                    points.add(new ArrayList<>());
                }
                gui.pushFolder("cluster " + i);
                updatePoints(points.get(i));
                gui.popFolder();
            }
            gui.popFolder();
            pg.blendMode(BLEND);
        }

        pg.endDraw();
        PostFxAdapter.apply(this, gui, pg);
        image(pg, 0, 0);
        gui.pushFolder("shader");

        String shaderPath = gui.text("shader path", "_23_02/FractalFlame/histogramInterpreter.glsl");
        if(gui.toggle("active")){
            ShaderReloader.getShader(shaderPath).set("time", radians(frameCount));
            ShaderReloader.filter(dataPath(shaderPath));
        }
        gui.popFolder();
        resetShader();
        Utils.record(this, gui);
    }

    private void updatePoints(ArrayList<PVector> points) {
        int pointCount = gui.sliderInt("point count", 100);
        if (gui.button("points.clear()")) {
            points.clear();
        }
        int spawnPerFrame = gui.sliderInt("spawn per frame");
        int countToRemove = min(points.size() - 1, spawnPerFrame);
        for (int i = 0; i < countToRemove; i++) {
            points.remove(0);
        }
        int itersPerFrame = gui.sliderInt("iters per frame", 5);
        float range = gui.slider("spawn range", 300);
        pg.strokeWeight(gui.slider("stroke weight", 1.99f));
        PVector lerpCenter = gui.plotXY("lerp center");
        float lerpAmt = gui.slider("lerp amount", 0.1f);
        int lerpSideCount = gui.sliderInt("lerp sides", 6);
        float lerpRadius = gui.slider("lerp radius", 600);
        float lerpAngleOffset = PI * gui.slider("lerp angle");
        float rotate = PI * gui.slider("rotamte");
        float scale = gui.slider("scale", 1);
        PVector addPos = gui.plotXY("add pos");
        for (int pointIndex = 0; pointIndex < pointCount; pointIndex++) {
            if (pointIndex > points.size() - 1) {
                points.add(new PVector(random(-range, range), random(-range, range)));
            }
            PVector p = points.get(pointIndex);
            for (int iter = 0; iter < itersPerFrame; iter++) {
                int randomFunctionIndex = floor(random(4));
if(randomFunctionIndex == 0){
    int randomSide = floor(random(lerpSideCount));
    float angle = lerpAngleOffset + TAU * norm(randomSide, 0, lerpSideCount);
    float cornerX = lerpCenter.x + lerpRadius * cos(angle);
    float cornerY = lerpCenter.y + lerpRadius * sin(angle);
    p.x = lerp(p.x, cornerX, lerpAmt);
    p.y = lerp(p.y, cornerY, lerpAmt);
}else if(randomFunctionIndex == 1){
    p.rotate(rotate);
}else if(randomFunctionIndex == 2){
                    p.mult(scale);
                }else if(randomFunctionIndex == 3){
                    p.add(addPos.copy());
                }
                pg.blendMode(ADD);
                pg.stroke(gui.colorPicker("point add", 0xFFFFFFFF).hex);
                pg.point(p.x, p.y);
                pg.blendMode(SUBTRACT);
                pg.stroke(gui.colorPicker("point sub", 0xFFFFFFFF).hex);
                pg.point(p.x, p.y);
            }
        }
    }

}
