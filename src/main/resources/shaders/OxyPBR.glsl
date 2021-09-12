//#type vertex
#version 450 core

layout(location = 0) in vec3 pos;
layout(location = 1) in vec2 tcs;
layout(location = 2) in vec3 normals;
layout(location = 3) in vec3 biTangent;
layout(location = 4) in vec3 tangent;
layout(location = 5) in float objectID;
layout(location = 6) in vec4 boneIds;
layout(location = 7) in vec4 weights;

#define NUMBER_CASCADES 4
#define MAX_BONES 100

layout (std140, binding = 0) uniform Camera {
    mat4 v_Matrix;
    mat4 v_Matrix_NoTransform;
    vec3 cameraPos;
};

struct RendererTransforms {
    mat4 model;
};

struct RendererVertexOutput {
    vec2 texCoords;
    vec3 normals;
    vec3 vertexPos;

    vec4 lightSpacePos[NUMBER_CASCADES];

    mat3 TBN;
};

struct RendererAnimation {
    mat4 finalBonesMatrices[MAX_BONES];
    int animatedModel;
};

out RendererVertexOutput Output;
uniform RendererTransforms Transforms;
uniform RendererAnimation Animation;

uniform mat4 lightSpaceMatrix[NUMBER_CASCADES];

flat out int v_ObjectID;
out float clipSpacePosZ;

void main(){

    v_ObjectID = int(objectID);
    Output.texCoords = tcs;

    ivec4 boneIDInt = ivec4(boneIds);
    vec4 totalPos = vec4(pos, 1.0f);
    vec4 totalNorm = vec4(normals, 1.0f);

    if(bool(Animation.animatedModel)){
        //mesh has animations
        mat4 transform = Animation.finalBonesMatrices[boneIDInt[0]] * weights[0];
             transform += Animation.finalBonesMatrices[boneIDInt[1]] * weights[1];
             transform += Animation.finalBonesMatrices[boneIDInt[2]] * weights[2];
             transform += Animation.finalBonesMatrices[boneIDInt[3]] * weights[3];

        totalPos = transform * vec4(pos, 1.0f);
        totalNorm = transform * vec4(normals, 0.0f);
    }

    Output.vertexPos = (Transforms.model * totalPos).xyz;
    Output.normals = mat3(Transforms.model) * totalNorm.xyz;

    for(int i = 0; i < NUMBER_CASCADES; i++){
        Output.lightSpacePos[i] = lightSpaceMatrix[i] * Transforms.model * vec4(pos, 1.0f);
    }

    vec3 T = normalize(mat3(Transforms.model) * tangent);
    vec3 B = normalize(mat3(Transforms.model) * biTangent);
    vec3 N = normalize(mat3(Transforms.model) * totalNorm.xyz);

    mat3 TBN = mat3(T, B, N);
    Output.TBN = TBN;

    gl_Position = v_Matrix * Transforms.model * totalPos;
    clipSpacePosZ = gl_Position.z;
}

//#type fragment
#version 450 core

layout(location = 0) out vec4 color;
layout(location = 1) out int o_IDBuffer;

#define NUMBER_CASCADES 4
#define MAX_REFLECTION_LOD 4.0

struct RendererVertexOutput {
    vec2 texCoords;
    vec3 normals;
    vec3 vertexPos;

    vec4 lightSpacePos[NUMBER_CASCADES];

    mat3 TBN;
};

struct RendererMaterial {
    vec4 diffuse;

    //PBR
    int albedoMapSlot;
    int metallicSlot;
    float metallicStrength;
    int roughnessSlot;
    float roughnessStrength;
    int aoSlot;
    float aoStrength;
    int emissiveSlot;
    float emissiveStrength;
    int normalMapSlot;
    float normalMapStrength;
};

struct PointLight {
    vec3 position;
    vec3 diffuse;

    float constant;
    float linear;
    float quadratic;

    int activeState;
};

struct DirectionalLight {
    vec3 direction;
    vec3 diffuse;

