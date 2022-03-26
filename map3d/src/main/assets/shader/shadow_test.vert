# version 300 es

uniform mat4 mat_proj;
uniform mat4 mat_view;
uniform mat4 mat_model;
uniform mat4 mat_normal;//法线矩阵
//阴影矩阵
uniform mat4 mat_shadow_proj;
uniform mat4 mat_shadow_view;

in vec3 v_position;
in vec3 v_normal;       //顶点颜色
in vec2 v_texCoords;       //顶点纹理坐标


out vec3 f_color;       //用于传递给片元着色器的变量
out vec2 f_texcoords;//传入纹理坐标
out vec3 f_normal;//法线
out vec3 f_position;//顶点世界坐标
//阴影光空间
out vec4 fragPosInLightSpace;


void main() { 
   gl_Position = mat_proj * mat_view * mat_model  * vec4(v_position, 1.0);
   f_position = vec3(mat_model * vec4(v_position, 1.0));
   f_normal = mat3(mat_normal) * v_normal;
   f_texcoords=v_texCoords;

   fragPosInLightSpace=mat_shadow_proj * mat_shadow_view * vec4(f_position, 1.0);
}