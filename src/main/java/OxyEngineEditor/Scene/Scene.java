package OxyEngineEditor.Scene;

import OxyEngine.Core.Renderer.Buffer.FrameBuffer;
import OxyEngine.Core.Renderer.Buffer.Mesh;
import OxyEngine.Core.Renderer.OxyRenderer3D;
import OxyEngine.Core.Renderer.RenderingMode;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.System.OxyDisposable;
import OxyEngine.System.OxyUISystem;
import OxyEngineEditor.Components.*;
import OxyEngineEditor.Scene.Objects.Model.ModelFactory;
import OxyEngineEditor.Scene.Objects.Model.ModelType;
import OxyEngineEditor.Scene.Objects.Model.OxyModel;
import OxyEngineEditor.Scene.Objects.Model.OxyModelLoader;
import OxyEngineEditor.Scene.Objects.Native.OxyNativeObject;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static OxyEngine.System.OxySystem.oxyAssert;

public class Scene implements OxyDisposable {

    private final Registry registry = new Registry();

    private final OxyRenderer3D renderer;
    private final OxyEntitySystemRunner entitySystemRunner;
    private OxyUISystem oxyUISystem;

    private final FrameBuffer frameBuffer;
    private final String sceneName;

    public Scene(String sceneName, OxyRenderer3D renderer, FrameBuffer frameBuffer) {
        this.renderer = renderer;
        this.frameBuffer = frameBuffer;
        this.sceneName = sceneName;
        entitySystemRunner = new OxyEntitySystemRunner(this);
    }

    final void put(OxyEntity e){
        registry.entityList.put(e, new LinkedHashSet<>(15));
    }

    public final OxyNativeObject createNativeObjectEntity() {
        return createNativeObjectEntity(1);
    }

    public final OxyNativeObject createNativeObjectEntity(int size) {
        OxyNativeObject e = new OxyNativeObject(this, size);
        put(e);
        e.addComponent(new TransformComponent(), new RenderableComponent(RenderingMode.Normal));
        return e;
    }

    public final List<OxyModel> createModelEntities(ModelType type, OxyShader shader) {
        return createModelEntities(type.getPath(), shader);
    }

    public final OxyModel createModelEntity(ModelType type, OxyShader shader) {
        return createModelEntity(type.getPath(), shader);
    }

    public final List<OxyModel> createModelEntities(String path, OxyShader shader) {
        List<OxyModel> models = new ArrayList<>();
        OxyModelLoader loader = new OxyModelLoader(path);

        for (OxyModelLoader.AssimpOxyMesh assimpMesh : loader.meshes) {
            OxyModel e = new OxyModel(this);
            put(e);
            e.originPos = new Vector3f(assimpMesh.pos);
            e.addComponent(
                    shader,
                    new BoundingBoxComponent(
                            assimpMesh.min,
                            assimpMesh.max
                    ),
                    new TransformComponent(new Vector3f(assimpMesh.pos)),
                    new ModelFactory(assimpMesh.vertices, assimpMesh.textureCoords, assimpMesh.normals, assimpMesh.faces, assimpMesh.tangents, assimpMesh.biTangents),
                    new TagComponent(assimpMesh.name == null ? "Unnamed" : assimpMesh.name),
                    new RenderableComponent(RenderingMode.Normal),
                    assimpMesh.material
            );
            e.initData(path);
            models.add(e);
        }
        return models;
    }

    public final OxyModel createModelEntity(String path, OxyShader shader) {
        OxyModelLoader loader = new OxyModelLoader(path);
        OxyModelLoader.AssimpOxyMesh assimpMesh = loader.meshes.get(0);
        OxyModel e = new OxyModel(this);
        put(e);
        e.originPos = new Vector3f(assimpMesh.pos);
        e.addComponent(
                shader,
                new BoundingBoxComponent(
                        assimpMesh.min,
                        assimpMesh.max
                ),
                new TransformComponent(new Vector3f(assimpMesh.pos)),
                new ModelFactory(assimpMesh.vertices, assimpMesh.textureCoords, assimpMesh.normals, assimpMesh.faces, assimpMesh.tangents, assimpMesh.biTangents),
                new TagComponent(assimpMesh.name == null ? "Unnamed" : assimpMesh.name),
                new RenderableComponent(RenderingMode.Normal),
                assimpMesh.material
        );
        e.initData(path);
        return e;
    }

    public final void removeEntity(OxyEntity e) {
        e.get(Mesh.class).dispose();
        var value = registry.entityList.remove(e);
        assert !registry.entityList.containsKey(e) && !registry.entityList.containsValue(value) : oxyAssert("Remove entity failed!");
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

    @SafeVarargs
    public final <U extends EntityComponent> Set<EntityComponent> distinct(RegistryPredicate<Boolean, U> predicate, Class<U> type,
                                                                           Class<? extends EntityComponent>... destClasses) {
        return registry.distinct(predicate, type, destClasses);
    }

    @SafeVarargs
    public final <U extends EntityComponent, K extends U> void each(RegistryEach.Group<OxyEntity, K> registryEach, Class<? extends K>... destClasses) {
        Set<OxyEntity> entities = group(destClasses);
        Set<K> components = new LinkedHashSet<>();
        entities.forEach(oxyEntity -> {
            for (Class<? extends K> classes : destClasses) {
                K c = oxyEntity.get(classes);
                components.add(c);
            }
            registryEach.each(oxyEntity, components);
        });
    }

    public final <U extends EntityComponent, K extends U> void each(RegistryEach.View<OxyEntity, K> registryEach, Class<K> destClass) {
        Set<OxyEntity> entities = view(destClass);
        entities.forEach(oxyEntity -> registryEach.each(oxyEntity, oxyEntity.get(destClass)));
    }

    @SafeVarargs
    public final <U extends EntityComponent> void each(RegistryEach.Single<OxyEntity> registryEach, Class<? extends U>... destClass) {
        Set<OxyEntity> entities = group(destClass);
        entities.forEach(registryEach::each);
    }

    public final void each(RegistryEach.Single<OxyEntity> registryEach) {
        Stream<OxyEntity> stream = registry.entityList.keySet().stream();
        stream.forEach(registryEach::each);
    }

    public void setUISystem(OxyUISystem oxyUISystem) {
        this.oxyUISystem = oxyUISystem;
    }

    public int getShapeCount() {
        return registry.entityList.keySet().size();
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

    public OxyEntitySystemRunner getEntitySystemRunner() {
        return entitySystemRunner;
    }

    @Override
    public void dispose() {
        oxyUISystem.dispose();
    }
}