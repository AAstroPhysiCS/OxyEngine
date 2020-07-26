package OxyEngine.Core.OxyObjects.Model;

import OxyEngine.Core.Camera.OxyCamera;
import OxyEngine.Core.OxyObjects.OxyEntity;
import OxyEngine.Core.Renderer.Buffer.BufferTemplate;
import OxyEngine.Core.OxyComponents.ModelMeshComponent;
import OxyEngine.Core.Renderer.OxyRenderer3D;
import OxyEngine.Core.Renderer.Texture.OxyColor;
import OxyEngine.Core.Renderer.Texture.OxyTexture;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class OxyModel extends OxyEntity {

    final float[] normals;
    final ModelMeshComponent mesh;
    final OxyRenderer3D renderer;
    OxyTexture texture;
    OxyColor color;

    private final List<Vector3f> allNonTransformVertices;

    public OxyModel(OxyRenderer3D renderer, ModelSpec spec, List<Vector3f> allNonTransformVertices, float[] vertices, int[] indices, float[] textureCoords, float[] normals) {
        this.allNonTransformVertices = allNonTransformVertices;
        this.vertices = vertices;
        this.tcs = textureCoords;
        this.normals = normals;
        this.indices = indices;
        this.renderer = renderer;
        this.position = spec.getPosition();
        this.rotation = spec.getRotation();
        this.scale = spec.getScale();

        if (spec.getColor() == null) this.texture = spec.getTexture();
        else this.color = spec.getColor();

        color.init();
        mesh = new ModelMeshComponent.ModelMeshBuilderImpl()
                .setMode(GL_TRIANGLES)
                .setUsage(BufferTemplate.Usage.DYNAMIC)
                .setVertices(vertices)
                .setIndices(indices)
                .setTextureCoords(textureCoords)
                .setNormals(normals)
                .create();
    }

    public static class ModelSpec {

        private final String objName;
        private final Vector3f position, rotation;
        private final float scale;
        private OxyTexture texture;
        private OxyColor color;

        private ModelSpec(String objName, Vector3f position, Vector3f rotation, float scale) {
            this.objName = objName;
            this.position = position;
            this.rotation = rotation;
            this.scale = scale;
        }

        public ModelSpec(String objName, OxyTexture texture, Vector3f position, Vector3f rotation, float scale) {
            this(objName, position, rotation, scale);
            this.texture = texture;
        }

        public ModelSpec(String objName, OxyTexture texture, Vector3f position, Vector3f rotation) {
            this(objName, position, rotation, 1);
            this.texture = texture;
        }

        public ModelSpec(String objName, OxyColor color, Vector3f position, Vector3f rotation, float scale) {
            this(objName, position, rotation, scale);
            this.color = color;
        }

        public ModelSpec(String objName, OxyColor color, Vector3f position, Vector3f rotation) {
            this(objName, position, rotation, 1);
            this.color = color;
        }

        public ModelSpec(String objName, OxyColor color) {
            this.objName = objName;
            this.color = color;
            this.position = new Vector3f(0, 0, 0);
            this.rotation = new Vector3f(0, 0, 0);
            this.scale = 1;
        }

        public Vector3f getRotation() {
            return rotation;
        }

        public Vector3f getPosition() {
            return position;
        }

        public OxyTexture getTexture() {
            return texture;
        }

        public String getObjName() {
            return objName;
        }

        public float getScale() {
            return scale;
        }

        public OxyColor getColor() {
            return color;
        }
    }

    @Override
    public void updateData() {
        transform = new Matrix4f()
                .scale(scale)
                .translate(position.x, position.y, position.z)
                .rotateX((float) Math.toRadians(rotation.x))
                .rotateY((float) Math.toRadians(rotation.y))
                .rotateZ((float) Math.toRadians(rotation.z));

        int vertPtr = 0;
        int slot = 0;
        if (texture != null) slot = texture.getTextureSlot();
        for (int i = 0; i < allNonTransformVertices.size() / 3; i++) {
            Vector3f v = allNonTransformVertices.get(i);
            Vector4f transformed = new Vector4f(v, 1.0f).mul(transform);
            vertices[vertPtr++] = transformed.x;
            vertices[vertPtr++] = transformed.y;
            vertices[vertPtr++] = transformed.z;
            vertices[vertPtr++] = slot;
        }
    }

    public void render(OxyCamera camera) {
        glEnable(GL_CULL_FACE);
        color.init();
        renderer.render(mesh, camera);
        glDisable(GL_CULL_FACE);
    }

    public ModelMeshComponent getMesh() {
        return mesh;
    }

    public OxyColor getColor() {
        return color;
    }
}
