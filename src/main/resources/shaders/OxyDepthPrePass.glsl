//#type vertex
#version 450 core

layout(location = 0) in vec3 pos;

layout (std140, binding = 0) uniform Camera {
    mat4 v_Matrix;
    mat4 v_Matrix_NoTransform;
    vec3 cameraPos;
};

uniform mat4 model;

void main(){
    gl_Position = v_Matrix * model * vec4(pos, 1.0f);
}

//#type fragment
#version 450 core

void main(){
}
