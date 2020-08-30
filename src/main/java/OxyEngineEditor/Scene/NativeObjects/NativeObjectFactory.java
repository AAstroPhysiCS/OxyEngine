package OxyEngineEditor.Scene.NativeObjects;

import OxyEngine.Core.Renderer.Texture.ImageTexture;
import OxyEngineEditor.Components.EntityComponent;
import OxyEngineEditor.Components.NativeObjectMesh;
import OxyEngineEditor.Components.TransformComponent;
import OxyEngineEditor.Scene.Model.OxyMaterial;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public abstract class NativeObjectFactory implements EntityComponent {

    ObjectType type;
    protected float[] vertexPos;

    public void constructData(OxyNativeObject e) {
        OxyMaterial material = e.get(OxyMaterial.class);
        ImageTexture texture = material.texture;
        TransformComponent c = e.get(TransformComponent.class);

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
            if (material.diffuseColor != null && slot == 0) {
                e.vertices[i++] = material.diffuseColor.getNumbers()[0];
                e.vertices[i++] = material.diffuseColor.getNumbers()[1];
                e.vertices[i++] = material.diffuseColor.getNumbers()[2];
                e.vertices[i++] = material.diffuseColor.getNumbers()[3];
            } else i += 4;
            ptr++;
        }
    }

    abstract void initData(OxyNativeObject e, NativeObjectMesh mesh);
}

