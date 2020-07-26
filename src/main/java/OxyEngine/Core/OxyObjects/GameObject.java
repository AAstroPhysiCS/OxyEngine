package OxyEngine.Core.OxyObjects;

import OxyEngine.Core.OxyComponents.GameObjectMeshComponent;
import OxyEngine.Core.Renderer.Texture.OxyColor;
import OxyEngine.Core.Renderer.Texture.OxyTexture;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;

public abstract class GameObject extends OxyEntity {

    protected OxyTexture texture;
    protected OxyColor color;

    public GameObject(OxyTexture texture, Vector3f position, Vector3f rotation, float scale) {
        this.texture = texture;
        this.rotation = rotation;
        this.position = position;
        this.scale = scale;
        this.transform = new Matrix4f()
                .scale(scale)
                .translate(position.x, position.y, position.z)
                .rotateX(rotation.x)
                .rotateY(rotation.y)
                .rotateZ(rotation.z);
        constructData();
    }

    public GameObject(OxyTexture texture, Vector3f position, Vector3f rotation) {
        this(texture, position, rotation, 1);
    }

    public GameObject(int slot, Vector3f position, Vector3f rotation, float scale) {
        for (OxyTexture t : OxyTexture.allTextures) {
            if (t.getTextureSlot() == slot) {
                this.texture = t;
            }
        }
        this.position = position;
        this.rotation = rotation;
        this.scale = scale;
        this.transform = new Matrix4f()
                .scale(scale)
                .translate(position.x, position.y, position.z)
                .rotateX(rotation.x)
                .rotateY(rotation.y)
                .rotateZ(rotation.z);
        constructData();
    }

    public GameObject(int slot, Vector3f position, Vector3f rotation) {
        this(slot, position, rotation, 1);
    }


    public GameObject(OxyColor color, Vector3f position, Vector3f rotation, float scale) {
        this.color = color;
        this.position = position;
        this.rotation = rotation;
        this.scale = scale;
        color.init();
        this.transform = new Matrix4f()
                .scale(scale)
                .translate(position.x, position.y, position.z)
                .rotateX(rotation.x)
                .rotateY(rotation.y)
                .rotateZ(rotation.z);
        constructData();
    }

    public GameObject(OxyColor color, Vector3f position, Vector3f rotation) {
        this(color, position, rotation, 1);
    }

    public enum Type {
        //TCS: 48
        Cube(144, 36),
        Grid(24, 6);

        private final int n_Vertices;
        private final int n_Indices;

        Type(int n_Vertices, int n_Indices) {
            this.n_Indices = n_Indices;
            this.n_Vertices = n_Vertices;
        }

        public int n_Indices() {
            return n_Indices;
        }

        public int n_Vertices() {
            return n_Vertices;
        }
    }

    public static float[] sumAllVertices(OxyEntity[] arr, Type type) {
        float[] allVertices = new float[arr.length * type.n_Vertices()];
        int ptr = 0;
        for (OxyEntity oxyObj : arr) {
            for (int i = 0; i < oxyObj.vertices.length; i++) {
                allVertices[ptr++] = oxyObj.vertices[i];
            }
        }
        return allVertices;
    }

    public static int[] sumAllIndices(OxyEntity[] arr, Type type) {
        int[] allIndices = new int[arr.length * type.n_Indices()];
        int ptr = 0;
        for (OxyEntity oxyObj : arr) {
            for (int i = 0; i < oxyObj.indices.length; i++) {
                allIndices[ptr++] = oxyObj.indices[i];
            }
        }
        return allIndices;
    }

    public static float[] sumAllVertices(List<OxyEntity> arr, Type type) {
        float[] allVertices = new float[arr.size() * type.n_Vertices()];
        int ptr = 0;
        for (OxyEntity oxyObj : arr) {
            for (int i = 0; i < oxyObj.vertices.length; i++) {
                allVertices[ptr++] = oxyObj.vertices[i];
            }
        }
        return allVertices;
    }

    public static int[] sumAllIndices(List<OxyEntity> arr, GameObject.Type type) {
        int[] allIndices = new int[arr.size() * type.n_Indices()];
        int ptr = 0;
        for (OxyEntity oxyObj : arr) {
            for (int i = 0; i < oxyObj.indices.length; i++) {
                allIndices[ptr++] = oxyObj.indices[i];
            }
        }
        return allIndices;
    }

    @Override
    public void updateData() {
        transform = new Matrix4f()
                .scale(scale)
                .translate(position.x, position.y, position.z)
                .rotateX(rotation.x)
                .rotateY(rotation.y)
                .rotateZ(rotation.z);
        constructData();
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getRotation() {
        return rotation;
    }

    public float getScale() {
        return scale;
    }

    public Matrix4f getTransform() {
        return transform;
    }

    public OxyColor getColor() {
        return color;
    }

    public OxyTexture getTexture() {
        return texture;
    }

    public abstract void initData(GameObjectMeshComponent mesh);

    protected abstract void constructData();
}
