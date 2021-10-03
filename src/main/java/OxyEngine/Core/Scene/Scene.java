package OxyEngine.Core.Scene;

import OxyEngine.Components.*;
import OxyEngine.Core.Camera.EditorCamera;
import OxyEngine.Core.Camera.Camera;
import OxyEngine.Core.Camera.PerspectiveCamera;
import OxyEngine.Core.Camera.SceneCamera;
import OxyEngine.Core.Renderer.Light.*;
import OxyEngine.Core.Renderer.Mesh.BufferUsage;
import OxyEngine.Core.Renderer.Mesh.OpenGLMesh;
import OxyEngine.Core.Renderer.Pipeline;
import OxyEngine.Core.Renderer.Renderer;
import OxyEngine.Core.Renderer.Shader;
import OxyEngine.Core.Renderer.Texture.EnvironmentTexture;
import OxyEngine.PhysX.PhysXComponent;
import OxyEngine.Scripting.ScriptEngine;
import OxyEngine.System.Disposable;
import org.joml.Vector3f;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static OxyEngine.Core.Renderer.Light.Light.LIGHT_SIZE;
import static OxyEngine.Core.Scene.SceneRuntime.entityContext;
import static OxyEngine.Core.Scene.SceneRuntime.sceneContext;
import static OxyEngine.Core.Scene.SceneSerializer.extensionName;
import static OxyEngine.Core.Scene.SceneSerializer.fileExtension;
import static OxyEngine.System.FileSystem.openDialog;
import static OxyEngine.System.FileSystem.saveDialog;
import static OxyEngine.System.OxySystem.oxyAssert;

public final class Scene implements Disposable {

    private final Registry registry = new Registry();

    private String workingDirectory;
    private String sceneName;

    public float[] gammaStrength = new float[]{2.2f};
    public float[] exposure = new float[]{1.0f};

    public static int OBJECT_ID_COUNTER = 0;

    private SceneState STATE = SceneState.STOP;

    public Scene(String sceneName, String workingDirectory) {
        this.sceneName = sceneName;
        this.workingDirectory = workingDirectory;
    }

    public final void put(Entity e) {
        registry.entityList.put(e, new ArrayList<>());
        e.addComponent(new UUIDComponent());
    }

    public Entity createMeshEntity() {
        Entity entity = createEmptyEntity();
        if (!entity.getGUINodes().contains(OpenGLMesh.guiNode))
            entity.getGUINodes().add(OpenGLMesh.guiNode);
        return entity;
    }

    public Entity createSkyLight() {
        Entity skyLightEnt = sceneContext.createEmptyEntity();
        if (entityContext != null) skyLightEnt.setFamily(new EntityFamily(entityContext.getFamily()));
        skyLightEnt.addComponent(new TagComponent("Sky Light"), new HDREnvironmentMap());
        if (!skyLightEnt.getGUINodes().contains(SkyLight.guiNode))
            skyLightEnt.getGUINodes().add(SkyLight.guiNode);
        Renderer.submitSkyLight(skyLightEnt.get(SkyLight.class));
        return skyLightEnt;
    }

    public Entity createPointLight() {

        Entity entity = sceneContext.createEmptyEntity();
        if (entityContext != null) entity.setFamily(new EntityFamily(entityContext.getFamily()));

        PointLight pointLight = new PointLight(1.0f, 1.0f, 0.0f);
        entity.addComponent(pointLight, new TagComponent("Point Light"));

        entity.getGUINodes().add(PointLight.guiNode);
        Renderer.submitPointLight(pointLight);

        return entity;
    }

    public Entity createDirectionalLight() {
        Entity entity = sceneContext.createEmptyEntity();
        if (entityContext != null) entity.setFamily(new EntityFamily(entityContext.getFamily()));
        entity.addComponent(new TagComponent("Directional Light"), new DirectionalLight(1.0f));
        entity.getGUINodes().add(DirectionalLight.guiNode);
        Renderer.submitDirectionalLight(entity.get(DirectionalLight.class));

        return entity;
    }

