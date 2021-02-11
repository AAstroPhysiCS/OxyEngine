package OxyEngine.Scene;

import OxyEngine.Components.*;
import OxyEngine.Core.Layers.GizmoLayer;
import OxyEngine.Core.Layers.SceneLayer;
import OxyEngine.Core.Renderer.Buffer.Platform.BufferProducer;
import OxyEngine.Core.Renderer.Buffer.Platform.OpenGLFrameBuffer;
import OxyEngine.Core.Renderer.Buffer.OpenGLMesh;
import OxyEngine.Core.Renderer.OxyRenderer3D;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.System.OxyDisposable;
import OxyEngine.Scene.Objects.Model.*;
import OxyEngine.Scene.Objects.Native.OxyNativeObject;
import OxyEngineEditor.UI.Gizmo.OxySelectHandler;
import org.joml.Vector3f;

import java.util.*;

import static OxyEngine.Components.EntityComponent.allEntityComponentChildClasses;
import static OxyEngine.Core.Renderer.Light.Light.LIGHT_SIZE;
import static OxyEngine.System.OxySystem.FileSystem.openDialog;
import static OxyEngine.System.OxySystem.oxyAssert;
import static OxyEngineEditor.EditorApplication.oxyShader;
import static OxyEngine.Scene.SceneRuntime.ACTIVE_SCENE;
import static OxyEngine.Scene.SceneSerializer.extensionName;
import static OxyEngine.Scene.SceneSerializer.fileExtension;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_NEAREST;

public final class Scene implements OxyDisposable {

    private final Registry registry = new Registry();

    private final OxyRenderer3D renderer;

    private final OpenGLFrameBuffer frameBuffer, blittedFrameBuffer, pickingBuffer;

    private final String sceneName;

    public static int OBJECT_ID_COUNTER = 0;

    public SceneState STATE = SceneState.IDLE;

    private OxyModelLoader modelLoader;


    public Scene(String sceneName, OxyRenderer3D renderer, OpenGLFrameBuffer frameBuffer) {
        this(sceneName, renderer, frameBuffer,
        BufferProducer.createFrameBuffer(frameBuffer.getWidth(), frameBuffer.getHeight(),
                OpenGLFrameBuffer.createNewSpec(OpenGLFrameBuffer.FrameBufferSpec.class)
                        .setAttachmentIndex(0)
                        .setFormats(OpenGLFrameBuffer.FrameBufferTextureFormat.RGBA8)
                        .setFilter(GL_LINEAR, GL_LINEAR)),
        BufferProducer.createFrameBuffer(frameBuffer.getWidth(), frameBuffer.getHeight(),
                OpenGLFrameBuffer.createNewSpec(OpenGLFrameBuffer.FrameBufferSpec.class)
                        .setAttachmentIndex(0)
                        .setFormats(OpenGLFrameBuffer.FrameBufferTextureFormat.RGBA8)
                        .setFilter(GL_LINEAR, GL_LINEAR),
                OpenGLFrameBuffer.createNewSpec(OpenGLFrameBuffer.FrameBufferSpec.class)
                        .setAttachmentIndex(1)
                        .setFormats(OpenGLFrameBuffer.FrameBufferTextureFormat.R32I)
                        .setFilter(GL_NEAREST, GL_NEAREST),
                OpenGLFrameBuffer.createNewSpec(OpenGLFrameBuffer.FrameBufferSpec.class)
                        .setStorage(true, 1)));
    }

    public Scene(String sceneName, OxyRenderer3D renderer, OpenGLFrameBuffer frameBuffer, OpenGLFrameBuffer blittedFrameBuffer, OpenGLFrameBuffer pickingBuffer) {
        this.renderer = renderer;
        this.frameBuffer = frameBuffer;
        this.sceneName = sceneName;
        this.blittedFrameBuffer = blittedFrameBuffer;
        this.pickingBuffer = pickingBuffer;
        this.pickingBuffer.drawBuffers(0, 1);
    }

    public final void put(OxyEntity e) {
        registry.entityList.put(e, new LinkedHashSet<>(allEntityComponentChildClasses.size()));
    }

