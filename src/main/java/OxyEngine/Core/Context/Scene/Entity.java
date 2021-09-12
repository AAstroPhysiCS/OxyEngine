package OxyEngine.Core.Context.Scene;

import OxyEngine.Components.*;
import OxyEngine.Core.Context.Renderer.Light.DirectionalLight;
import OxyEngine.Core.Context.Renderer.Light.PointLight;
import OxyEngine.Core.Context.Renderer.Mesh.OpenGLMesh;
import OxyEngine.PhysX.PhysXComponent;
import OxyEngine.Scripting.Script;
import OxyEngineEditor.UI.GUINode;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class Entity {

    private final List<Script> scripts = new ArrayList<>();
    protected final List<GUINode> guiNodes = new ArrayList<>();

    private EntityFamily family = new EntityFamily();

    protected final Scene scene;

    Entity(Scene scene) {
        this.scene = scene;
    }

    Entity(Entity other) {
        this.scene = other.scene;
        scene.put(this);

        this.addComponent(new TransformComponent(other.get(TransformComponent.class)),
                new TagComponent(other.get(TagComponent.class).tag() == null ? "Unnamed" : other.get(TagComponent.class).tag()),
                new SelectedComponent(false)
        );

        if (other.has(PhysXComponent.class)) {
            PhysXComponent physXComponent = new PhysXComponent(other.get(PhysXComponent.class), this);
            this.addComponent(physXComponent);
        }

        this.setFamily(new EntityFamily(other.getFamily().root()));

        if (other.has(PointLight.class)) this.addComponent(other.get(PointLight.class));
        if (other.has(DirectionalLight.class)) this.addComponent(other.get(DirectionalLight.class));
        if (other.has(AnimationComponent.class))
            this.addComponent(new AnimationComponent(other.get(AnimationComponent.class)));

        //SCRIPTS (with GUINode-Script)
        for (Script s : other.getScripts()) this.addScript(new Script(s.getPath()));

        //adding all the parent gui nodes (except OxyScript, bcs that gui node is instance dependent)
        this.getGUINodes().addAll(other.getGUINodes().stream().filter(c -> !(c instanceof Script)).collect(Collectors.toList()));

        SceneRuntime.runtimeStop();

        if (other.has(OpenGLMesh.class)) {
            this.addComponent(new OpenGLMesh(other.get(OpenGLMesh.class)));
        }

        copyChildRecursive(this);
    }

    private void copyChildRecursive(Entity parent) {
        for (Entity child : getEntitiesRelatedTo()) {
            Entity copy = new Entity(child);
            copy.setFamily(new EntityFamily(parent.getFamily()));
        }
    }

    public void setFamily(EntityFamily component) {
        this.family = component;
    }

    public EntityFamily getFamily() {
        return family;
    }

    public void updateTransform() {
        TransformComponent c = get(TransformComponent.class);
        Entity root = getRoot();

        c.transform.identity();

        if(root != null)
            c.transform.mul(root.getTransform());

        c.transform.translate(c.position)
                .rotateX(c.rotation.x)
                .rotateY(c.rotation.y)
                .rotateZ(c.rotation.z);

        c.transform.scale(c.scale);

        addParentTransformToChildren(this);
    }

    public void addComponent(EntityComponent... component) {
        scene.addComponent(this, component);
    }

    public List<Entity> getEntitiesRelatedTo() {
        List<Entity> related = new ArrayList<>();
        for (var ent : scene.getEntities()) {
            if (ent.equals(this)) continue;
            EntityFamily c = ent.getFamily();
            if (c == this.getFamily()) {
                related.add(ent);
            } else if (c.root() != null && c.root() == this.getFamily()) {
                related.add(ent);
            }
        }
        return related;
    }

    private static void addParentTransformToChildren(Entity root) {
        List<Entity> relatedEntities = root.getEntitiesRelatedTo();
        if (relatedEntities.size() == 0) return;
        for (Entity m : relatedEntities) {
            m.updateTransform();
            if (m.has(PhysXComponent.class))
                m.get(PhysXComponent.class).getActor().setGlobalPose(m.get(TransformComponent.class).transform);
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
        }
    }

    public void addScript(Script component) {
        component.setScene(scene);
        component.setEntity(this);
        component.loadAssembly();
        getGUINodes().add(component.guiNode);
        scripts.add(component);
    }

    public void addScript(List<Script> components) {
        for (Script s : components) addScript(s);
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

    public Vector3f getPosition() {
        return scene.get(this, TransformComponent.class).position;
    }

    public Vector3f getRotation() {
        return scene.get(this, TransformComponent.class).rotation;
    }

    public Matrix4f getTransform() {
        return scene.get(this, TransformComponent.class).transform;
    }

    public Entity getRoot() {
        return scene.getRoot(this);
    }

    public List<GUINode> getGUINodes() {
        return guiNodes;
    }

    public boolean familyHasRoot() {
        return getFamily().root() != null;
    }

    public List<Script> getScripts() {
        return scripts;
    }
}