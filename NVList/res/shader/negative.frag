//Tweens an image to its negative

#version 110

uniform sampler2D tex;
uniform float time;

void main() {
	vec4 src = texture2D(tex, gl_TexCoord[0].st);

    vec4 dst = vec4(1-src.r, 1-src.g, 1-src.b, src.a);
    src = mix(src, dst, time);
    
	//Multiply with glColor and clamp (otherwise the result might overflow)
    gl_FragColor = clamp(gl_Color * src, 0.0, 1.0);
}
