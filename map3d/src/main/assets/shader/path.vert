# version 300 es

in vec3 aPos;//顶点

out vec3 aColor;

uniform vec3 color;//平移缩放旋转

uniform mat4 model;//平移缩放旋转
uniform mat4 view;//lookat 大概眼睛的位置
uniform mat4 projection;//投影属性


void main()
{
    gl_Position = projection * view * model * vec4(aPos, 1.0);
    aColor=color;
}

