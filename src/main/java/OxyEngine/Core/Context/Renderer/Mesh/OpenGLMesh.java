package OxyEngine.Core.Context.Renderer.Mesh;

import OxyEngine.Components.EntityComponent;
import OxyEngine.Components.EntityFamily;
import OxyEngine.Components.SelectedComponent;
import OxyEngine.Components.TransformComponent;
import OxyEngine.Core.Context.OxyRenderer;
import OxyEngine.Core.Context.Renderer.Mesh.Platform.*;
import OxyEngine.Core.Context.Renderer.Pipeline.OxyPipeline;
import OxyEngine.Core.Context.Renderer.Pipeline.ShaderType;
import OxyEngine.Core.Context.Scene.OxyEntity;
import OxyEngine.Core.Context.Scene.OxyMaterial;
import OxyEngine.Core.Context.Scene.OxyModel;
import OxyEngine.Core.Context.Scene.SceneRuntime;
import OxyEngine.Core.Context.SceneRenderer;
import OxyEngine.System.OxyDisposable;
import OxyEngineEditor.UI.Panels.GUINode;
import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.type.ImString;

import java.util.List;

import static OxyEngine.Core.Context.Scene.SceneRuntime.entityContext;
import static OxyEngine.System.OxyFileSystem.openDialog;
import static OxyEngine.System.OxySystem.oxyAssert;
import static OxyEngineEditor.UI.Panels.ProjectPanel.dirAssetGrey;
import static org.lwjgl.opengl.GL45.*;

//TODO: Make a Mesh class that this class will inherit from, as well as the VulkanMesh class.
public final class OpenGLMesh implements OxyDisposable, EntityComponent {

    protected OpenGLIndexBuffer indexBuffer;
    protected OpenGLVertexBuffer vertexBuffer;
    protected OpenGLTextureBuffer textureBuffer;
    protected OpenGLNormalsBuffer normalsBuffer;
    protected OpenGLTangentBuffer tangentBuffer;

    protected OxyPipeline pipeline;

    protected String path;

    protected int vao;
    protected MeshRenderMode mode;

    public OpenGLMesh(OxyPipeline pipeline, String path, MeshRenderMode mode, MeshUsage usage, float[] vertices, int[] indices, float[] textureCoords, float[] normals, float[] tangents, float[] biTangents) {
        this.pipeline = pipeline;
        this.path = path;
        this.mode = mode;

        vertexBuffer = VertexBuffer.create(pipeline, usage);
        indexBuffer = IndexBuffer.create(pipeline);
        textureBuffer = TextureBuffer.create(pipeline);
        normalsBuffer = NormalsBuffer.create(pipeline);
        tangentBuffer = TangentBuffer.create(pipeline);

        vertexBuffer.setVertices(vertices);
        indexBuffer.setIndices(indices);
        textureBuffer.setTextureCoords(textureCoords);
        normalsBuffer.setNormals(normals);
        tangentBuffer.setBiAndTangent(tangents, biTangents);

        assert textureCoords != null && indices != null && vertices != null : oxyAssert("Data that is given is null.");
    }

    public OpenGLMesh(OxyPipeline pipeline, MeshUsage usage){
        this.pipeline = pipeline;
        this.mode = this.pipeline.getRenderPass().getMeshRenderingMode();
        vertexBuffer = VertexBuffer.create(pipeline, usage);
        indexBuffer = IndexBuffer.create(pipeline);
    }

    public boolean empty() {
        return indexBuffer.glBufferNull() && vertexBuffer.glBufferNull();
    }

    public String getPath() {
        return path;
    }

