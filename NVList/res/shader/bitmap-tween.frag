//Tweens from one texture to another using a grayscale bitmap to control the
//relative fade time of each pixel

#version 110

uniform sampler2D src0;
uniform sampler2D src1;
uniform sampler2D fade;
uniform sampler2D remap;

void main() {    
    vec4 c0 = texture2D(src0, gl_TexCoord[0].st);
    vec4 c1 = texture2D(src1, gl_TexCoord[1].st);

	float i = texture2D(fade, gl_TexCoord[2].st).r;

	//Generate 2D lookup vector into remap tex
	i = 255.0 * i;
	vec2 rv = vec2(fract(i), floor(i)) / 255.0;

    vec2 aa = texture2D(remap, rv).bg;
    float alpha = clamp((aa.y * 255.0 + aa.x) / 256.0, 0.0, 1.0);
    vec4 c = mix(c0, c1, alpha); 
    
	//Multiply with glColor and clamp (otherwise the result might overflow)
    gl_FragColor = clamp(gl_Color * c, 0.0, 1.0);
}
