//#type fragment
#version 460 core

layout(location = 0) out vec4 color;

in vec3 tcsOut;
uniform samplerCube u_skyBoxTexture;
uniform float u_mipLevel;
uniform float u_exposure;
uniform float u_gamma;

void main(){
    vec3 textureRGB = textureLod(u_skyBoxTexture, tcsOut, u_mipLevel).rgb;
    textureRGB = vec3(1.0) - exp(-textureRGB * u_exposure);
    textureRGB = pow(textureRGB, vec3(1.0/u_gamma));
    color = vec4(textureRGB, 1.0);
}

//#type vertex
#version 460 core

layout(location = 0) in vec3 pos;

out vec3 tcsOut;

uniform mat4 v_Matrix_NoTransform;

void main(){
    tcsOut = pos;
    gl_Position = (vec4(pos, 1.0f) * v_Matrix_NoTransform).xyww;
}