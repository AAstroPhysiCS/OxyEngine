package OxyEngineEditor.Sandbox.Scene;

import OxyEngine.Core.Camera.OxyCamera;
import OxyEngine.Core.Renderer.Buffer.FrameBuffer;
import OxyEngine.Core.Renderer.Buffer.Mesh;
import OxyEngine.Core.Renderer.Light.Light;
import OxyEngine.Core.Renderer.OxyRenderer;
import OxyEngine.Core.Renderer.OxyRenderer3D;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Core.Renderer.Texture.CubemapTexture;
import OxyEngine.Core.Renderer.Texture.OxyColor;
import OxyEngine.Core.Renderer.Texture.OxyTexture;
import OxyEngine.Core.Window.WindowHandle;
import OxyEngine.OpenGL.OpenGLRendererAPI;
import OxyEngine.System.OxyDisposable;
import OxyEngine.System.OxySystem;
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

import static org.lwjgl.opengl.GL11.glDepthMask;

public class Scene implements OxyDisposable {

    private final Registry registry = new Registry();

    private final OxyRenderer3D renderer;
    private OxyUISystem oxyUISystem;

    private Set<OxyEntity> cachedLightEntities;
    private Set<EntityComponent> cachedInternMeshes, cachedModelMeshes, cachedCameraComponents;

    private final FrameBuffer frameBuffer;
    private final String sceneName;

    public Scene(String sceneName, WindowHandle windowHandle, OxyRenderer3D renderer, FrameBuffer frameBuffer) {
        this.renderer = renderer;
        this.frameBuffer = frameBuffer;
        this.sceneName = sceneName;
    }

    public final OxyInternObject createInternObjectEntity() {
        OxyInternObject e = new OxyInternObject(this);
        registry.entityList.put(e, new LinkedHashSet<>(10));
        e.addComponent(new TransformComponent());
        return e;
    }

    public final List<OxyModel> createModelEntities(ModelType type, OxyShader shader) {
        return createModelEntities(type.getPath(), shader);
    }

    public final List<OxyModel> createModelEntities(String path, OxyShader shader) {
        List<OxyModel> models = new ArrayList<>();
        OxyModelLoader loader = new OxyModelLoader(path);

        for (OxyModelLoader.AssimpOxyMesh assimpMesh : loader.meshes) {
            OxyModel e = new OxyModel(this);
            registry.entityList.put(e, new LinkedHashSet<>(10));
            e.addComponent(
                    shader,
                    new TransformComponent(),
                    assimpMesh.material.texture(),
                    new OxyColor(assimpMesh.material.diffuseColor()),
                    new ModelFactory(assimpMesh.vertices, assimpMesh.textureCoords, assimpMesh.normals, assimpMesh.faces)
            );
            e.name = assimpMesh.name;
            assimpMesh.material.setValues(shader);
            e.initData();
            models.add(e);
        }
        return models;
    }

    public final OxyModel createModelEntity(String path, OxyShader shader) {
        OxyModelLoader loader = new OxyModelLoader(path);
        OxyModelLoader.AssimpOxyMesh assimpMesh = loader.meshes.get(0);
        OxyModel e = new OxyModel(this);
        registry.entityList.put(e, new LinkedHashSet<>(10));
        e.addComponent(
                shader,
                new TransformComponent(),
                assimpMesh.material.texture(),
                new OxyColor(assimpMesh.material.diffuseColor()),
                new ModelFactory(assimpMesh.vertices, assimpMesh.textureCoords, assimpMesh.normals, assimpMesh.faces)
        );
        e.initData();
        return e;
    }

    CubemapTexture cubemapTexture;

    public void build() {

        Set<EntityComponent> cachedShaders = distinct(OxyShader.class);
        cubemapTexture = OxyTexture.loadCubemap(OxySystem.FileSystem.getResourceByPath("/images/skybox/skyBoxBlue"), this);
        cubemapTexture.init(cachedShaders);
        cachedInternMeshes = distinct(InternObjectMesh.class);
        cachedModelMeshes = distinct(ModelMesh.class);
        cachedCameraComponents = distinct(OxyCamera.class);
        cachedLightEntities = view(Light.class);


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

        for (OxyEntity e : cachedLightEntities) {
            if (!e.has(EmittingComponent.class)) continue;
            Light l = (Light) e.get(Light.class);
            EmittingComponent emittingComponent = (EmittingComponent) e.get(EmittingComponent.class);
            l.setAmbient(emittingComponent.ambient());
            l.setDiffuse(emittingComponent.diffuse());
            l.setSpecular(emittingComponent.specular());
            l.setPosition(emittingComponent.position());
            l.setDirection(emittingComponent.direction());
            l.update((OxyShader) e.get(OxyShader.class));
        }
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
                Mesh mesh = (Mesh) c;
                if (mesh.getShader().equals(cubemapTexture.getCube().get(OxyShader.class))) {
                    //skybox
                    glDepthMask(false);
                    render(ts, (Mesh) c, mainCamera);
                    glDepthMask(true);
                } else {
                    render(ts, (Mesh) c, mainCamera);
                }
            }
        }
        if (frameBuffer != null) frameBuffer.unbind();
        oxyUISystem.start(registry.entityList.keySet(), OxyRenderer.currentBoundedCamera);
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

    public void setUISystem(OxyUISystem oxyUISystem) {
        this.oxyUISystem = oxyUISystem;
    }
}
