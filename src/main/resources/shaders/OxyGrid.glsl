//#type vertex
#version 450 core

layout(location = 0) in vec3 pos;

layout (std140, binding = 0) uniform Camera {
    mat4 v_Matrix;
    mat4 v_Matrix_NoTransform;
    vec3 cameraPos;
};

void main(){
    gl_Position = v_Matrix * vec4(pos, 1.0f);
}

//#type fragment
#version 450 core

layout(location = 0) out vec4 color;

void main(){
    color = vec4(1.0f, 1.0f, 1.0f, 0.2f);
}