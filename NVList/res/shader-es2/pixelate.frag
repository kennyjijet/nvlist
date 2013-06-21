precision highp float;

uniform lowp sampler2D tex;
uniform float time;

varying vec2 texCoord;
varying lowp vec4 color;

void main() {
    float pixelSize = time * time * .50; //Pixel size as a fraction of the total size
    texCoord = floor((texCoord - .5) / pixelSize + .5); //Do the rounding that causes the pixelate :: floor(f+.5) = round(f)
    texCoord = (texCoord * pixelSize) + .5; //Transform back to (0, 0, 1, 1)
    
    gl_FragColor = color * texture2D(tex, texCoord);
}
