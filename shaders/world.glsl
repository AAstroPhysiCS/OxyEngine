//#type fragment
#version 460 core

layout(location = 0) out vec4 color;

in vec2 texCoordsOut;
in float textureSlotOut;
in vec4 colorOut;
in vec4 normalsOut;
in vec4 modelMatrixPosOut;

uniform sampler2D tex[32];
uniform samplerCube skyBoxTexture;
uniform vec3 cameraPos;

struct Material{
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
    float reflectance;
};

struct PointLight {
    vec3 position;
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;

    float constant;
    float linear;
    float quadratic;
};

struct DirectionalLight{
    vec3 direction;
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
};

uniform Material material;
uniform PointLight p_Light;
uniform DirectionalLight d_Light;
uniform float currentLightIndex = -1;

vec3 calcAmbient(vec3 ambient, vec3 lightAmbient){
    return lightAmbient * ambient;
}

vec3 calcDiffuse(vec3 lightDir, vec3 diffuse, vec3 lightDiffuse){
    vec3 norm = vec3(normalize(normalsOut));
    float diff = max(dot(norm, lightDir), 0.0);
    return lightDiffuse * (diff * diffuse);
}

vec3 calcSpecular(vec3 lightDir, vec3 specular, vec3 lightSpecular){
    vec3 viewDir = normalize(cameraPos - vec3(modelMatrixPosOut));
    vec3 reflectDir = reflect(-lightDir, vec3(normalize(normalsOut)));
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), material.reflectance);
    return lightSpecular * (spec * specular);
}

void main(){
      //TODO: SUMMARIZE THIS

    vec3 I = normalize(vec3(modelMatrixPosOut) - cameraPos);
    vec3 R = reflect(I, normalize(vec3(normalsOut)));

    if(currentLightIndex == 0){ // POINTLIGHT
        vec3 lightDir = normalize(p_Light.position - vec3(modelMatrixPosOut));
        int index = int(round(textureSlotOut));
        if (index == 0){
            vec3 ambient = calcAmbient(material.ambient, p_Light.ambient);
            vec3 diffuse = calcDiffuse(lightDir, material.diffuse, p_Light.diffuse);
            vec3 specular = calcSpecular(lightDir, material.specular, p_Light.specular);

            float distance = length(p_Light.position - vec3(modelMatrixPosOut));
            float attenuation = 1.0 / (p_Light.constant + p_Light.linear * distance + p_Light.quadratic * (distance * distance));
            ambient *= attenuation;
            diffuse *= attenuation;
            specular *= attenuation;
            vec3 result = specular + diffuse + ambient;

            color = vec4(result, 1.0f) * colorOut * texture(skyBoxTexture, R);
        }
        else {
            vec3 ambient = calcAmbient(texture(tex[index], texCoordsOut).rgb, p_Light.ambient);
            vec3 diffuse = calcDiffuse(lightDir, texture(tex[index], texCoordsOut).rgb, p_Light.diffuse);
            vec3 specular = calcSpecular(lightDir, texture(tex[index], texCoordsOut).rgb, p_Light.specular);

            float distance = length(p_Light.position - vec3(modelMatrixPosOut));
            float attenuation = 1.0 / (p_Light.constant + p_Light.linear * distance + p_Light.quadratic * (distance * distance));
            ambient *= attenuation;
            diffuse *= attenuation;
            specular *= attenuation;
            vec3 result = specular + diffuse + ambient;

            color = vec4(result, 1.0f) * texture(skyBoxTexture, R);
        }
    } else if(currentLightIndex == 1){ // DIRECTIONAL LIGHT
        vec3 lightDir = normalize(-d_Light.direction);
        int index = int(round(textureSlotOut));
        if (index == 0){
            vec3 ambient = calcAmbient(material.ambient, d_Light.ambient);
            vec3 diffuse = calcDiffuse(lightDir, material.diffuse, d_Light.diffuse);
            vec3 specular = calcSpecular(lightDir, material.specular, d_Light.specular);
            vec3 result = specular + diffuse + ambient;

            color = vec4(result, 1.0f) * colorOut * texture(skyBoxTexture, R);
        }
        else {
            vec3 ambient = calcAmbient(texture(tex[index], texCoordsOut).rgb, d_Light.ambient);
            vec3 diffuse = calcDiffuse(lightDir, texture(tex[index], texCoordsOut).rgb, d_Light.diffuse);
            vec3 specular = calcSpecular(lightDir, texture(tex[index], texCoordsOut).rgb, d_Light.specular);
            vec3 result = specular + diffuse + ambient;

            color = vec4(result, 1.0f) * texture(skyBoxTexture, R);
        }
    } else { // NO LIGHT
        int index = int(round(textureSlotOut));
        if (index == 0){
            color = colorOut * texture(skyBoxTexture, R);
        }
        else {
            color = texture(tex[index], texCoordsOut) * texture(skyBoxTexture, R);
        }
    }
}

//#type vertex
#version 460 core

layout(location = 0) in vec4 pos;
layout(location = 1) in vec2 tcs;
layout(location = 2) in float textureSlot;
layout(location = 3) in vec4 color;
layout(location = 4) in vec4 normals;

uniform mat4 v_Matrix;

out vec2 texCoordsOut;
out float textureSlotOut;
out vec4 colorOut;
out vec4 normalsOut;
out vec4 modelMatrixPosOut;

void main(){
    textureSlotOut = textureSlot;
    texCoordsOut = tcs;
    colorOut = color;
    modelMatrixPosOut = 1.0 * pos;
    normalsOut = mat4(transpose(inverse(mat4(1.0)))) * normals;
    gl_Position = pos * v_Matrix;
}