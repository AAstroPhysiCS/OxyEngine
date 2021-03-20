//#type fragment
#version 460 core

layout(location = 0) out vec4 color;
layout(location = 1) out int o_IDBuffer;

in OUT_VARIABLES {
    vec2 texCoordsOut;
    vec3 normalsOut;
    vec3 vertexPos;

    //NORMAL MAPPING
    mat3 TBN;
} inVar;

uniform sampler2D tex[32];
uniform samplerCube skyBoxTexture;
uniform vec3 cameraPos;

flat in int v_ObjectID;

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

#define PI 3.14159265358979323

float DistributionGGX(vec3 N, vec3 H, float roughness)
{
    float a = roughness*roughness;
    float a2 = a*a;
    float NdotH = max(dot(N, H), 0.0);
    float NdotH2 = NdotH*NdotH;

    float nom   = a2;
    float denom = (NdotH2 * (a2 - 1.0) + 1.0);
    denom = PI * denom * denom;

    return nom / max(denom, 0.001); // prevent divide by zero for roughness=0.0 and NdotH=1.0
}

float GeometrySchlickGGX(float NdotV, float roughness)
{
    float r = (roughness + 1.0);
    float k = (r*r) / 8.0;

    float nom   = NdotV;
    float denom = NdotV * (1.0 - k) + k;

    return nom / denom;
}

float GeometrySmith(vec3 N, vec3 V, vec3 L, float roughness)
{
    float NdotV = max(dot(N, V), 0.0);
    float NdotL = max(dot(N, L), 0.0);
    float ggx2 = GeometrySchlickGGX(NdotV, roughness);
    float ggx1 = GeometrySchlickGGX(NdotL, roughness);

    return ggx1 * ggx2;
}

vec3 fresnelSchlick(float cosTheta, vec3 F0)
{
    return F0 + (1.0 - F0) * pow(1.0 - cosTheta, 5.0);
}

vec3 fresnelSchlickRoughness(float cosTheta, vec3 F0, float roughness)
{
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
     float denominator = 4 * max(dot(N, V), 0.0) * max(dot(N, L), 0.0) + 0.001; // 0.001 to prevent divide by zero.
     vec3 specular     = nominator / denominator;

     // kS is equal to Fresnel
     vec3 kS = F;
     // for energy conservation, the diffuse and specular light can't
     // be above 1.0 (unless the surface emits light); to preserve this
     // relationship the diffuse component (kD) should equal 1.0 - kS.
     vec3 kD = vec3(1.0) - kS;
     // multiply kD by the inverse metalness such that only non-metals
     // have diffuse lighting, or a linear blend if partly metal (pure metals
     // have no diffuse light).
     kD *= 1.0 - metallic;

     // scale light by NdotL
     float NdotL = max(dot(N, L), 0.0);

     // add to outgoing radiance Lo
     return (kD * albedo / PI + specular) * radiance * NdotL;  // note that we already multiplied the BRDF by the Fresnel (kS) so we won't multiply by kS again
}

vec4 startPBR(vec3 vertexPos, vec3 cameraPosVec3, vec2 texCoordsOut, vec3 viewDir, vec3 norm){
    const float MAX_REFLECTION_LOD = 4.0;
    vec3 albedo;
    float metallicMap, roughnessMap, aoMap;

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
       vec4 texture = texture(tex[albedoMapSlot], texCoordsOut).rgba;
       if(texture.w < 0.1f) discard;
       albedo = pow(texture.rgb, vec3(gamma));
    }

    vec3 IBL = texture(iblMap, norm).rgb;
    if(IBL.rgb == vec3(0.0f, 0.0f, 0.0f)) IBL = vec3(0.05f, 0.05f, 0.05f);
    vec3 Lo;
    vec3 F0 = vec3(0.04);
    F0 = mix(F0, albedo, metallicMap);
    for(int i = 0; i < d_Light.length; i++){
        if(d_Light[i].activeState == 0) continue;
        vec3 lightDir = normalize(-d_Light[i].direction);

        Lo += calcPBR(lightDir, d_Light[i].diffuse, norm, viewDir, vertexPos, F0, albedo, roughnessMap, metallicMap, 1.0);
    }

    for(int i = 0; i < p_Light.length; i++){
        if(p_Light[i].activeState == 0) continue;
        vec3 lightPos = p_Light[i].position;

        vec3 lightDir = normalize(lightPos - vertexPos);
        float distance = length(lightPos - vertexPos);
        float attenuation = 1.0 / (p_Light[i].constant + p_Light[i].linear * distance + p_Light[i].quadratic * (distance * distance));

        Lo += calcPBR(lightDir, p_Light[i].diffuse, norm, viewDir, vertexPos, F0, albedo, roughnessMap, metallicMap, attenuation);
    }

    vec3 F = fresnelSchlickRoughness(max(dot(norm, viewDir), 0.0), F0, roughnessMap);

    vec3 kS = F;
    vec3 kD = 1.0 - kS;
    kD *= 1.0 - metallicMap;

    vec3 diffuseMap = IBL * albedo;

    vec3 R = reflect(-viewDir, norm);
    vec3 prefilteredColor = textureLod(prefilterMap, R, roughnessMap * MAX_REFLECTION_LOD).rgb;
    vec2 envBRDF  = texture(brdfLUT, vec2(max(dot(norm, viewDir), 0.0), roughnessMap)).rg;
    vec3 specular = prefilteredColor * (F * envBRDF.x + envBRDF.y);
    vec3 ambient = (kD * diffuseMap + specular) * aoMap * hdrIntensity;

    vec3 emissive = texture(tex[emissiveSlot], inVar.texCoordsOut).rgb * emissiveStrength;

    vec3 result = ambient + Lo + emissive;
    result = result / (result + vec3(1.0));
    result = vec3(1.0) - exp(-result * exposure);
    result = pow(result, vec3(1f / gamma));

    return vec4(result, 1.0f);
}

