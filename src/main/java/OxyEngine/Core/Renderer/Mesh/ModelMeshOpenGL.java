package OxyEngine.Core.Renderer.Mesh;

import OxyEngine.Components.EntityFamily;
import OxyEngine.Components.SelectedComponent;
import OxyEngine.Components.TransformComponent;
import OxyEngine.Core.Layers.SceneLayer;
import OxyEngine.Core.Renderer.Buffer.*;
import OxyEngine.Core.Renderer.Buffer.Platform.*;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Scene.Objects.Model.OxyMaterial;
import OxyEngine.Scene.Objects.Model.OxyModel;
import OxyEngine.Scene.OxyEntity;
import OxyEngine.Scene.SceneRuntime;
import OxyEngineEditor.UI.Panels.GUINode;
import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.type.ImString;

import java.util.List;

import static OxyEngine.System.OxySystem.FileSystem.openDialog;
import static OxyEngine.System.OxySystem.oxyAssert;
import static OxyEngineEditor.UI.Gizmo.OxySelectHandler.entityContext;
import static org.lwjgl.opengl.GL11.GL_FLOAT;

public class ModelMeshOpenGL extends OpenGLMesh {

    public static final BufferLayoutAttributes attributeVert = new BufferLayoutAttributes(OxyShader.VERTICES, 3, GL_FLOAT, false, 12 * Float.BYTES, 0);
    private static final BufferLayoutAttributes attributeObjectID = new BufferLayoutAttributes(OxyShader.OBJECT_ID, 1, GL_FLOAT, false, 12 * Integer.BYTES, 3 * Integer.BYTES);
    private static final BufferLayoutAttributes attributeBoneIDS = new BufferLayoutAttributes(OxyShader.BONEIDS, 4, GL_FLOAT, false, 12 * Integer.BYTES, 4 * Integer.BYTES);
    private static final BufferLayoutAttributes attributeWeights = new BufferLayoutAttributes(OxyShader.WEIGHTS, 4, GL_FLOAT, false, 12 * Float.BYTES, 8 * Float.BYTES);

    private static final BufferLayoutAttributes attributeTXCoords = new BufferLayoutAttributes(OxyShader.TEXTURE_COORDS, 2, GL_FLOAT, false, 0, 0);

    private static final BufferLayoutAttributes attributeNormals = new BufferLayoutAttributes(OxyShader.NORMALS, 3, GL_FLOAT, false, 0, 0);

    private static final BufferLayoutAttributes attributeTangent = new BufferLayoutAttributes(OxyShader.TANGENT, 3, GL_FLOAT, false, 6 * Float.BYTES, 0);
    private static final BufferLayoutAttributes attributeBiTangent = new BufferLayoutAttributes(OxyShader.BITANGENT, 3, GL_FLOAT, false, 6 * Float.BYTES, 3 * Float.BYTES);
    private final String path;

