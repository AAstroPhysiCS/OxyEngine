package OxyEngine.Scene;

import OxyEngine.Components.*;
import OxyEngine.Core.Camera.OxyCamera;
import OxyEngine.Core.Renderer.Buffer.OpenGLMesh;
import OxyEngine.PhysX.OxyPhysXComponent;
import OxyEngine.Scripting.OxyScript;
import OxyEngine.Scene.Objects.Model.OxyModel;
import OxyEngineEditor.UI.Panels.GUINode;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

import static OxyEngine.Globals.toPrimitiveFloat;
import static OxyEngine.Globals.toPrimitiveInteger;
import static org.lwjgl.opengl.GL45.glBindTextureUnit;

public abstract class OxyEntity {

    private final List<OxyScript> scripts = new ArrayList<>();
    private final List<GUINode> guiNodes = new ArrayList<>();

    private EntityFamily family = new EntityFamily();

    public float[] vertices, tcs, normals, tangents, biTangents;
    public int[] indices;

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

    public abstract OxyEntity copyMe();

    public void unbindTextures() {
        for (int i = 1; i <= 5; i++) {
            glBindTextureUnit(i, 0);
        }
    }

    public void setFamily(EntityFamily component) {
        this.family = component;
    }

    public EntityFamily getFamily() {
        return family;
    }

    protected void addToScene() {
        scene.put(this);
    }

    public abstract void constructData();

    public void transformLocally() {
        TransformComponent c = get(TransformComponent.class);
        c.transform = new Matrix4f()
                .translate(c.position)
                .rotateX((float) Math.toRadians(c.rotation.x))
                .rotateY((float) Math.toRadians(c.rotation.y))
                .rotateZ((float) Math.toRadians(c.rotation.z))
                .scale(c.scale);

        var root = getRoot();
        if (root != null)
            c.transform.mulLocal(root.get(TransformComponent.class).transform);

        c.transform.getTranslation(c.worldSpacePosition);
    }

    public void addComponent(EntityComponent... component) {
        scene.addComponent(this, component);
        for (EntityComponent c : component) {
            if (c instanceof OpenGLMesh m) {
                m.addToList(this);
            }

            //Camera position and rotation from the entity to the camera component
            if (c instanceof OxyCamera m) {
                TransformComponent t = this.get(TransformComponent.class);
                m.setPosition(t.worldSpacePosition);
                m.setRotation(t.rotation);
            }
        }
    }

    public List<OxyEntity> getEntitiesRelatedTo() {
        List<OxyEntity> related = new ArrayList<>();
        for (var ent : scene.getEntities()) {
            if (ent.equals(this)) continue;
            EntityFamily c = ent.getFamily();
            if (c == this.getFamily()) {
                related.add(ent);
            } else if (c.root() != null) { //bcs in case of null, null == null would be true... and we dont want that
                if (c.root() == this.getFamily()) related.add(ent);
            }
        }
        return related;
    }

    public static void addParentTransformToChildren(OxyEntity root) {
        List<OxyEntity> relatedEntities = root.getEntitiesRelatedTo();
        if (relatedEntities == null) return;
        if (relatedEntities.size() == 0) return;
        for (OxyEntity m : relatedEntities) {
            m.transformLocally();
            if(m.has(OxyPhysXComponent.class))
                m.get(OxyPhysXComponent.class).getActor().setGlobalPose(m.get(TransformComponent.class).transform);
            addParentTransformToChildren(m);
        }
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

    public OxyEntity getRoot() {
        return scene.getRoot(this);
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
            if (oxyObj.indices == null) continue;
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

    public boolean familyHasRoot() {
        return getFamily().root() != null;
    }

    public List<OxyScript> getScripts() {
        return scripts;
    }
}