    public final OxyNativeObject createNativeObjectEntity() {
        return createNativeObjectEntity(1);
    }

    public final OxyNativeObject createNativeObjectEntity(int size) {
        OxyNativeObject e = new OxyNativeObject(this, size);
        e.importedFromFile = false;
        put(e);
        e.addComponent(
                new TransformComponent(),
                new SelectedComponent(false),
                new RenderableComponent(RenderingMode.Normal),
                new UUIDComponent(UUID.randomUUID())
        );
        return e;
    }

    public final List<OxyModel> createModelEntities(ModelType type, OxyShader shader, boolean importedFromFile) {
        return createModelEntities(type.getPath(), shader, importedFromFile);
    }

    public final OxyModel createModelEntity(ModelType type, OxyShader shader) {
        return createModelEntity(type.getPath(), shader);
    }

    public final List<OxyModel> createModelEntities(ModelType type, OxyShader shader) {
        return createModelEntities(type.getPath(), shader, false);
    }

    public final OxyModel createEmptyModel(OxyShader shader) {
        OxyModel e = new OxyModel(this, ++OBJECT_ID_COUNTER);
        e.importedFromFile = false;
        put(e);
        e.addComponent(
                new UUIDComponent(UUID.randomUUID()),
                shader,
                new TransformComponent(new Vector3f(0, 0, 0)),
                new TagComponent("Empty Entity"),
                new MeshPosition(0),
                new RenderableComponent(RenderingMode.Normal),
                new SelectedComponent(false)
        );
        return e;
    }

    public final OxyModel createEmptyModel(OxyShader shader, int i) {
        OxyModel e = new OxyModel(this, ++OBJECT_ID_COUNTER);
        put(e);
        e.addComponent(
                new UUIDComponent(UUID.randomUUID()),
                shader,
                new TransformComponent(new Vector3f(0, 0, 0)),
                new TagComponent("Empty Entity"),
                new MeshPosition(i),
                new RenderableComponent(RenderingMode.Normal),
                new SelectedComponent(false)
        );
        return e;
    }

    public final List<OxyModel> createModelEntities(String path, OxyShader shader, boolean importedFromFile) {
        List<OxyModel> models = new ArrayList<>();
        modelLoader = new OxyModelLoader(path);

        int pos = 0;
        OxyMaterialPool.newBatch();
        for (OxyModelLoader.AssimpMesh assimpMesh : modelLoader.meshes) {
            int index = OxyMaterialPool.addMaterial(assimpMesh, modelLoader.materials.get(assimpMesh.materialIndex));
            OxyModel e = new OxyModel(this, ++OBJECT_ID_COUNTER);
            e.importedFromFile = importedFromFile;
            put(e);
            e.factory = new ModelFactory(assimpMesh.vertices, assimpMesh.textureCoords, assimpMesh.normals, assimpMesh.faces, assimpMesh.tangents, assimpMesh.biTangents);
            e.addComponent(
                    assimpMesh.rootEntity.get(FamilyComponent.class),
                    new UUIDComponent(UUID.randomUUID()),
                    shader,
                    new BoundingBoxComponent(
                            assimpMesh.min,
                            assimpMesh.max
                    ),
                    new TransformComponent(new Vector3f(assimpMesh.pos)),
                    new TagComponent(assimpMesh.name == null ? "Unnamed" : assimpMesh.name),
                    new MeshPosition(pos),
                    new RenderableComponent(RenderingMode.Normal),
                    new OxyMaterialIndex(index)
            );
            e.initData(path);
            models.add(e);
            pos++;
        }
        return models;
    }

    public final List<OxyModel> createModelEntities(String path, OxyShader shader) {
        return createModelEntities(path, shader, false);
    }

