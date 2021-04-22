package OxyEngine.Core.Renderer.Mesh;

import OxyEngine.Components.EntityFamily;
import OxyEngine.Components.SelectedComponent;
import OxyEngine.Components.TransformComponent;
import OxyEngine.Core.Renderer.Buffer.*;
import OxyEngine.Core.Renderer.Pipeline.OxyPipeline;
import OxyEngine.Scene.Objects.Model.OxyMaterial;
import OxyEngine.Scene.Objects.Model.OxyModel;
import OxyEngine.Scene.OxyEntity;
import OxyEngine.Scene.SceneRenderer;
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
import static OxyEngineEditor.UI.Panels.ProjectPanel.dirAssetGrey;

public class ModelMeshOpenGL extends OpenGLMesh {

    private final String path;

    public ModelMeshOpenGL(OxyPipeline pipeline, String path, MeshRenderMode mode, float[] vertices, int[] indices, float[] textureCoords, float[] normals, float[] tangents, float[] biTangents) {
        this.path = path;
        this.mode = mode;

        assert pipeline != null : oxyAssert("Pipeline null!");

        vertexBuffer = VertexBuffer.create(pipeline, MeshUsage.DYNAMIC);
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
                                e.getGUINodes().add(ModelMeshOpenGL.guiNode);
                                if (!e.getGUINodes().contains(OxyMaterial.guiNode))
                                    e.getGUINodes().add(OxyMaterial.guiNode);
                                e.constructData();
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

    public String getPath() {
        return path;
    }
}
