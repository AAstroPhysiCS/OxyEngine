package OxyEngineEditor.Sandbox.Scene;

import OxyEngine.Core.Camera.OxyCamera;
import OxyEngine.Core.Renderer.Buffer.FrameBuffer;
import OxyEngine.Core.Renderer.Buffer.Mesh;
import OxyEngine.Core.Renderer.OxyRenderer;
import OxyEngine.Core.Renderer.OxyRenderer3D;
import OxyEngine.Core.Renderer.Texture.OxyColor;
import OxyEngine.Core.Window.WindowHandle;
import OxyEngine.OpenGL.OpenGLRendererAPI;
import OxyEngine.System.OxyDisposable;
import OxyEngineEditor.Sandbox.OxyComponents.*;
import OxyEngineEditor.Sandbox.Scene.InternObjects.OxyInternObject;
import OxyEngineEditor.Sandbox.Scene.Model.ModelFactory;
import OxyEngineEditor.Sandbox.Scene.Model.ModelType;
import OxyEngineEditor.Sandbox.Scene.Model.OxyModel;
import OxyEngineEditor.Sandbox.Scene.Model.OxyModelLoader;
import OxyEngineEditor.UI.OxyUISystem;

import java.util.*;

import static OxyEngineEditor.Sandbox.Sandbox3D.camera;
import static org.lwjgl.opengl.GL11.*;

public class Scene implements OxyDisposable {

    private final Registry registry = new Registry();

    private final OxyRenderer3D renderer;
    private final WindowHandle windowHandle;
    private OxyUISystem oxyUISystem;

    private Set<EntityComponent> cachedGameObjectsEntities;
    private FrameBuffer frameBuffer;

    public Scene(WindowHandle windowHandle, OxyRenderer3D renderer) {
        this.renderer = renderer;
        this.windowHandle = windowHandle;
    }

    public Scene(WindowHandle windowHandle, OxyRenderer3D renderer, FrameBuffer frameBuffer) {
        this.renderer = renderer;
        this.windowHandle = windowHandle;
        this.frameBuffer = frameBuffer;
    }

    final OxyInternObject createInternObjectEntity() {
        OxyInternObject e = new OxyInternObject(this);
        registry.componentList.put(e, new LinkedHashSet<>(10));
        e.addComponent(new TransformComponent());
        return e;
    }

    public final List<OxyModel> createModelEntity(ModelType type){
        return createModelEntity(type.getPath());
    }

    public final List<OxyModel> createModelEntity(String path) {
        List<OxyModel> models = new ArrayList<>();
        OxyModelLoader loader = new OxyModelLoader(path);

        for (OxyModelLoader.AssimpOxyMesh assimpMesh : loader.meshes) {
            OxyModel e = new OxyModel(this);
            registry.componentList.put(e, new LinkedHashSet<>(10));
            e.addComponent(
                    new TransformComponent(),
                    assimpMesh.material.texture(),
                    new OxyColor(assimpMesh.material.diffuseColor()),
                    new ModelFactory(assimpMesh.vertices, assimpMesh.textureCoords, assimpMesh.normals, assimpMesh.faces)
            );
            e.initData();
            models.add(e);
        }
        return models;
    }

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
        cachedGameObjectsEntities = distinct(GameObjectMesh.class, ModelMesh.class);
        //Prep
        {
            List<EntityComponent> cachedConverted = new ArrayList<>(cachedGameObjectsEntities);
            Mesh mesh = (Mesh) cachedConverted.get(cachedConverted.size() - 1);
            mesh.initList();
        }
    }

    public void update(float ts, float deltaTime) {
        oxyUISystem.updateImGuiContext(deltaTime);
    }

    public void render(float ts, float deltaTime) {

        if (frameBuffer != null) frameBuffer.bind();
        OpenGLRendererAPI.clearBuffer();

        //Rendering
        {
            for (EntityComponent c : cachedGameObjectsEntities) {
                if (c instanceof ModelMesh) {
                    glEnable(GL_CULL_FACE);
                    render(ts, (Mesh) c, camera);
                    glDisable(GL_CULL_FACE);
                } else {
                    render(ts, (Mesh) c, camera);
                }
            }
        }
        if (frameBuffer != null) frameBuffer.unbind();
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

    private void render(float ts, Mesh mesh, OxyCamera camera) {
        renderer.render(ts, mesh, camera);
        OxyRenderer.Stats.totalShapeCount = registry.componentList.keySet().size();
    }

    private void render(float ts, Mesh mesh) {
        renderer.render(ts, mesh);
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

    public FrameBuffer getFrameBuffer() {
        return frameBuffer;
    }

    @Override
    public void dispose() {
        oxyUISystem.dispose();
    }
}
