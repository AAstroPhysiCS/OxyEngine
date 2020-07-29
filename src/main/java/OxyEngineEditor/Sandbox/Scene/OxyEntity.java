package OxyEngineEditor.Sandbox.Scene;

import OxyEngine.Core.Renderer.Buffer.Mesh;
import OxyEngine.Events.OxyEventListener;
import OxyEngineEditor.Sandbox.OxyComponents.EntityComponent;

import java.util.ArrayList;
import java.util.List;

import static OxyEngine.System.Globals.Globals.toPrimitiveFloat;
import static OxyEngine.System.Globals.Globals.toPrimitiveInteger;
import static OxyEngineEditor.UI.OxyUISystem.OxyEventSystem.dispatcherThread;

public abstract class OxyEntity {

    float[] vertices, tcs, normals;
    int[] indices;

    protected ObjectType type;
    protected final Scene scene;

    public OxyEntity(Scene scene) {
        this.scene = scene;
    }

    public abstract void initData(Mesh mesh);

    public abstract void updateData();

    public final void addComponent(EntityComponent... component) {
        scene.addComponent(this, component);
    }

    /*
     * returns true if the component is already in the set
     */
    public boolean has(Class<? extends EntityComponent> destClass) {
        return scene.has(this, destClass);
    }

    /*
     * gets the component from the set
     */
    public EntityComponent get(Class<? extends EntityComponent> destClass) {
        return scene.get(this, destClass);
    }

    public static float[] sumAllVertices(OxyGameObject[] arr, ObjectType type) {
        float[] allVertices = new float[arr.length * type.n_Vertices()];
        int ptr = 0;
        for (OxyGameObject oxyObj : arr) {
            for (int i = 0; i < oxyObj.vertices.length; i++) {
                allVertices[ptr++] = oxyObj.vertices[i];
            }
        }
        return allVertices;
    }

    public static int[] sumAllIndices(OxyGameObject[] arr, ObjectType type) {
        int[] allIndices = new int[arr.length * type.n_Indices()];
        int ptr = 0;
        for (OxyGameObject oxyObj : arr) {
            for (int i = 0; i < oxyObj.indices.length; i++) {
                allIndices[ptr++] = oxyObj.indices[i];
            }
        }
        return allIndices;
    }

    public static float[] sumAllVertices(List<OxyGameObject> arr) {
        List<Float> allVertices = new ArrayList<>();
        for (OxyGameObject oxyObj : arr) {
            for (int i = 0; i < oxyObj.vertices.length; i++) {
                allVertices.add(oxyObj.vertices[i]);
            }
        }
        return toPrimitiveFloat(allVertices);
    }

    public static int[] sumAllIndices(List<OxyGameObject> arr) {
        List<Integer> allIndices = new ArrayList<>();
        for (OxyGameObject oxyObj : arr) {
            for (int i = 0; i < oxyObj.indices.length; i++) {
                allIndices.add(oxyObj.indices[i]);
            }
        }
        return toPrimitiveInteger(allIndices);
    }

    public void addEventListener(OxyEventListener listener) {
        dispatcherThread.addDispatchersToThread(this, listener);
    }

    public float[] getVertices() {
        return vertices;
    }

    public int[] getIndices() {
        return indices;
    }

    public float[] getTcs() {
        return tcs;
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ObjectType getType() {
        return type;
    }
}
