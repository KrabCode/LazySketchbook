precision highp float;

uniform sampler2D texture;
uniform vec2 resolution;

uniform float dA;
uniform float dB;
uniform float f;
uniform float k;
uniform float t;

// Based on https://karlsims.com/rd.html
// A is represented by RED
// green is unused
// B is represented by BLUE

const vec3[] grid = vec3[](
vec3(-1, -1, 0.05), vec3(0, -1, 0.2), vec3(1, -1, 0.05),
vec3(-1, 0, 0.2), vec3(0,0,-1),      vec3(1, 0, 0.2),
vec3(-1, 1, 0.05), vec3(0, 1, 0.2), vec3(1, 1, 0.05)
);

vec2 getValuesFromColor(vec4 col){
    return vec2(col.r, col.b);
}

vec4 setValuesFromColor(vec2 vars){
    return vec4(vars.x, 0., vars.y, 1);
}

vec4 getPixel(vec2 coord){
    return texture(texture, coord.xy / resolution.xy);
}

vec2 getPixelValues(vec2 coord){
    return getValuesFromColor(getPixel(coord));
}

vec2 getLaplacianAverageValues(vec2 p){
    return
        getPixelValues(p+grid[0].xy) * grid[0].z +
        getPixelValues(p+grid[1].xy) * grid[1].z +
        getPixelValues(p+grid[2].xy) * grid[2].z +
        getPixelValues(p+grid[3].xy) * grid[3].z +
        getPixelValues(p+grid[4].xy) * grid[4].z +
        getPixelValues(p+grid[5].xy) * grid[5].z +
        getPixelValues(p+grid[6].xy) * grid[6].z +
        getPixelValues(p+grid[7].xy) * grid[7].z +
        getPixelValues(p+grid[8].xy) * grid[8].z;
}

vec2 simulateReactionDiffusion(vec2 p){
    vec2 AB = getPixelValues(p);
    float A = AB.x;
    float B = AB.y;
    vec2 lapDiff = getLaplacianAverageValues(p);
    float ABsquared = A * B * B;
    float newA = A + (dA * lapDiff.x - ABsquared + f * (1.0 - A)) * t;
    float newB = B + (dB * lapDiff.y + ABsquared - ((k + f) * B)) * t;
    return clamp(vec2(newA, newB), vec2(0), vec2(1));
}

void main(){
    vec2 p = gl_FragCoord.xy;
    vec2 newValues = simulateReactionDiffusion(p);
    gl_FragColor = setValuesFromColor(newValues);
}