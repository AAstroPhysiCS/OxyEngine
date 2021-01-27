package OxyEngineEditor.Scene;

import OxyEngine.Components.*;
import OxyEngine.Core.Renderer.Buffer.OpenGLMesh;
import OxyEngine.Core.Renderer.Light.Light;
import OxyEngine.Core.Renderer.Mesh.ModelMeshOpenGL;
import OxyEngine.Scripting.OxyScript;
import OxyEngineEditor.Scene.Objects.Model.OxyMaterial;
import OxyEngineEditor.Scene.Objects.Model.OxyMaterialPool;
import OxyEngineEditor.Scene.Objects.Model.OxyModel;
import OxyEngineEditor.UI.Panels.GUINode;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static OxyEngine.Globals.toPrimitiveFloat;
import static OxyEngine.Globals.toPrimitiveInteger;
import static org.lwjgl.opengl.GL45.glBindTextureUnit;

public abstract class OxyEntity {

    private final List<OxyScript> scripts = new ArrayList<>();
    private final List<GUINode> guiNodes = new ArrayList<>();

    public float[] vertices, tcs, normals, tangents, biTangents;
    public int[] indices;

    protected boolean root;

    protected final Scene scene;

    protected boolean importedFromFile;
    protected int objectID; //for selection

    public OxyEntity(Scene scene) {
        this.scene = scene;
    }

    public OxyEntity(OxyModel other) {
        this(other.scene);
        this.tangents = other.tangents.clone();
        this.biTangents = other.biTangents.clone();
        this.normals = other.normals.clone();
        this.vertices = other.vertices.clone();
        this.tcs = other.tcs.clone();
        this.indices = other.indices.clone();
    }

    public void setRoot(boolean root) {
        this.root = root;
    }

    public boolean isRoot() {
        return root;
    }

    public abstract OxyEntity copyMe();

    public void unbindTextures() {
        for (int i = 1; i <= 5; i++) {
            glBindTextureUnit(i, 0);
        }
    }

    protected void addToScene() {
        scene.put(this);
    }

    protected abstract void initData(String path);

    public abstract void constructData();

    public abstract void updateData();

    public void addComponent(EntityComponent... component) {
        scene.addComponent(this, component);
        for (EntityComponent c : component) {
            if (c instanceof OpenGLMesh m) {
                m.addToList(this);
            }
            //if someone decides to add a seperate TransformComponent, then validate it
            //if the entity was imported from a oxy scene file, then do not validate it, because it has been already validated.
            if (c instanceof TransformComponent t && !importedFromFile) {
                t.validate(this);
            }
        }
    }

    public List<OxyEntity> getEntitiesRelatedTo(Class<? extends EntityComponent> familyComponentClass) {
        if (!this.has(familyComponentClass)) return null;
        List<OxyEntity> related = new ArrayList<>();
        for (var ent : scene.getEntities()) {
            if (ent.equals(this)) continue;
            if (ent.has(familyComponentClass)) {
                EntityComponent c = ent.get(familyComponentClass);
                if (c == this.get(familyComponentClass)) {
                    related.add(ent);
                }
            }
        }
        return related;
    }

    @SafeVarargs
    public final void removeComponent(Class<? extends EntityComponent>... components) {
        for (var classes : components) {
            scene.removeComponent(this, this.get(classes));
        }
    }

    public void addComponent(List<EntityComponent> component) {
        for (EntityComponent c : component) {
            scene.addComponent(this, c);
            if (c instanceof OpenGLMesh m) {
                m.addToList(this);
            }
            //if someone decides to add a seperate TransformComponent, then validate it
            //if the entity was imported from a oxy scene file, then do not validate it, because it has been already validated.
            if (c instanceof TransformComponent t && !importedFromFile) {
                t.validate(this);
            }
        }
    }

    public void addScript(OxyScript component) {
        component.setScene(scene);
        component.setEntity(this);
        component.loadAssembly();
        getGUINodes().add(component.guiNode);
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

    public <T extends EntityComponent> OxyEntity getRoot(Class<T> destClass){
        return scene.getRoot(this, destClass);
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

    public float[] getVertices() {
        return vertices;
    }

    public int[] getIndices() {
        return indices;
    }

    public float[] getTcs() {
        return tcs;
    }

    public int getObjectId() {
        return objectID;
    }

    public List<GUINode> getGUINodes() {
        return guiNodes;
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void dump(int i, OxyJSON.OxyJSONArray arr) {
        int meshPos = -1;
        String tag = "null";
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
        if (has(OxyMaterialIndex.class)) {
            OxyMaterial m = OxyMaterialPool.getMaterial(this);
            if (m.albedoColor != null) albedoColor = Arrays.toString(m.albedoColor.getNumbers());
            if (m.albedoTexture != null) albedoTexture = m.albedoTexture.getPath();
            if (m.normalTexture != null) normalTexture = m.normalTexture.getPath();
            normalTextureStrength = String.valueOf(m.normalStrength[0]);
            if (m.roughnessTexture != null) roughnessTexture = m.roughnessTexture.getPath();
            roughnessTextureStrength = String.valueOf(m.roughness[0]);
            if (m.metallicTexture != null) metallicTexture = m.metallicTexture.getPath();
            metalnessTextureStrength = String.valueOf(m.metalness[0]);
            if (m.aoTexture != null) aoTexture = m.aoTexture.getPath();
            aoTextureStrength = String.valueOf(m.aoStrength[0]);
        }
        if (has(ModelMeshOpenGL.class)) mesh = get(ModelMeshOpenGL.class).getPath();
        if (has(Light.class)) emitting = true;

        var obj = arr.createOxyJSONObject("OxyModel " + i)
                .putField("ID", id)
                .putField("Mesh Position", String.valueOf(meshPos))
                .putField("Name", tag)
                .putField("Emitting", String.valueOf(emitting))
                .putField("Emitting Type", emitting ? get(Light.class).getClass().getSimpleName() : "null")
                .putField("Position", transform.position.toString())
                .putField("Rotation", transform.rotation.toString())
                .putField("Scale", transform.scale.toString())
                .putField("Bounds Min", minBound.toString())
                .putField("Bounds Max", maxBound.toString())
                .putField("Material Name", Objects.requireNonNull(OxyMaterialPool.getMaterial(get(OxyMaterialIndex.class).index())).name)
                .putField("Color", albedoColor)
                .putField("Albedo Texture", albedoTexture)
                .putField("Normal Map Texture", normalTexture)
                .putField("Normal Map Strength", normalTextureStrength)
                .putField("Roughness Map Texture", roughnessTexture)
                .putField("Roughness Map Strength", roughnessTextureStrength)
                .putField("AO Map Texture", aoTexture)
                .putField("AO Map Strength", aoTextureStrength)
                .putField("Metallic Map Texture", metallicTexture)
                .putField("Metallic Map Strength", metalnessTextureStrength)
                .putField("Mesh", mesh)
                .createInnerObject("Script");
        for (var scripts : getScripts()) obj.putField("Path", scripts.getPath());
    }

    public List<OxyScript> getScripts() {
        return scripts;
    }
}