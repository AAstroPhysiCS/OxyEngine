//#type vertex
#version 450 core

layout(location = 0) in vec3 pos;

out vec3 tcsOut;

layout (std140, binding = 0) uniform Camera {
    mat4 v_Matrix;
    mat4 v_Matrix_NoTransform;
    vec3 cameraPos;
};

void main(){
    tcsOut = pos;
    gl_Position = (v_Matrix_NoTransform * vec4(pos, 1.0f)).xyww;
}

//#type fragment
#version 450 core

layout(location = 0) out vec4 color;

in vec3 tcsOut;
uniform samplerCube u_skyBoxTexture;
uniform float u_mipLevel;

layout (std140, binding = 1) uniform EnvironmentSettings {
    float gamma;
    float exposure;
    float hdrIntensity;
};

void main(){
    vec3 textureRGB = textureLod(u_skyBoxTexture, tcsOut, u_mipLevel).rgb;
    textureRGB = vec3(1.0) - exp(-textureRGB * exposure);
    textureRGB = pow(textureRGB, vec3(1.0/gamma));
    color = vec4(textureRGB, 1.0);
}
