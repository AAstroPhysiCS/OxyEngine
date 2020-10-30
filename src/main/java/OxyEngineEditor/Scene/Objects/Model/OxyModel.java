package OxyEngineEditor.Scene.Objects.Model;

import OxyEngine.Core.Renderer.Buffer.BufferTemplate;
import OxyEngine.Core.Renderer.Buffer.Mesh;
import OxyEngine.Core.Renderer.RenderingMode;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngineEditor.Components.*;
import OxyEngineEditor.Scene.OxyEntity;
import OxyEngineEditor.Scene.OxySerializable;
import OxyEngineEditor.Scene.Scene;
import org.joml.Vector3f;

import static OxyEngine.System.OxySystem.oxyAssert;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;

@OxySerializable(info = """
        \t\tOxyModel %s {
               \tID: %s
               \tNative Name: %s
               \tName: %s
               \tGrouped: %s
               \tEmitting: %s
               \tPosition: X %s, Y %s, Z %s
               \tRotation: X %s, Y %s, Z %s
               \tScale: X %s, Y %s, Z %s
               \tBounds Min: X %s, Y %s, Z %s
               \tBounds Max: X %s, Y %s, Z %s
               \tColor: %s
               \tScripts: [%s]
               \tAlbedo Texture: %s
               \tNormal Map Texture: %s
               \tRoughness Map Texture: %s
               \tAO Map Texture: %s
               \tMetallic Map Texture: %s
               \tMesh: %s
            }"""
)
public class OxyModel extends OxyEntity {

    private ModelFactory factory;

    public OxyModel(Scene scene) {
        super(scene);
    }

    public OxyModel(OxyModel other) {
        super(other.scene);
        this.tangents = other.tangents.clone();
        this.biTangents = other.biTangents.clone();
        this.originPos = new Vector3f(other.originPos);
        this.normals = other.normals.clone();
        this.vertices = other.vertices.clone();
        this.tcs = other.tcs.clone();
        this.indices = other.indices.clone();
    }

    @Override
    public OxyEntity copyMe() {
        OxyModel e = new OxyModel(this);
        e.addToScene();
        var boundingBox = get(BoundingBoxComponent.class);
        var transform = get(TransformComponent.class);
        e.addComponent(
                get(UUIDComponent.class),
                get(ModelFactory.class),
                get(OxyShader.class),
                new BoundingBoxComponent(
                        boundingBox.min(),
                        boundingBox.max()
                ),
                new TransformComponent(new TransformComponent(new Vector3f(0, 0, 0), transform.rotation, transform.scale)),
                new NativeTagComponent(get(NativeTagComponent.class).originalTag()),
                new TagComponent(get(TagComponent.class).tag() == null ? "Unnamed" : get(TagComponent.class).tag()),
                new RenderableComponent(RenderingMode.Normal),
                new OxyMaterial(get(OxyMaterial.class)),
                new SelectedComponent(false),
                new EntitySerializationInfo(get(EntitySerializationInfo.class))
        );
        e.initData(get(Mesh.class).getPath());
        return e;
    }

    @Override
    public void initData(String path) {
        assert has(ModelFactory.class) : oxyAssert("Models should have a Model Template");
        factory = get(ModelFactory.class);
        factory.constructData(this);
        addComponent(new ModelMesh.ModelMeshBuilderImpl()
                .setPath(path)
                .setShader(get(OxyShader.class))
                .setMode(GL_TRIANGLES)
                .setUsage(BufferTemplate.Usage.DYNAMIC)
                .setVertices(vertices)
                .setIndices(indices)
                .setTextureCoords(tcs)
                .setNormals(normals)
                .setTangents(tangents)
                .setBiTangents(biTangents)
                .create());
    }

    @Override
    public void constructData() {
        factory.constructData(this);
        get(Mesh.class).updateSingleEntityData(0, vertices);
    }

    @Override
    public void updateData() {
        factory.updateData(this);
        get(Mesh.class).updateSingleEntityData(0, vertices);
    }
}