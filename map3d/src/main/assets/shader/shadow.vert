# version 300 es

in vec3 v_position;//顶点

uniform mat4 mat_model;//平移缩放旋转
uniform mat4 mat_view;//lookat 大概眼睛的位置
uniform mat4 mat_proj;//投影属性


void main()
{
    gl_Position = mat_proj * mat_view  * mat_model * vec4(v_position, 1.0);
}

