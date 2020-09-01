//#type fragment
#version 460 core

layout(location = 0) out vec4 color;

void main(){
    color = vec4(0.34, 0.74, 0.42, 1.0);
}

    //#type vertex
    #version 460 core

layout(location = 0) in vec4 pos;
layout(location = 3) in vec4 color;
layout(location = 4) uniform mat4 v_Matrix;

void main(){
    gl_Position = pos * v_Matrix;
}