    public Entity createPerspectiveCamera() {
        Entity entity = sceneContext.createEmptyEntity();
        if (entityContext != null) entity.setFamily(new EntityFamily(entityContext.getFamily()));
        entity.addComponent(new SceneCamera(entity.getTransform()));
        if (!entity.getGUINodes().contains(Camera.guiNode))
            entity.getGUINodes().add(Camera.guiNode);

        return entity;
    }

    public final Entity createEntity(DefaultModelType type) {
        return createEntity(type.getPath());
    }

    public final Entity createEmptyEntity() {
        Entity e = new Entity(this);
        put(e);
        e.addComponent(
                new TransformComponent(new Vector3f(0, 0, 0)),
                new TagComponent("Empty Entity"),
                new SelectedComponent(false)
        );
        return e;
    }

    public final Entity createEntity(String path) {
        Entity e = createEmptyEntity();

        Pipeline geometryPipeline = Renderer.getGeometryPipeline();

        OpenGLMesh mesh = new OpenGLMesh(geometryPipeline, path, BufferUsage.STATIC);
        e.addComponent(mesh);

        if (mesh.getAIScene().mNumAnimations() > 0)
            e.addComponent(new AnimationComponent(mesh.getAIScene(), mesh.getBoneInfoMap())); //deletes aiScene

        Renderer.submitMesh(mesh, e.get(TransformComponent.class), e.get(AnimationComponent.class));
        e.getGUINodes().add(OpenGLMesh.guiNode);

        return e;
    }

    public final void removeEntity(Entity e) {

        List<Entity> entitiesRelatedTo = e.getEntitiesRelatedTo();
        if (entitiesRelatedTo.size() != 0) {
            for (Entity eRT : entitiesRelatedTo) {
                removeEntity(eRT);
            }
        }

        for (var scripts : e.getScripts()) {
            if (scripts.getProvider() != null) ScriptEngine.removeProvider(scripts.getProvider());
        }

        if (e.has(PhysXComponent.class)) e.get(PhysXComponent.class).dispose();
        //not removing animation component because mesh already deletes the command
        if (e.has(OpenGLMesh.class)) {
            OpenGLMesh mesh = e.get(OpenGLMesh.class);
            Renderer.removeFromCommand(mesh);
            mesh.dispose();
        }
        if (e.has(HDREnvironmentMap.class)) e.get(HDREnvironmentMap.class).dispose();
        if (e.has(Light.class)) Renderer.removeFromCommand(e.get(Light.class));

        var value = registry.entityList.remove(e);
        assert !registry.entityList.containsKey(e) && !registry.entityList.containsValue(value) : oxyAssert("Remove entity failed!");
    }

    public Entity copyEntity(Entity other) {
        return new Entity(other);
    }

    public final Entity getEntityByIndex(int index) {
        int i = 0;
        for (Entity e : registry.entityList.keySet()) {
            if (i == index) {
                return e;
            }
            i++;
        }
        return null;
    }

    public final Entity getEntityByUUID(UUIDComponent uuidComponent) {
        return registry.getEntityByUUID(uuidComponent);
    }

    public final Entity getEntityByUUID(String uuid) {
        return registry.getEntityByUUID(uuid);
    }

    public final Entity findEntityByComponent(EntityComponent entityComponent) {
        Class<? extends EntityComponent> targetClass = entityComponent.getClass();
        Set<Entity> entities = view(targetClass);
        for (Entity e : entities) {
            if (e.get(targetClass).equals(entityComponent)) return e;
        }
        return null;
    }

    public final boolean isValid(Entity entity) {
        return registry.entityList.containsKey(entity);
    }

    /*
     * add component to the registry
     */
    public final void addComponent(Entity entity, EntityComponent... component) {
        registry.addComponent(entity, component);
    }

    public final void removeComponent(Entity entity, EntityComponent components) {
        registry.removeComponent(entity, components);
    }

    /*
     * returns true if the component is already in the set
     */
    public boolean has(Entity entity, Class<? extends EntityComponent> destClass) {
        return registry.has(entity, destClass);
    }

    /*
     * gets the component from the set
     */

    public <T extends EntityComponent> T get(Entity entity, Class<T> destClass) {
        return registry.get(entity, destClass);
    }

