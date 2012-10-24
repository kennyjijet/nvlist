#version 110

uniform float time;
uniform sampler2D tex;

varying vec3 fc;

void main(void) {
	vec2 tpos = gl_TexCoord[0].st;
    //tpos = tpos * 0.9 + 0.05; //Slightly zooms in so background remains hidden
    
    tpos.x += (tpos.y + 0.5) * sin(11.0 * (time + tpos.x * 7.0 + tpos.y)) * 0.002;
    tpos.y += (tpos.y + 1.0) * cos(time) * 0.005;

	tpos = clamp(tpos, 0.0, 1.0);
	
	vec4 c = texture2D(tex, tpos);	
    c.rgb = (c.rgb + tpos.yyy) * fc;

    gl_FragColor = clamp(gl_Color * c, 0.0, 1.0);
}