    public final OxyModel createModelEntity(String path, OxyShader shader, int i) {
        modelLoader = new OxyModelLoader(path);
        OxyMaterialPool.newBatch();
        OxyModelLoader.AssimpMesh assimpMesh = modelLoader.meshes.get(i);
        int index = OxyMaterialPool.addMaterial(assimpMesh, modelLoader.materials.get(assimpMesh.materialIndex));
        OxyModel e = new OxyModel(this, ++OBJECT_ID_COUNTER);
        put(e);
        e.factory = new ModelFactory(assimpMesh.vertices, assimpMesh.textureCoords, assimpMesh.normals, assimpMesh.faces, assimpMesh.tangents, assimpMesh.biTangents);
        e.addComponent(
                assimpMesh.rootEntity.get(FamilyComponent.class),
                new UUIDComponent(UUID.randomUUID()),
                shader,
                new BoundingBoxComponent(
                        assimpMesh.min,
                        assimpMesh.max
                ),
                new TransformComponent(new Vector3f(assimpMesh.pos)),
                new TagComponent(assimpMesh.name == null ? "Unnamed" : assimpMesh.name),
                new MeshPosition(i),
                new RenderableComponent(RenderingMode.Normal),
                new OxyMaterialIndex(index)
        );
        e.initData(path);
        return e;
    }

    static String optimization_Path = ""; //optimization for the scene serialization import

    public final OxyModel createModelEntity(String path, OxyShader shader, int i, int materialIndex, OxyEntity root) {
        if (!Scene.optimization_Path.equals(path)) {
            modelLoader = new OxyModelLoader(path, root);
            Scene.optimization_Path = path;
        }
        OxyModelLoader.AssimpMesh assimpMesh = modelLoader.meshes.get(i);
        OxyMaterialPool.newBatch();
        OxyModel e = new OxyModel(this, ++OBJECT_ID_COUNTER);
        put(e);
        e.factory = new ModelFactory(assimpMesh.vertices, assimpMesh.textureCoords, assimpMesh.normals, assimpMesh.faces, assimpMesh.tangents, assimpMesh.biTangents);
        e.addComponent(
                assimpMesh.rootEntity.get(FamilyComponent.class),
                new UUIDComponent(UUID.randomUUID()),
                shader,
                new BoundingBoxComponent(
                        assimpMesh.min,
                        assimpMesh.max
                ),
                new TransformComponent(new Vector3f(assimpMesh.pos)),
                new TagComponent(assimpMesh.name == null ? "Unnamed" : assimpMesh.name),
                new MeshPosition(i),
                new RenderableComponent(RenderingMode.Normal),
                new OxyMaterialIndex(materialIndex)
        );
        e.initData(path);
        return e;
    }

    public final OxyModel createModelEntity(String path, OxyShader shader) {
        return createModelEntity(path, shader, 0);
    }

