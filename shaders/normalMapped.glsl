//#type fragment
#version 460 core

layout(location = 0) out vec4 color;

struct PointLight {
    vec3 position;
    vec3 ambient;
    vec3 diffuse;
    vec3 specular;

    float constant;
    float linear;
    float quadratic;
};

in OUT_VARIABLES {
    vec2 texCoordsOut;
    float textureSlotOut;
    vec4 colorOut;

    vec3 FragPos;
    vec2 TexCoords;
    mat3 TBNOut;
} inVar;

uniform PointLight p_Light;
uniform sampler2D tex[32];
uniform int normalMapTextureSlot;
uniform vec3 viewPos;

void main(){

    vec3 TangentLightPos = inVar.TBNOut * p_Light.position;
    vec3 TangentViewPos  = inVar.TBNOut * viewPos;
    vec3 TangentFragPos  = inVar.TBNOut * inVar.FragPos;

    // obtain normal from normal map in range [0,1]
    vec3 normal = texture(tex[int(round(normalMapTextureSlot))], inVar.texCoordsOut).rgb;
    if(normal.x == 0 && normal.y == 0 && normal.z == 0){
        color = texture(tex[int(round(inVar.textureSlotOut))], inVar.texCoordsOut);
        return;
    }
    // transform normal vector to range [-1,1]
    normal = normalize(normal * 2.0 - 1.0);  // this normal is in tangent space

    // get diffuse color
    vec3 colorDiff = texture(tex[int(round(inVar.textureSlotOut))], inVar.texCoordsOut).rgb;
    // ambient
    vec3 ambient = 0.1 * colorDiff;
    // diffuse
    vec3 lightDir = normalize(TangentLightPos - TangentFragPos);
    float diff = max(dot(lightDir, normal), 0.0);
    vec3 diffuse = diff * colorDiff;
    // specular
    vec3 viewDir = normalize(TangentViewPos - TangentFragPos);
    vec3 reflectDir = reflect(-lightDir, normal);
    vec3 halfwayDir = normalize(lightDir + viewDir);
    float spec = pow(max(dot(normal, halfwayDir), 0.0), 32.0);

    vec3 specular = vec3(0.2) * spec;
    color = vec4(ambient + diffuse + specular, 1.0);
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
uniform mat4 m_Matrix; //Camera Pos
uniform mat4 model; //glm::translate(model) scale usw

out OUT_VARIABLES {
    vec2 texCoordsOut;
    float textureSlotOut;
    vec4 colorOut;

    vec3 FragPos;
    mat3 TBNOut;
} outVar;

void main(){
    outVar.FragPos = vec3(model * pos);
    outVar.textureSlotOut = textureSlot;
    outVar.texCoordsOut = tcs;
    outVar.colorOut = color;

    mat3 normalMatrix = transpose(inverse(mat3(model)));
    vec3 T = normalize(normalMatrix * vec3(tangent));
    vec3 N = normalize(normalMatrix * vec3(normals));
    T = normalize(T - dot(T, N) * N);
    vec3 B = cross(N, T);

    mat3 TBN = transpose(mat3(T, B, N));
    outVar.TBNOut = TBN;

    gl_Position = pos * v_Matrix;
}