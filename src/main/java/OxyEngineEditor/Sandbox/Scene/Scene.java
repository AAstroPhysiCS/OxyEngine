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
import OxyEngine.OpenGL.OpenGLRendererAPI;
import OxyEngine.System.OxyDisposable;
import OxyEngine.System.OxySystem;
import OxyEngineEditor.Sandbox.Components.*;
import OxyEngineEditor.Sandbox.Scene.Model.ModelFactory;
import OxyEngineEditor.Sandbox.Scene.Model.ModelType;
import OxyEngineEditor.Sandbox.Scene.Model.OxyModel;
import OxyEngineEditor.Sandbox.Scene.Model.OxyModelLoader;
import OxyEngineEditor.Sandbox.Scene.NativeObjects.OxyNativeObject;
import OxyEngineEditor.UI.OxyUISystem;
import org.joml.Vector3f;

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
    private Set<EntityComponent> cachedNativeMeshes, cachedModelMeshes, cachedCameraComponents/*, cachedModelMeshesMasked*/;

    private final FrameBuffer frameBuffer;
    private final String sceneName;

    public Scene(String sceneName, OxyRenderer3D renderer, FrameBuffer frameBuffer) {
        this.renderer = renderer;
        this.frameBuffer = frameBuffer;
        this.sceneName = sceneName;
    }

    public final OxyNativeObject createNativeObjectEntity() {
        OxyNativeObject e = new OxyNativeObject(this);
        registry.entityList.put(e, new LinkedHashSet<>(15));
        e.addComponent(new TransformComponent(), new RenderableComponent(true));
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
            registry.entityList.put(e, new LinkedHashSet<>(15));
            e.originPos = new Vector3f(assimpMesh.pos);
            e.addComponent(
                    shader,
                    new BoundingBoxComponent(
                            assimpMesh.min,
                            assimpMesh.max
                    ),
                    new TransformComponent(new Vector3f(assimpMesh.pos)),
                    assimpMesh.material.texture(),
                    new OxyColor(assimpMesh.material.diffuseColor()),
                    new ModelFactory(assimpMesh.vertices, assimpMesh.textureCoords, assimpMesh.normals, assimpMesh.faces),
                    new TagComponent(assimpMesh.name),
                    new RenderableComponent(true)
            );
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
        registry.entityList.put(e, new LinkedHashSet<>(15));
        e.originPos = new Vector3f(assimpMesh.pos);
        e.addComponent(
                shader,
                new BoundingBoxComponent(
                        assimpMesh.min,
                        assimpMesh.max
                ),
                new TransformComponent(new Vector3f(assimpMesh.pos)),
                assimpMesh.material.texture(),
                new OxyColor(assimpMesh.material.diffuseColor()),
                new ModelFactory(assimpMesh.vertices, assimpMesh.textureCoords, assimpMesh.normals, assimpMesh.faces),
                new TagComponent(assimpMesh.name),
                new RenderableComponent(true)
        );
        assimpMesh.material.setValues(shader);
        e.initData();
        return e;
    }

    private CubemapTexture cubemapTexture; //skyboxtexture

    public void build() {

        Set<EntityComponent> cachedShaders = distinct(OxyShader.class);
        cubemapTexture = OxyTexture.loadCubemap(OxySystem.FileSystem.getResourceByPath("/images/skybox/skyboxNature1"), this);
        cubemapTexture.init(cachedShaders);
        cachedNativeMeshes = distinct(NativeObjectMesh.class);
        cachedModelMeshes = distinct(ModelMesh.class);
        cachedCameraComponents = distinct(OxyCamera.class);
        cachedLightEntities = view(Light.class);

        //Prep
        {
            for (EntityComponent e : cachedNativeMeshes) {
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
            Light l = e.get(Light.class);
            EmittingComponent emittingComponent = e.get(EmittingComponent.class);
            l.setAmbient(emittingComponent.ambient());
            l.setDiffuse(emittingComponent.diffuse());
            l.setSpecular(emittingComponent.specular());
            l.setPosition(emittingComponent.position());
            l.setDirection(emittingComponent.direction());
            l.update(e.get(OxyShader.class));
        }
    }

//    static OxyShader outlineShader = new OxyShader("D:\\programming\\Java\\OxyEngine\\shaders\\outline.glsl");

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
                Mesh mesh = (Mesh) c;
                RenderableComponent rC = mesh.renderableComponent;
                if (rC.renderable) render(ts, mesh, mainCamera);
                /*glEnable(GL_STENCIL_TEST);
                glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);

                glStencilMask(0xFF);
                glClear(GL_STENCIL_BUFFER_BIT);

                glStencilFunc(GL_ALWAYS, 1, 0xFF);
                if (rC.renderable) render(ts, mesh, mainCamera);
                outlineShader.enable();
                mesh.scaleUp(1.01f);
                glStencilFunc(GL_NOTEQUAL, 1, 0xFF);
                glStencilMask(0x00);
                render(ts, mesh, mainCamera, outlineShader); // draw with the outline shader
                mesh.finalizeScaleUp();
                glDisable(GL_STENCIL_TEST);*/
            }

            for (EntityComponent c : cachedNativeMeshes) {
                Mesh mesh = (Mesh) c;
                RenderableComponent rC = mesh.renderableComponent;
                if (mesh.getShader().equals(cubemapTexture.getCube().get(OxyShader.class)) && rC.renderable) {
                    //skybox
                    glDepthMask(false);
                    render(ts, mesh, mainCamera);
                    glDepthMask(true);
                    continue;
                }
                if (rC.renderable)
                    render(ts, mesh, mainCamera);
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

    public <T extends EntityComponent> T get(OxyEntity entity, Class<T> destClass) {
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

    private void render(float ts, Mesh mesh, OxyCamera camera, OxyShader shader) {
        renderer.render(ts, mesh, camera, shader);
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