    int activeState;
};

struct RendererEnvironmentTextures {
    samplerCube skyBoxTexture;
    samplerCube prefilterMap;
    sampler2D brdfLUT;
    samplerCube iblMap;
};

struct RendererShadows {
    sampler2D shadowMap[NUMBER_CASCADES];
    vec3 lightShadowDirPos[NUMBER_CASCADES];
    float cascadeSplits[NUMBER_CASCADES];
    int castShadows;
    int cascadeIndicatorToggle;
};

layout (std140, binding = 0) uniform Camera {
    mat4 v_Matrix;
    mat4 v_Matrix_NoTransform;
    vec3 cameraPos;
};

layout (std140, binding = 1) uniform EnvironmentSettings {
    float gamma;
    float exposure;
    float hdrIntensity;
};

flat in int v_ObjectID;
in float clipSpacePosZ;
in RendererVertexOutput Output;

uniform RendererMaterial Material;
uniform PointLight p_Light[4];
uniform DirectionalLight d_Light[4];
uniform RendererShadows Shadows;
uniform RendererEnvironmentTextures EnvironmentTex;

uniform sampler2D tex[32];

#define PI 3.14159265358979323

float DistributionGGX(vec3 N, vec3 H, float roughness);
float GeometrySchlickGGX(float NdotV, float roughness);
float GeometrySmith(vec3 N, vec3 V, vec3 L, float roughness);
vec3 fresnelSchlick(float cosTheta, vec3 F0);
vec3 fresnelSchlickRoughness(float cosTheta, vec3 F0, float roughness);
vec3 calcPBR(vec3 L, vec3 lightDiffuseColor, vec3 N, vec3 V, vec3 vertexPos, vec3 F0, vec3 albedo, float roughness, float metallic, float attenuation);
float ShadowCalculation(vec3 norm, vec4 lightSpacePos, vec3 lightDir, int index);

