//#type fragment
#version 460 core

layout(location = 0) out vec4 color;

void main(){
    color = vec4(1.0f, 1.0f, 1.0f, 0.2f);
}

//#type vertex
#version 460 core

layout(location = 0) in vec3 pos;

uniform mat4 v_Matrix;

void main(){
    gl_Position = vec4(pos, 1.0f) * v_Matrix;
}