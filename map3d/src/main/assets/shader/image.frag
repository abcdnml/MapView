#version 300 es

in vec2 TexCoords;
in vec3 f_position;
out vec4 color;

uniform sampler2D screenTexture;

void main()
{
    float alpha = (1.0-f_position.y);
    vec4 texture = texture(screenTexture, TexCoords);
//    texture = vec4(0.0, 0.0, 1.0, 1.0);
    color = texture;
    color = vec4(texture.rgb, texture.a*alpha);
}