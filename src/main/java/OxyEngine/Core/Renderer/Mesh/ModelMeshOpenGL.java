package OxyEngine.Core.Renderer.Mesh;

import OxyEngine.Components.SelectedComponent;
import OxyEngine.Components.TransformComponent;
import OxyEngine.Core.Renderer.Buffer.*;
import OxyEngine.Core.Renderer.Buffer.Platform.*;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngineEditor.Scene.Objects.Model.OxyMaterial;
import OxyEngineEditor.Scene.Objects.Model.OxyModel;
import OxyEngineEditor.Scene.SceneRuntime;
import OxyEngineEditor.UI.Panels.GUINode;
import OxyEngineEditor.UI.Panels.PropertiesPanel;
import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.type.ImString;

import java.util.List;

import static OxyEngine.System.OxySystem.FileSystem.openDialog;
import static OxyEngine.System.OxySystem.oxyAssert;
import static OxyEngineEditor.UI.Selector.OxySelectHandler.entityContext;
import static org.lwjgl.opengl.GL11.GL_FLOAT;

public class ModelMeshOpenGL extends OpenGLMesh {

    public static final BufferLayoutAttributes attributeVert = new BufferLayoutAttributes(OxyShader.VERTICES, 3, GL_FLOAT, false, 4 * Float.BYTES, 0);
    public static final BufferLayoutAttributes attributeTXSlot = new BufferLayoutAttributes(OxyShader.TEXTURE_SLOT, 1, GL_FLOAT, false, 4 * Float.BYTES, 3 * Float.BYTES);

    private static final BufferLayoutAttributes attributeTXCoords = new BufferLayoutAttributes(OxyShader.TEXTURE_COORDS, 2, GL_FLOAT, false, 0, 0);

    private static final BufferLayoutAttributes attributeNormals = new BufferLayoutAttributes(OxyShader.NORMALS, 3, GL_FLOAT, false, 0, 0);

    private static final BufferLayoutAttributes attributeTangent = new BufferLayoutAttributes(OxyShader.TANGENT, 3, GL_FLOAT, false, 6 * Float.BYTES, 0);
    private static final BufferLayoutAttributes attributeBiTangent = new BufferLayoutAttributes(OxyShader.BITANGENT, 3, GL_FLOAT, false, 6 * Float.BYTES, 3 * Float.BYTES);

    private final String path;

    public ModelMeshOpenGL(String path, OxyShader shader, int mode, BufferLayoutProducer.Usage usage, float[] vertices, int[] indices, float[] textureCoords, float[] normals, float[] tangents, float[] biTangents) {
        this.path = path;
        this.shader = shader;
        this.mode = mode;

        assert textureCoords != null && indices != null && vertices != null : oxyAssert("Data that is given is null.");

        BufferLayoutRecord layout = BufferLayoutProducer.create()
                .createLayout(VertexBuffer.class)
                .setStrideSize(4)
                .setUsage(usage)
                .setAttribPointer(
                        attributeVert,
                        attributeTXSlot
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
                .finalizeLayout();

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

    private static final boolean initPanel = false;
    private static ImString meshPath = new ImString(0);
    public static final GUINode guiNode = () -> {
        {
            if (ImGui.collapsingHeader("Mesh Renderer", ImGuiTreeNodeFlags.DefaultOpen)) {
                if (entityContext.has(OpenGLMesh.class))
                    meshPath = new ImString(entityContext.get(OpenGLMesh.class).getPath());
                else meshPath = new ImString("");

                ImGui.checkbox("Cast Shadows", false);

                if (!initPanel) ImGui.setNextItemOpen(true);
                ImGui.columns(2, "myColumns");
                if (!initPanel) ImGui.setColumnOffset(0, -120f);
                ImGui.alignTextToFramePadding();
                ImGui.text("Mesh:");
                ImGui.nextColumn();
                ImGui.pushItemWidth(ImGui.getContentRegionAvailWidth() - 30f);
                ImGui.inputText("##hidelabel", meshPath, ImGuiInputTextFlags.ReadOnly);
                ImGui.popItemWidth();
                ImGui.sameLine();
                if (ImGui.button("...")) {
                    String path = openDialog("", null);
                    if (path != null) {
                        if (entityContext != null) {
                            List<OxyModel> eList = SceneRuntime.ACTIVE_SCENE.createModelEntities(path, entityContext.get(OxyShader.class));
                            for (OxyModel e : eList) {
                                TransformComponent t = new TransformComponent(entityContext.get(TransformComponent.class));
                                e.addComponent(t, new SelectedComponent(true, false), entityContext.get(OxyMaterial.class));
                                e.getGUINodes().add(ModelMeshOpenGL.guiNode);
                                e.getGUINodes().add(OxyMaterial.guiNode);
                                e.constructData();
                            }
                            SceneRuntime.ACTIVE_SCENE.removeEntity(entityContext);
                            PropertiesPanel.sceneLayer.updateAllEntities();
                            meshPath = new ImString(path);
                            entityContext = null;
                        }
                    }
                }
                ImGui.columns(1);
            }
        }
    };

    public String getPath() {
        return path;
    }
}
