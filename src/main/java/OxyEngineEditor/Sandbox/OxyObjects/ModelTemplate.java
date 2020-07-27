package OxyEngineEditor.Sandbox.OxyObjects;

import OxyEngine.Core.Renderer.Buffer.BufferTemplate;
import OxyEngine.Core.Renderer.Buffer.Mesh;
import OxyEngine.Core.Renderer.Texture.OxyTexture;
import OxyEngineEditor.Sandbox.OxyComponents.GameObjectMesh;
import OxyEngineEditor.Sandbox.OxyComponents.ModelMesh;
import OxyEngineEditor.Sandbox.OxyComponents.TransformComponent;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.List;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;

public class ModelTemplate implements ObjectTemplate {

    private ModelMesh mesh;
    private final List<Vector3f> allNonTransformVertices;

    public ModelTemplate(List<Vector3f> allNonTransformVertices) {
        this.allNonTransformVertices = allNonTransformVertices;
    }

    @Override
    public void constructData(OxyEntity e) {
        mesh = new ModelMesh.ModelMeshBuilderImpl()
                .setMode(GL_TRIANGLES)
                .setUsage(BufferTemplate.Usage.DYNAMIC)
                .setVertices(e.vertices)
                .setIndices(e.indices)
                .setTextureCoords(e.tcs)
                .setNormals(e.normals)
                .create();
    }

    @Override
    public void initData(OxyEntity e, Mesh mesh) {

    }

    public void updateData(OxyEntity e) {

        OxyTexture texture = (OxyTexture) e.get(OxyTexture.class);
        TransformComponent c = (TransformComponent) e.get(TransformComponent.class);

        Matrix4f transform;
        if (c.transform == null) {
            transform = new Matrix4f()
                    .scale(c.scale)
                    .translate(c.position)
                    .rotateX(c.rotation.x)
                    .rotateY(c.rotation.y)
                    .rotateZ(c.rotation.z);
        } else transform = c.transform;

        int vertPtr = 0;
        int slot = 0;
        if (texture != null) slot = texture.getTextureSlot();
        for (int i = 0; i < allNonTransformVertices.size() / 3; i++) {
            Vector3f v = allNonTransformVertices.get(i);
            Vector4f transformed = new Vector4f(v, 1.0f).mul(transform);
            e.vertices[vertPtr++] = transformed.x;
            e.vertices[vertPtr++] = transformed.y;
            e.vertices[vertPtr++] = transformed.z;
            e.vertices[vertPtr++] = slot;
        }
    }

    public ModelMesh getMesh() {
        return mesh;
    }
}
