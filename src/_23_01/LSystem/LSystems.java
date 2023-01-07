package _23_01.LSystem;

import _0_utils.Utils;
import processing.core.PApplet;
import processing.core.PGraphics;
import lazy.LazyGui;
import processing.core.PVector;

import java.util.ArrayList;

public class LSystems extends PApplet {
    ArrayList<LSystem> systems = new ArrayList<>();
    LazyGui gui;
    PGraphics pg;

    public static void main(String[] args) {
        PApplet.main(java.lang.invoke.MethodHandles.lookup().lookupClass());
    }

    @Override
    public void settings() {
//        size(1080, 1080, P2D);
        fullScreen(P2D);
    }

    @Override
    public void setup() {
        gui = new LazyGui(this);
        pg = createGraphics(width, height, P2D);
        colorMode(HSB, 1, 1, 1, 1);
        frameRate(144);
    }

    @Override
    public void draw() {
        pg.beginDraw();
        drawBackground();
        drawLSystems();
        pg.endDraw();
        image(pg, 0, 0);
        gui.draw();
        Utils.record(this, gui);
    }

    private void drawTutorial() {
        gui.pushFolder("tutorial");

        gui.text("overview",
                "An L-system or Lindenmayer system is a parallel rewriting system and a type of formal grammar.\n" +
                "An L-system consists of an alphabet of symbols that can be used to make strings,\n" +
                "a collection of production rules that expand each symbol into some larger string\n" +
                " of symbols, an initial \"axiom\" string from which to begin construction,\n" +
                "and a mechanism for translating the generated strings into geometric structures.");
        gui.text("history",
                "L-systems were introduced and developed in 1968 by Aristid Lindenmayer, \n" +
                        "a Hungarian theoretical biologist and botanist at the University of Utrecht.\n" +
                        "Lindenmayer used L-systems to describe the behaviour of plant cells and to model\n" +
                        "the growth processes of plant development. L-systems have also been used to model\n" +
                        "the morphology of a variety of organisms and can be used to generate self-similar fractals.");
        gui.text("mechanics",
                "an \"axiom\" is a string that acts as the first generation\n" +
                    "the \"rules\" are applied on the current generation to produce a new generation\n" +
                    "the final result is a list of instructions of how to draw the recursive tree");
        gui.text("rules", "a rule does a \"find + replace\" on the current string\n" +
                "the part being searched for is called the \"matcher\" in this sketch\n" +
                "and its new value is called the \"replacement\" \n");
        gui.text("drawing instructions",
                    "F : go forward\n" +
                        "+ : turn right\n" +
                        "- : turn left\n" +
                        "[ : save position and angle\n" +
                        "] : restore position and angle"
        );
        gui.pushFolder("example fractal flower");
        gui.text("axiom", "X");
        gui.text("rule 1 matcher", "F");
        gui.text("rule 1 replacement", "FF");
        gui.text("rule 2 matcher", "X");
        gui.text("rule 2 replacement", "F+[[X]-X]-F[-FX]+X");
        gui.text("angle (degrees)", "25");
        gui.popFolder();

        gui.pushFolder("example koch curve");
        gui.text("axiom", "F");
        gui.text("rule 1 matcher", "F");
        gui.text("rule 1 replacement", "F+F−F−F+F");
        gui.text("angle (degrees)", "90");
        gui.popFolder();

        gui.popFolder();
    }

    private void drawLSystems() {
        gui.pushFolder("systems");
        drawTutorial();
        int systemCount = gui.sliderInt("count", 1);
        if(gui.button("add system")){
            gui.sliderSet("count", systemCount + 1);
        }
        for (int i = 0; i < systemCount; i++) {
            if(systems.size() <= i){
                systems.add(new LSystem());
            }
            gui.pushFolder("system " + (i+1));
            systems.get(i).update();
            gui.popFolder();
        }
        gui.popFolder();
    }

    private void drawBackground() {
        pg.fill(gui.colorPicker("background", color(0.15f)).hex);
        pg.noStroke();
        pg.rectMode(CORNER);
        pg.rect(0, 0, width, height);
    }

    class LSystem{
        String axiom = "";
        String current = "";
        int generation = 0;

        public void update() {
            gui.pushFolder("rules");
            float angle = radians(gui.slider("angle (deg)", 25));
            String newAxiom = gui.text("axiom", axiom);
            if(!newAxiom.equals(axiom)){
                axiom = newAxiom;
                current = axiom;
            }
            int ruleCount = gui.sliderInt("rule count", 1);
            if(gui.button("add rule")){
                gui.sliderSet("rule count", ruleCount + 1);
            }
            ArrayList<String[]> rules = new ArrayList<>();
            for (int i = 0; i < ruleCount; i++) {
                String matcher = gui.text("rule " + (i+1) + " matcher", "");
                String replacement = gui.text("rule " + (i+1) + " replacement", "");
                String[] rule = new String[]{matcher, replacement};
                rules.add(rule);
            }
            gui.popFolder();

            gui.textSet("current string", current);
            gui.textSet("current string length", "" + current.length());
            gui.sliderInt("generation");
            gui.sliderSet("generation", generation);
            if(gui.button("advance generation") || frameCount < 3){
                if(current == null || "".equals(current)){
                    current = axiom;
                }
                for (String[] rule : rules) {
                    current = current.replaceAll(rule[0], rule[1]);
                }
                println("current string: " + current);
                generation++;
            }
            if(gui.button("reset to axiom")){
                current = axiom;
                generation = 0;
            }

            gui.pushFolder("display");
            if(gui.toggle("active", true)){
                float size = gui.slider("scale", 100);
                char[] path = current.toCharArray();
                pg.pushMatrix();
                PVector pos = gui.plotXY("position");
                pg.translate(width/2f+pos.x, height/2f + pos.y);
                pg.rotate(radians(gui.slider("rotation (deg)")));
                pg.stroke(gui.colorPicker("stroke").hex);
                pg.strokeWeight(gui.slider("weight", 1.99f));
                for(char c : path){
                    if(c == 'F'){
                        pg.line(size,0,0,0);
                        pg.translate(size, 0);
                    }else if(c == '−'){
                        pg.rotate(-angle);
                    }else if(c == '+'){
                        pg.rotate(angle);
                    }else if(c == '['){
                        pg.pushMatrix();
                    }else if(c == ']'){
                        pg.popMatrix();
                    }
                }
                pg.popMatrix();
            }
            gui.popFolder();

        }

    }


}

