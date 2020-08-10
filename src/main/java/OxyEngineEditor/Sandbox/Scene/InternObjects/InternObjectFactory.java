package OxyEngineEditor.Sandbox.Scene.InternObjects;

import OxyEngine.Core.Renderer.Texture.ImageTexture;
import OxyEngine.Core.Renderer.Texture.OxyColor;
import OxyEngineEditor.Sandbox.OxyComponents.EntityComponent;
import OxyEngineEditor.Sandbox.OxyComponents.InternObjectMesh;
import OxyEngineEditor.Sandbox.OxyComponents.TransformComponent;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public abstract class InternObjectFactory implements EntityComponent {

    ObjectType type;
    protected float[] vertexPos;

    public void constructData(OxyInternObject e) {
        OxyColor color = (OxyColor) e.get(OxyColor.class);
        ImageTexture texture = (ImageTexture) e.get(ImageTexture.class);
        TransformComponent c = (TransformComponent) e.get(TransformComponent.class);

        c.transform = new Matrix4f()
                .scale(c.scale)
                .translate(c.position)
                .rotateX(c.rotation.x)
                .rotateY(c.rotation.y)
                .rotateZ(c.rotation.z);

        int slot = 0; // 0 => color
        float[] tcs = null;

        if (texture != null) {
            slot = texture.getTextureSlot();
            tcs = texture.getTextureCoords();
        }
        Vector4f[] vec4Vertices = new Vector4f[vertexPos.length / 3];
        int vecPtr = 0;
        for (int i = 0; i < vec4Vertices.length; i++) {
            vec4Vertices[i] = new Vector4f(vertexPos[vecPtr++], vertexPos[vecPtr++], vertexPos[vecPtr++], 1.0f).mul(c.transform);
        }

        e.vertices = new float[e.type.n_Vertices()];
        int ptr = 0, texIndex = 0;
        for (int i = 0; i < e.type.n_Vertices(); ) {
            e.vertices[i++] = vec4Vertices[ptr].x;
            e.vertices[i++] = vec4Vertices[ptr].y;
            e.vertices[i++] = vec4Vertices[ptr].z;
            if (texture != null) {
                e.vertices[i++] = tcs[texIndex++];
                e.vertices[i++] = tcs[texIndex++];
            } else i += 2;
            e.vertices[i++] = slot;
            if (color != null && slot == 0) {
                e.vertices[i++] = color.getNumbers()[0];
                e.vertices[i++] = color.getNumbers()[1];
                e.vertices[i++] = color.getNumbers()[2];
                e.vertices[i++] = color.getNumbers()[3];
            } else i += 4;
            ptr++;
        }
    }

    abstract void initData(OxyInternObject e, InternObjectMesh mesh);
}

