//#type fragment
#version 460 core

layout(location = 0) out vec4 color;
uniform vec3 colorOut;

void main(){
    color = vec4(colorOut, 1.0f);
}

//#type vertex
#version 460 core

layout(location = 0) in vec4 pos;
layout(location = 4) uniform mat4 v_Matrix;

void main(){
    gl_Position = pos * v_Matrix;
}