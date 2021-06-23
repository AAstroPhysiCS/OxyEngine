//#type fragment
#version 460 core

#define NUMBER_CASCADES 4 //TODO: convert this and many things into uniform buffer objects

layout(location = 0) out vec4 color;
layout(location = 1) out int o_IDBuffer;

in OUT_VARIABLES {
    vec2 texCoordsOut;
    vec3 normalsOut;
    vec3 vertexPos;

    vec4 lightSpacePos[NUMBER_CASCADES];

    mat3 TBN;
} inVar;

uniform sampler2D tex[32];
uniform samplerCube skyBoxTexture;
uniform vec3 cameraPos;

flat in int v_ObjectID;

struct Material {
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

    int activeState;
};

struct DirectionalLight{
    vec3 direction;
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;

    int activeState;
};

uniform Material material;
uniform PointLight p_Light[4];
uniform DirectionalLight d_Light[4];

//NORMAL MAP
uniform int normalMapSlot = -1;
uniform float normalMapStrength;
vec3 normalMap;

//GAMMA
uniform float gamma;

//PBR
uniform int albedoMapSlot;
uniform int metallicSlot;
uniform float metallicStrength;
uniform int roughnessSlot;
uniform float roughnessStrength;
uniform int aoSlot;
uniform float aoStrength;
uniform int emissiveSlot;
uniform float emissiveStrength;

//PBR FILTERING
uniform samplerCube prefilterMap;
uniform sampler2D brdfLUT;

//irradiance
uniform samplerCube iblMap;

//hdr
uniform float exposure;
uniform float hdrIntensity;

//shadows
uniform sampler2D shadowMap[NUMBER_CASCADES];
uniform vec3 lightShadowDirPos[NUMBER_CASCADES];
uniform float cascadeSplits[NUMBER_CASCADES];
in float clipSpacePosZ;
uniform int castShadows;
uniform int cascadeIndicatorToggle;

#define PI 3.14159265358979323

float DistributionGGX(vec3 N, vec3 H, float roughness) {
    float a = roughness*roughness;
    float a2 = a*a;
    float NdotH = max(dot(N, H), 0.0);
    float NdotH2 = NdotH*NdotH;

    float nom   = a2;
    float denom = (NdotH2 * (a2 - 1.0) + 1.0);
    denom = PI * denom * denom;

    return nom / max(denom, 0.001); // prevent divide by zero for roughness=0.0 and NdotH=1.0
}

float GeometrySchlickGGX(float NdotV, float roughness) {
    float r = (roughness + 1.0);
    float k = (r*r) / 8.0;

    float nom   = NdotV;
    float denom = NdotV * (1.0 - k) + k;

    return nom / denom;
}

float GeometrySmith(vec3 N, vec3 V, vec3 L, float roughness) {
    float NdotV = max(dot(N, V), 0.0);
    float NdotL = max(dot(N, L), 0.0);
    float ggx2 = GeometrySchlickGGX(NdotV, roughness);
    float ggx1 = GeometrySchlickGGX(NdotL, roughness);

    return ggx1 * ggx2;
}

vec3 fresnelSchlick(float cosTheta, vec3 F0) {
    return F0 + (1.0 - F0) * pow(1.0 - cosTheta, 5.0);
}

vec3 fresnelSchlickRoughness(float cosTheta, vec3 F0, float roughness) {
    return F0 + (max(vec3(1.0 - roughness), F0) - F0) * pow(1.0 - cosTheta, 5.0);
}

vec3 calcPBR(vec3 L, vec3 lightDiffuseColor, vec3 N, vec3 V, vec3 vertexPos, vec3 F0, vec3 albedo, float roughness, float metallic, float attenuation){
     //calculate per-light radiance
     vec3 H = normalize(V + L);
     vec3 radiance = lightDiffuseColor * attenuation;

     // Cook-Torrance BRDF
     float NDF = DistributionGGX(N, H, roughness);
     float G   = GeometrySmith(N, V, L, roughness);
     vec3 F    = fresnelSchlick(max(dot(H, V), 0.0), F0);

     vec3 nominator    = NDF * G * F;
     float denominator = 4 * max(dot(N, V), 0.0) * max(dot(N, L), 0.0) + 0.001;
     vec3 specular     = nominator / denominator;

     vec3 kS = F;
     vec3 kD = vec3(1.0) - kS;

     kD *= 1.0 - metallic;

     float NdotL = max(dot(N, L), 0.0);

     return (kD * albedo / PI + specular) * radiance * NdotL;
}

