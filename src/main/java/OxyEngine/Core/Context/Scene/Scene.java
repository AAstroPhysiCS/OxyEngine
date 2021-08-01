package OxyEngine.Core.Context.Scene;

import OxyEngine.Components.*;
import OxyEngine.Core.Camera.OxyCamera;
import OxyEngine.Core.Camera.PerspectiveCamera;
import OxyEngine.Core.Camera.SceneCamera;
import OxyEngine.Core.Context.Renderer.Buffer.OpenGLMesh;
import OxyEngine.Core.Context.Renderer.Light.*;
import OxyEngine.Core.Context.Renderer.Mesh.ModelMeshOpenGL;
import OxyEngine.Core.Context.Renderer.Pipeline.OxyShader;
import OxyEngine.Core.Context.Renderer.Pipeline.ShaderLibrary;
import OxyEngine.Core.Context.Renderer.Texture.HDRTexture;
import OxyEngine.PhysX.OxyPhysXComponent;
import OxyEngine.Core.Context.SceneRenderer;
import OxyEngine.Scripting.ScriptEngine;
import OxyEngine.System.OxyDisposable;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.io.File;
import java.util.*;

import static OxyEngine.Components.EntityComponent.allEntityComponentChildClasses;
import static OxyEngine.Core.Context.Renderer.Light.Light.LIGHT_SIZE;
import static OxyEngine.Core.Context.Scene.SceneRuntime.ACTIVE_SCENE;
import static OxyEngine.Core.Context.Scene.SceneRuntime.entityContext;
import static OxyEngine.Core.Context.Scene.SceneSerializer.extensionName;
import static OxyEngine.Core.Context.Scene.SceneSerializer.fileExtension;
import static OxyEngine.System.OxyFileSystem.openDialog;
import static OxyEngine.System.OxyFileSystem.saveDialog;
import static OxyEngine.System.OxySystem.oxyAssert;

public final class Scene implements OxyDisposable {

    private final Registry registry = new Registry();

    private String workingDirectory;
    private String sceneName;

    public float[] gammaStrength = new float[]{2.2f};
    public float[] exposure = new float[]{1.0f};

    public static int OBJECT_ID_COUNTER = 0;

    public SceneState STATE = SceneState.IDLE;

    private OxyModelImporter modelImporter;

    public Scene(String sceneName, String workingDirectory) {
        this.sceneName = sceneName;
        this.workingDirectory = workingDirectory;
    }

    public final void put(OxyEntity e) {
        registry.entityList.put(e, new LinkedHashSet<>(allEntityComponentChildClasses.size()));
        e.addComponent(new UUIDComponent(UUID.randomUUID()));
    }

    public final OxyNativeObject createNativeObjectEntity(float[] vertices, int[] indices) {
        OxyNativeObject e = new OxyNativeObject(this);
        e.vertices = vertices;
        e.indices = indices;
        e.importedFromFile = false;
        put(e);
        e.addComponent(
                new TransformComponent(),
                new SelectedComponent(false),
                new RenderableComponent(RenderingMode.Normal));
        return e;
    }

    public OxyEntity createEmptyEntity() {
        OxyEntity model = ACTIVE_SCENE.createEmptyModel();
        if (entityContext != null) {
            model.addComponent(new TagComponent("Empty Group"), new SelectedComponent(false));
            model.setFamily(new EntityFamily(entityContext.getFamily()));
            model.transformLocally();
        } else {
            model.addComponent(new TagComponent("Empty Group"), new SelectedComponent(false));
            //model.setFamily(new EntityFamily()); this is already the default behaviour once the entity is created
            model.transformLocally();
        }
        return model;
    }

    public void createMeshEntity() {
        OxyEntity model = createEmptyEntity();
        if (!model.getGUINodes().contains(ModelMeshOpenGL.guiNode))
            model.getGUINodes().add(ModelMeshOpenGL.guiNode);
    }

    public OxyNativeObject createSkyLight() {
        OxyNativeObject skyLightEnt = createNativeObjectEntity(null, null);
        if (entityContext != null) skyLightEnt.setFamily(new EntityFamily(entityContext.getFamily()));
        skyLightEnt.addComponent(new TagComponent("Sky Light"), new OpenGLHDREnvironmentMap());
        if (!skyLightEnt.getGUINodes().contains(SkyLight.guiNode))
            skyLightEnt.getGUINodes().add(SkyLight.guiNode);
        SceneRenderer.getInstance().updateLightEntities();
        SceneRenderer.getInstance().updateNativeEntities();
        return skyLightEnt;
    }

