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

struct RendererMaterial {
    vec4 diffuse;
};

uniform RendererMaterial Material;

void main() {
    color = Material.diffuse;
}
