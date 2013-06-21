precision highp float;

uniform mat4 mvpMatrix;
uniform vec4 screen;

attribute vec4 attrPos;
attribute vec2 attrTexCoord;
attribute lowp vec4 attrColor;

varying vec2 texCoord;
varying vec2 maskTexCoord;
varying lowp vec4 color;

void main() {
    color = attrColor;
    texCoord = attrTexCoord;
    maskTexCoord = (gl_Position.xy - screen.xy) / screen.zw;    
    gl_Position = mvpMatrix * attrPos;
}
