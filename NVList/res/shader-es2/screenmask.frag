precision highp float;

uniform lowp sampler2D tex;
uniform lowp sampler2D mask;

varying vec2 texCoord;
varying vec2 maskTexCoord;
varying lowp vec4 color;

void main() {
    lowp vec4 c = texture2D(mask, maskTexCoord).r * texture2D(tex, texCoord);
    gl_FragColor = color * c;
}