    public void createPointLight() {

        OxyModel model = ACTIVE_SCENE.createEmptyModel();
        if (entityContext != null) model.setFamily(new EntityFamily(entityContext.getFamily()));

        PointLight pointLight = new PointLight(1.0f, 0.027f, 0.0028f);
        int index = OxyMaterialPool.addMaterial(new OxyMaterial(ShaderLibrary.get("OxyPBR"), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f)));
        model.addComponent(pointLight, new OxyMaterialIndex(index), new TagComponent("Point Light"));

        if (!model.getGUINodes().contains(OxyMaterial.guiNode))
            model.getGUINodes().add(OxyMaterial.guiNode);
        model.getGUINodes().add(PointLight.guiNode);
        model.transformLocally();
        SceneRenderer.getInstance().updateModelEntities();
    }

    public void createDirectionalLight() {
        OxyModel model = ACTIVE_SCENE.createEmptyModel();
        if (entityContext != null) model.setFamily(new EntityFamily(entityContext.getFamily()));
        int index = OxyMaterialPool.addMaterial(new OxyMaterial(ShaderLibrary.get("OxyPBR"), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f)));
        model.addComponent(new TagComponent("Directional Light"), new DirectionalLight(1.0f));
        model.addComponent(new OxyMaterialIndex(index));
        model.getGUINodes().add(DirectionalLight.guiNode);
        model.transformLocally();
        SceneRenderer.getInstance().updateModelEntities();
    }

    public void createPerspectiveCamera() {
        OxyModel model = ACTIVE_SCENE.createEmptyModel();
        if (entityContext != null) model.setFamily(new EntityFamily(entityContext.getFamily()));
        model.addComponent(new SceneCamera());
        if (!model.getGUINodes().contains(OxyCamera.guiNode))
            model.getGUINodes().add(OxyCamera.guiNode);
        model.transformLocally();
        SceneRenderer.getInstance().updateCameraEntities();
    }

    public final List<OxyModel> createModelEntities(DefaultModelType type, boolean importedFromFile) {
        return createModelEntities(type.getPath(), importedFromFile);
    }

    public final List<OxyModel> createModelEntities(DefaultModelType type) {
        return createModelEntities(type.getPath(), false);
    }

    public final OxyModel createModelEntity(DefaultModelType type) {
        return createModelEntity(type.getPath(), 0, 0);
    }

    public final OxyModel createEmptyModel() {
        OxyModel e = new OxyModel(this, ++OBJECT_ID_COUNTER);
        e.importedFromFile = false;
        put(e);
        e.addComponent(
                new TransformComponent(new Vector3f(0, 0, 0)),
                new TagComponent("Empty Group"),
                new MeshPosition(0),
                new RenderableComponent(RenderingMode.Normal),
                new SelectedComponent(false)
        );
        return e;
    }

    public final OxyModel createEmptyModel(int i) {
        OxyModel e = new OxyModel(this, ++OBJECT_ID_COUNTER);
        put(e);
        e.addComponent(
                new TransformComponent(new Vector3f(0, 0, 0)),
                new TagComponent("Empty Group"),
                new MeshPosition(i),
                new RenderableComponent(RenderingMode.Normal),
                new SelectedComponent(false)
        );
        return e;
    }

    public final List<OxyModel> createModelEntities(String path, boolean importedFromFile) {
        List<OxyModel> models = new ArrayList<>();
        modelImporter = new OxyModelImporter(path, ImporterType.MeshImporter, ImporterType.AnimationImporter);

        int pos = 0;
        OxyMaterialPool.newBatch();
        for (int i = 0; i < modelImporter.getMeshSize(); i++) {
            int materialIndex = modelImporter.getMaterialIndex(i);
            int index = OxyMaterialPool.addMaterial(modelImporter.getMaterialName(materialIndex), materialIndex, modelImporter.getMaterialPaths(materialIndex));
            OxyModel e = new OxyModel(this, ++OBJECT_ID_COUNTER, modelImporter.getVertexList(i), modelImporter.getFaces(i));
            e.importedFromFile = importedFromFile;
            put(e);
            e.setFamily(new EntityFamily(modelImporter.getRootEntity(i).getFamily()));
            e.addComponent(
                    new UUIDComponent(UUID.randomUUID()),
                    new BoundingBoxComponent(
                            modelImporter.getBoundingBoxMin(i),
                            modelImporter.getBoundingBoxMax(i)
                    ),
                    new TransformComponent(modelImporter.getTransformation(i)),
                    new TagComponent(modelImporter.getMeshName(i)),
                    new MeshPosition(pos),
                    new RenderableComponent(RenderingMode.Normal),
                    new OxyMaterialIndex(index)
            );
            if (modelImporter.getScene().mNumAnimations() > 0) {
                e.addComponent(new AnimationComponent(modelImporter.getScene(), modelImporter.getBoneInfoMap()));
                System.gc();
            }
            e.initMesh(path);
            models.add(e);
            pos++;
        }
        return models;
    }

    public final List<OxyModel> createModelEntities(String path) {
        return createModelEntities(path, false);
    }

    public final OxyModel createModelEntity(String path, int i) {
        modelImporter = new OxyModelImporter(path, ImporterType.MeshImporter, ImporterType.AnimationImporter);
        OxyMaterialPool.newBatch();
        int materialIndex = modelImporter.getMaterialIndex(i);
        int index = OxyMaterialPool.addMaterial(modelImporter.getMaterialName(materialIndex), materialIndex, modelImporter.getMaterialPaths(materialIndex));
        OxyModel e = new OxyModel(this, ++OBJECT_ID_COUNTER, modelImporter.getVertexList(i), modelImporter.getFaces(i));
        put(e);
        e.setFamily(new EntityFamily(modelImporter.getRootEntity(i).getFamily()));
        e.addComponent(
                new UUIDComponent(UUID.randomUUID()),
                new BoundingBoxComponent(
                        modelImporter.getBoundingBoxMin(i),
                        modelImporter.getBoundingBoxMax(i)
                ),
                new TransformComponent(modelImporter.getTransformation(i)),
                new TagComponent(modelImporter.getMeshName(i)),
                new MeshPosition(i),
                new RenderableComponent(RenderingMode.Normal),
                new OxyMaterialIndex(index)
        );
        if (modelImporter.getScene().mNumAnimations() > 0) {
            e.addComponent(new AnimationComponent(modelImporter.getScene(), modelImporter.getBoneInfoMap()));
            System.gc();
        }
        e.initMesh(path);
        return e;
    }

    static String optimization_Path = ""; //optimization for the scene serialization import

    public final OxyModel createModelEntity(String path, int i, int materialIndex) {
        if (!Scene.optimization_Path.equals(path)) {
            modelImporter = new OxyModelImporter(path, ImporterType.MeshImporter, ImporterType.AnimationImporter);
            Scene.optimization_Path = path;
        }
        OxyMaterialPool.newBatch();
        OxyModel e = new OxyModel(this, ++OBJECT_ID_COUNTER, modelImporter.getVertexList(i), modelImporter.getFaces(i));
        put(e);
        e.addComponent(
                new BoundingBoxComponent(
                        modelImporter.getBoundingBoxMin(i),
                        modelImporter.getBoundingBoxMax(i)
                ),
                new TransformComponent(modelImporter.getTransformation(i)),
                new TagComponent(modelImporter.getMeshName(i)),
                new MeshPosition(i),
                new RenderableComponent(RenderingMode.Normal),
                new OxyMaterialIndex(materialIndex)
        );
        if (modelImporter.getScene().mNumAnimations() > 0) {
            e.addComponent(new AnimationComponent(modelImporter.getScene(), modelImporter.getBoneInfoMap()));
            System.gc();
        }
        e.initMesh(path);
        return e;
    }

    public final void removeEntity(OxyEntity e) {

        List<OxyEntity> entitiesRelatedTo = e.getEntitiesRelatedTo();
        if (entitiesRelatedTo.size() != 0) {
            for (OxyEntity eRT : entitiesRelatedTo) {
                removeEntity(eRT);
            }
        }

        for (var scripts : e.getScripts()) {
            if (scripts.getProvider() != null) ScriptEngine.removeProvider(scripts.getProvider());
        }

        int index = e.get(OxyMaterialIndex.class) != null ? e.get(OxyMaterialIndex.class).index() : -1;

        if (e.has(OxyPhysXComponent.class)) e.get(OxyPhysXComponent.class).dispose();

        if (e.has(ModelMeshOpenGL.class)) e.get(ModelMeshOpenGL.class).dispose();

        if (e.has(OpenGLHDREnvironmentMap.class)) {
            OpenGLHDREnvironmentMap skyLight = e.get(OpenGLHDREnvironmentMap.class);
            HDRTexture texture = skyLight.getHDRTexture();
            if (texture != null) texture.dispose();
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
            OxyMaterialPool.getMaterial(index).ifPresent((m) -> {
                OxyMaterialPool.removeMaterial(m);
                m.dispose();
            });
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

    public final OxyEntity getEntityByUUID(UUIDComponent uuidComponent) {
        return registry.getEntityByUUID(uuidComponent);
    }

    public final boolean isValid(OxyEntity entity) {
        return registry.entityList.containsKey(entity);
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

    Set<EntityComponent> getAllComponents(OxyEntity e){
        return registry.entityList.get(e);
    }

    public int getShapeCount() {
        return registry.entityList.keySet().size();
    }

    public Set<OxyEntity> getEntities() {
        return registry.entityList.keySet();
    }

    Set<Map.Entry<OxyEntity, Set<EntityComponent>>> getEntityEntrySet() {
        return registry.entityList.entrySet();
    }

    public String getSceneName() {
        return sceneName;
    }

    public void disposeAllEntities() {
        Iterator<OxyEntity> it = registry.entityList.keySet().iterator();
        while (it.hasNext()) {
            OxyEntity e = it.next();
            if (e instanceof OxyModel) {
                if (e.has(OpenGLMesh.class)) e.get(OpenGLMesh.class).dispose();
                if (e.has(OxyMaterialIndex.class)) {
                    OxyMaterialPool.getMaterial(e).ifPresent((m) -> {
                        if (m.index != -1) {
                            OxyMaterialPool.removeMaterial(m);
                            m.dispose();
                        }
                    });
                }
                it.remove();
            }

            //REMOVING ENV MAP BCS WE ARE GONNA REPLACE IT WITH THE NEW ENV MAP FROM THE NEW SCENE
            if (e instanceof OxyNativeObject) {
                if(e.has(OpenGLHDREnvironmentMap.class)) {
                    HDRTexture texture = e.get(OpenGLHDREnvironmentMap.class).getHDRTexture();
                    if (texture != null) texture.dispose();
                    it.remove();
                } else if(e.has(DynamicSky.class)) {
                    DynamicSky dynamicSky = e.get(DynamicSky.class);
                    dynamicSky.dispose();
                    it.remove();
                }
            }
        }
        OxyShader pbrShader = ShaderLibrary.get("OxyPBR");
        for (int i = 0; i < LIGHT_SIZE; i++) {
            pbrShader.begin();
            pbrShader.setUniformVec3("p_Light[" + i + "].position", 0, 0, 0);
            pbrShader.setUniformVec3("p_Light[" + i + "].diffuse", 0, 0, 0);
            pbrShader.setUniform1f("p_Light[" + i + "].constant", 0);
            pbrShader.setUniform1f("p_Light[" + i + "].linear", 0);
            pbrShader.setUniform1f("p_Light[" + i + "].quadratic", 0);

            pbrShader.setUniformVec3("d_Light[" + i + "].direction", 0, 0, 0);
            pbrShader.setUniformVec3("d_Light[" + i + "].diffuse", 0, 0, 0);

            pbrShader.end();
        }
    }

    @Override
    public void dispose() {
        OxyMaterialPool.clear();
        disposeAllEntities();
        registry.entityList.clear();
        PerspectiveCamera.disposeUniformBuffer();
    }

    public static void openScene() {
        String openScene = openDialog(extensionName, ACTIVE_SCENE.getWorkingDirectory());
        if (openScene != null) {
            ScriptEngine.clearProviders();
            SceneRuntime.onStop();
            ACTIVE_SCENE = SceneSerializer.deserializeScene(openScene);
            SceneRenderer.getInstance().initScene();
        }
    }

    public static void saveScene() {
        SceneRuntime.onStop();
        if (ACTIVE_SCENE.workingDirectory == null || ACTIVE_SCENE.workingDirectory.equals("null")) {
            String s = saveDialog(extensionName, null);
            if (s == null) return;
            File f = new File(s);
            ACTIVE_SCENE.workingDirectory = f.getParent();
            ACTIVE_SCENE.sceneName = f.getName();
        }
        SceneSerializer.serializeScene(ACTIVE_SCENE.workingDirectory + "//" + ACTIVE_SCENE.getSceneName() + fileExtension);
    }

    public static void saveAs() {
        SceneRuntime.onStop();
        String saveAs = saveDialog(extensionName, null);
        if (saveAs != null) SceneSerializer.serializeScene(saveAs + fileExtension);
    }

    public static void newScene() {
        ScriptEngine.clearProviders();
        SceneRuntime.onStop();
        SceneRuntime.entityContext = null;
        Scene oldScene = ACTIVE_SCENE;
        oldScene.disposeAllEntities();

        Scene scene = new Scene("Test Scene 1", null);
        for (var n : oldScene.getEntityEntrySet()) {
            scene.put(n.getKey());
            scene.addComponent(n.getKey(), n.getValue().toArray(EntityComponent[]::new));
        }
        ACTIVE_SCENE = scene;
        SceneRenderer.getInstance().initScene();
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public OxyEntity getRoot(OxyEntity entity) {
        return registry.getRoot(entity);
    }
}