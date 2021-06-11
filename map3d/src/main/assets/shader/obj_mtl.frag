# version 300 es
precision mediump float;

in vec2 TexCoords;
in vec3 Normal;
in vec3 FragPos;



out vec4 fragColor;

struct Material {
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
    sampler2D map_ka;
    sampler2D map_kd;
    sampler2D map_ks;
    float shininess;
};

struct DirLight {
//平行光参数
    vec3 direction;
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
};

uniform Material material;
uniform DirLight dirLight;
uniform vec3 viewPos;//Camera观察者坐标


vec3 k_directionalLight()
{
    //平行光 太阳光
    // ambient 环境光照
    vec3 ambient = dirLight.ambient * material.ambient ;

    // diffuse 漫反射光照
    vec3 norm = normalize(Normal);
    vec3 lightDir = normalize(-dirLight.direction);
    float diff = max(dot(norm, lightDir), 0.0);
    vec3 diffuse = dirLight.diffuse * (diff * material.diffuse);

    // specular 镜面光照
    vec3 viewDir = normalize(viewPos - FragPos);
    vec3 reflectDir = reflect(-lightDir, norm);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), material.shininess);
    vec3 specular = dirLight.specular * (spec * material.specular);

    vec3 result = ambient + diffuse + specular;
    return result;
}

void main()
{
    vec3  result = k_directionalLight();
    fragColor = vec4(result, 1.0);
}

