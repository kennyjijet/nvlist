#version 110

uniform float time;
uniform vec4 screen;

varying vec3 fc;

void main() {
    gl_TexCoord[0] = gl_MultiTexCoord0;        
    gl_Position = ftransform(); //gl_ProjectionMatrix * gl_ModelViewMatrix * gl_Vertex;
	
    vec2 screenPos = (gl_Position.x, screen.a - gl_Position.y) - screen.xy;
    vec2 normScreenPos = screenPos.xy / screen.ba;
	        
    float s = 0.0; //clamp(normScreenPos.y * 0.1, 0.0, 1.0);	
	gl_FrontColor = gl_Color - vec4(s, s, s, 0.0);
	
    s = clamp(1.0 + sin(fract(time) * 7.0) * cos(time * 3.0), 0.5, 3.5);
    fc = s * vec3(normScreenPos.y * 0.1 + 0.9, normScreenPos.y * 0.15 + 0.3, 0.2);	
}
