precision highp float;

uniform mat4 mvpMatrix;
uniform vec4 screen;
uniform float time;

attribute vec4 attrPos;
attribute vec2 attrTexCoord;
attribute vec4 attrColor;

varying vec2 texCoord;
varying vec4 color;
varying vec3 fc;

void main() {
    texCoord = attrTexCoord;    
    color = attrColor;    
    gl_Position = mvpMatrix * attrPos;
    
    vec2 screenPos = (gl_Position.x, screen.a - gl_Position.y) - screen.xy;
    vec2 normScreenPos = screenPos.xy / screen.ba;
    
    float s = clamp(1.0 + sin(fract(time) * 7.0) * cos(time * 3.0), 0.5, 3.5);
    fc = s * vec3(normScreenPos.y * 0.1 + 0.9, normScreenPos.y * 0.15 + 0.3, 0.2);
}
