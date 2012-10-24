#version 110

uniform float time;
uniform sampler2D tex;

varying vec3 fc;

void main (void) {	
	vec4 c = texture2D(tex, gl_TexCoord[0].st);
	
    c.rgb = (c.rgb * 0.6 * fc); // + (0.5 * fc);
	
    gl_FragColor = clamp(gl_Color * c, 0.0, 1.0);
}
