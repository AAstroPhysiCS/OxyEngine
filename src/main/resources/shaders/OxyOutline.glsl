//#type vertex
#version 450 core

layout(location = 0) in vec3 pos;
layout(location = 6) in vec4 boneIds;
layout(location = 7) in vec4 weights;

uniform mat4 model;
const int MAX_BONES = 100;
uniform mat4 finalBonesMatrices[MAX_BONES];
uniform mat4 v_Matrix;

void main(){

    ivec4 boneIDInt = ivec4(boneIds);
    vec4 totalPos = vec4(0.0f);

    if(boneIDInt[0] == -1 && boneIDInt[1] == -1 && boneIDInt[2] == -1 && boneIDInt[3] == -1){
        //mesh has no animations
        totalPos = vec4(pos, 1.0f);
    } else {
        //mesh has animations
        mat4 transformPos = finalBonesMatrices[boneIDInt[0]] * weights[0];
                transformPos += finalBonesMatrices[boneIDInt[1]] * weights[1];
                transformPos += finalBonesMatrices[boneIDInt[2]] * weights[2];
                transformPos += finalBonesMatrices[boneIDInt[3]] * weights[3];

        totalPos = transformPos * vec4(pos, 1.0f);
    }

    gl_Position = model * totalPos * v_Matrix;
}

//#type fragment
#version 450 core

layout(location = 0) out vec4 color;

void main(){
    color = vec4(0.34, 0.74, 0.42, 1.0);
}

