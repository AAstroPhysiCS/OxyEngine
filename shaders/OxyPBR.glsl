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
    vec3 position;
    vec3 direction;
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;
};

uniform Material material;
uniform PointLight p_Light[2];
uniform DirectionalLight d_Light;
uniform float currentLightIndex = -1;

//NORMAL MAP
uniform int normalMapSlot = -1;
uniform float normalMapStrength;
vec3 normalMap;
vec3 tangentLightPos, tangentViewPos, tangentVertexPos;
//GAMMA
uniform float gamma;
//PBR
uniform int metallicSlot;
uniform float metallicFloat;
uniform int roughnessSlot;
uniform float roughnessFloat;
uniform int aoSlot;
uniform float aoFloat;
//PBR FILTERING
uniform samplerCube prefilterMap;
uniform sampler2D brdfLUT;
//irradiance
uniform samplerCube irradianceMap;
//hdr
uniform float exposure;

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

vec3 Lo = vec3(0.0);
vec3 calcPBR(vec3 lightPos, vec3 lightDiffuseColor, vec3 N, vec3 V, vec3 vertexPos, vec3 F0, vec3 albedo, float roughness, float metallic, float attenuation){
     //calculate per-light radiance
     vec3 L = normalize(lightPos - vertexPos);
     vec3 H = normalize(V + L);
     vec3 radiance = lightDiffuseColor * attenuation;

     // Cook-Torrance BRDF
     float NDF = DistributionGGX(N, H, roughness);
     float G   = GeometrySmith(N, V, L, roughness);
     vec3 F    = fresnelSchlick(max(dot(H, V), 0.0), F0);

     vec3 nominator    = NDF * G * F;
     float denominator = 4 * max(dot(N, V), 0.0) * max(dot(N, L), 0.0) + 0.001; // 0.001 to prevent divide by zero.
     vec3 specular = nominator / denominator;

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
     Lo += (kD * albedo / PI + specular) * radiance * NdotL;  // note that we already multiplied the BRDF by the Fresnel (kS) so we won't multiply by kS again
     return Lo;
}


void beginPBR(vec3 norm, vec3 lightPos, vec3 viewDir, vec3 vertexPos, vec2 texCoordsOut, float attenuation, vec3 diffuse){
    const float MAX_REFLECTION_LOD = 4.0;
    vec3 albedo;
    float metallicMap, roughnessMap, aoMap;

    if (int(round(inVar.textureSlotOut)) == 0){ //color
        albedo = pow(vec3(material.diffuse), vec3(2.2));
        metallicMap = metallicFloat;
        roughnessMap = roughnessFloat;
        aoMap = aoFloat;
    }
    else { //texture
        albedo = pow(texture(tex[int(round(inVar.textureSlotOut))], texCoordsOut).rgb, vec3(2.2));
        metallicMap = texture(tex[metallicSlot], inVar.texCoordsOut).r;
        roughnessMap = texture(tex[roughnessSlot], inVar.texCoordsOut).r;
        aoMap = texture(tex[aoSlot], inVar.texCoordsOut).r;
    }

    vec3 irradiance = texture(irradianceMap, norm).rgb;
    if(irradiance.rgb != vec3(0.0f, 0.0f, 0.0f)){ //the scene has env map
        vec3 F0 = vec3(0.04);
        F0 = mix(F0, albedo, metallicMap);
        vec3 Lo = calcPBR(lightPos, diffuse, norm, viewDir, vertexPos, F0, albedo, roughnessMap, metallicMap, attenuation);

        vec3 kS = fresnelSchlickRoughness(max(dot(norm, viewDir), 0.0), F0, roughnessMap);
        vec3 kD = 1.0 - kS;
        kD *= 1.0 - metallicMap;

        vec3 diffuseMap = irradiance * albedo;

        vec3 R = reflect(-viewDir, norm);
        vec3 prefilteredColor = textureLod(prefilterMap, R, roughnessMap * MAX_REFLECTION_LOD).rgb;
        vec2 envBRDF  = texture(brdfLUT, vec2(max(dot(norm, viewDir), 0.0), roughnessMap)).rg;
        vec3 specular = prefilteredColor * (kS * envBRDF.x + envBRDF.y);
        vec3 ambient = (kD * diffuseMap + specular) * aoMap;

        vec3 result = ambient + Lo;
        //result = result / (result + vec3(1.0));
        result = vec3(1.0) - exp(-result * exposure);
        result = pow(result, vec3(1f / gamma));
        color = vec4(result, 1.0f);
    } else {
        color = vec4(0.0f, 0.0f, 0.0f, 1.0f);
    }
}

void calcDirectionalLightImpl(DirectionalLight d_Light){
    vec3 vertexPos = inVar.vertexPos;
    vec3 lightPos = vec3(0.0f);
    vec3 cameraPosVec3 = cameraPos;
    vec2 texCoordsOut = inVar.texCoordsOut;

    if(normalMapSlot != 0){
        vertexPos = inVar.TBN * inVar.vertexPos;
        //lightPos = inVar.TBN * d_Light.position;
        cameraPosVec3 = inVar.TBN * cameraPos;
    }

    vec3 lightDir = normalize(-d_Light.direction);
    vec3 viewDir = normalize(cameraPosVec3 - vertexPos);

    vec3 norm;
    if(normalMapSlot != 0){
        vec3 normalMap = texture(tex[normalMapSlot], texCoordsOut).rgb;
        normalMap = normalMap * 2.0 - 1.0;
        normalMap.xy *= normalMapStrength;
        normalMap = normalize(normalMap);
        norm = normalMap;
    } else {
        norm = vec3(normalize(inVar.lightModelNormal));
    }

    float attenuation = 1.0;
    float diff = max(dot(norm, lightDir), 0.0);
    vec3 diffuse = d_Light.diffuse * diff;

    beginPBR(norm, lightPos, viewDir, vertexPos, texCoordsOut, attenuation, diffuse);
}

void calcPointLightImpl(PointLight p_Light){

    vec3 vertexPos = inVar.vertexPos;
    vec3 lightPos = p_Light.position;
    vec3 cameraPosVec3 = cameraPos;
    vec2 texCoordsOut = inVar.texCoordsOut;

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
        vec3 normalMap = texture(tex[normalMapSlot], texCoordsOut).rgb;
        normalMap = normalMap * 2.0 - 1.0;
        normalMap.xy *= normalMapStrength;
        normalMap = normalize(normalMap);
        norm = normalMap;
    } else {
        norm = vec3(normalize(inVar.lightModelNormal));
    }

    float distance = length(lightPos - vertexPos);
    float attenuation = 1.0 / (p_Light.constant + p_Light.linear * distance + p_Light.quadratic * (distance * distance));
    float diff = max(dot(norm, lightDir), 0.0);
    vec3 diffuse = p_Light.diffuse * diff;

    beginPBR(norm, lightPos, viewDir, vertexPos, texCoordsOut, attenuation, diffuse);
}

void main(){
    if (currentLightIndex == 0){
        for(int i = 0; i < p_Light.length; i++){
            calcPointLightImpl(p_Light[i]);
        }
    } else if (currentLightIndex == 1){
        calcDirectionalLightImpl(d_Light);
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