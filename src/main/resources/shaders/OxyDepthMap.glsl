//#type vertex
#version 450 core

layout(location = 0) in vec3 pos;
layout(location = 6) in vec4 boneIds;
layout(location = 7) in vec4 weights;

#define MAX_BONES 100

uniform mat4 model;
uniform mat4 lightSpaceMatrix;

uniform mat4 finalBonesMatrices[MAX_BONES];
uniform int animatedModel;

void main(){

    ivec4 boneIDInt = ivec4(boneIds);
    vec4 totalPos = vec4(pos, 1.0f);
    if(bool(animatedModel)){
        //mesh has animations
        mat4 transform = finalBonesMatrices[boneIDInt[0]] * weights[0];
        transform += finalBonesMatrices[boneIDInt[1]] * weights[1];
        transform += finalBonesMatrices[boneIDInt[2]] * weights[2];
        transform += finalBonesMatrices[boneIDInt[3]] * weights[3];

        totalPos = transform * vec4(pos, 1.0f);
    }

    gl_Position = lightSpaceMatrix * model * totalPos;
}

//#type fragment
#version 450 core

void main(){
}
