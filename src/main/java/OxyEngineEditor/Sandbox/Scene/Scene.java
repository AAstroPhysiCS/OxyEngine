package OxyEngineEditor.Sandbox.Scene;

import OxyEngine.Core.Camera.OxyCamera;
import OxyEngine.Core.Renderer.Buffer.FrameBuffer;
import OxyEngine.Core.Renderer.Buffer.Mesh;
import OxyEngine.Core.Renderer.OxyRenderer;
import OxyEngine.Core.Renderer.OxyRenderer3D;
import OxyEngine.Core.Window.WindowHandle;
import OxyEngine.OpenGL.OpenGLRendererAPI;
import OxyEngine.System.OxyDisposable;
import OxyEngineEditor.Sandbox.OxyComponents.EntityComponent;
import OxyEngineEditor.Sandbox.OxyComponents.GameObjectMesh;
import OxyEngineEditor.Sandbox.OxyComponents.ModelMesh;
import OxyEngineEditor.Sandbox.OxyComponents.TransformComponent;
import OxyEngineEditor.UI.OxyUISystem;

import java.util.LinkedHashSet;
import java.util.Set;

import static OxyEngineEditor.Sandbox.Sandbox3D.camera;

public class Scene implements OxyDisposable {

    private final Registry registry = new Registry();

    private final OxyRenderer3D renderer;
    private final WindowHandle windowHandle;
    private OxyUISystem oxyUISystem;

    public Scene(WindowHandle windowHandle, OxyRenderer3D renderer) {
        this.renderer = renderer;
        this.windowHandle = windowHandle;
    }

    public final OxyGameObject createGameObjectEntity() {
        OxyGameObject e = new OxyGameObject(this);
        registry.componentList.put(e, new LinkedHashSet<>(10));
        e.addComponent(new TransformComponent());
        return e;
    }

    public final OxyModel createModelEntity(ModelFileType type, String... paths) {
        OxyModel e = new OxyModel(this);
        OxyModelLoader loader;
        switch (type) {
            case OBJ -> {
                if (paths.length < 2 || paths[0].contains(".mtl") || paths[1].contains(".obj"))
                    throw new IllegalStateException("Not enough parameters are given or in the wrong order!");
                loader = new OxyObjFileLoader(paths[0], paths[1]);
            }
            default -> throw new IllegalStateException("Unexpected value: " + type);
        }

        registry.componentList.put(e, new LinkedHashSet<>(10));
        e.addComponent(new TransformComponent(), new ModelTemplate(loader.vertices, loader.textureCoords, loader.normals, loader.faces));
        e.initData();
        return e;
    }

    private Set<EntityComponent> cachedGameObjectsEntities;
    public static FrameBuffer currentFrameBuffer;

    public void build() {
        oxyUISystem = new OxyUISystem(this, windowHandle);
        cachedGameObjectsEntities = distinct(GameObjectMesh.class, ModelMesh.class);
        //Prep
        {
            for (EntityComponent e : cachedGameObjectsEntities) {
                ((Mesh) e).initList();
            }
        }
    }

    public void rebuild() {
        //Prep
        {
            for (EntityComponent e : cachedGameObjectsEntities) {
                Mesh mesh = (Mesh) e;
                mesh.clear();
                mesh.initList();
            }
        }
    }

    public void update(float deltaTime){
        oxyUISystem.updateImGuiContext(deltaTime);
    }

    public void render(float deltaTime) {

        //Framebuffer
        {
            for (EntityComponent e : cachedGameObjectsEntities) {
                if(e instanceof Mesh mesh) {
                    if (mesh.getFrameBuffer() != null) {
                        if (mesh.getFrameBuffer().isPrimary()) {
                            currentFrameBuffer = mesh.getFrameBuffer();
                            break;
                        }
                    }
                }
            }
        }


        if (currentFrameBuffer != null) currentFrameBuffer.bind();
        OpenGLRendererAPI.clearBuffer();

        //Rendering
        {
            for (EntityComponent c : cachedGameObjectsEntities) {
                render((Mesh) c, camera);
            }
        }
        if (currentFrameBuffer != null) currentFrameBuffer.unbind();
        oxyUISystem.render(registry.componentList.keySet(), camera);
    }

    public final OxyEntity getEntityByIndex(int index) {
        int i = 0;
        for (OxyEntity e : registry.componentList.keySet()) {
            if (i == index) {
                return e;
            }
            i++;
        }
        return null;
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

    @SafeVarargs
    public final Set<OxyEntity> group(Class<? extends EntityComponent>... destClasses) {
        return registry.group(destClasses);
    }

    @SafeVarargs
    public final Set<EntityComponent> distinct(Class<? extends EntityComponent>... destClasses) {
        return registry.distinct(destClasses);
    }

    private void render(Mesh mesh, OxyCamera camera) {
        renderer.render(mesh, camera);
        OxyRenderer.Stats.totalShapeCount = registry.componentList.keySet().size();
    }

    private void render(Mesh mesh) {
        renderer.render(mesh);
        OxyRenderer.Stats.totalShapeCount = registry.componentList.keySet().size();
    }

    public OxyRenderer3D getRenderer() {
        return renderer;
    }

    public OxyUISystem getOxyUISystem() {
        return oxyUISystem;
    }

    public Set<OxyEntity> getEntities() {
        return registry.componentList.keySet();
    }

    @Override
    public void dispose() {
        oxyUISystem.dispose();
    }
}
