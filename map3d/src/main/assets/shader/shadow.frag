# version 300 es
precision mediump float;

out vec4 f_color;

void main()
{
//    gl_FragDepth = gl_FragCoord.z;
    f_color=vec4(gl_FragCoord.z);
}