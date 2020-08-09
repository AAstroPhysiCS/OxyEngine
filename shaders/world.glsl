//#type fragment
#version 460 core

layout(location = 0) out vec4 color;

uniform vec4 colorInput;

in vec2 texCoordsOut;
in float textureSlotOut;
in vec4 colorOut;
in vec4 normalsOut;
in vec4 modelMatrixPosOut;

uniform sampler2D tex[32];
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

uniform Material material;
uniform PointLight p_Light;

vec3 calcAmbient(vec3 ambient){
    return p_Light.diffuse * ambient;
}

vec3 calcDiffuse(vec3 lightDir, vec3 diffuse){
    vec3 norm = vec3(normalize(normalsOut));
    float diff = max(dot(norm, lightDir), 0.0);
    return p_Light.diffuse * (diff * diffuse);
}

vec3 calcSpecular(vec3 lightDir, vec3 specular){
    vec3 viewDir = normalize(cameraPos - vec3(modelMatrixPosOut));
    vec3 reflectDir = reflect(-lightDir, vec3(normalize(normalsOut)));
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), material.reflectance);
    return p_Light.diffuse * (spec * specular);
}

void main(){

    vec3 lightDir = normalize(p_Light.position - vec3(modelMatrixPosOut));
    //TODO: Summarize
    int index = int(round(textureSlotOut));
    if (index == 0){
        vec3 ambient = calcAmbient(material.ambient);
        vec3 diffuse = calcDiffuse(lightDir, material.diffuse);
        vec3 specular = calcSpecular(lightDir, material.specular);

        float distance = length(p_Light.position - vec3(modelMatrixPosOut));
        float attenuation = 1.0 / (p_Light.constant + p_Light.linear * distance + p_Light.quadratic * (distance * distance));

        ambient *= attenuation;
        diffuse *= attenuation;
        specular *= attenuation;

        vec3 result = specular + diffuse + ambient;

        color = vec4(result, 1.0f) * colorOut;
    }
    else {
        vec3 ambient = calcAmbient(texture(tex[index], texCoordsOut).rgb);
        vec3 diffuse = calcDiffuse(lightDir, texture(tex[index], texCoordsOut).rgb);
        vec3 specular = calcSpecular(lightDir, texture(tex[index], texCoordsOut).rgb);

        float distance = length(p_Light.position - vec3(modelMatrixPosOut));
        float attenuation = 1.0 / (p_Light.constant + p_Light.linear * distance + p_Light.quadratic * (distance * distance));

        ambient *= attenuation;
        diffuse *= attenuation;
        specular *= attenuation;

        vec3 result = specular + diffuse + ambient;

        color = vec4(result, 1.0f);
    }
}

    //#type vertex
    #version 460 core

layout(location = 0) in vec4 pos;
layout(location = 1) in vec2 tcs;
layout(location = 2) in float textureSlot;
layout(location = 3) in vec4 color;
layout(location = 4) in vec4 normals;

layout(location = 5) uniform mat4 v_Matrix;
layout(location = 6) uniform mat4 m_Matrix;
layout(location = 7) uniform mat4 pr_Matrix;

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