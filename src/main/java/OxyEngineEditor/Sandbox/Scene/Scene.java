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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Scene implements OxyDisposable {

    private final Registry registry = new Registry();

    private final OxyRenderer3D renderer;
    private final WindowHandle windowHandle;
    private OxyUISystem oxyUISystem;

    private Set<OxyEntity> cachedModelEntities;
    private Set<EntityComponent> cachedInternMeshes, cachedModelMeshes, cachedCameraComponents;

    private final FrameBuffer frameBuffer;
    private final String sceneName;

    public Scene(String sceneName, WindowHandle windowHandle, OxyRenderer3D renderer, FrameBuffer frameBuffer) {
        this.renderer = renderer;
        this.windowHandle = windowHandle;
        this.frameBuffer = frameBuffer;
        this.sceneName = sceneName;
    }

    public final OxyInternObject createInternObjectEntity() {
        OxyInternObject e = new OxyInternObject(this);
        registry.entityList.put(e, new LinkedHashSet<>(10));
        e.addComponent(new TransformComponent());
        return e;
    }

    public final List<OxyModel> createModelEntities(ModelType type) {
        return createModelEntities(type.getPath());
    }

    public final List<OxyModel> createModelEntities(String path) {
        List<OxyModel> models = new ArrayList<>();
        OxyModelLoader loader = new OxyModelLoader(path);

        for (OxyModelLoader.AssimpOxyMesh assimpMesh : loader.meshes) {
            OxyModel e = new OxyModel(this);
            registry.entityList.put(e, new LinkedHashSet<>(10));
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

    public final OxyModel createModelEntity(String path) {
        OxyModelLoader loader = new OxyModelLoader(path);
        OxyModelLoader.AssimpOxyMesh assimpMesh = loader.meshes.get(0);
        OxyModel e = new OxyModel(this);
        registry.entityList.put(e, new LinkedHashSet<>(10));
        e.addComponent(
                new TransformComponent(),
                assimpMesh.material.texture(),
                new OxyColor(assimpMesh.material.diffuseColor()),
                new ModelFactory(assimpMesh.vertices, assimpMesh.textureCoords, assimpMesh.normals, assimpMesh.faces)
        );
        e.initData();
        return e;
    }

    public void build() {
        oxyUISystem = new OxyUISystem(this, windowHandle);
        cachedInternMeshes = distinct(InternObjectMesh.class);
        cachedModelMeshes = distinct(ModelMesh.class);
        cachedCameraComponents = distinct(OxyCamera.class);
        //Prep
        {
            for (EntityComponent e : cachedInternMeshes) {
                ((Mesh) e).initList();
            }
            for (EntityComponent model : cachedModelMeshes) {
                ((ModelMesh) model).initList();
            }
        }
    }

    public void rebuild() {
        cachedModelMeshes = distinct(ModelMesh.class);
        //Prep
        {
            List<EntityComponent> cachedConverted = new ArrayList<>(cachedModelMeshes);
            ModelMesh mesh = (ModelMesh) cachedConverted.get(cachedConverted.size() - 1);
            mesh.initList();
        }
    }

    public void update(float ts, float deltaTime) {
        oxyUISystem.updateImGuiContext(deltaTime);
    }

    public void render(float ts, float deltaTime) {

        if (frameBuffer != null) frameBuffer.bind();
        OpenGLRendererAPI.clearBuffer();

        //Camera
        PerspectiveCamera mainCamera = null;
        {
            for (EntityComponent camera : cachedCameraComponents) {
                if (camera instanceof PerspectiveCamera p) {
                    if (p.isPrimary()) {
                        mainCamera = p;
                        break;
                    }
                }
            }
        }

        //Rendering
        {
            for (EntityComponent c : cachedModelMeshes) {
                if (c != null && mainCamera != null)
                    render(ts, (Mesh) c, mainCamera);
            }

            for (EntityComponent c : cachedInternMeshes) {
                render(ts, (Mesh) c);
            }
        }
        if (frameBuffer != null) frameBuffer.unbind();
        oxyUISystem.render(registry.entityList.keySet(), OxyRenderer.currentBoundedCamera);
    }

    public final OxyEntity getEntityByIndex(int index) {
        int i = 0;
        for (OxyEntity e : registry.entityList.keySet()) {
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
        OxyRenderer.Stats.totalShapeCount = registry.entityList.keySet().size();
    }

    private void render(float ts, Mesh mesh) {
        renderer.render(ts, mesh);
        OxyRenderer.Stats.totalShapeCount = registry.entityList.keySet().size();
    }

    public OxyRenderer3D getRenderer() {
        return renderer;
    }

    public OxyUISystem getOxyUISystem() {
        return oxyUISystem;
    }

    public Set<OxyEntity> getEntities() {
        return registry.entityList.keySet();
    }

    public FrameBuffer getFrameBuffer() {
        return frameBuffer;
    }

    public String getSceneName() {
        return sceneName;
    }

    @Override
    public void dispose() {
        oxyUISystem.dispose();
    }
}
