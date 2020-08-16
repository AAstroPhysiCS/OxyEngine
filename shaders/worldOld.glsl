//#type fragment
#version 460 core

layout(location = 0) out vec4 color;

in vec2 texCoordsOut;
in float textureSlotOut;
in vec4 colorOut;

uniform sampler2D tex[32];

void main(){
    int index = int(round(textureSlotOut));
    if(index == 0)
    color = colorOut;
    else
    color = texture(tex[index], texCoordsOut);
}

    //#type vertex
    #version 460 core

layout(location = 0) in vec4 pos;
layout(location = 1) in vec2 tcs;
layout(location = 2) in float textureSlot;
layout(location = 3) in vec4 color;

layout(location = 4) uniform mat4 v_Matrix;

out vec2 texCoordsOut;
out float textureSlotOut;
out vec4 colorOut;

void main(){
    textureSlotOut = textureSlot;
    texCoordsOut = tcs;
    colorOut = color;
    gl_Position = pos * v_Matrix;
}