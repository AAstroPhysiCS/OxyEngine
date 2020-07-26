//#type fragment
#version 460 core

layout(location = 0) out vec4 color;

uniform vec4 colorInput;

in vec2 texCoordsOut;
in float textureSlotOut;
uniform sampler2D tex[32];

void main(){
    int index = int(round(textureSlotOut));
    if(index == 0)
        color = colorInput;
    else
        color = texture(tex[index], texCoordsOut);
}

//#type vertex
#version 460 core

layout(location = 0) in vec4 pos;
layout(location = 1) in vec2 tcs;
layout(location = 2) in float textureSlot;

layout(location = 4) uniform mat4 v_Matrix;

out vec2 texCoordsOut;
out float textureSlotOut;

void main(){
    textureSlotOut = textureSlot;
    texCoordsOut = tcs;
    gl_Position = pos * v_Matrix;
}
