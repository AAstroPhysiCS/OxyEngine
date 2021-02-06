package OxyEngine.Scene;

import OxyEngine.Components.*;
import OxyEngine.Core.Camera.OxyCamera;
import OxyEngine.Core.Renderer.Buffer.OpenGLMesh;
import OxyEngine.Core.Renderer.Light.DirectionalLight;
import OxyEngine.Core.Renderer.Light.Light;
import OxyEngine.Core.Renderer.Light.PointLight;
import OxyEngine.Scripting.OxyScript;
import OxyEngine.Scene.Objects.Model.OxyMaterial;
import OxyEngine.Scene.Objects.Model.OxyMaterialPool;
import OxyEngine.Scene.Objects.Model.OxyModel;
import OxyEngineEditor.UI.Panels.GUINode;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public void transformLocally() {
        TransformComponent c = get(TransformComponent.class);
        c.transform = new Matrix4f()
                .translate(c.position)
                .rotateX(c.rotation.x)
                .rotateY(c.rotation.y)
                .rotateZ(c.rotation.z)
                .scale(c.scale);
    }

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

            //Camera position and rotation from the entity to the camera component
            if(c instanceof OxyCamera m){
                TransformComponent t = this.get(TransformComponent.class);
                m.setPosition(t.position);
                m.setRotation(t.rotation);
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

    public <T extends EntityComponent> OxyEntity getRoot(Class<T> destClass) {
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

    public void dump(OxyJSON.OxyJSONObject arr) {
        if (!isRoot()) throw new IllegalStateException("Only roots can be dumped");

        int meshPosRoot = -1;
        String idRoot = get(UUIDComponent.class).getUUIDString();
        String tagRoot = "null";
        if (has(MeshPosition.class)) meshPosRoot = get(MeshPosition.class).meshPos();
        if (has(TagComponent.class)) tagRoot = get(TagComponent.class).tag();
        boolean emittingRoot = false;
        if (has(Light.class)) emittingRoot = true;

        arr.putField("ID", idRoot)
                .putField("Mesh Position", String.valueOf(meshPosRoot))
                .putField("Name", tagRoot)
                .putField("Emitting", String.valueOf(emittingRoot));
        addCommonFields(arr, emittingRoot, this);

        int i = 0;
        for (OxyEntity e : getEntitiesRelatedTo(FamilyComponent.class)) {
            if (!(e instanceof OxyModel)) continue;
            int meshPos = -1;
            String id = e.get(UUIDComponent.class).getUUIDString();
            String tag = "null";
            if (e.has(MeshPosition.class)) meshPos = e.get(MeshPosition.class).meshPos();
            if (e.has(TagComponent.class)) tag = e.get(TagComponent.class).tag();
            boolean emitting = false;
            if (e.has(Light.class)) emitting = true;

            var obj = arr.createInnerObject("OxyModel " + (i++))
                    .putField("ID", id)
                    .putField("Mesh Position", String.valueOf(meshPos))
                    .putField("Name", tag)
                    .putField("Emitting", String.valueOf(emitting));

            addCommonFields(obj, emitting, e);
        }
    }

    private void addCommonFields(OxyJSON.OxyJSONObject obj, boolean emitting, OxyEntity e) {

        TransformComponent transform = e.get(TransformComponent.class);
        Vector3f minBound = new Vector3f(0, 0, 0), maxBound = new Vector3f(0, 0, 0);
        String albedoColor = "null";
        String albedoTexture = "null";
        String normalTexture = "null", normalTextureStrength = "0";
        String roughnessTexture = "null", roughnessTextureStrength = "0";
        String metallicTexture = "null", metalnessTextureStrength = "0";
        String aoTexture = "null", aoTextureStrength = "0";
        String mesh = "null";
        String materialName = "null";

        if (e.has(BoundingBoxComponent.class)) {
            minBound = e.get(BoundingBoxComponent.class).min();
            maxBound = e.get(BoundingBoxComponent.class).max();
        }
        if (e.has(OxyMaterialIndex.class)) {
            OxyMaterial m = OxyMaterialPool.getMaterial(e);
            materialName = m.name;
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
        if (e.has(OpenGLMesh.class)) mesh = e.get(OpenGLMesh.class).getPath();
        if (e.has(Light.class)) emitting = true;

        if (emitting) {
            Light l = e.get(Light.class);
            obj = obj.createInnerObject("Light Attributes")
                    .putField("Intensity", String.valueOf(l.getColorIntensity()));
            if (l instanceof PointLight p) {
                obj.putField("Constant", String.valueOf(p.getConstantValue()));
                obj.putField("Linear", String.valueOf(p.getLinearValue()));
                obj.putField("Quadratic", String.valueOf(p.getQuadraticValue()));
            } else if (l instanceof DirectionalLight d) {
                obj.putField("Direction", d.getDirection().toString());
            }
            obj = obj.backToObject();
        }

        obj = obj.putField("Emitting Type", emitting ? e.get(Light.class).getClass().getSimpleName() : "null")
                .putField("Position", transform.position.toString())
                .putField("Rotation", transform.rotation.toString())
                .putField("Scale", transform.scale.toString())
                .putField("Bounds Min", minBound.toString())
                .putField("Bounds Max", maxBound.toString())
                .putField("Material Name", materialName)
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