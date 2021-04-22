package OxyEngine.Core.Renderer.Buffer.Platform;

import OxyEngine.Core.Renderer.Buffer.NormalsBuffer;
import OxyEngine.Core.Renderer.Pipeline.OxyPipeline;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL45.glCreateBuffers;

public class OpenGLNormalsBuffer extends NormalsBuffer {

    OpenGLNormalsBuffer(OxyPipeline.Layout layout) {
        super(layout);
    }

    @Override
    public void load() {
        if (bufferId == 0) bufferId = glCreateBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, bufferId);
        glBufferData(GL_ARRAY_BUFFER, normals, GL_STATIC_DRAW);
    }

    public void setNormals(float[] normals) {
        this.normals = normals;
    }

    @Override
    public void dispose() {
        normals = null;
        glDeleteBuffers(bufferId);
        bufferId = 0;
    }
}
