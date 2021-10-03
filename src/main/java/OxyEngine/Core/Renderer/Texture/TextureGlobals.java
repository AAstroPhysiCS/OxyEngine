package OxyEngine.Core.Renderer.Texture;

import org.joml.Matrix4f;

interface TextureGlobals {
    Matrix4f[] captureViews = new Matrix4f[]{
            new Matrix4f()
                    .lookAt(0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f),
            new Matrix4f()
                    .lookAt(0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f),
            new Matrix4f()
                    .lookAt(0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f),
            new Matrix4f()
                    .lookAt(0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, -1.0f),
            new Matrix4f()
                    .lookAt(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, -1.0f, 0.0f),
            new Matrix4f()
                    .lookAt(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, -1.0f, 0.0f),
    };

    Matrix4f captureProjection = new Matrix4f().setPerspective((float) Math.toRadians(90), 1.0f, 0.4768f, 10.0f);
}
