//#type fragment
#version 460 core

layout(location = 0) out vec4 color;

in vec3 tcsOut;
uniform samplerCube skyBoxTexture;

void main(){
    color = texture(skyBoxTexture, tcsOut);
}

//#type vertex
#version 460 core

layout(location = 0) in vec3 pos;

out vec3 tcsOut;

uniform mat4 v_Matrix_NoTransform;

void main(){
    tcsOut = pos;
    gl_Position = vec4(pos * 1000, 1.0f) * v_Matrix_NoTransform;
}