float ShadowCalculation(vec3 norm, vec4 lightSpacePos, vec3 lightDir, int index)
{
    vec3 projCoords = lightSpacePos.xyz / lightSpacePos.w;

    projCoords = projCoords * 0.5 + 0.5;

    float closestDepth = texture(shadowMap[index], projCoords.xy).r;

    float currentDepth = projCoords.z;

    vec3 normal = norm;
    float bias = max(0.005 * (1.0 - dot(normal, lightDir)), 0.003);

    float shadow = 0.0;
    vec2 texelSize = 1.0 / textureSize(shadowMap[index], 0);

    int pcfSample = 4;

    for(int x = -pcfSample; x <= pcfSample; ++x)
    {
        for(int y = -pcfSample; y <= pcfSample; ++y)
        {
            float pcfDepth = texture(shadowMap[index], projCoords.xy + vec2(x, y) * texelSize).r;
            shadow += currentDepth - bias > pcfDepth ? 1.0 : 0.0;
        }
    }

    shadow /= pow((pcfSample * 2.0 + 1.0), 2);

    if(projCoords.z > 1.0)
        shadow = 0.0;

    return shadow;
}

vec4 startPBR(vec3 vertexPos, vec2 texCoordsOut, vec3 viewDir, vec3 norm){
    const float MAX_REFLECTION_LOD = 4.0;
    vec3 albedo, emissive;
    float metallicMap, roughnessMap, aoMap;
    float alpha = 1.0f;

    if(metallicSlot == 0){
       metallicMap = metallicStrength;
    } else {
       metallicMap = texture(tex[metallicSlot], inVar.texCoordsOut).r;
    }

    if(roughnessSlot == 0){
        roughnessMap = roughnessStrength;
    } else {
        roughnessMap = texture(tex[roughnessSlot], inVar.texCoordsOut).r;
    }

    if(aoSlot == 0){
       aoMap = aoStrength;
    } else {
       aoMap = texture(tex[aoSlot], inVar.texCoordsOut).r;
    }

    if(albedoMapSlot == 0){
       albedo = pow(vec3(material.diffuse), vec3(gamma));
    } else {
       albedo = pow(texture(tex[albedoMapSlot], texCoordsOut).rgb, vec3(gamma));
       alpha = texture(tex[albedoMapSlot], texCoordsOut).w;
    }
    if(alpha < 0.1f) discard;

    if(emissiveSlot != 0)
        emissive = texture(tex[emissiveSlot], inVar.texCoordsOut).rgb * emissiveStrength;

    vec3 Lo = vec3(0f);
    vec3 F0 = vec3(0.04);

    vec4 cascadeIndicator = vec4(0.0f, 0.0f, 0.0f, 0.0f);

    F0 = mix(F0, albedo, metallicMap);

    for(int i = 0; i < d_Light.length; i++){
        if(d_Light[i].activeState == 0) continue;
        vec3 lightDir = d_Light[i].direction;

        float shadowCalc = 0.0f;

        if(bool(castShadows)){
            for(int j = 0; j < NUMBER_CASCADES; j++) {
                if(clipSpacePosZ <= cascadeSplits[j]) {
                   shadowCalc = ShadowCalculation(norm, inVar.lightSpacePos[j], lightDir, j);
                   if (j == 0)
                       cascadeIndicator = vec4(0.1, 0.0, 0.0, 0.0);
                   else if (j == 1)
                       cascadeIndicator = vec4(0.0, 0.1, 0.0, 0.0);
                   else if (j == 2)
                       cascadeIndicator = vec4(0.0, 0.0, 0.1, 0.0);
                   else if(j == 3)
                       cascadeIndicator = vec4(0.0, 0.5, 0.5, 0.0);
                   break;
                }
            }
        }

        Lo += calcPBR(lightDir, d_Light[i].diffuse, norm, viewDir, vertexPos, F0, albedo, roughnessMap, metallicMap, 1.0) * (1.0 - shadowCalc);
    }

    for(int i = 0; i < p_Light.length; i++) {
        if(p_Light[i].activeState == 0) continue;
        vec3 lightPos = p_Light[i].position;

        vec3 lightDir = normalize(lightPos - vertexPos);
        float distance = length(lightPos - vertexPos);
        float attenuation = 1.0 / (p_Light[i].constant + p_Light[i].linear * distance + p_Light[i].quadratic * (distance * distance));

        Lo += calcPBR(lightDir, p_Light[i].diffuse, norm, viewDir, vertexPos, F0, albedo, roughnessMap, metallicMap, attenuation);
    }

    vec3 IBL = texture(iblMap, norm).rgb;
    if(IBL.rgb == vec3(0.0f, 0.0f, 0.0f)) IBL = vec3(0.05f, 0.05f, 0.05f);

    vec3 kS = fresnelSchlickRoughness(max(dot(norm, viewDir), 0.0), F0, roughnessMap);
    vec3 kD = 1.0 - kS;
    kD *= 1.0 - metallicMap;

    vec3 diffuseMap = albedo * IBL;

    vec3 R = reflect(-viewDir, norm);
    vec3 prefilteredColor = textureLod(prefilterMap, R, roughnessMap * MAX_REFLECTION_LOD).rgb;
    vec2 envBRDF = texture(brdfLUT, vec2(max(dot(norm, viewDir), 0.0), roughnessMap)).rg;
    vec3 specular = prefilteredColor * (kS * envBRDF.x + envBRDF.y);
    vec3 ambient = (kD * diffuseMap + specular) * aoMap * hdrIntensity;

    vec3 result = ambient + Lo + emissive;
    //result = result / (result + vec3(1.0));
    result = vec3(1.0) - exp(-result * exposure);
    result = pow(result, vec3(1f / gamma));

    if(bool(cascadeIndicatorToggle))
        result += cascadeIndicator.xyz;

    return vec4(result, alpha);
}

