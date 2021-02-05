package OxyEngine.Core.Camera;

import OxyEngine.Core.Camera.Controller.OxyCameraController;
import OxyEngine.Components.EntityComponent;
import OxyEngineEditor.Scene.Objects.Model.OxyModel;
import OxyEngineEditor.UI.Panels.GUINode;
import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;
import org.joml.Matrix4f;

import static OxyEngineEditor.UI.Gizmo.OxySelectHandler.entityContext;

public abstract class OxyCamera implements EntityComponent {

    protected final boolean transpose;
    protected Matrix4f viewMatrix, modelMatrix, projectionMatrix, viewMatrixNoTranslation;

    protected OxyCameraController cameraController;

    public OxyCamera(boolean transpose) {
        this.transpose = transpose;
    }

    public abstract Matrix4f setProjectionMatrix();

    public abstract Matrix4f setModelMatrix();

    public abstract void finalizeCamera(float ts);

    public boolean isTranspose() {
        return transpose;
    }

    public Matrix4f getViewMatrix() {
        return viewMatrix;
    }

    public Matrix4f getViewMatrixNoTranslation() {
        return viewMatrixNoTranslation;
    }

    public Matrix4f getProjectionMatrix() {
        return projectionMatrix;
    }

    public Matrix4f getModelMatrix() {
        return modelMatrix;
    }

    public OxyCameraController getCameraController() {
        return cameraController;
    }

    private static final String[] selection = {"Perspective Camera", "Orthographic Camera"};
    private static String currentItem = selection[0];

    private static float[] verticalFovDragArr;
    private static float[] nearClipDragArr;
    private static float[] farClipDragArr;

    public static final GUINode guiNode = () -> {
        if (ImGui.treeNodeEx("Camera", ImGuiTreeNodeFlags.DefaultOpen)) {

            if (ImGui.beginCombo("Projection", currentItem)) {
                for (String s : selection) {
                    boolean is_selected = (currentItem.equals(s));
                    if (ImGui.selectable(s, is_selected)) currentItem = s;
                    if (is_selected) ImGui.setItemDefaultFocus();
                }
                ImGui.endCombo();
            }

            if (entityContext.get(OxyCamera.class) instanceof PerspectiveCamera c
                    && entityContext instanceof OxyModel
                    && c.isPrimary()
                    && currentItem.equals(selection[0])) {
                ImGui.columns(2, "CameraColumns");
                ImGui.alignTextToFramePadding();
                ImGui.text("Vertical FOV");
                ImGui.alignTextToFramePadding();
                ImGui.text("Near Clip");
                ImGui.alignTextToFramePadding();
                ImGui.text("Far Clip");

                ImGui.nextColumn();

                verticalFovDragArr = new float[]{c.fovY};
                nearClipDragArr = new float[]{c.zNear};
                farClipDragArr = new float[]{c.zFar};

                ImGui.dragFloat("##hideLabelVerticalFovDrag", verticalFovDragArr);
                ImGui.dragFloat("##hideLabelNearClipDrag", nearClipDragArr);
                ImGui.dragFloat("##hideLabelFarClipDrag", farClipDragArr);

                c.fovY = verticalFovDragArr[0];
                c.zNear = nearClipDragArr[0];
                c.zFar = farClipDragArr[0];

                ImGui.nextColumn();
                ImGui.columns(1);
                ImGui.separator();
            } else {
                //Later
            }
            ImGui.treePop();
        }
    };
}