    /*
     * gets all the entities associated with these classes
     */

    public Set<Entity> view(Class<? extends EntityComponent> destClass) {
        return registry.view(destClass);
    }

    @SafeVarargs
    public final <V extends EntityComponent> Set<V> distinct(Class<? super V>... destClasses) {
        return registry.distinct(destClasses);
    }

    List<EntityComponent> getAllComponents(Entity e) {
        return registry.entityList.get(e);
    }

    public int getShapeCount() {
        return registry.entityList.keySet().size();
    }

    public Set<Entity> getEntities() {
        return registry.entityList.keySet();
    }

    public void setState(SceneState STATE) {
        this.STATE = STATE;
    }

    public SceneState getState() {
        return STATE;
    }

    Set<Map.Entry<Entity, List<EntityComponent>>> getEntityEntrySet() {
        return registry.entityList.entrySet();
    }

    public String getSceneName() {
        return sceneName;
    }

    public void disposeAllEntities() {
        var it = registry.entityList.entrySet().iterator();
        while (it.hasNext()) {
            var map = it.next();
            Entity e = map.getKey();
            if (e.has(EditorCamera.class)) continue;
            if (e.has(OpenGLMesh.class)) e.get(OpenGLMesh.class).dispose();

            //REMOVING ENV MAP BCS WE ARE GONNA REPLACE IT WITH THE NEW ENV MAP FROM THE NEW SCENE
            if (e.has(HDREnvironmentMap.class)) {
                EnvironmentTexture texture = e.get(HDREnvironmentMap.class).getEnvironmentTexture();
                if (texture != null) texture.dispose();
            } else if (e.has(DynamicSky.class)) {
                DynamicSky dynamicSky = e.get(DynamicSky.class);
                dynamicSky.dispose();
            }

            it.remove();
        }
        Shader pbrShader = Renderer.getShader("OxyPBR");
        pbrShader.begin();
        pbrShader.setUniformVec3("d_Light.direction", 0, 0, 0);
        pbrShader.setUniformVec3("d_Light.diffuse", 0, 0, 0);
        pbrShader.setUniform1i("d_Light.activeState", 0);
        pbrShader.end();
        Renderer.flushList();
    }

    @Override
    public void dispose() {
        disposeAllEntities();
        registry.entityList.clear();
        PerspectiveCamera.disposeUniformBuffer();
    }

    public static void openScene() {
        Renderer.flushList();
        String openScene = openDialog(extensionName, sceneContext.getWorkingDirectory());
        if (openScene != null) {
            ScriptEngine.clearProviders();
            SceneRuntime.runtimeStop();
            sceneContext = SceneSerializer.deserializeScene(openScene);
        }
    }

    public static void saveScene() {
        SceneRuntime.runtimeStop();
        if (sceneContext.workingDirectory == null || sceneContext.workingDirectory.equals("null")) {
            String s = saveDialog(extensionName, null);
            if (s == null) return;
            File f = new File(s);
            sceneContext.workingDirectory = f.getParent();
            sceneContext.sceneName = f.getName();
        }
        SceneSerializer.serializeScene(sceneContext.workingDirectory + "//" + sceneContext.getSceneName() + fileExtension);
    }

    public static void saveAs() {
        SceneRuntime.runtimeStop();
        String saveAs = saveDialog(extensionName, null);
        if (saveAs != null) SceneSerializer.serializeScene(saveAs + fileExtension);
    }

    public static void newScene() {
        Renderer.flushList();
        ScriptEngine.clearProviders();
        SceneRuntime.runtimeStop();
        SceneRuntime.entityContext = null;
        Scene oldScene = sceneContext;
        oldScene.disposeAllEntities();

        Scene scene = new Scene("Test Scene 1", null);
        for (var n : oldScene.getEntityEntrySet()) {
            scene.put(n.getKey());
            scene.addComponent(n.getKey(), n.getValue().toArray(EntityComponent[]::new));
        }
        sceneContext = scene;
//        SceneRenderer.getInstance().initScene();
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public Entity getRoot(Entity entity) {
        return registry.getRoot(entity);
    }
}