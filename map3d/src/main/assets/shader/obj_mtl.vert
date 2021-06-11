# version 300 es

in vec3 aPos;//顶点
in vec3 aNormal;//法线
in vec2 aTexCoords;//纹理

uniform mat4 model;//平移缩放旋转
uniform mat4 view;//lookat 大概眼睛的位置
uniform mat4 projection;//投影属性
uniform mat4 normal_matrix;//法线矩阵

out vec2 TexCoords;//传入纹理坐标
out vec3 Normal;//法线
out vec3 FragPos;//顶点世界坐标

void main()
{
    TexCoords = aTexCoords;
    FragPos = vec3(model * vec4(aPos, 1.0));
    Normal = mat3(normal_matrix) * aNormal;
    gl_Position = projection * view * model * vec4(aPos, 1.0);
}

