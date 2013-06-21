precision highp float;

uniform sampler2D tex;
uniform float time;

varying vec2 texCoord;
varying vec4 color;
varying vec3 fc;

void main() {
    vec4 c = texture2D(tex, texCoord);
    c.rgb = (c.rgb * 0.6 * fc); // + (0.5 * fc);    
    gl_FragColor = clamp(color * c, 0.0, 1.0);
}
