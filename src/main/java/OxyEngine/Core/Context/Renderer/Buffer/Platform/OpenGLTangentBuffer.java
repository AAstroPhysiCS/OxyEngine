package OxyEngine.Core.Context.Renderer.Buffer.Platform;

import OxyEngine.Core.Context.Renderer.Buffer.TangentBuffer;
import OxyEngine.Core.Context.Renderer.Pipeline.OxyPipeline;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL45.glCreateBuffers;

public class OpenGLTangentBuffer extends TangentBuffer {

    OpenGLTangentBuffer(OxyPipeline.Layout layout) {
        super(layout);
    }

    @Override
    public void load() {
        if (bufferId == 0) bufferId = glCreateBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, bufferId);
        glBufferData(GL_ARRAY_BUFFER, biAndTangent, GL_STATIC_DRAW);
    }

    public void setBiAndTangent(float[] tangents, float[] biTangents) {
        biAndTangent = new float[tangents.length + biTangents.length];
        int tangentPtr = 0, biTangentPtr = 0;
        for (int i = 0; i < biAndTangent.length; ) {
            biAndTangent[i++] = tangents[tangentPtr++];
            biAndTangent[i++] = tangents[tangentPtr++];
            biAndTangent[i++] = tangents[tangentPtr++];
            biAndTangent[i++] = biTangents[biTangentPtr++];
            biAndTangent[i++] = biTangents[biTangentPtr++];
            biAndTangent[i++] = biTangents[biTangentPtr++];
        }
    }

    @Override
    public void dispose() {
        glDeleteBuffers(bufferId);
        bufferId = 0;
    }
}