void main(){

    o_IDBuffer = v_ObjectID;

    vec3 vertexPos = inVar.vertexPos;
    vec2 texCoordsOut = inVar.texCoordsOut;

    vec3 viewDir = normalize(cameraPos - vertexPos);

    vec3 norm;
    if(normalMapSlot != 0 && normalMapStrength != 0.0f){
        vec3 normalMap = texture(tex[normalMapSlot], texCoordsOut).xyz * 2.0 - 1.0;
        normalMap.xy *= normalMapStrength;
        norm = normalize(inVar.TBN * normalMap);
    } else {
        norm = vec3(normalize(inVar.normalsOut));
    }

    vec4 result = startPBR(vertexPos, texCoordsOut, viewDir, norm);
    color = vec4(result.xyz, 1.0f);
}

//#type vertex
#version 460 core

layout(location = 0) in vec3 pos;
layout(location = 1) in vec2 tcs;
layout(location = 2) in vec3 normals;
layout(location = 3) in vec3 biTangent;
layout(location = 4) in vec3 tangent;
layout(location = 5) in float objectID;
layout(location = 6) in vec4 boneIds;
layout(location = 7) in vec4 weights;

uniform mat4 v_Matrix;
uniform mat4 model;

#define NUMBER_CASCADES 4

uniform mat4 lightSpaceMatrix[NUMBER_CASCADES];

flat out int v_ObjectID;
out float clipSpacePosZ;

out OUT_VARIABLES {
    vec2 texCoordsOut;
    vec3 normalsOut;
    vec3 vertexPos;

    vec4 lightSpacePos[NUMBER_CASCADES];

    //NORMAL MAPPING
    mat3 TBN;
} outVar;

const int MAX_BONES = 100;
uniform mat4 finalBonesMatrices[MAX_BONES];
uniform int animatedModel;

void main(){

    v_ObjectID = int(objectID);
    outVar.texCoordsOut = tcs;

    ivec4 boneIDInt = ivec4(boneIds);
    vec4 totalPos = vec4(pos, 1.0f);
    vec4 totalNorm = vec4(normals, 1.0f);

    if(bool(animatedModel)){
        //mesh has animations
        mat4 transform = finalBonesMatrices[boneIDInt[0]] * weights[0];
             transform += finalBonesMatrices[boneIDInt[1]] * weights[1];
             transform += finalBonesMatrices[boneIDInt[2]] * weights[2];
             transform += finalBonesMatrices[boneIDInt[3]] * weights[3];

        totalPos = transform * vec4(pos, 1.0f);
        totalNorm = transform * vec4(normals, 0.0f);
    }

    outVar.vertexPos = (model * totalPos).xyz;
    outVar.normalsOut = mat3(model) * totalNorm.xyz;

    for(int i = 0; i < NUMBER_CASCADES; i++){
        outVar.lightSpacePos[i] = lightSpaceMatrix[i] * model * vec4(pos, 1.0f);
    }

    vec3 T = normalize(mat3(model) * tangent);
    vec3 B = normalize(mat3(model) * biTangent);
    vec3 N = normalize(mat3(model) * totalNorm.xyz);

    mat3 TBN = mat3(T, B, N);
    outVar.TBN = TBN;

    vec4 modelPos = model * totalPos;
    gl_Position = modelPos * v_Matrix;
    clipSpacePosZ = gl_Position.z;
}