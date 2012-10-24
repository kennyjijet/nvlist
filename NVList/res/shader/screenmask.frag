//Changes the texture's transparency based on the instensity of a mask texture

#version 110

uniform sampler2D tex;
uniform sampler2D mask;

void main() {
    float alpha = texture2D(mask, gl_TexCoord[1].st).r;
    vec4 c = alpha * texture2D(tex, gl_TexCoord[0].st);
    
    //vec4 c = vec4(gl_TexCoord[1].s, gl_TexCoord[1].t, 0, 1); //Debug mask texcoords
    
	//Multiply with glColor and clamp (otherwise the result might overflow)
    gl_FragColor = clamp(gl_Color * c, 0.0, 1.0);
}
