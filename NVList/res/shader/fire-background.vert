#version 110

uniform float time;
uniform vec4 screen;

varying vec3 fc;

void main() {
    gl_TexCoord[0] = gl_MultiTexCoord0;        
    gl_Position = ftransform(); //gl_ProjectionMatrix * gl_ModelViewMatrix * gl_Vertex;
	gl_FrontColor = gl_Color;
	
    vec2 normScreenPos = (gl_Position.xy - screen.xy) / screen.zw;
    
	vec2 tpos = normScreenPos.xy * gl_MultiTexCoord0.st;
	float s = (0.8 - 0.3 * tpos.y) + sin(time * 0.1) * 0.3;
	fc = s * vec3(0.9 - tpos.y * 0.3, 0.2 - tpos.y * 0.5, 0);
}
