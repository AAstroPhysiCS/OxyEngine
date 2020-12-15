package OxyEngineEditor.Scene;

import OxyEngine.Components.*;
import OxyEngine.Core.Renderer.Buffer.FrameBuffer;
import OxyEngine.Core.Renderer.Buffer.Mesh;
import OxyEngine.Core.Renderer.OxyRenderer3D;
import OxyEngine.Core.Renderer.RenderingMode;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.System.OxyDisposable;
import OxyEngine.System.OxyUISystem;
import OxyEngineEditor.Scene.Objects.Model.*;
import OxyEngineEditor.Scene.Objects.Native.OxyNativeObject;
import org.joml.Vector3f;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static OxyEngine.System.OxySystem.oxyAssert;

public final class Scene implements OxyDisposable {

    private final Registry registry = new Registry();

    private final OxyRenderer3D renderer;
    private OxyUISystem oxyUISystem;

    private final FrameBuffer frameBuffer;
    private final String sceneName;

    public Scene(String sceneName, OxyRenderer3D renderer, FrameBuffer frameBuffer) {
        this.renderer = renderer;
        this.frameBuffer = frameBuffer;
        this.sceneName = sceneName;
    }

    public final void put(OxyEntity e) {
        registry.entityList.put(e, new LinkedHashSet<>(15));
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

    public final OxyModel createModelEntity(ModelType type, OxyShader shader, boolean importedFromFile) {
        return createModelEntity(type.getPath(), shader, importedFromFile);
    }

    public final List<OxyModel> createModelEntities(ModelType type, OxyShader shader) {
        return createModelEntities(type.getPath(), shader, false);
    }

    public final OxyModel createModelEntity(ModelType type, OxyShader shader) {
        return createModelEntity(type.getPath(), shader, false);
    }

    public final OxyModel createEmptyModel(OxyShader shader) {
        OxyModel e = new OxyModel(this);
        e.importedFromFile = false;
        put(e);
        e.originPos = new Vector3f(0, 0, 0);
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

    public final List<OxyModel> createModelEntities(String path, OxyShader shader, boolean importedFromFile) {
        List<OxyModel> models = new ArrayList<>();
        OxyModelLoader loader = new OxyModelLoader(path);

        int pos = 0;
        for (OxyModelLoader.AssimpOxyMesh assimpMesh : loader.meshes) {
            OxyModel e = new OxyModel(this);
            e.importedFromFile = importedFromFile;
            put(e);
            e.originPos = new Vector3f(assimpMesh.pos);
            e.addComponent(
                    new UUIDComponent(UUID.randomUUID()),
                    shader,
                    new BoundingBoxComponent(
                            assimpMesh.min,
                            assimpMesh.max
                    ),
                    new TransformComponent(new Vector3f(assimpMesh.pos)),
                    new ModelFactory(assimpMesh.vertices, assimpMesh.textureCoords, assimpMesh.normals, assimpMesh.faces, assimpMesh.tangents, assimpMesh.biTangents),
                    new TagComponent(assimpMesh.name == null ? "Unnamed" : assimpMesh.name),
                    new MeshPosition(pos),
                    new RenderableComponent(RenderingMode.Normal),
                    assimpMesh.material
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

    //performance improvement by caching the models
    static OxyModelLoader cachedLoader;
    static String cachedPath = "";

    public final OxyModel createModelEntity(String path, OxyShader shader, boolean importedFromFile, int i) {
        if (!cachedPath.equals(path)) {
            cachedLoader = new OxyModelLoader(path);
            cachedPath = path;
        }
        OxyModelLoader.AssimpOxyMesh assimpMesh = cachedLoader.meshes.get(i);
        OxyModel e = new OxyModel(this);
        e.importedFromFile = importedFromFile;
        put(e);
        e.originPos = new Vector3f(assimpMesh.pos);
        e.addComponent(
                new UUIDComponent(UUID.randomUUID()),
                shader,
                new BoundingBoxComponent(
                        assimpMesh.min,
                        assimpMesh.max
                ),
                new TransformComponent(new Vector3f(assimpMesh.pos)),
                new ModelFactory(assimpMesh.vertices, assimpMesh.textureCoords, assimpMesh.normals, assimpMesh.faces, assimpMesh.tangents, assimpMesh.biTangents),
                new TagComponent(assimpMesh.name == null ? "Unnamed" : assimpMesh.name),
                new MeshPosition(i),
                new RenderableComponent(RenderingMode.Normal),
                assimpMesh.material
        );
        e.initData(path);
        return e;
    }

    public final OxyModel createModelEntity(String path, OxyShader shader, boolean importedFromFile) {
        return createModelEntity(path, shader, importedFromFile, 0);
    }

    public final OxyModel createModelEntity(String path, OxyShader shader) {
        return createModelEntity(path, shader, false);
    }

    public final void removeEntity(OxyEntity e) {
        if (e.has(OxyMaterial.class)) e.get(OxyMaterial.class).dispose();
        if (e.has(Mesh.class)) e.get(Mesh.class).dispose();
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
    public final <V extends EntityComponent> Set<V> distinct(Class<? super V>... destClasses) {
        return registry.distinct(destClasses);
    }

    @SafeVarargs
    public final <U extends EntityComponent> Set<U> distinct(RegistryPredicate<Boolean, U> predicate, Class<U> type,
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

    public final void each(RegistryEach.Single<OxyEntity> registryEach, Predicate<OxyEntity> predicate) {
        Set<OxyEntity> entities = getEntities();
        entities.forEach(oxyEntity -> {
            if (predicate.test(oxyEntity)) {
                registryEach.each(oxyEntity);
            }
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

    public Set<Map.Entry<OxyEntity, Set<EntityComponent>>> getNativeObjects() {
        return registry.entityList.entrySet().stream().filter(oxyEntitySetEntry -> oxyEntitySetEntry.getKey() instanceof OxyNativeObject).collect(Collectors.toSet());
    }

    public FrameBuffer getFrameBuffer() {
        return frameBuffer;
    }

    public String getSceneName() {
        return sceneName;
    }

    @Override
    public void dispose() {
        Iterator<OxyEntity> it = registry.entityList.keySet().iterator();
        while (it.hasNext()) {
            OxyEntity e = it.next();
            if (e instanceof OxyModel) {
                if (e.has(Mesh.class)) e.get(Mesh.class).dispose();
                if (e.has(OxyMaterial.class)) e.get(OxyMaterial.class).dispose();
                it.remove();
            }
        }
//        assert registry.entityList.keySet().size() == 0 : oxyAssert("Scene dispose failed");
    }
}