    public void load() {

        if (vao == 0) vao = glCreateVertexArrays();
        glBindVertexArray(vao);

        vertexBuffer.load();
        if (indexBuffer != null) if (indexBuffer.glBufferNull() && !indexBuffer.emptyData()) indexBuffer.load();
        if (normalsBuffer != null) if (normalsBuffer.glBufferNull() && !normalsBuffer.emptyData()) normalsBuffer.load();
        if (tangentBuffer != null) if (tangentBuffer.glBufferNull() && !tangentBuffer.emptyData()) tangentBuffer.load();
        if (textureBuffer != null) if (textureBuffer.glBufferNull() && !textureBuffer.emptyData()) textureBuffer.load();

        if (vertexBuffer.getUsage() == MeshUsage.DYNAMIC) {
            glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer.getBufferId());
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertexBuffer.getVertices());
        }

        processPipeline(pipeline);
    }

    private void draw() {
        glDrawElements(mode.getModeID(), indexBuffer.length(), GL_UNSIGNED_INT, 0);
    }

    private void bind() {
        glBindVertexArray(vao);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer.bufferId);
        if (vertexBuffer.getUsage() == MeshUsage.DYNAMIC && vertexBuffer.offsetToUpdate != -1) {
            glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer.bufferId);
            glBufferSubData(GL_ARRAY_BUFFER, vertexBuffer.offsetToUpdate, vertexBuffer.dataToUpdate);

            vertexBuffer.offsetToUpdate = -1;
        }
    }

    private void unbind() {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        OxyRenderer.Stats.drawCalls++;
        OxyRenderer.Stats.totalVertexCount += vertexBuffer.getVertices().length;
        OxyRenderer.Stats.totalIndicesCount += indexBuffer.getIndices().length;
    }

    public void pushVertices(float[] vertices){
        vertexBuffer.addToBuffer(vertices);
    }

    public void pushIndices(int[] indices){
        indexBuffer.addToBuffer(indices);
    }

    public void addToList(OxyEntity e) {
        vertexBuffer.addToBuffer(e.getVertices());
        indexBuffer.addToBuffer(e.getIndices());
    }

    //TODO: Dont do this public
    public void render() {
        bind();
        draw();
        unbind();
    }

    public void updateSingleEntityData(int offsetToUpdate, float[] dataToUpdate) {
        vertexBuffer.updateSingleEntityData(offsetToUpdate, dataToUpdate);
    }

    @Override
    public void dispose() {
        vertexBuffer.dispose();
        if (indexBuffer != null) indexBuffer.dispose();
        if (textureBuffer != null) textureBuffer.dispose();
        if (normalsBuffer != null) normalsBuffer.dispose();
        if (tangentBuffer != null) tangentBuffer.dispose();
        glDeleteVertexArrays(vao);
        vao = 0;
    }

    public void setRenderingMode(MeshRenderMode mode) {
        this.mode = mode;
    }
    
    private void processPipeline(OxyPipeline pipeline) {
        if (pipeline.getLayouts() == null) return;
        for (OxyPipeline.Layout layout : pipeline.getLayouts()) {

            int offset = 0;
            int stride = 0;

            //stride = 0 when just vertexbuffer and indexbuffer is defined
            if (pipeline.getLayouts().size() != 2) { //vertexbuffer and indexbuffer
                for (var type : layout.shaderLayout().values()) {
                    stride += type.getSize();
                }
            }

            Class<? extends Buffer> targetBuffer = layout.getTargetBuffer();

            if (targetBuffer.equals(VertexBuffer.class)) {
                glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer.bufferId);
            } else if (targetBuffer.equals(IndexBuffer.class)) {
                glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer.bufferId);
            } else if (targetBuffer.equals(TextureBuffer.class)) {
                glBindBuffer(GL_ARRAY_BUFFER, textureBuffer.bufferId);
            } else if (targetBuffer.equals(NormalsBuffer.class)) {
                glBindBuffer(GL_ARRAY_BUFFER, normalsBuffer.bufferId);
            } else if (targetBuffer.equals(TangentBuffer.class)) {
                glBindBuffer(GL_ARRAY_BUFFER, tangentBuffer.bufferId);
            }

            for (var entrySet : layout.shaderLayout().entrySet()) {
                int bufferIndex = entrySet.getKey();
                ShaderType type = entrySet.getValue();

                glEnableVertexAttribArray(bufferIndex);
                switch (type) {
                    case Float1, Float2, Float3, Float4, Matrix3f, Matrix4f -> glVertexAttribPointer(bufferIndex, type.getSize(), type.getOpenGLType(), layout.normalized(), stride * Float.BYTES, (long) offset * Float.BYTES);
                    case Int1, Int2, Int3, Int4 -> glVertexAttribIPointer(bufferIndex, type.getSize(), type.getOpenGLType(), stride * Float.BYTES, (long) offset * Integer.BYTES);
                    default -> throw new IllegalStateException("No implementation to a type");
                }
                offset += type.getSize();
            }

            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        }
    }

    private static ImString meshPath = new ImString(0);

    public static final GUINode guiNode = () -> {
        if (entityContext == null) return;

        {
            if (ImGui.treeNodeEx("Mesh Renderer", ImGuiTreeNodeFlags.DefaultOpen)) {
                if (entityContext.has(OpenGLMesh.class))
                    meshPath = new ImString(entityContext.get(OpenGLMesh.class).getPath());
                else meshPath = new ImString("");

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
                if (ImGui.imageButton(dirAssetGrey.getTextureId(), 20, 20, 0, 1, 1, 0, 0)) {
                    String path = openDialog("", null);
                    if (path != null) {
                        if (entityContext != null) {
                            //removing the added entity with the new model entities and carrying the transform of the old entity to the new models.
                            TransformComponent c = entityContext.get(TransformComponent.class);
                            List<OxyModel> eList = SceneRuntime.ACTIVE_SCENE.createModelEntities(path);
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
                                e.getGUINodes().add(OpenGLMesh.guiNode);
                                if (!e.getGUINodes().contains(OxyMaterial.guiNode))
                                    e.getGUINodes().add(OxyMaterial.guiNode);
                                e.updateData();
                            }

                            cRoot.transform.mulLocal(c.transform);

                            for (OxyModel e : eList) {
                                e.transformLocally();
                            }

                            SceneRenderer.getInstance().updateModelEntities();
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
}