void main(){

    o_IDBuffer = v_ObjectID;

    vec3 vertexPos = inVar.vertexPos;
    vec3 cameraPosVec3 = cameraPos;
    vec2 texCoordsOut = inVar.texCoordsOut;

    vec3 viewDir = normalize(cameraPosVec3 - vertexPos);

    vec3 norm;
    if(normalMapSlot != 0 && normalMapStrength != 0.0f){
        vec3 normalMap = texture(tex[normalMapSlot], texCoordsOut).xyz * 2.0 - 1.0;
        normalMap.xy *= normalMapStrength;
        norm = normalize(inVar.TBN * normalMap);
    } else {
        norm = vec3(normalize(inVar.normalsOut));
    }

    vec4 result = startPBR(vertexPos, cameraPosVec3, texCoordsOut, viewDir, norm);
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

flat out int v_ObjectID;

out OUT_VARIABLES {
    vec2 texCoordsOut;
    vec3 normalsOut;
    vec3 vertexPos;

    //NORMAL MAPPING
    mat3 TBN;
} outVar;

const int MAX_BONES = 100;
uniform mat4 finalBonesMatrices[MAX_BONES];

void main(){

    v_ObjectID = int(objectID);
    outVar.texCoordsOut = tcs;

    ivec4 boneIDInt = ivec4(boneIds);
    vec4 totalPos = vec4(0.0f);
    vec4 totalNorm = vec4(0.0f);

    if(boneIDInt[0] == -1 && boneIDInt[1] == -1 && boneIDInt[2] == -1 && boneIDInt[3] == -1){
        //mesh has no animations
        totalPos = vec4(pos, 1.0f);
        totalNorm = vec4(normals, 0.0f);
    } else {
        //mesh has animations
        mat4 transformPos = finalBonesMatrices[boneIDInt[0]] * weights[0];
                transformPos += finalBonesMatrices[boneIDInt[1]] * weights[1];
                transformPos += finalBonesMatrices[boneIDInt[2]] * weights[2];
                transformPos += finalBonesMatrices[boneIDInt[3]] * weights[3];

        totalPos = transformPos * vec4(pos, 1.0f);

        mat4 transformNorm = finalBonesMatrices[boneIDInt[0]] * weights[0];
                transformNorm += finalBonesMatrices[boneIDInt[1]] * weights[1];
                transformNorm += finalBonesMatrices[boneIDInt[2]] * weights[2];
                transformNorm += finalBonesMatrices[boneIDInt[3]] * weights[3];

        totalNorm = transformNorm * vec4(normals, 0.0f);
    }

    outVar.vertexPos = mat3(model) * totalPos.xyz;
    outVar.normalsOut = mat3(model) * totalNorm.xyz;

    vec3 T = normalize(mat3(model) * tangent);
    vec3 B = normalize(mat3(model) * biTangent);
    vec3 N = normalize(mat3(model) * totalNorm.xyz);

    mat3 TBN = mat3(T, B, N);
    outVar.TBN = TBN;

    gl_Position = model * totalPos * v_Matrix;
}