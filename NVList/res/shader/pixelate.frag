//Pixelates the image

#version 110

uniform sampler2D tex;
uniform float time;

void main() {
    float pixelSize = time * time * .50; //Pixel size as a fraction of the total size
    vec2 texCoords = gl_TexCoord[0].st - .5; //Translate so texture coords so rounding will be towards the center
    texCoords = floor(texCoords / pixelSize + .5); //Do the rounding that causes the pixelate :: floor(f+.5) = round(f)
    texCoords = (texCoords * pixelSize) + .5; //Transform back to (0, 0, 1, 1)
	vec4 src = texture2D(tex, texCoords.st);
    
	//Multiply with glColor and clamp (otherwise the result might overflow)
    gl_FragColor = clamp(gl_Color * src, 0.0, 1.0);
}
