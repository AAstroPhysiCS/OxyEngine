package OxyEngineEditor.Scene;

import OxyEngine.Core.Renderer.Buffer.FrameBuffer;
import OxyEngine.Core.Renderer.OxyRenderer3D;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Core.Renderer.Texture.OxyColor;
import OxyEngine.System.OxyDisposable;
import OxyEngineEditor.Components.*;
import OxyEngineEditor.Scene.Model.ModelFactory;
import OxyEngineEditor.Scene.Model.ModelType;
import OxyEngineEditor.Scene.Model.OxyModel;
import OxyEngineEditor.Scene.Model.OxyModelLoader;
import OxyEngineEditor.Scene.NativeObjects.OxyNativeObject;
import OxyEngineEditor.UI.OxyUISystem;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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

    public void setUISystem(OxyUISystem oxyUISystem) {
        this.oxyUISystem = oxyUISystem;
    }

    public int getShapeCount() { return registry.entityList.keySet().size(); }

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