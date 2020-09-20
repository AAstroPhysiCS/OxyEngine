//#type fragment
#version 460 core

layout(location = 0) out vec4 color;
in vec4 colorOut;

void main(){
    color = colorOut;
}

//#type vertex
#version 460 core

layout(location = 0) in vec4 pos;
layout(location = 3) in vec4 color;
layout(location = 4) uniform mat4 v_Matrix;

out vec4 colorOut;

void main(){
    colorOut = color;
    gl_Position = pos * v_Matrix;
}