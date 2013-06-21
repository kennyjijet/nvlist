precision highp float;

uniform lowp sampler2D tex;
uniform float time;

varying vec2 texCoord;
varying lowp vec4 color;

void main() {
    lowp vec4 src = texture2D(tex, texCoord);
    lowp vec4 dst = vec4(1.0-src.r, 1.0-src.g, 1.0-src.b, src.a);
    
    gl_FragColor = color * mix(src, dst, time);
}
