#version 300 es
in vec3 position;
in vec2 texCoords;
out vec2 TexCoords;
out vec3 f_position;
uniform mat4 mat_model;
uniform mat4 mat_view;
uniform mat4 mat_proj;

void main()
{
    gl_Position = mat_proj * mat_view  * mat_model * vec4(position, 1.0f);
    TexCoords = texCoords;
    f_position = position;
}