package OxyEngineEditor.Scene.Objects.Native;

import OxyEngineEditor.Components.EntityComponent;
import OxyEngineEditor.Components.NativeObjectMesh;
import OxyEngineEditor.Components.TransformComponent;
import OxyEngineEditor.Scene.Objects.Model.OxyMaterial;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public abstract class NativeObjectFactory implements EntityComponent {

    public ObjectType type;
    protected float[] vertexPos;
    private int vertPtr = 0;

    //Texture does not work in native objects, only colors
    public void constructData(OxyNativeObject e, int size) {
        OxyMaterial material = e.get(OxyMaterial.class);
        TransformComponent c = e.get(TransformComponent.class);

        c.transform = new Matrix4f()
                .scale(c.scale)
                .translate(c.position)
                .rotateX(c.rotation.x)
                .rotateY(c.rotation.y)
                .rotateZ(c.rotation.z);

        if (e.vertices == null) e.vertices = new float[e.type.n_Vertices() * size];
        for (int i = 0; i < vertexPos.length; ) {
            Vector4f transformed = new Vector4f(vertexPos[i++], vertexPos[i++], vertexPos[i++], 1.0f).mul(c.transform);
            e.vertices[vertPtr++] = transformed.x;
            e.vertices[vertPtr++] = transformed.y;
            e.vertices[vertPtr++] = transformed.z;
            e.vertices[vertPtr++] = 0;
            if (material.diffuseColor != null) {
                e.vertices[vertPtr++] = material.diffuseColor.getNumbers()[0];
                e.vertices[vertPtr++] = material.diffuseColor.getNumbers()[1];
                e.vertices[vertPtr++] = material.diffuseColor.getNumbers()[2];
                e.vertices[vertPtr++] = material.diffuseColor.getNumbers()[3];
            } else vertPtr += 4;
        }
    }

    public abstract void initData(OxyNativeObject e, NativeObjectMesh mesh);
}

