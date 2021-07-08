//#type vertex
#version 450 core

layout(location = 0) in vec3 pos;

out vec3 localPosOut;

uniform mat4 u_viewIBL;
uniform mat4 u_projectionIBL;

void main(){
    localPosOut = pos;
    gl_Position = u_projectionIBL * u_viewIBL * vec4(pos, 1.0f);
}

//#type fragment
#version 450 core

layout(location = 0) out vec4 color;

in vec3 localPosOut;
uniform samplerCube u_skyBoxTextureIBL;

#define PI 3.14159265358979323

void main(){
    vec3 N = normalize(localPosOut);
    vec3 irradiance = vec3(0.0);

    vec3 up    = vec3(0.0, 1.0, 0.0);
    vec3 right = cross(up, N);
    up = cross(N, right);
    float sampleDelta = 0.025f;
    float nrSamples = 0.0f;
    for(float phi = 0.0; phi < 2.0 * PI; phi += sampleDelta)
    {
        for(float theta = 0.0; theta < 0.5 * PI; theta += sampleDelta)
        {
            // spherical to cartesian (in tangent space)
            vec3 tangentSample = vec3(sin(theta) * cos(phi),  sin(theta) * sin(phi), cos(theta));
            // tangent space to world
            vec3 sampleVec = tangentSample.x * right + tangentSample.y * up + tangentSample.z * N;

            irradiance += texture(u_skyBoxTextureIBL, sampleVec).rgb * cos(theta) * sin(theta);
            nrSamples++;
        }
    }
    irradiance = PI * irradiance * (1.0 / float(nrSamples));
    color = vec4(irradiance, 1.0);
}
