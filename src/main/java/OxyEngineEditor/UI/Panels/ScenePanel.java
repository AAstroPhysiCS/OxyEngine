package OxyEngineEditor.UI.Panels;

import OxyEngine.Components.SelectedComponent;
import OxyEngine.Components.TagComponent;
import OxyEngine.Components.TransformComponent;
import OxyEngine.Core.Camera.EditorCamera;
import OxyEngine.Core.Camera.PerspectiveCamera;
import OxyEngine.Core.Context.Renderer.Buffer.Platform.OpenGLFrameBuffer;
import OxyEngine.Core.Context.Renderer.Mesh.ModelMeshOpenGL;
import OxyEngine.Scene.Objects.Model.OxyModel;
import OxyEngine.Scene.Objects.Model.OxyNativeObject;
import OxyEngine.Scene.OxyMaterial;
import OxyEngine.Scene.SceneRenderer;
import OxyEngine.Scene.SceneRuntime;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.extension.imguizmo.ImGuizmo;
import imgui.extension.imguizmo.flag.Operation;
import imgui.flag.ImGuiStyleVar;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static OxyEngine.Scene.OxyEntity.addParentTransformToChildren;
import static OxyEngine.Scene.SceneRuntime.ACTIVE_SCENE;
import static OxyEngine.Scene.SceneRuntime.currentBoundedCamera;
import static OxyEngine.System.OxySystem.getExtension;
import static OxyEngine.System.OxySystem.isSupportedModelFileExtension;
import static OxyEngineEditor.UI.Gizmo.OxySelectHandler.*;

public class ScenePanel extends Panel {

    public static boolean focusedWindowDragging, focusedWindow, hoveredWindow;

    public static final ImVec2 windowSize = new ImVec2();
    public static final ImVec2 windowPos = new ImVec2();
    public static final ImVec2 mousePos = new ImVec2();
    public static final ImVec2 offsetScreenPos = new ImVec2();
    public static final ImVec2 offset = new ImVec2(); //Window position relative to the window... means that it subtracts the tab

    private static final ImVec2 availContentRegionSize = new ImVec2();

    private static ScenePanel INSTANCE = null;

    public static ScenePanel getInstance() {
        if (INSTANCE == null) INSTANCE = new ScenePanel();
        return INSTANCE;
    }

    private ScenePanel() {
    }

    @Override
    public void preload() {
    }

    public static OxyNativeObject editorCameraEntity;

    @Override
    public void renderPanel() {

        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 0, 0);

        ImGui.begin("Viewport");

        ImGui.getWindowSize(windowSize);

        ImGui.getWindowPos(windowPos);
        ImGui.getMousePos(mousePos);
        ImGui.getCursorPos(offset);
        ImGui.getCursorScreenPos(offsetScreenPos);

        if (editorCameraEntity == null) { //editor camera init
            editorCameraEntity = ACTIVE_SCENE.createNativeObjectEntity(null, null);
            EditorCamera editorCamera = new EditorCamera(true, 45f, ScenePanel.windowSize.x / ScenePanel.windowSize.y, 1f, 10000f, true);
            editorCameraEntity.addComponent(new TransformComponent(new Vector3f(0), new Vector3f(-0.35f, -0.77f, 0.0f)), editorCamera, new TagComponent("Editor Camera"));
            currentBoundedCamera = editorCamera;

            currentBoundedCamera.finalizeCamera(SceneRuntime.TS);
            if (currentBoundedCamera instanceof PerspectiveCamera c) c.setViewMatrixNoTranslation();
        }

        focusedWindowDragging = ImGui.isWindowFocused() && ImGui.isMouseDragging(2);
        focusedWindow = ImGui.isWindowFocused();
        hoveredWindow = ImGui.isWindowHovered();

        ImGui.getContentRegionAvail(availContentRegionSize);

        OpenGLFrameBuffer blittedFrameBuffer = SceneRenderer.getInstance().getMainFrameBuffer().getBlittedFrameBuffer();

