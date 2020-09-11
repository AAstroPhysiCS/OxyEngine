//#type fragment
#version 460 core

layout(location = 0) out vec4 color;

in OUT_VARIABLES {
    vec2 texCoordsOut;
    float textureSlotOut;
    vec4 colorOut;
    vec4 normalsOut;
    vec3 lightModelNormal;
    vec3 vertexPos;

    //NORMAL MAPPING
    mat3 TBN;
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

//NORMAL MAP
uniform int normalMapSlot = -1;
uniform float normalMapStrength;
vec3 normalMap;
vec3 tangentLightPos, tangentViewPos, tangentVertexPos;

vec3 calcAmbient(vec3 ambient, vec3 lightAmbient){
    return lightAmbient * ambient;
}

vec3 calcDiffuse(vec3 lightDir, vec3 diffuse, vec3 lightDiffuse, vec3 norm){
    float diff = max(dot(lightDir, norm), 0.0);
    return lightDiffuse * (diff * diffuse);
}

vec3 calcSpecular(vec3 lightDir, vec3 viewDir, vec3 specular, vec3 lightSpecular, vec3 norm){
    vec3 reflectDir = reflect(-lightDir, norm);
    vec3 halfwayDir = normalize(lightDir + viewDir);
    float spec = pow(max(dot(norm, halfwayDir), 0.0), material.reflectance);
    return lightSpecular * (spec * specular);
}

void calcPointLightImpl(PointLight p_Light, vec3 I, vec3 R){

    vec3 vertexPos = inVar.vertexPos;
    vec3 lightPos = p_Light.position;
    vec3 cameraPosVec3 = cameraPos;

    if(normalMapSlot != 0){
        vertexPos = inVar.TBN * inVar.vertexPos;
        lightPos = inVar.TBN * p_Light.position;
        cameraPosVec3 = inVar.TBN * cameraPos;
    }

    vec3 lightDir = normalize(lightPos - vertexPos);
    vec3 viewDir = normalize(cameraPosVec3 - vertexPos);

    vec3 norm;
    //NORMAL MAP
    if(normalMapSlot != 0){
        vec3 normalMap = texture(tex[normalMapSlot], inVar.texCoordsOut).rgb;
        normalMap = normalMap * 2.0 - 1.0;
        normalMap.xy *= normalMapStrength;
        normalMap = normalize(normalMap);
        norm = normalMap;
    } else {
        norm = vec3(normalize(inVar.lightModelNormal));
    }

    int index = int(round(inVar.textureSlotOut));
    float distance = length(p_Light.position - inVar.vertexPos);
    float attenuation = 1.0 / (p_Light.constant + p_Light.linear * distance + p_Light.quadratic * (distance * distance));
    if (index == 0){ //color
        vec3 ambient = calcAmbient(material.ambient, p_Light.ambient);
        vec3 diffuse = calcDiffuse(lightDir, material.diffuse, p_Light.diffuse, norm);
        vec3 specular = calcSpecular(lightDir, viewDir, material.specular, p_Light.specular, norm);

        ambient *= attenuation;
        diffuse *= attenuation;
        specular *= attenuation;
        vec3 result = specular + diffuse + ambient;

        color = vec4(result, 1.0f) * inVar.colorOut * texture(skyBoxTexture, R);
    }
    else { //texture
        vec3 ambient = calcAmbient(texture(tex[index], inVar.texCoordsOut).rgb, p_Light.ambient);
        vec3 diffuse = calcDiffuse(lightDir, texture(tex[index], inVar.texCoordsOut).rgb, p_Light.diffuse, norm);
        vec3 specular = calcSpecular(lightDir, viewDir, texture(tex[index], inVar.texCoordsOut).rgb, p_Light.specular, norm);

        ambient *= attenuation;
        diffuse *= attenuation;
        specular *= attenuation;
        vec3 result = specular + diffuse + ambient;

        color = vec4(result, 1.0f) * texture(skyBoxTexture, R);
    }
}

void calcDirectionalLightImpl(DirectionalLight d_Light, vec3 I, vec3 R){
    vec3 lightDir = normalize(-d_Light.direction);
    vec3 viewDir = normalize(cameraPos - vec3(inVar.vertexPos));
    int index = int(round(inVar.textureSlotOut));

    vec3 norm;
    //NORMAL MAP
    if(normalMapSlot != 0) {
        vec3 normalMap = texture(tex[normalMapSlot], inVar.texCoordsOut).rgb;
        normalMap = normalize(normalMap * 2.0 - 1.0);
        norm = normalMap;
    } else {
        norm = vec3(normalize(inVar.lightModelNormal));
    }

    vec3 result, ambient, diffuse, specular;
    if (index == 0){
        ambient = calcAmbient(material.ambient, d_Light.ambient);
        diffuse = calcDiffuse(lightDir, material.diffuse, d_Light.diffuse, norm);
        specular = calcSpecular(lightDir,viewDir, material.specular, d_Light.specular, norm);
        result = specular + diffuse + ambient;

        color = vec4(result, 1.0f) * inVar.colorOut * texture(skyBoxTexture, R);
    }
    else {
        ambient = calcAmbient(texture(tex[index], inVar.texCoordsOut).rgb, d_Light.ambient);
        diffuse = calcDiffuse(lightDir, texture(tex[index], inVar.texCoordsOut).rgb, d_Light.diffuse, norm);
        specular = calcSpecular(lightDir, viewDir, texture(tex[index], inVar.texCoordsOut).rgb, d_Light.specular, norm);
        result = specular + diffuse + ambient;

        color = vec4(result, 1.0f) * texture(skyBoxTexture, R);
    }
}

void main(){
    vec3 I = normalize(vec3(inVar.vertexPos) - cameraPos);
    vec3 R = reflect(I, normalize(vec3(inVar.normalsOut)));

    if (currentLightIndex == 0){
        calcPointLightImpl(p_Light, I, R);
    } else if (currentLightIndex == 1){
        calcDirectionalLightImpl(d_Light, I, R); //TODO: DO DIRECTIONAL LIGHT WITH NORMAL MAPPING
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
layout(location = 5) in vec4 biTangent;
layout(location = 6) in vec4 tangent;

uniform mat4 v_Matrix;
uniform mat4 model;

out OUT_VARIABLES {
    vec2 texCoordsOut;
    float textureSlotOut;
    vec4 colorOut;
    vec4 normalsOut;
    vec3 lightModelNormal;
    vec3 vertexPos;

    //NORMAL MAPPING
    mat3 TBN;
} outVar;

void main(){
    outVar.textureSlotOut = textureSlot;
    outVar.texCoordsOut = tcs;
    outVar.colorOut = color;

    outVar.vertexPos = pos.xyz;
    outVar.normalsOut = mat4(transpose(inverse(model))) * normals;
    outVar.lightModelNormal = normalize(model * vec4(normals.xyz, 0.0)).xyz;

    mat4 normalMatrix = transpose(inverse(model));
    vec3 T = normalize(vec3(model * vec4(tangent.xyz, 0.0)));
    vec3 B = normalize(vec3(model * vec4(biTangent.xyz, 0.0)));
    vec3 N = normalize(vec3(model * vec4(normals.xyz, 0.0)));

    mat3 TBN = transpose(mat3(T, B, N));
    outVar.TBN = TBN;

    gl_Position = pos * v_Matrix;
}