vec4 startPBR(vec3 vertexPos, vec2 texCoords, vec3 viewDir, vec3 norm){
    vec3 albedo, emissive;
    float metallicMap, roughnessMap, aoMap;
    float alpha = 1.0f;

    if(Material.metallicSlot == 0){
       metallicMap = Material.metallicStrength;
    } else {
       metallicMap = texture(tex[Material.metallicSlot], Output.texCoords).r;
    }

    if(Material.roughnessSlot == 0){
        roughnessMap = Material.roughnessStrength;
    } else {
        roughnessMap = texture(tex[Material.roughnessSlot], Output.texCoords).r;
    }

    if(Material.aoSlot == 0){
       aoMap = Material.aoStrength;
    } else {
       aoMap = texture(tex[Material.aoSlot], Output.texCoords).r;
    }

    if(Material.albedoMapSlot == 0){
       albedo = pow(vec3(Material.diffuse), vec3(gamma));
    } else {
       albedo = pow(texture(tex[Material.albedoMapSlot], Output.texCoords).rgb, vec3(gamma));
       alpha = texture(tex[Material.albedoMapSlot], Output.texCoords).w;
    }
    if(alpha < 0.5f) discard;

    if(Material.emissiveSlot != 0)
        emissive = texture(tex[Material.emissiveSlot], Output.texCoords).rgb * Material.emissiveStrength;

    vec3 Lo = vec3(0f);
    vec3 F0 = vec3(0.04);

    vec4 cascadeIndicator = vec4(0.0f, 0.0f, 0.0f, 0.0f);

    F0 = mix(F0, albedo, metallicMap);

    for(int i = 0; i < d_Light.length; i++){
        if(d_Light[i].activeState == 0) continue;
        vec3 lightDir = d_Light[i].direction;

        float shadowCalc = 0.0f;

        if(bool(Shadows.castShadows)){
            for(int j = 0; j < NUMBER_CASCADES; j++) {
                if(clipSpacePosZ <= Shadows.cascadeSplits[j]) {
                   shadowCalc = ShadowCalculation(norm, Output.lightSpacePos[j], lightDir, j);
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

        Lo += calcPBR(lightDir, d_Light[i].diffuse, norm, viewDir, Output.vertexPos, F0, albedo, roughnessMap, metallicMap, 1.0) * (1.0 - shadowCalc);
    }

    for(int i = 0; i < p_Light.length; i++) {
        if(p_Light[i].activeState == 0) continue;
        vec3 lightPos = p_Light[i].position;

        vec3 lightDir = normalize(lightPos - vertexPos);
        float distance = length(lightPos - vertexPos);
        float attenuation = 1.0 / (p_Light[i].constant + p_Light[i].linear * distance + p_Light[i].quadratic * (distance * distance));

        Lo += calcPBR(lightDir, p_Light[i].diffuse, norm, viewDir, Output.vertexPos, F0, albedo, roughnessMap, metallicMap, attenuation);
    }

    vec3 IBL = texture(EnvironmentTex.iblMap, norm).rgb;
    if(IBL.rgb == vec3(0.0f, 0.0f, 0.0f)) IBL = vec3(0.05f, 0.05f, 0.05f);

    vec3 kS = fresnelSchlickRoughness(max(dot(norm, viewDir), 0.0), F0, roughnessMap);
    vec3 kD = 1.0 - kS;
    kD *= 1.0 - metallicMap;

    vec3 diffuseMap = albedo * IBL;

    vec3 R = reflect(-viewDir, norm);
    vec3 prefilteredColor = textureLod(EnvironmentTex.prefilterMap, R, roughnessMap * MAX_REFLECTION_LOD).rgb;
    vec2 envBRDF = texture(EnvironmentTex.brdfLUT, vec2(max(dot(norm, viewDir), 0.0), roughnessMap)).rg;
    vec3 specular = prefilteredColor * (kS * envBRDF.x + envBRDF.y);
    vec3 ambient = (kD * diffuseMap + specular) * aoMap * hdrIntensity;

    vec3 result = ambient + Lo + emissive;
    result = result / (result + vec3(1.0));
    result = vec3(1.0) - exp(-result * exposure);
    result = pow(result, vec3(1f / gamma));

    if(bool(Shadows.cascadeIndicatorToggle))
        result += cascadeIndicator.xyz;

    return vec4(result, alpha);
}

void main() {

    o_IDBuffer = v_ObjectID;

    vec3 vertexPos = Output.vertexPos;
    vec2 texCoords = Output.texCoords;

    vec3 viewDir = normalize(cameraPos - vertexPos);

    vec3 norm;
    if(Material.normalMapSlot != 0 && Material.normalMapStrength != 0.0f){
        vec3 normalMap = texture(tex[Material.normalMapSlot], Output.texCoords).xyz * 2.0 - 1.0;
        normalMap.xy *= Material.normalMapStrength;
        norm = normalize(Output.TBN * normalMap);
    } else {
        norm = vec3(normalize(Output.normals));
    }

    vec4 result = startPBR(vertexPos, texCoords, viewDir, norm);
    color = result;
}

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

    float closestDepth = texture(Shadows.shadowMap[index], projCoords.xy).r;

    float currentDepth = projCoords.z;

    vec3 normal = norm;
    float bias = max(0.005 * (1.0 - dot(normal, lightDir)), 0.002);

    float shadow = 0.0;
    vec2 texelSize = 1.0 / textureSize(Shadows.shadowMap[index], 0);

    int pcfSample = 4;

    for(int x = -pcfSample; x <= pcfSample; ++x)
    {
        for(int y = -pcfSample; y <= pcfSample; ++y)
        {
            float pcfDepth = texture(Shadows.shadowMap[index], projCoords.xy + vec2(x, y) * texelSize).r;
            shadow += currentDepth - bias > pcfDepth ? 1.0 : 0.0;
        }
    }

    shadow /= pow((pcfSample * 2.0 + 1.0), 2);

    if(projCoords.z > 1.0)
        shadow = 0.0;

    return shadow;
}