    public final void removeEntity(OxyEntity e) {

        if (e.isRoot()) {
            List<OxyEntity> entitiesRelatedTo = e.getEntitiesRelatedTo(FamilyComponent.class);
            if (entitiesRelatedTo != null) {
                for (OxyEntity eRT : entitiesRelatedTo) {
                    removeEntity(eRT);
                }
            }
        }

        int index = e.get(OxyMaterialIndex.class) != null ? e.get(OxyMaterialIndex.class).index() : -1;

        if (e.has(OpenGLMesh.class)) e.get(OpenGLMesh.class).dispose();

        for (var scripts : e.getScripts()) {
            if (scripts.getProvider() != null) SceneRuntime.scriptThread.getProviders().remove(scripts.getProvider());
        }
        var value = registry.entityList.remove(e);
        assert !registry.entityList.containsKey(e) && !registry.entityList.containsValue(value) : oxyAssert("Remove entity failed!");

        if (ACTIVE_SCENE.getEntities()
                .stream()
                .filter(oxyEntity -> oxyEntity instanceof OxyModel)
                .filter(oxyEntity -> oxyEntity.has(OxyMaterialIndex.class))
                .map(entity -> entity.get(OxyMaterialIndex.class).index())
                .noneMatch(integer -> index == integer) && index != -1) {
            //if there's no entity that is using this material => dispose it
            OxyMaterial m = OxyMaterialPool.getMaterial(index);
            OxyMaterialPool.removeMaterial(m);
            m.dispose();
        }
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

    public final void removeComponent(OxyEntity entity, EntityComponent components) {
        registry.removeComponent(entity, components);
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

    @SafeVarargs
    public final <V extends EntityComponent> Set<V> distinct(Class<? super V>... destClasses) {
        return registry.distinct(destClasses);
    }

    public int getShapeCount() {
        return registry.entityList.keySet().size();
    }

    public OxyRenderer3D getRenderer() {
        return renderer;
    }

    public Set<OxyEntity> getEntities() {
        return registry.entityList.keySet();
    }

    Set<Map.Entry<OxyEntity, Set<EntityComponent>>> getEntityEntrySet() {
        return registry.entityList.entrySet();
    }

    public OpenGLFrameBuffer getFrameBuffer() {
        return frameBuffer;
    }

    public OpenGLFrameBuffer getBlittedFrameBuffer() {
        return blittedFrameBuffer;
    }

    public OpenGLFrameBuffer getPickingBuffer() {
        return pickingBuffer;
    }

    public String getSceneName() {
        return sceneName;
    }

    public void disposeAllModels() {
        Iterator<OxyEntity> it = registry.entityList.keySet().iterator();
        while (it.hasNext()) {
            OxyEntity e = it.next();
            if (e instanceof OxyModel) {
                if (e.has(OpenGLMesh.class)) e.get(OpenGLMesh.class).dispose();
                if (e.has(OxyMaterialIndex.class)) {
                    OxyMaterial m = OxyMaterialPool.getMaterial(e);
                    if (m != null) {
                        if (m.index != -1) {
                            OxyMaterialPool.removeMaterial(m);
                            m.dispose();
                        }
                    }
                }
                it.remove();
            }
        }
        for (int i = 0; i < LIGHT_SIZE; i++) {
            oxyShader.enable();
            oxyShader.setUniformVec3("p_Light[" + i + "].position", 0, 0, 0);
            oxyShader.setUniformVec3("p_Light[" + i + "].diffuse", 0, 0, 0);
            oxyShader.setUniform1f("p_Light[" + i + "].constant", 0);
            oxyShader.setUniform1f("p_Light[" + i + "].linear", 0);
            oxyShader.setUniform1f("p_Light[" + i + "].quadratic", 0);

            oxyShader.setUniformVec3("d_Light[" + i + "].direction", 0, 0, 0);
            oxyShader.setUniformVec3("d_Light[" + i + "].diffuse", 0, 0, 0);

            oxyShader.disable();
        }
    }

    @Override
    public void dispose() {
        OxyMaterialPool.clear();
        Iterator<OxyEntity> it = registry.entityList.keySet().iterator();
        while (it.hasNext()) {
            if (it.next() != null) it.remove();
        }
        frameBuffer.dispose();
        blittedFrameBuffer.dispose();
        assert !it.hasNext() : oxyAssert("Scene dispose failed");
    }

    public static void openScene() {
        String openScene = openDialog(extensionName, null);
        if (openScene != null) {
            ACTIVE_SCENE = SceneSerializer.deserializeScene(openScene, SceneLayer.getInstance(), oxyShader);
            GizmoLayer.getInstance().build();
            SceneLayer.getInstance().build();
        }
    }

    public static void saveScene() {
        SceneSerializer.serializeScene(ACTIVE_SCENE.getSceneName() + fileExtension);
    }

    public static void newScene() {
        OxySelectHandler.entityContext = null;
        Scene oldScene = ACTIVE_SCENE;
        oldScene.disposeAllModels();

        Scene scene = new Scene("Test Scene 1", oldScene.getRenderer(), oldScene.getFrameBuffer(), oldScene.getBlittedFrameBuffer(), oldScene.getPickingBuffer());
        for (var n : oldScene.getEntityEntrySet()) {
            scene.put(n.getKey());
            scene.addComponent(n.getKey(), n.getValue().toArray(EntityComponent[]::new));
        }
        ACTIVE_SCENE = scene;
        if (SceneLayer.hdrTexture != null) SceneLayer.hdrTexture.dispose();
        SceneLayer.hdrTexture = null;
        GizmoLayer.getInstance().build();
        SceneLayer.getInstance().build();
    }

    public <T extends EntityComponent> OxyEntity getRoot(OxyEntity entity, Class<T> destClass) {
        return registry.getRoot(entity, destClass);
    }
}