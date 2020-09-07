//#type fragment
#version 460 core

layout(location = 0) out vec4 color;

in OUT_VARIABLES {
    vec2 texCoordsOut;
    float textureSlotOut;
    vec4 colorOut;
    vec4 normalsOut;
    vec4 modelMatrixPosOut;
} inVar;

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
    vec3 norm = vec3(normalize(inVar.normalsOut));
    float diff = max(dot(norm, lightDir), 0.0);
    return lightDiffuse * (diff * diffuse);
}

vec3 calcSpecular(vec3 lightDir, vec3 specular, vec3 lightSpecular){
    vec3 viewDir = normalize(cameraPos - vec3(inVar.modelMatrixPosOut));
    vec3 reflectDir = reflect(-lightDir, vec3(normalize(inVar.normalsOut)));
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), material.reflectance);
    return lightSpecular * (spec * specular);
}

void calcPointLightImpl(vec3 I, vec3 R){
    vec3 lightDir = normalize(p_Light.position - vec3(inVar.modelMatrixPosOut));
    int index = int(round(inVar.textureSlotOut));
    if (index == 0){
        vec3 ambient = calcAmbient(material.ambient, p_Light.ambient);
        vec3 diffuse = calcDiffuse(lightDir, material.diffuse, p_Light.diffuse);
        vec3 specular = calcSpecular(lightDir, material.specular, p_Light.specular);

        float distance = length(p_Light.position - vec3(inVar.modelMatrixPosOut));
        float attenuation = 1.0 / (p_Light.constant + p_Light.linear * distance + p_Light.quadratic * (distance * distance));
        ambient *= attenuation;
        diffuse *= attenuation;
        specular *= attenuation;
        vec3 result = specular + diffuse + ambient;

        color = vec4(result, 1.0f) * inVar.colorOut * texture(skyBoxTexture, R);
    }
    else {
        vec3 ambient = calcAmbient(texture(tex[index], inVar.texCoordsOut).rgb, p_Light.ambient);
        vec3 diffuse = calcDiffuse(lightDir, texture(tex[index], inVar.texCoordsOut).rgb, p_Light.diffuse);
        vec3 specular = calcSpecular(lightDir, texture(tex[index], inVar.texCoordsOut).rgb, p_Light.specular);

        float distance = length(p_Light.position - vec3(inVar.modelMatrixPosOut));
        float attenuation = 1.0 / (p_Light.constant + p_Light.linear * distance + p_Light.quadratic * (distance * distance));
        ambient *= attenuation;
        diffuse *= attenuation;
        specular *= attenuation;
        vec3 result = specular + diffuse + ambient;

        color = vec4(result, 1.0f) * texture(skyBoxTexture, R);
    }
}

void calcDirectionalLightImpl(vec3 I, vec3 R){
    vec3 lightDir = normalize(-d_Light.direction);
    int index = int(round(inVar.textureSlotOut));
    vec3 result, ambient, diffuse, specular;
    if (index == 0){
        ambient = calcAmbient(material.ambient, d_Light.ambient);
        diffuse = calcDiffuse(lightDir, material.diffuse, d_Light.diffuse);
        specular = calcSpecular(lightDir, material.specular, d_Light.specular);
        result = specular + diffuse + ambient;

        color = vec4(result, 1.0f) * inVar.colorOut * texture(skyBoxTexture, R);
    }
    else {
        ambient = calcAmbient(texture(tex[index], inVar.texCoordsOut).rgb, d_Light.ambient);
        diffuse = calcDiffuse(lightDir, texture(tex[index], inVar.texCoordsOut).rgb, d_Light.diffuse);
        specular = calcSpecular(lightDir, texture(tex[index], inVar.texCoordsOut).rgb, d_Light.specular);
        result = specular + diffuse + ambient;

        color = vec4(result, 1.0f) * texture(skyBoxTexture, R);
    }
}

void main(){
    vec3 I = normalize(vec3(inVar.modelMatrixPosOut) - cameraPos);
    vec3 R = reflect(I, normalize(vec3(inVar.normalsOut)));

    if (currentLightIndex == 0){
        calcPointLightImpl(I, R);
    } else if (currentLightIndex == 1){
        calcDirectionalLightImpl(I, R);
    } else {
        int index = int(round(inVar.textureSlotOut));
        if (index == 0){
            color = inVar.colorOut * texture(skyBoxTexture, R);
        }
        else {
            color = texture(tex[index], inVar.texCoordsOut) * texture(skyBoxTexture, R);
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
layout(location = 5) in vec4 tangent;
layout(location = 6) in vec4 biTangent;

uniform mat4 v_Matrix;

out OUT_VARIABLES {
    vec2 texCoordsOut;
    float textureSlotOut;
    vec4 colorOut;
    vec4 normalsOut;
    vec4 modelMatrixPosOut;
} outVar;

void main(){
    outVar.textureSlotOut = textureSlot;
    outVar.texCoordsOut = tcs;
    outVar.colorOut = color;
    outVar.modelMatrixPosOut = 1.0 * pos;
    outVar.normalsOut = mat4(transpose(inverse(mat4(1.0)))) * normals;
    gl_Position = pos * v_Matrix;
}