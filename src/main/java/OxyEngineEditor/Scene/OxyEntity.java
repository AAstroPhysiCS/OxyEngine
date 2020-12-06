package OxyEngineEditor.Scene;

import OxyEngine.Components.*;
import OxyEngine.Core.Renderer.Buffer.Mesh;
import OxyEngine.Core.Renderer.Light.Light;
import OxyEngine.Events.OxyEventListener;
import OxyEngine.Scripting.OxyScript;
import OxyEngineEditor.Scene.Objects.Model.OxyMaterial;
import OxyEngineEditor.Scene.Objects.Model.OxyModel;
import OxyEngineEditor.Scene.Objects.Native.ObjectType;
import OxyEngineEditor.UI.Panels.GUIProperty;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
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
               \tEmitting: %s%s
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

    public String dump(int ptr) {
        int meshPos = -1;
        String tag = "null";
        String grouped = "false";
        StringBuilder scripts = new StringBuilder("[\n");
        TransformComponent transform = get(TransformComponent.class);
        Vector3f minBound = new Vector3f(0, 0, 0), maxBound = new Vector3f(0, 0, 0);
        String albedoColor = "null";
        String albedoTexture = "null";
        String normalTexture = "null", normalTextureStrength = "0";
        String roughnessTexture = "null", roughnessTextureStrength = "0";
        String metallicTexture = "null", metalnessTextureStrength = "0";
        String aoTexture = "null", aoTextureStrength = "0";
        String mesh = "null";
        String id = get(UUIDComponent.class).getUUIDString();
        boolean emitting = false;

        if (has(BoundingBoxComponent.class)) {
            minBound = get(BoundingBoxComponent.class).min();
            maxBound = get(BoundingBoxComponent.class).max();
        }
        if (has(TagComponent.class)) tag = get(TagComponent.class).tag();
        if (has(MeshPosition.class)) meshPos = get(MeshPosition.class).meshPos();
        if (has(EntitySerializationInfo.class)) grouped = String.valueOf(get(EntitySerializationInfo.class).grouped());
        if (has(OxyMaterial.class)) {
            OxyMaterial m = get(OxyMaterial.class);
            if (m.albedoColor != null) albedoColor = Arrays.toString(m.albedoColor.getNumbers());
            if (m.albedoTexture != null) albedoTexture = m.albedoTexture.getPath();
            if (m.normalTexture != null) normalTexture = m.normalTexture.getPath();
            else normalTextureStrength = String.valueOf(m.normalStrength[0]);
            if (m.roughnessTexture != null) roughnessTexture = m.roughnessTexture.getPath();
            else roughnessTextureStrength = String.valueOf(m.roughness[0]);
            if (m.metallicTexture != null) metallicTexture = m.metallicTexture.getPath();
            else metalnessTextureStrength = String.valueOf(m.metalness[0]);
            if (m.aoTexture != null) aoTexture = m.aoTexture.getPath();
            else aoTextureStrength = String.valueOf(m.aoStrength[0]);
        }
        if (has(ModelMesh.class)) mesh = get(ModelMesh.class).getPath();
        if (has(Light.class)) emitting = true;

        int size = getScripts().size();
        if (size == 0) scripts.replace(0, scripts.length(), "[]");
        else {
            for (OxyScript c : getScripts()) {
                scripts.append("\t\t\t").append(c.getPath()).append("\n");
            }
            scripts.append("\t\t").append("]");
        }

        OxySerializable objInfo = getClass().getAnnotation(OxySerializable.class);
        return objInfo.info().formatted("OxyModel", ptr, id, meshPos, tag, grouped, emitting, emitting ? ", " + get(Light.class).getClass().getSimpleName() : "",
                transform.position.x, transform.position.y, transform.position.z,
                transform.rotation.x, transform.rotation.y, transform.rotation.z,
                transform.scale.x, transform.scale.y, transform.scale.z,
                minBound.x, minBound.y, minBound.z, maxBound.x, maxBound.y, maxBound.z,
                albedoColor, scripts.toString(), albedoTexture, normalTexture, normalTextureStrength,
                roughnessTexture, roughnessTextureStrength, aoTexture, aoTextureStrength, metallicTexture, metalnessTextureStrength, mesh).trim();
    }

    public List<OxyScript> getScripts() {
        return scripts;
    }
}