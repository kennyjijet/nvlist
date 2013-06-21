precision highp float;

uniform sampler2D tex;
uniform float time;

varying vec2 texCoord;
varying vec4 color;
varying vec3 fc;

void main() {
    vec2 tpos = texCoord;
    tpos.x += (tpos.y + 0.5) * sin(11.0 * (time + tpos.x * 7.0 + tpos.y)) * 0.002;
    tpos.y += (tpos.y + 1.0) * cos(time) * 0.005;
	tpos = clamp(tpos, 0.0, 1.0);    

    vec4 c = texture2D(tex, tpos);
    c.rgb = (c.rgb + tpos.yyy) * fc;

    gl_FragColor = clamp(color * c, 0.0, 1.0);
}