        if (blittedFrameBuffer != null) {

            if (blittedFrameBuffer.getColorAttachmentTexture(0) != null) {
                ImGui.image(blittedFrameBuffer.getColorAttachmentTexture(0)[0], blittedFrameBuffer.getWidth(), blittedFrameBuffer.getHeight(), 0, 1, 1, 0);
                if (ImGui.beginDragDropTarget()) {
                    File f = ImGui.acceptDragDropPayload("projectPanelFile");
                    if (f != null) {
                        String fPath = f.getPath();
                        String extension = getExtension(fPath);
                        if (isSupportedModelFileExtension(extension)) {
                            List<OxyModel> eList = ACTIVE_SCENE.createModelEntities(fPath);
                            for (OxyModel e : eList) {
                                e.addComponent(new SelectedComponent(false));
                                e.getGUINodes().add(ModelMeshOpenGL.guiNode);
                                if (!e.getGUINodes().contains(OxyMaterial.guiNode))
                                    e.getGUINodes().add(OxyMaterial.guiNode);
                            }
                            SceneRenderer.getInstance().updateModelEntities();
                        }
                        ProjectPanel.lastDragDropFile = null;
                    }
                    ImGui.endDragDropTarget();
                }

                var instance = SceneRenderer.getInstance();
                OpenGLFrameBuffer mainFrameBuffer = instance.getMainFrameBuffer();
                if (availContentRegionSize.x != mainFrameBuffer.getWidth() || availContentRegionSize.y != mainFrameBuffer.getHeight()) {
                    mainFrameBuffer.setNeedResize(true, (int) availContentRegionSize.x, (int) availContentRegionSize.y);
                    instance.getPickingFrameBuffer().setNeedResize(true, (int) availContentRegionSize.x, (int) availContentRegionSize.y);
                }
            }

            if (entityContext != null && currentBoundedCamera != null && currentGizmoOperation != -1) {
                ImGuizmo.setOrthographic(false);
                ImGuizmo.setDrawList();
                ImGuizmo.setRect(windowPos.x, windowPos.y, windowSize.x, windowSize.y);

                float[] modelMatrix = currentBoundedCamera.getModelMatrixAsFloatArray();
                float[] projectionMatrix = currentBoundedCamera.getProjectionMatrixAsFloatArray();

                float[] entityModelMatrix = new float[4 * 4];
                TransformComponent c = entityContext.get(TransformComponent.class);
                c.transform.get(entityModelMatrix);

                if (currentGizmoOperation == Operation.ROTATE) snapValue = 45f;
                else snapValue = 0.5f;

                Arrays.fill(snapValueBuffer, snapValue);

                if (useSnap)
                    ImGuizmo.manipulate(modelMatrix, projectionMatrix, entityModelMatrix, currentGizmoOperation, currentGizmoMode, snapValueBuffer);
                else
                    ImGuizmo.manipulate(modelMatrix, projectionMatrix, entityModelMatrix, currentGizmoOperation, currentGizmoMode);

                if (ImGuizmo.isUsing() && ImGuizmo.isOver()) {
                    Matrix4f entityModelMatrix4f = new Matrix4f().set(entityModelMatrix);
                    //Inverting the root transform.
                    if (entityContext.familyHasRoot())
                        entityModelMatrix4f.mulLocal(new Matrix4f(entityContext.getRoot().get(TransformComponent.class).transform).invert());

                    Quaternionf quat = new Quaternionf();
                    entityModelMatrix4f.getTranslation(c.position);
                    entityModelMatrix4f.getUnnormalizedRotation(quat);
                    entityModelMatrix4f.getScale(c.scale);
                    quat.getEulerAnglesXYZ(c.rotation);
                    entityContext.transformLocally();

                    addParentTransformToChildren(entityContext);
                }
            }
        }

        ImGui.popStyleVar();
        ImGui.end();
    }
}
