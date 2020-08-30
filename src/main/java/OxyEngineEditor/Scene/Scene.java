package OxyEngineEditor.Scene;

import OxyEngine.Core.Renderer.Buffer.FrameBuffer;
import OxyEngine.Core.Renderer.OxyRenderer3D;
import OxyEngine.Core.Renderer.RenderingMode;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.System.OxyDisposable;
import OxyEngineEditor.Components.*;
import OxyEngineEditor.Scene.Model.*;
import OxyEngineEditor.Scene.NativeObjects.OxyNativeObject;
import OxyEngineEditor.UI.OxyUISystem;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class Scene implements OxyDisposable {

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

    public final OxyNativeObject createNativeObjectEntity() {
        OxyNativeObject e = new OxyNativeObject(this);
        registry.entityList.put(e, new LinkedHashSet<>(15));
        OxyModel a = new OxyModel(this);
        e.addComponent(new TransformComponent(), new RenderableComponent(RenderingMode.Normal));
        return e;
    }

    public final List<OxyEntity> createModelEntities(ModelType type, OxyShader shader) {
        return createModelEntities(type.getPath(), shader);
    }

    public final List<OxyEntity> createModelEntities(String path, OxyShader shader) {
        List<OxyEntity> models = new ArrayList<>();
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
                    new ModelFactory(assimpMesh.vertices, assimpMesh.textureCoords, assimpMesh.normals, assimpMesh.faces),
                    new TagComponent(assimpMesh.name == null ? "Unnamed" : assimpMesh.name),
                    new RenderableComponent(RenderingMode.Normal),
                    new OxyMaterial(
                            assimpMesh.material.texture,
                            assimpMesh.material.ambientColor,
                            assimpMesh.material.diffuseColor,
                            assimpMesh.material.specularColor,
                            assimpMesh.material.reflectance
                    )
            );
            e.initData();
            models.add(e);
        }
        return models;
    }

    public final OxyEntity createModelEntity(String path, OxyShader shader) {
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
                new ModelFactory(assimpMesh.vertices, assimpMesh.textureCoords, assimpMesh.normals, assimpMesh.faces),
                new TagComponent(assimpMesh.name == null ? "Unnamed" : assimpMesh.name),
                new RenderableComponent(RenderingMode.Normal),
                new OxyMaterial(
                        assimpMesh.material.texture,
                        assimpMesh.material.ambientColor,
                        assimpMesh.material.diffuseColor,
                        assimpMesh.material.specularColor,
                        assimpMesh.material.reflectance
                )
        );
        e.initData();
        return e;
    }

    public final void deleteEntity(OxyEntity e){
        registry.entityList.remove(e);
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
        Stream<OxyEntity> stream = entities.stream();
        Set<K> components = new LinkedHashSet<>();
        stream.forEach(oxyEntity -> {
            for (Class<? extends K> classes : destClasses) {
                K c = oxyEntity.get(classes);
                components.add(c);
            }
            registryEach.each(oxyEntity, components);
        });
    }

    public final <U extends EntityComponent, K extends U> void each(RegistryEach.View<OxyEntity, K> registryEach, Class<K> destClass) {
        Set<OxyEntity> entities = view(destClass);
        Stream<OxyEntity> stream = entities.stream();
        stream.forEach(oxyEntity -> registryEach.each(oxyEntity, oxyEntity.get(destClass)));
    }

    @SafeVarargs
    public final <U extends EntityComponent> void each(RegistryEach.SingleWithClass<OxyEntity> registryEach, Class<? extends U>... destClass) {
        Set<OxyEntity> entities = group(destClass);
        Stream<OxyEntity> stream = entities.stream();
        stream.forEach(registryEach::each);
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

    @Override
    public void dispose() {
        oxyUISystem.dispose();
    }
}