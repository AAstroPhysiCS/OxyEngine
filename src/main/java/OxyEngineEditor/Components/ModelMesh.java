package OxyEngineEditor.Components;

import OxyEngine.Core.Renderer.Buffer.*;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngineEditor.Scene.Objects.Model.OxyModel;
import OxyEngineEditor.Scene.SceneRuntime;
import OxyEngineEditor.UI.Panels.PropertiesPanel;
import OxyEngineEditor.UI.Panels.PropertyEntry;
import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.type.ImString;

import java.util.List;

import static OxyEngine.System.OxySystem.FileSystem.openDialog;
import static OxyEngine.System.OxySystem.logger;
import static OxyEngine.System.OxySystem.oxyAssert;
import static OxyEngineEditor.UI.Selector.OxySelectHandler.entityContext;
import static org.lwjgl.opengl.GL11.GL_FLOAT;

public class ModelMesh extends Mesh {

    public static final BufferTemplate.Attributes attributesVert = new BufferTemplate.Attributes(OxyShader.VERTICES, 3, GL_FLOAT, false, 4 * Float.BYTES, 0);
    public static final BufferTemplate.Attributes attributesTXSlots = new BufferTemplate.Attributes(OxyShader.TEXTURE_SLOT, 1, GL_FLOAT, false, 4 * Float.BYTES, 3 * Float.BYTES);

    public static final BufferTemplate.Attributes attributesNormals = new BufferTemplate.Attributes(OxyShader.NORMALS, 3, GL_FLOAT, false, 0, 0);

    public static final BufferTemplate.Attributes attributesTangents = new BufferTemplate.Attributes(OxyShader.TANGENT, 3, GL_FLOAT, false, 6 * Float.BYTES, 0);
    public static final BufferTemplate.Attributes attributesBiTangents = new BufferTemplate.Attributes(OxyShader.BITANGENT, 3, GL_FLOAT, false, 6 * Float.BYTES, 3 * Float.BYTES);

    public static final BufferTemplate.Attributes attributesTXCoords = new BufferTemplate.Attributes(OxyShader.TEXTURE_COORDS, 2, GL_FLOAT, false, 0, 0);

    private final float[] vertices, textureCoords, normals, tangents, biTangents;
    private final int[] indices;

    private final String path;

    private ModelMesh(String path, OxyShader shader, BufferTemplate.Usage usage, int mode, float[] vertices, int[] indices, float[] textureCoords, float[] normals, float[] tangents, float[] biTangents) {
        this.path = path;
        this.shader = shader;
        this.vertices = vertices;
        this.indices = indices;
        this.textureCoords = textureCoords;
        this.normals = normals;
        this.mode = mode;
        this.tangents = tangents;
        this.biTangents = biTangents;

        vertexBuffer = new VertexBuffer(() -> new BufferTemplate.BufferTemplateImpl()
                .setVerticesStrideSize(attributesVert.stride() / Float.BYTES)
                .setUsage(usage)
                .setAttribPointer(attributesVert, attributesTXSlots));

        indexBuffer = new IndexBuffer();

        textureBuffer = new TextureBuffer(() -> new BufferTemplate.BufferTemplateImpl()
                .setAttribPointer(attributesTXCoords));

        normalsBuffer = new NormalsBuffer(() -> new BufferTemplate.BufferTemplateImpl()
                .setAttribPointer(attributesNormals));

        tangentBuffer = new TangentBuffer(() -> new BufferTemplate.BufferTemplateImpl()
                .setAttribPointer(attributesTangents, attributesBiTangents));

        vertexBuffer.setVertices(vertices);
        indexBuffer.setIndices(indices);
        textureBuffer.setTextureCoords(textureCoords);
        normalsBuffer.setNormals(normals);

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
        tangentBuffer.setBiAndTangent(biAndTangents);
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

        ModelMeshBuilder setUsage(BufferTemplate.Usage usage);

        ModelMeshBuilder setPath(String path);

        ModelMesh create();
    }

    public static class ModelMeshBuilderImpl implements ModelMeshBuilder {

        private OxyShader shader;
        private float[] vertices, textureCoords, normals, tangents, biTangents;
        private int[] indices;
        private int mode;
        private BufferTemplate.Usage usage;
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
        public ModelMeshBuilderImpl setUsage(BufferTemplate.Usage usage) {
            this.usage = usage;
            return this;
        }

        @Override
        public ModelMeshBuilderImpl setPath(String path) {
            this.path = path;
            return this;
        }

        @Override
        public ModelMesh create() {
            assert textureCoords != null && indices != null && vertices != null : oxyAssert("Data that is given is null.");
            return new ModelMesh(path, shader, usage, mode, vertices, indices, textureCoords, normals, tangents, biTangents);
        }
    }


    private static final boolean initPanel = false;
    private static ImString meshPath = new ImString(0);
    public static final PropertyEntry node = () -> {
        {
            if (ImGui.collapsingHeader("Mesh Renderer", ImGuiTreeNodeFlags.DefaultOpen)) {
                if (entityContext.has(Mesh.class))
                    meshPath = new ImString(entityContext.get(Mesh.class).getPath());
                else
                    meshPath = new ImString("");

                ImGui.checkbox("Cast Shadows", false);

                if (!initPanel) ImGui.setNextItemOpen(true);
                ImGui.columns(2, "myColumns");
                if (!initPanel) ImGui.setColumnOffset(0, -120f);
                ImGui.alignTextToFramePadding();
                ImGui.text("Mesh:");
                ImGui.nextColumn();
                ImGui.pushItemWidth(ImGui.getContentRegionAvailWidth() - 30f);
                ImGui.inputText("##hidelabel", meshPath);
                ImGui.popItemWidth();
                ImGui.sameLine();
                if (ImGui.button("...")) {
                    String path = openDialog("", null);
                    if (path != null) {
                        if (entityContext != null) {
                            List<OxyModel> eList = SceneRuntime.ACTIVE_SCENE.createModelEntities(path, entityContext.get(OxyShader.class));
                            boolean isGrouped = true;
                            boolean exception = false;
                            if (eList.size() == 1) isGrouped = false;
                            if (eList.size() == 0) {
                                exception = true;
                                logger.warning("Could not load the mesh");
                            }
                            for (OxyModel e : eList) {
                                TransformComponent t = new TransformComponent(entityContext.get(TransformComponent.class));
                                e.addComponent(t, new SelectedComponent(true, false), new EntitySerializationInfo(isGrouped, false));
                                e.getPropertyEntries().add(ModelMesh.node);
                                e.constructData();
                            }
                            if (!exception) {
                                SceneRuntime.ACTIVE_SCENE.removeEntity(entityContext);
                                PropertiesPanel.sceneLayer.updateAllModelEntities();
                                meshPath = new ImString(path);
                                entityContext = null;
                            }
                        }
                    }
                }
                ImGui.columns(1);
            }
        }
    };

    public float[] getTextureCoords() {
        return textureCoords;
    }

    public float[] getVertices() {
        return vertices;
    }

    public float[] getNormals() {
        return normals;
    }

    public float[] getTangents() {
        return tangents;
    }

    public float[] getBiTangents() {
        return biTangents;
    }

    public int[] getIndices() {
        return indices;
    }

    public String getPath() {
        return path;
    }
}
