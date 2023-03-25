package _23_02.FractalFlame;

import _0_utils.Shapes;
import _0_utils.Utils;
import _22_03.PostFxAdapter;
import com.krab.lazy.LazyGui;
import com.krab.lazy.LazyGuiSettings;
import com.krab.lazy.ShaderReloader;
import processing.core.*;
import processing.opengl.PShader;

import java.util.ArrayList;

/**
 * based on: <a href="https://flam3.com/flame_draves.pdf">The Fractal Flame Algorithm</a>
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
    PGraphics pg, fg;
    ArrayList<ArrayList<Point>> points = new ArrayList<>();

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    public void settings() {
        fullScreen(P2D);
    }

    public void setup() {
        gui = new LazyGui(this, new LazyGuiSettings().setLoadLatestSaveOnStartup(false));
        pg = createGraphics(width, height, P2D);
        fg = createGraphics(width, height, P2D);
    }

    public void draw() {
        clear();
        pg.beginDraw();
        if (gui.toggle("update scene", true)) {
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
            int emitterCount = gui.sliderInt("cluster count", 1);
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

        gui.pushFolder("histogram shader");
        fg.beginDraw();
        String shaderPath = gui.text("shader path", "_23_02/FractalFlame/histogramInterpreter.glsl");

        if (gui.toggle("active", true)) {
            PShader shader = ShaderReloader.getShader(shaderPath);
            shader.set("time", radians(frameCount));
            shader.set("histogram", pg);
            PGraphics palette = gui.gradient("palette");
            shader.set("palette", palette);
            shader.set("gammaPow", gui.slider("gamma pow", 2.2f));
            shader.set("logMax", gui.slider("log max", 0.001f));
            ShaderReloader.filter(shaderPath, fg);
        }
        gui.popFolder();
        gui.pushFolder("text");
        Shapes.drawSimpleText("text 1/", gui, fg);
        Shapes.drawSimpleText("text 2/", gui, fg);
        gui.popFolder();
        fg.endDraw();
        PostFxAdapter.apply(this, gui, fg);
        image(fg, 0, 0);

        Utils.record(this, gui);
    }

    private void updatePoints(ArrayList<Point> points) {
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
        int invisibleIterCount = gui.sliderInt("invis iters", 0);
        int pointColor = gui.colorPicker("point add", 0xFFFFFFFF).hex;
        float shapeSize = gui.slider("global shape size", 800);
        ArrayList<Function> functions = rebuildFunctions();
        for (int pointIndex = 0; pointIndex < pointCount; pointIndex++) {
            if (pointIndex > points.size() - 1) {
                points.add(new Point(random(-range, range), random(-range, range)));
            }
            Point p = points.get(pointIndex);
            for (int iter = 0; iter < itersPerFrame; iter++) {
                if(functions.size() > 0){

                    int randomFunctionIndex = floor(random(functions.size()));
                    functions.get(randomFunctionIndex).transform(p);
                }
                if (p.iters++ < invisibleIterCount) {
                    continue;
                }
                pg.blendMode(ADD);
                pg.stroke(pointColor);
                pg.point(p.x*shapeSize, p.y*shapeSize);
            }
        }
    }

    private ArrayList<Function> rebuildFunctions() {
        ArrayList<Function> functions = new ArrayList<>();
        gui.pushFolder("functions");
        gui.pushFolder("lerp a");
        if (gui.toggle("active")) {
            PVector lerpCenter = gui.plotXY("pos");
            float lerpAmt = gui.slider("amount", 0.1f);
            int lerpSideCount = gui.sliderInt("sides", 6);
            float shapeRadius = gui.slider("radius", 1);
            float lerpAngleOffset = PI * gui.slider("angle");
            functions.add(p -> {
                if (lerpAmt > 0) {
                    int randomSide = floor(random(lerpSideCount));
                    float angle = lerpAngleOffset + TAU * norm(randomSide, 0, lerpSideCount);
                    float cornerX = lerpCenter.x + shapeRadius * cos(angle);
                    float cornerY = lerpCenter.y + shapeRadius * sin(angle);
                    p.x = lerp(p.x, cornerX, lerpAmt);
                    p.y = lerp(p.y, cornerY, lerpAmt);
                }
            });
        }
        gui.popFolder();

        gui.pushFolder("lerp b");
        if (gui.toggle("active")) {
            PVector lerpCenter = gui.plotXY("pos");
            float lerpAmt = gui.slider("amount", 0.1f);
            int lerpSideCount = gui.sliderInt("sides", 6);
            float shapeRadius = gui.slider("radius", 1);
            float lerpAngleOffset = PI * gui.slider("angle");
            functions.add(p -> {
                if (lerpAmt > 0) {
                    int randomSide = floor(random(lerpSideCount));
                    float angle = lerpAngleOffset + TAU * norm(randomSide, 0, lerpSideCount);
                    float cornerX = lerpCenter.x + shapeRadius * cos(angle);
                    float cornerY = lerpCenter.y + shapeRadius * sin(angle);
                    p.x = lerp(p.x, cornerX, lerpAmt);
                    p.y = lerp(p.y, cornerY, lerpAmt);
                }
            });
        }
        gui.popFolder();

        gui.pushFolder("sinusoidal");
        if (gui.toggle("active")) {
            float freq = gui.slider("freq", 1);
            float amp = gui.slider("amp", 1);
            functions.add(p -> {
                p.x = amp*sin(p.x*freq);
                p.y = amp*sin(p.y*freq);
            });
        }
        gui.popFolder();

        if(gui.toggle("circle inversion")){
            functions.add(p -> {
                float dot = PVector.dot(p, p);
                p.x /= dot;
                p.y /= dot;
            });
        }

        gui.pushFolder("spherical");
        if(gui.toggle("active")){
            float radius = gui.slider("radius", 1);
            float offset = gui.slider("offset", 1);
            functions.add(p -> {
               p.x = p.x / (radius * PVector.dot(p, p) + offset);
               p.y = p.y / (radius * PVector.dot(p, p) + offset);
            });
        }
        gui.popFolder();

        gui.pushFolder("rot + scl");
        if(gui.toggle("rotate active")){
            float rot = gui.slider("rotate amount");
            functions.add(p -> {
                p.rotate(rot);
            });
        }

        if(gui.toggle("scale active")){
            float scl = gui.slider("scale amount");
            functions.add(p -> {
                p.mult(scl);
            });
        }
        gui.popFolder();
        gui.popFolder();
        return functions;
    }

    static class Point extends PVector {
        int iters;

        public Point(float x, float y) {
            super(x, y);
        }
    }

    interface Function {
        void transform(PVector p);
    }

}
