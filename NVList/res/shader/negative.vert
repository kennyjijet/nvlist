#version 110

void main() {
    gl_TexCoord[0] = gl_MultiTexCoord0;    
    gl_Position = ftransform(); //gl_ProjectionMatrix * gl_ModelViewMatrix * gl_Vertex;
	gl_FrontColor = gl_Color;
}
