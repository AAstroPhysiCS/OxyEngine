//#type fragment
#version 460 core

layout(location = 0) out vec4 color;

in vec3 tcsOut;
uniform samplerCube skyBoxTexture;

void main(){
    vec3 textureRGB = texture(skyBoxTexture, tcsOut).rgb;
    textureRGB = textureRGB / (textureRGB + vec3(1.0));
    textureRGB = pow(textureRGB, vec3(1.0/2.2));

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