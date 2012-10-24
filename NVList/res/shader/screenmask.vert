#version 110

uniform vec4 screen;

void main() {
    gl_TexCoord[0] = gl_MultiTexCoord0;    
    gl_Position = ftransform();
	gl_FrontColor = gl_Color;
    
    vec2 normScreenPos = (gl_Position.xy - screen.xy) / screen.zw;
    gl_TexCoord[1].st = normScreenPos;
}
