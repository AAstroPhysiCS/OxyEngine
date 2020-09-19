//#type fragment
#version 460 core

layout(location = 0) out vec4 color;

in vec3 localPosOut;
uniform sampler2D hdrTexture;

const vec2 invAtan = vec2(0.1591, 0.3183);
vec2 SampleSphericalMap(vec3 v)
{
    vec2 uv = vec2(atan(v.z, v.x), asin(v.y));
    uv *= invAtan;
    uv += 0.5;
    return uv;
}

void main(){
    vec2 uv = SampleSphericalMap(normalize(localPosOut));
    color = vec4(texture(hdrTexture, uv).rgb, 1.0f);
}

//#type vertex
#version 460 core

layout(location = 0) in vec3 pos;

out vec3 localPosOut;

uniform mat4 view;
uniform mat4 projection;

void main(){
    localPosOut = pos;
    gl_Position = projection * view * vec4(pos, 1.0f);
}