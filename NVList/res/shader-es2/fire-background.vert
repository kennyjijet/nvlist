precision highp float;

uniform mat4 mvpMatrix;
uniform float time;
uniform vec4 screen;

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
    
    vec2 normScreenPos = (gl_Position.xy - screen.xy) / screen.zw;
    
	vec2 tpos = normScreenPos.xy * gl_MultiTexCoord0.st;
	float s = (0.8 - 0.3 * tpos.y) + sin(time * 0.1) * 0.3;
	fc = s * vec3(0.9 - tpos.y * 0.3, 0.2 - tpos.y * 0.5, 0);    
}
