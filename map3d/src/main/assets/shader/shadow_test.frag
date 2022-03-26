# version 300 es

precision mediump float;

struct Material {
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
    float shininess;
    sampler2D map_kd;
    sampler2D map_kd_normal;
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
uniform vec3 eye;//Camera观察者坐标
uniform sampler2D shadowMap;//阴影贴图

in vec3 f_normal;
in vec3 f_position;
in vec2 f_texcoords;

in vec4 fragPosInLightSpace;

out vec4 fragColor;

float shadowCalculation(vec3 lightDir, vec3 normal)
{
    // 执行透视除法 [-1,-1]
    vec3 projCoords = fragPosInLightSpace.xyz / fragPosInLightSpace.w;
    // 变换到[0,1]的范围
    projCoords = projCoords * 0.5 + 0.5;

    if (projCoords.z > 1.0){
        return 1.0;
    } else {
        // 取得最近点的深度(使用[0,1]范围下的fragPosLight当坐标)
        float closestDepth = texture(shadowMap, projCoords.xy).r;
        // 取得当前片段在光源视角下的深度
        float currentDepth = projCoords.z;
        //计算阴影偏移  偏移距离太大暂时不用
//                        float bias = max(0.01 * (1.0 - dot(normal, lightDir)), 0.001);
        float bias=0.0;
        // 检查当前片段是否在阴影中
        //          float shadow = currentDepth - bias > closestDepth  ? 1.0 : 0.0;
        float shadow = 0.0;
        ivec2 mapSize=textureSize(shadowMap, 0);
        vec2 texelSize = vec2(1.0 / float(mapSize.x), 1.0/float(mapSize.y));
        for (int x = -1; x <= 1; ++x)
        {
            for (int y = -1; y <= 1; ++y)
            {
                float pcfDepth = texture(shadowMap, projCoords.xy + vec2(x, y) * texelSize).r;
                shadow += currentDepth - bias > pcfDepth ? 1.0 : 0.0;
            }
        }
        shadow /= 9.0;

        //        shadow = 1.0 - shadow;
        shadow = 1.0 - shadow;

        return shadow;
    }

}

vec3 directionalLight()
{
    //漫反射贴图 0.4加到环境光 0.6加到漫反射
    vec3 map= texture(material.map_kd, f_texcoords).rgb;
    // ambient 环境光照
    vec3 ambient = dirLight.ambient * material.ambient+ 0.4* map ;

    // diffuse 漫反射光照
    //    vec3 norm = texture(material.map_kd_normal, f_texcoords).rgb;
    //    norm = normalize(norm * 2.0 - 1.0);
    vec3 norm = normalize(f_normal);
    vec3 lightDir = normalize(-dirLight.direction);
    float diff = max(dot(norm, lightDir), 0.0);

    //    vec3 diffuse = dirLight.diffuse * (diff * material.diffuse); //普通光照材质计算
    vec3 diffuse = dirLight.diffuse * (diff *  0.6* map);//使用贴图做材质

    // specular 镜面光照
    vec3 viewDir = normalize(eye - f_position);
    vec3 halfway = normalize(lightDir + viewDir);
    float spec = pow(max(dot(norm, halfway), 0.0), material.shininess);
    vec3 specular = dirLight.specular * (spec * material.specular);

    float shadow = shadowCalculation(lightDir, norm);
    vec3 result = ambient  + shadow * (diffuse + specular);
    //    vec3 result = ambient  + diffuse + specular;
    //    return texture(material.map_kd, f_texcoords).rgb;
    return result;
}

void main() {
    fragColor = vec4(directionalLight(), 1.0);
    //    fragColor = vec4(texture(shadowMap,f_texcoords).rrr,1.0);
}