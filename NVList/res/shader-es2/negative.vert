precision highp float;

uniform mat4 mvpMatrix;

attribute vec4 attrPos;
attribute vec2 attrTexCoord;
attribute lowp vec4 attrColor;

varying vec2 texCoord;
varying lowp vec4 color;

void main() {
    texCoord = attrTexCoord;
    color = attrColor;
    gl_Position = mvpMatrix * attrPos;
}
