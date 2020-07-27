package OxyEngineEditor.Sandbox.Scene;

import OxyEngine.Core.Camera.OxyCamera;
import OxyEngine.Core.Renderer.Buffer.Mesh;
import OxyEngineEditor.Sandbox.OxyComponents.SelectedComponent;
import OxyEngineEditor.Sandbox.OxyComponents.TransformComponent;
import OxyEngineEditor.Sandbox.OxyObjects.GameObjectType;
import OxyEngineEditor.Sandbox.OxyObjects.ObjectTemplate;
import OxyEngineEditor.Sandbox.OxyObjects.OxyEntity;
import OxyEngine.Core.Renderer.OxyRenderer3D;
import OxyEngineEditor.Sandbox.OxyComponents.EntityComponent;

import java.util.LinkedHashSet;
import java.util.Set;

public class Scene {

    private final Registry registry = new Registry();

    private final OxyRenderer3D renderer;

    public Scene(OxyRenderer3D renderer) {
        this.renderer = renderer;
    }

    public final OxyEntity createEntity(ObjectTemplate template){
        OxyEntity e = new OxyEntity(this, template);
        registry.componentList.put(e, new LinkedHashSet<>(10));
        e.addComponent(new TransformComponent());
        e.addComponent(new SelectedComponent(false));
        return e;
    }

    public void updateSingleEntityData(OxyEntity entity, Mesh mesh) {
        int i = 0;
        for (OxyEntity e : registry.componentList.keySet()) {
            if (e.equals(entity)) {
                mesh.getVertexBuffer().updateSingleEntityData(i * GameObjectType.Cube.n_Vertices(), entity.getVertices());
            }
            i++;
        }
    }

    /*
     * add component to the registry
     */
    public final void addComponent(OxyEntity entity, EntityComponent... component) {
        registry.addComponent(entity, component);
    }

    /*
     * returns true if the component is already in the set
     */
    public boolean has(OxyEntity entity, Class<? extends EntityComponent> destClass) {
        return registry.has(entity, destClass);
    }

    /*
     * gets the component from the set
     */
    public EntityComponent get(OxyEntity entity, Class<? extends EntityComponent> destClass) {
        return registry.get(entity, destClass);
    }

    /*
     * gets all the entities associated with these classes
     */
    public Set<OxyEntity> view(Class<? extends EntityComponent> destClass) {
        return registry.view(destClass);
    }

    /*
     * gets all the entities associated with multiple classes
     */
    public final Set<OxyEntity> group(Class<? extends EntityComponent>... destClasses) {
        return registry.group(destClasses);
    }

    public void render(Mesh mesh, OxyCamera camera) {
        renderer.render(mesh, camera);
    }

    public void render(Mesh mesh) {
        renderer.render(mesh);
    }

    public OxyRenderer3D getRenderer() {
        return renderer;
    }

    public Set<OxyEntity> getEntities(){
        return registry.componentList.keySet();
    }
}
