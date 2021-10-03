package OxyEngineEditor.UI.Panels;

import OxyEngine.Components.TransformComponent;
import OxyEngine.Core.Renderer.Mesh.Platform.OpenGLFrameBuffer;
import OxyEngine.Core.Renderer.Renderer;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.extension.imguizmo.ImGuizmo;
import imgui.extension.imguizmo.flag.Operation;
import imgui.flag.ImGuiStyleVar;
import org.joml.Matrix4f;

import java.io.File;
import java.util.Arrays;

import static OxyEngine.Core.Scene.SceneRuntime.*;
import static OxyEngine.System.OxySystem.getExtension;
import static OxyEngine.System.OxySystem.isSupportedModelFileExtension;
import static OxyEngineEditor.UI.SelectHandler.*;

public final class ScenePanel extends Panel {

    public static boolean focusedWindowDragging, focusedWindow, hoveredWindow;

    public static final ImVec2 windowSize = new ImVec2();
    public static final ImVec2 windowPos = new ImVec2();
    public static final ImVec2 mousePos = new ImVec2();
    public static final ImVec2 offsetScreenPos = new ImVec2();
    public static final ImVec2 offset = new ImVec2(); //Window position relative to the window... means that it subtracts the tab

    public static final ImVec2 availContentRegionSize = new ImVec2();

    private static ScenePanel INSTANCE = null;

    public static ScenePanel getInstance() {
        if (INSTANCE == null) INSTANCE = new ScenePanel();
        return INSTANCE;
    }

    private ScenePanel() {
    }

    @Override
    public void renderPanel() {

        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 0, 0);

        ImGui.begin("Viewport");

        ImGui.getWindowSize(windowSize);

        ImGui.getWindowPos(windowPos);
        ImGui.getMousePos(mousePos);
        ImGui.getCursorPos(offset);
        ImGui.getCursorScreenPos(offsetScreenPos);
        ImGui.getContentRegionAvail(availContentRegionSize);

        focusedWindowDragging = ImGui.isWindowFocused() && ImGui.isMouseDragging(2);
        focusedWindow = ImGui.isWindowFocused();
        hoveredWindow = ImGui.isWindowHovered();

        if (Renderer.getMainFrameBuffer().getBlittedFrameBuffer() instanceof OpenGLFrameBuffer blittedFrameBuffer) {

            if (blittedFrameBuffer.getColorAttachmentTexture(0) != null) {
                ImGui.image(blittedFrameBuffer.getColorAttachmentTexture(0)[0], blittedFrameBuffer.getWidth(), blittedFrameBuffer.getHeight(), 0, 1, 1, 0);
                if (ImGui.beginDragDropTarget()) {
                    File f = ImGui.acceptDragDropPayload("projectPanelFile");
                    if (f != null) {
                        String fPath = f.getPath();
                        String extension = getExtension(fPath);
                        if (isSupportedModelFileExtension(extension)) {
                            sceneContext.createEntity(fPath);
                        }
                        ProjectPanel.lastDragDropFile = null;
                    }
                    ImGui.endDragDropTarget();
                }
            }

            if (entityContext != null && cameraContext != null && currentGizmoOperation != -1) {
                ImGuizmo.setOrthographic(false);
                ImGuizmo.setDrawList();
                ImGuizmo.setRect(windowPos.x, windowPos.y, windowSize.x, windowSize.y);

                float[] modelMatrix = cameraContext.getModelMatrixAsFloatArray();
                float[] projectionMatrix = cameraContext.getProjectionMatrixAsFloatArray();

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
                        entityModelMatrix4f.mulLocal(new Matrix4f(entityContext.getRoot().getTransform()).invert());

                    c.set(entityModelMatrix4f);
                    entityContext.updateTransform();
                }
            }
        }

        ImGui.popStyleVar();
        ImGui.end();
    }
}
