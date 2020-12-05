package OxyEngineEditor.Scene;

import OxyEngine.Core.Renderer.Buffer.Mesh;
import OxyEngine.Events.OxyEventListener;
import OxyEngine.Components.EntityComponent;
import OxyEngine.Components.EntitySerializationInfo;
import OxyEngine.Scripting.OxyScript;
import OxyEngine.Components.TransformComponent;
import OxyEngineEditor.Scene.Objects.Model.OxyModel;
import OxyEngineEditor.Scene.Objects.Native.ObjectType;
import OxyEngineEditor.UI.Panels.GUIProperty;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

import static OxyEngine.System.OxyEventSystem.eventDispatcher;
import static OxyEngine.Tools.Globals.toPrimitiveFloat;
import static OxyEngine.Tools.Globals.toPrimitiveInteger;

@OxySerializable(info = """
        \t\t%s %s {
               \tID: %s
               \tMesh Position: %s
               \tName: %s
               \tGrouped: %s
               \tEmitting: %s
               \tPosition: X %s, Y %s, Z %s
               \tRotation: X %s, Y %s, Z %s
               \tScale: X %s, Y %s, Z %s
               \tBounds Min: X %s, Y %s, Z %s
               \tBounds Max: X %s, Y %s, Z %s
               \tColor: %s
               \tScripts: %s
               \tAlbedo Texture: %s
               \tNormal Map Texture: %s
               \tNormal Map Strength: %s
               \tRoughness Map Texture: %s
               \tRoughness Map Strength: %s
               \tAO Map Texture: %s
               \tAO Map Strength: %s
               \tMetallic Map Texture: %s
               \tMetallic Map Strength: %s
               \tMesh: %s
            }"""
)
public abstract class OxyEntity {

    private final List<OxyScript> scripts = new ArrayList<>();
    private final List<GUIProperty> guiProperties = new ArrayList<>();

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
            //if someone decides to add a seperate TransformComponent, then validate it
            //if the entity was imported from a oxy scene file, then do not validate it, because it has been already validated.
            if (c instanceof TransformComponent t && !get(EntitySerializationInfo.class).imported()) {
                t.validate(this);
            }
        }
    }

    public void addComponent(List<EntityComponent> component) {
        for (EntityComponent c : component) {
            scene.addComponent(this, c);
            if (c instanceof Mesh m) {
                m.addToList(this);
            }
            //if someone decides to add a seperate TransformComponent, then validate it
            //if the entity was imported from a oxy scene file, then do not validate it, because it has been already validated.
            if (c instanceof TransformComponent t && !get(EntitySerializationInfo.class).imported()) {
                t.validate(this);
            }
        }
    }

    public void addScript(OxyScript component) {
        component.setScene(scene);
        component.setEntity(this);
        component.loadAssembly();
        getGUIProperties().add(component.guiNode);
        scripts.add(component);
    }

    public void addScript(List<OxyScript> components) {
        for (OxyScript s : components) addScript(s);
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

    public List<GUIProperty> getGUIProperties() {
        return guiProperties;
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<OxyScript> getScripts() {
        return scripts;
    }
}