package OxyEngineEditor.Scene;

import OxyEngine.Core.Renderer.Buffer.Mesh;
import OxyEngine.Events.OxyEventListener;
import OxyEngineEditor.Components.EntityComponent;
import OxyEngineEditor.Components.ScriptingComponent;
import OxyEngineEditor.Components.TransformComponent;
import OxyEngineEditor.Scene.Objects.Model.OxyModel;
import OxyEngineEditor.Scene.Objects.Native.ObjectType;
import OxyEngineEditor.UI.Panels.PropertyEntry;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

import static OxyEngine.System.OxyEventSystem.eventDispatcher;
import static OxyEngine.Tools.Globals.toPrimitiveFloat;
import static OxyEngine.Tools.Globals.toPrimitiveInteger;

public abstract class OxyEntity {

    private final List<PropertyEntry> nodes = new ArrayList<>();

    public float[] vertices, tcs, normals, tangents, biTangents;
    public int[] indices;

    public Vector3f originPos;
    protected final Scene scene;

    public OxyEntity(Scene scene) {
        this.scene = scene;
    }

    public OxyEntity(OxyModel other) {
        this(other.scene);
        this.tangents = other.tangents.clone();
        this.biTangents = other.biTangents.clone();
        this.originPos = new Vector3f(other.originPos);
        this.normals = other.normals.clone();
        this.vertices = other.vertices.clone();
        this.tcs = other.tcs.clone();
        this.indices = other.indices.clone();
    }

    public abstract OxyEntity copyMe();

    protected void addToScene() {
        scene.put(this);
    }

    protected abstract void initData(String path);

    public abstract void constructData();

    public abstract void updateData();

    public void addComponent(EntityComponent... component) {
        scene.addComponent(this, component);
        for (EntityComponent c : component) {
            if (c instanceof Mesh m) {
                m.addToList(this);
            }
            if (c instanceof TransformComponent t) { //if someone decides to add a seperate TransformComponent, then validate it
                t.validate(this);
            }
            if (c instanceof ScriptingComponent s) {
                s.setScene(scene);
                s.setEntity(this);
                s.finalizeComponent();
            }
        }
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
    public <T extends EntityComponent> T get(Class<T> destClass) {
        return scene.get(this, destClass);
    }

    public static float[] sumAllVertices(OxyEntity[] arr, ObjectType type) {
        float[] allVertices = new float[arr.length * type.n_Vertices()];
        int ptr = 0;
        for (OxyEntity oxyObj : arr) {
            for (int i = 0; i < oxyObj.vertices.length; i++) {
                allVertices[ptr++] = oxyObj.vertices[i];
            }
        }
        return allVertices;
    }

    public static int[] sumAllIndices(OxyEntity[] arr, ObjectType type) {
        int[] allIndices = new int[arr.length * type.n_Indices()];
        int ptr = 0;
        for (OxyEntity oxyObj : arr) {
            for (int i = 0; i < oxyObj.indices.length; i++) {
                allIndices[ptr++] = oxyObj.indices[i];
            }
        }
        return allIndices;
    }

    public static float[] sumAllVertices(List<OxyEntity> arr) {
        List<Float> allVertices = new ArrayList<>();
        for (OxyEntity oxyObj : arr) {
            for (int i = 0; i < oxyObj.vertices.length; i++) {
                allVertices.add(oxyObj.vertices[i]);
            }
        }
        return toPrimitiveFloat(allVertices);
    }

    public static int[] sumAllIndices(List<OxyEntity> arr) {
        List<Integer> allIndices = new ArrayList<>();
        for (OxyEntity oxyObj : arr) {
            for (int i = 0; i < oxyObj.indices.length; i++) {
                allIndices.add(oxyObj.indices[i]);
            }
        }
        return toPrimitiveInteger(allIndices);
    }

    public void addEventListener(OxyEventListener listener) {
        eventDispatcher.addListeners(this, listener);
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

    public List<PropertyEntry> getPropertyEntries() {
        return nodes;
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }
}