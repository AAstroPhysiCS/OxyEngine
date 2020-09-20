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

//irradiance
uniform samplerCube irradianceMap;

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
//GAMMA
uniform float gamma;
//PBR
uniform int metallicSlot;
uniform int roughnessSlot;
uniform int aoSlot;
uniform int heightSlot;

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

uniform float heightScale;
//DOES NOT WORK!!!!
vec2 ParallaxMapping(vec2 texCoords, vec3 viewDir)
{
    float height =  texture(tex[heightSlot], texCoords).r;
    return texCoords - viewDir.xy * (height * heightScale);
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

        color = vec4(result, 1.0f) * inVar.colorOut;
    }
    else {
        ambient = calcAmbient(texture(tex[index], inVar.texCoordsOut).rgb, d_Light.ambient);
        diffuse = calcDiffuse(lightDir, texture(tex[index], inVar.texCoordsOut).rgb, d_Light.diffuse, norm);
        specular = calcSpecular(lightDir, viewDir, texture(tex[index], inVar.texCoordsOut).rgb, d_Light.specular, norm);
        result = specular + diffuse + ambient;

        color = vec4(result, 1.0f);
    }
}

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

vec3 calcPBR(vec3 pointLightPos, vec3 pointLightDiffuseColor, vec3 N, vec3 V, vec3 vertexPos, vec3 F0, vec3 albedo, float roughness, float metallic){
     vec3 Lo = vec3(0.0);
     //calculate per-light radiance
     vec3 L = normalize(pointLightPos - vertexPos);
     vec3 H = normalize(V + L);
     float distance = length(pointLightPos - vertexPos);
     float attenuation = 1.0 / (p_Light.constant + p_Light.linear * distance + p_Light.quadratic * (distance));
     //float attenuation = 1.0 / (distance * distance);
     vec3 radiance = pointLightDiffuseColor * attenuation;

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

void calcPointLightImpl(PointLight p_Light, vec3 I, vec3 R){

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

    //PARALLAX MAPPING
    if(heightSlot != 0){
        //texCoordsOut = ParallaxMapping(inVar.texCoordsOut, viewDir);
        //if(texCoordsOut.x > 1.0 || texCoordsOut.y > 1.0 || texCoordsOut.x < 0.0 || texCoordsOut.y < 0.0)
        //    discard;
    }

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

    int index = int(round(inVar.textureSlotOut));
    float distance = length(p_Light.position - inVar.vertexPos);
    float attenuation = 1.0 / (p_Light.constant + p_Light.linear * distance + p_Light.quadratic * (distance));

    if (index == 0){ //color
        vec3 ambient = calcAmbient(material.ambient, p_Light.ambient);
        vec3 diffuse = calcDiffuse(lightDir, material.diffuse, p_Light.diffuse, norm);
        vec3 specular = calcSpecular(lightDir, viewDir, material.specular, p_Light.specular, norm);

        ambient *= attenuation;
        diffuse *= attenuation;
        specular *= attenuation;
        vec3 result = specular + diffuse + ambient;
        result = result / (result + vec3(1.0));
        result = pow(result, vec3(1f / gamma));

        color = vec4(result, 1.0f) * inVar.colorOut;
    }
    else { //texture
        vec3 albedo = pow(texture(tex[index], texCoordsOut).rgb, vec3(2.2));
        float metallicMap = texture(tex[metallicSlot], inVar.texCoordsOut).r;
        float roughnessMap = texture(tex[roughnessSlot], inVar.texCoordsOut).r;
        float aoMap = texture(tex[aoSlot], inVar.texCoordsOut).r;

        vec3 F0 = vec3(0.04);
        F0 = mix(F0, albedo, metallicMap);
        vec3 Lo = calcPBR(lightPos, p_Light.diffuse, norm, viewDir, vertexPos, F0, albedo, roughnessMap, metallicMap);
        //vec3 ambient = vec3(0.03) * albedo * aoMap;

        vec3 kS = fresnelSchlickRoughness(max(dot(norm, viewDir), 0.0), F0, roughnessMap);
        vec3 kD = 1.0 - kS;
        kD *= 1.0 - metallicMap;
        vec3 irradiance = texture(irradianceMap, norm).rgb;
        vec3 diffuseMap = irradiance * albedo;
        vec3 ambient = (kD * diffuseMap) * aoMap;

        vec3 result = ambient + Lo;
        result = result / (result + vec3(1.0));
        result = pow(result, vec3(1f / gamma));

        color = vec4(result, 1.0f);
    }
}

void main(){
    vec3 I = normalize(vec3(inVar.vertexPos) - cameraPos);
    vec3 R = reflect(I, normalize(vec3(inVar.normalsOut)));

    //NORMAL MAPPING
    if (currentLightIndex == 0){
        calcPointLightImpl(p_Light, I, R);
    } else if (currentLightIndex == 1){
        calcDirectionalLightImpl(d_Light, I, R);
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