    public ModelMeshOpenGL(String path, int mode, BufferLayoutProducer.Usage usage, float[] vertices, int[] indices, float[] textureCoords, float[] normals, float[] tangents, float[] biTangents) {
        this.path = path;
        this.mode = mode;

        assert textureCoords != null && indices != null && vertices != null : oxyAssert("Data that is given is null.");

        BufferLayoutRecord layout = BufferLayoutProducer.create()
                .createLayout(VertexBuffer.class)
                .setUsage(usage)
                .setAttribPointer(
                        attributeVert,
                        attributeObjectID,
                        attributeBoneIDS,
                        attributeWeights
                )
                .create()
                .createLayout(IndexBuffer.class).create()
                .createLayout(TextureBuffer.class)
                .setAttribPointer(
                        attributeTXCoords
                )
                .create()
                .createLayout(NormalsBuffer.class)
                .setAttribPointer(
                        attributeNormals
                )
                .create()
                .createLayout(TangentBuffer.class)
                .setAttribPointer(
                        attributeTangent,
                        attributeBiTangent
                )
                .create()
                .finalizeRecord();

        layout.vertexBuffer().setVertices(vertices);
        layout.indexBuffer().setIndices(indices);
        layout.textureBuffer().setTextureCoords(textureCoords);
        layout.normalsBuffer().setNormals(normals);

        float[] biAndTangents = new float[tangents.length + biTangents.length];
        int tangentPtr = 0, biTangentPtr = 0;
        for (int i = 0; i < biAndTangents.length; ) {
            biAndTangents[i++] = tangents[tangentPtr++];
            biAndTangents[i++] = tangents[tangentPtr++];
            biAndTangents[i++] = tangents[tangentPtr++];
            biAndTangents[i++] = biTangents[biTangentPtr++];
            biAndTangents[i++] = biTangents[biTangentPtr++];
            biAndTangents[i++] = biTangents[biTangentPtr++];
        }
        layout.tangentBuffer().setBiAndTangent(biAndTangents);

        vertexBuffer = (OpenGLVertexBuffer) layout.vertexBuffer();
        indexBuffer = (OpenGLIndexBuffer) layout.indexBuffer();
        normalsBuffer = (OpenGLNormalsBuffer) layout.normalsBuffer();
        tangentBuffer = (OpenGLTangentBuffer) layout.tangentBuffer();
        textureBuffer = (OpenGLTextureBuffer) layout.textureBuffer();
    }

    private static ImString meshPath = new ImString(0);
    public static final GUINode guiNode = () -> {
        if (entityContext == null) return;

        {
            if (ImGui.treeNodeEx("Mesh Renderer", ImGuiTreeNodeFlags.DefaultOpen)) {
                if (entityContext.has(OpenGLMesh.class))
                    meshPath = new ImString(entityContext.get(OpenGLMesh.class).getPath());
                else meshPath = new ImString("");

                ImGui.checkbox("Cast Shadows", false);

                ImGui.setNextItemOpen(true);
                ImGui.columns(2, "myColumns");
                ImGui.setColumnOffset(0, -120f);
                ImGui.alignTextToFramePadding();
                ImGui.text("Mesh:");
                ImGui.nextColumn();
                ImGui.pushItemWidth(ImGui.getContentRegionAvailX() - 30f);
                ImGui.inputText("##hidelabel", meshPath, ImGuiInputTextFlags.ReadOnly);
                ImGui.popItemWidth();
                ImGui.sameLine();
                if (ImGui.button("...")) {
                    String path = openDialog("", null);
                    if (path != null) {
                        if (entityContext != null) {
                            //removing the added entity with the new model entities and carrying the transform of the old entity to the new models.
                            TransformComponent c = entityContext.get(TransformComponent.class);
                            List<OxyModel> eList = SceneRuntime.ACTIVE_SCENE.createModelEntities(path, entityContext.get(OxyShader.class));
                            OxyEntity root = eList.get(0).getRoot();
                            EntityFamily family = entityContext.getFamily();
                            SceneRuntime.ACTIVE_SCENE.removeEntity(entityContext);
                            root.setFamily(family);

                            TransformComponent cRoot = root.get(TransformComponent.class);
                            cRoot.position.set(c.position);
                            cRoot.rotation.set(c.rotation);
                            cRoot.scale.set(c.scale);

                            for (OxyModel e : eList) {
                                e.addComponent(new SelectedComponent(false));
                                e.setFamily(new EntityFamily(root.getFamily()));
                                e.getGUINodes().add(ModelMeshOpenGL.guiNode);
                                if (!e.getGUINodes().contains(OxyMaterial.guiNode))
                                    e.getGUINodes().add(OxyMaterial.guiNode);
                                e.constructData();
                            }

                            cRoot.transform.mulLocal(c.transform);

                            for (OxyModel e : eList) {
                                e.transformLocally();
                            }

                            SceneLayer.getInstance().updateModelEntities();
                            meshPath = new ImString(path);
                            entityContext = root;
                        }
                    }
                }
                ImGui.columns(1);
                ImGui.separator();
                ImGui.treePop();
                ImGui.spacing();
            }
        }
    };

    public String getPath() {
        return path;
    }
}
