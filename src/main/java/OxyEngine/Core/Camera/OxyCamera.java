package OxyEngine.Core.Camera;

import OxyEngine.Components.EntityComponent;
import OxyEngine.Core.Window.OxyEvent;
import OxyEngine.Scene.OxyModel;
import OxyEngineEditor.UI.Panels.GUINode;
import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static OxyEngine.Scene.SceneRuntime.entityContext;

public abstract class OxyCamera implements EntityComponent {

    protected final boolean transpose;
    protected boolean primary;
    protected Matrix4f viewMatrix, modelMatrix, projectionMatrix, viewMatrixNoTranslation;

    protected Vector3f rotationRef;
    protected Vector3f positionRef;
    public final Vector3f origin = new Vector3f();

    protected double oldMouseX, oldMouseY;
    protected final float mouseSpeed, horizontalSpeed, verticalSpeed;

    public OxyCamera(float mouseSpeed, float horizontalSpeed, float verticalSpeed, boolean transpose) {
        this.transpose = transpose;
        this.mouseSpeed = mouseSpeed;
        this.horizontalSpeed = horizontalSpeed;
        this.verticalSpeed = verticalSpeed;
    }

    public abstract void update();

    public abstract void onEvent(OxyEvent event);

    public boolean isTranspose() {
        return transpose;
    }

    public boolean isPrimary() {
        return primary;
    }

    public Matrix4f getViewMatrix() {
        return viewMatrix;
    }

    public float[] getModelMatrixAsFloatArray(){
        float[] buffer = new float[4 * 4];
        modelMatrix.get(buffer);
        return buffer;
    }

    public float[] getProjectionMatrixAsFloatArray(){
        float[] buffer = new float[4 * 4];
        projectionMatrix.get(buffer);
        return buffer;
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

    public void setPosition(Vector3f position){
        this.positionRef = position;
    }

    public void setRotation(Vector3f rotationRef) {
        this.rotationRef = rotationRef;
    }

    public Vector3f getRotation() {
        return rotationRef;
    }

    public Vector3f getPosition() {
        return positionRef;
    }

    public float getMouseSpeed() {
        return mouseSpeed;
    }
    private static final String[] selection = {"Perspective Camera", "Orthographic Camera"};

    private static String currentItem = selection[0];

    public void setPrimary(boolean primary){
        this.primary = primary;
    }

    public static final GUINode guiNode = () -> {
        if (ImGui.treeNodeEx("Camera", ImGuiTreeNodeFlags.DefaultOpen)) {

            ImGui.alignTextToFramePadding();
            ImGui.text("Projection:");
            ImGui.sameLine();
            if (ImGui.beginCombo("##hideLabelProjection", currentItem)) {
                for (String s : selection) {
                    boolean isSelected = (currentItem.equals(s));
                    if (ImGui.selectable(s, isSelected)) currentItem = s;
                    if (isSelected) ImGui.setItemDefaultFocus();
                }
                ImGui.endCombo();
            }

            if (entityContext.get(OxyCamera.class) instanceof PerspectiveCamera c
                    && entityContext instanceof OxyModel
                    && currentItem.equals(selection[0])) {
                ImGui.columns(2, "CameraColumns");
                ImGui.alignTextToFramePadding();
                ImGui.text("Vertical FOV:");
                ImGui.alignTextToFramePadding();
                ImGui.text("Near Clip:");
                ImGui.alignTextToFramePadding();
                ImGui.text("Far Clip:");

                ImGui.nextColumn();

                float[] verticalFovDragArr = new float[]{c.fovY};
                float[] nearClipDragArr = new float[]{c.zNear};
                float[] farClipDragArr = new float[]{c.zFar};

                ImGui.dragFloat("##hideLabelVerticalFovDrag", verticalFovDragArr, 1f, 1f, 180f);
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
            ImGui.spacing();
        }
    };
}
