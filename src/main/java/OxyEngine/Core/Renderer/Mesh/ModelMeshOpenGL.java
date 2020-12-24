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

    private final String path;

    private ModelMeshOpenGL(String path, OxyShader shader, int mode, BufferLayoutRecord layout) {
        this.path = path;
        this.shader = shader;
        this.mode = mode;

        vertexBuffer = (OpenGLVertexBuffer) layout.vertexBuffer();
        indexBuffer = (OpenGLIndexBuffer) layout.indexBuffer();
        normalsBuffer = (OpenGLNormalsBuffer) layout.normalsBuffer();
        tangentBuffer = (OpenGLTangentBuffer) layout.tangentBuffer();
        textureBuffer = (OpenGLTextureBuffer) layout.textureBuffer();
    }

    interface ModelMeshBuilder {

        ModelMeshBuilder setShader(OxyShader shader);

        ModelMeshBuilder setVertices(float[] vertices);

        ModelMeshBuilder setIndices(int[] vertices);

        ModelMeshBuilder setTextureCoords(float[] vertices);

        ModelMeshBuilder setNormals(float[] normals);

        ModelMeshBuilder setTangents(float[] tangents);

        ModelMeshBuilder setBiTangents(float[] biTangents);

        ModelMeshBuilder setMode(int mode);

        ModelMeshBuilder setUsage(BufferLayoutProducer.Usage usage);

        ModelMeshBuilder setPath(String path);

        ModelMeshOpenGL create();
    }

    public static class ModelMeshBuilderImpl implements ModelMeshBuilder {

        private OxyShader shader;
        private float[] vertices, textureCoords, normals, tangents, biTangents;
        private int[] indices;
        private int mode;
        private BufferLayoutProducer.Usage usage;
        private String path;

        @Override
        public ModelMeshBuilderImpl setShader(OxyShader shader) {
            this.shader = shader;
            return this;
        }

        @Override
        public ModelMeshBuilderImpl setVertices(float[] vertices) {
            this.vertices = vertices;
            return this;
        }

        @Override
        public ModelMeshBuilderImpl setIndices(int[] indices) {
            this.indices = indices;
            return this;
        }

        @Override
        public ModelMeshBuilderImpl setTextureCoords(float[] textureCoords) {
            this.textureCoords = textureCoords;
            return this;
        }

        @Override
        public ModelMeshBuilderImpl setNormals(float[] normals) {
            this.normals = normals;
            return this;
        }

        @Override
        public ModelMeshBuilderImpl setTangents(float[] tangents) {
            this.tangents = tangents;
            return this;
        }

        @Override
        public ModelMeshBuilderImpl setBiTangents(float[] biTangents) {
            this.biTangents = biTangents;
            return this;
        }

        @Override
        public ModelMeshBuilderImpl setMode(int mode) {
            this.mode = mode;
            return this;
        }

        @Override
        public ModelMeshBuilderImpl setUsage(BufferLayoutProducer.Usage usage) {
            this.usage = usage;
            return this;
        }

        @Override
        public ModelMeshBuilderImpl setPath(String path) {
            this.path = path;
            return this;
        }

        @Override
        public ModelMeshOpenGL create() {
            assert textureCoords != null && indices != null && vertices != null : oxyAssert("Data that is given is null.");

            BufferLayoutRecord layout = BufferLayoutProducer.create()
                    .createLayout(VertexBuffer.class)
                        .setStrideSize(4)
                        .setUsage(usage)
                        .setAttribPointer(
                                new BufferLayoutAttributes(OxyShader.VERTICES, 3, GL_FLOAT, false, 4 * Float.BYTES, 0),
                                new BufferLayoutAttributes(OxyShader.TEXTURE_SLOT, 1, GL_FLOAT, false, 4 * Float.BYTES, 3 * Float.BYTES)
                        )
                        .create()
                    .createLayout(IndexBuffer.class).create()
                    .createLayout(TextureBuffer.class)
                        .setAttribPointer(
                            new BufferLayoutAttributes(OxyShader.TEXTURE_COORDS, 2, GL_FLOAT, false, 0, 0)
                        )
                        .create()
                    .createLayout(NormalsBuffer.class)
                        .setAttribPointer(
                            new BufferLayoutAttributes(OxyShader.NORMALS, 3, GL_FLOAT, false, 0, 0)
                        )
                        .create()
                    .createLayout(TangentBuffer.class)
                        .setAttribPointer(
                            new BufferLayoutAttributes(OxyShader.TANGENT, 3, GL_FLOAT, false, 6 * Float.BYTES, 0),
                            new BufferLayoutAttributes(OxyShader.BITANGENT, 3, GL_FLOAT, false, 6 * Float.BYTES, 3 * Float.BYTES)
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

            return new ModelMeshOpenGL(path, shader, mode, layout);
        }
    }

    private static final boolean initPanel = false;
    private static ImString meshPath = new ImString(0);
    public static final GUINode guiNode = () -> {
        {
            if (ImGui.collapsingHeader("Mesh Renderer", ImGuiTreeNodeFlags.DefaultOpen)) {
                if (entityContext.has(OpenGLMesh.class)) meshPath = new ImString(entityContext.get(OpenGLMesh.class).getPath());
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
