package OxyEngineEditor.UI.Selector;

import OxyEngine.Core.Renderer.OxyRenderer;
import OxyEngine.Core.Renderer.Texture.OxyColor;
import OxyEngine.Core.Window.WindowHandle;
import OxyEngine.Events.OxyMouseListener;
import OxyEngineEditor.Components.TransformComponent;
import OxyEngineEditor.Scene.Model.OxyModel;
import OxyEngineEditor.Scene.OxyEntity;
import OxyEngineEditor.Scene.Scene;
import OxyEngineEditor.UI.OxyUISystem;
import imgui.flag.ImGuiMouseButton;
import org.joml.Vector2d;

public class OxyGizmoController implements OxyMouseListener {

    /*
     * It is a pretty mess... so TODO: REFACTOR SOMETIME IN THE FUTURE
     */

    OxyEntity hoveredGameObject = null;
    OxyColor standardColor = null;
    boolean init = false;

    static Vector2d oldMousePos = new Vector2d();

    static OxyGizmo3D gizmo;
    static Scene scene;
    static WindowHandle windowHandle;

    static boolean pressedXTranslation, pressedYTranslation, pressedZTranslation;
    static boolean pressedXScale, pressedYScale, pressedZScale, pressedScaleFactor;

    public static OxyEntity currentEntitySelected;

    OxyGizmoController(WindowHandle windowHandle, Scene scene, OxyGizmo3D gizmo) {
        OxyGizmoController.gizmo = gizmo;
        OxyGizmoController.scene = scene;
        OxyGizmoController.windowHandle = windowHandle;
    }

    public static void setCurrentEntitySelected(OxyEntity currentEntitySelected) {
        OxyGizmoController.currentEntitySelected = currentEntitySelected;
    }

    @Override
    public void mouseClicked(OxyEntity selectedEntity, int mouseButton) {
        if (mouseButton == ImGuiMouseButton.Right) {
            switch (gizmo.mode) {
                case Translation -> handleTranslationSwitch(selectedEntity);
                case Scale -> handleScalingSwitch(selectedEntity);
            }
        }
    }

    @Override
    public void mouseDown(OxyEntity selectedEntity, int mouseButton) {
        if (mouseButton == ImGuiMouseButton.Right && currentEntitySelected != null) {
            switch (gizmo.mode) {
                case Translation -> handleTranslation();
                case Scale -> handleScaling();
            }
        }
    }

    @Override
    public void mouseHovered(OxyEntity hoveredEntity) {
        if (hoveredEntity == null && hoveredGameObject != null) {
            OxyColor hoveredColor = hoveredGameObject.get(OxyColor.class);
            hoveredColor.setColorRGBA(standardColor.getNumbers());
            hoveredGameObject.updateData();
        }
        if (hoveredEntity instanceof OxyModel) {
            if (!init) {
                OxyColor color = hoveredEntity.get(OxyColor.class);
                standardColor = (OxyColor) color.clone();
                init = true;
            }
            hoveredGameObject = hoveredEntity;
            OxyColor hoveredColor = hoveredGameObject.get(OxyColor.class);
            hoveredColor.setColorRGBA(new float[]{1.0f, 1.0f, 0.0f, 1.0f});
            hoveredGameObject.updateData();
        }
    }

    @Override
    public void mouseDragged(OxyEntity selectedEntity, int mouseButton) {
        oldMousePos = new Vector2d(OxyUISystem.OxyEventSystem.mouseCursorPosDispatcher.getXPos(), OxyUISystem.OxyEventSystem.mouseCursorPosDispatcher.getYPos());
    }

    @Override
    public void mouseReleased(OxyEntity selectedEntity, int mouseButton) {
    }

    private void handleTranslationSwitch(OxyEntity selectedEntity) {
        OxyGizmo3D.Translation t = (OxyGizmo3D.Translation) gizmo.mode.component;
        if (selectedEntity == t.getXModelTranslation()) {
            pressedXTranslation = true;
            pressedYTranslation = false;
            pressedZTranslation = false;
            pressedXScale = false;
            pressedYScale = false;
            pressedZScale = false;
            pressedScaleFactor = false;
        }
        if (selectedEntity == t.getYModelTranslation()) {
            pressedYTranslation = true;
            pressedXTranslation = false;
            pressedZTranslation = false;
            pressedXScale = false;
            pressedYScale = false;
            pressedZScale = false;
            pressedScaleFactor = false;
        }
        if (selectedEntity == t.getZModelTranslation()) {
            pressedZTranslation = true;
            pressedXTranslation = false;
            pressedYTranslation = false;
            pressedXScale = false;
            pressedYScale = false;
            pressedZScale = false;
            pressedScaleFactor = false;
        }
    }

    private void handleTranslation() {
        OxyGizmo3D.Translation t = (OxyGizmo3D.Translation) gizmo.mode.component;
        OxyModel xAxis = t.getXModelTranslation();
        OxyModel yAxis = t.getYModelTranslation();
        OxyModel zAxis = t.getZModelTranslation();

        TransformComponent xC = xAxis.get(TransformComponent.class);
        TransformComponent yC = yAxis.get(TransformComponent.class);
        TransformComponent zC = zAxis.get(TransformComponent.class);
        TransformComponent currC = currentEntitySelected.get(TransformComponent.class);

        Vector2d nowMousePos = new Vector2d(OxyUISystem.OxyEventSystem.mouseCursorPosDispatcher.getXPos(), OxyUISystem.OxyEventSystem.mouseCursorPosDispatcher.getYPos());
        Vector2d delta = nowMousePos.sub(oldMousePos);

        float mouseSpeed = OxyRenderer.currentBoundedCamera.getCameraController().getMouseSpeed();
        float deltaX = (float) ((delta.x * mouseSpeed) * xC.scale.x) / 8f;
        float deltaY = (float) ((delta.y * mouseSpeed) * yC.scale.y) / 8f;
        if (deltaX <= -1f * xC.scale.x / 8f || deltaX >= 1f * xC.scale.x / 8f) deltaX = 0; // for safety reasons
        if (deltaY <= -1f * yC.scale.y / 8f || deltaY >= 1f * yC.scale.y / 8f) deltaY = 0;
        if (pressedZTranslation) {
            xC.position.add(0, 0, -deltaX);
            yC.position.add(0, 0, -deltaX);
            zC.position.add(0, 0, -deltaX);
            currC.position.add(0, 0, -deltaX);
            for (int i = 0; i < 4; i++)
                OxyGizmo3D.GizmoMode.Scale.component.models.get(i).get(TransformComponent.class).position.add(0, 0, -deltaX);
        } else if (pressedYTranslation) {
            xC.position.add(0, deltaY, 0);
            yC.position.add(0, deltaY, 0);
            zC.position.add(0, deltaY, 0);
            currC.position.add(0, deltaY, 0);
            for (int i = 0; i < 4; i++)
                OxyGizmo3D.GizmoMode.Scale.component.models.get(i).get(TransformComponent.class).position.add(0, deltaY, 0);
        } else if (pressedXTranslation) {
            xC.position.add(deltaX, 0, 0);
            yC.position.add(deltaX, 0, 0);
            zC.position.add(deltaX, 0, 0);
            currC.position.add(deltaX, 0, 0);
            for (int i = 0; i < 4; i++)
                OxyGizmo3D.GizmoMode.Scale.component.models.get(i).get(TransformComponent.class).position.add(deltaX, 0, 0);
        }
        currentEntitySelected.updateData();
        xAxis.updateData();
        yAxis.updateData();
        zAxis.updateData();
        for (int i = 0; i < 4; i++)
            OxyGizmo3D.GizmoMode.Scale.component.models.get(i).updateData();
    }

    private void handleScaling() {
        OxyGizmo3D.Scaling s = (OxyGizmo3D.Scaling) gizmo.mode.component;

        OxyModel xAxis = s.getXModelScale();
        OxyModel yAxis = s.getYModelScale();

        TransformComponent xC = xAxis.get(TransformComponent.class);
        TransformComponent yC = yAxis.get(TransformComponent.class);

        TransformComponent currC = currentEntitySelected.get(TransformComponent.class);

        Vector2d nowMousePos = new Vector2d(OxyUISystem.OxyEventSystem.mouseCursorPosDispatcher.getXPos(), OxyUISystem.OxyEventSystem.mouseCursorPosDispatcher.getYPos());
        Vector2d delta = nowMousePos.sub(oldMousePos);

        float mouseSpeed = OxyRenderer.currentBoundedCamera.getCameraController().getMouseSpeed();
        float deltaX = (float) ((delta.x * mouseSpeed) * xC.scale.x) / 8f;
        float deltaY = (float) ((delta.y * mouseSpeed) * yC.scale.y) / 8f;
        if (deltaX <= -1f * xC.scale.x / 8f || deltaX >= 1f * xC.scale.x / 8f) deltaX = 0; // for safety reasons
        if (deltaY <= -1f * yC.scale.y / 8f || deltaY >= 1f * yC.scale.y / 8f) deltaY = 0;

        if (pressedZScale) {
            currC.scale.x += -deltaX;
            if (currC.scale.x <= 0) currC.scale.x = 0;
        } else if (pressedYScale) {
            currC.scale.y += -deltaY;
            if (currC.scale.y <= 0) currC.scale.y = 0;
        } else if (pressedXScale) {
            currC.scale.z += deltaX;
            if (currC.scale.z <= 0) currC.scale.z = 0;
        } else if(pressedScaleFactor){
            currC.scale.add(deltaX, deltaX, deltaX);
            if (currC.scale.x <= 0) currC.scale.x = 0;
            if (currC.scale.y <= 0) currC.scale.y = 0;
            if (currC.scale.z <= 0) currC.scale.z = 0;
        }

        currentEntitySelected.updateData();
    }

    private void handleScalingSwitch(OxyEntity selectedEntity) {
        OxyGizmo3D.Scaling s = (OxyGizmo3D.Scaling) gizmo.mode.component;
        if (selectedEntity == s.getXModelScale()) {
            pressedXScale = true;
            pressedYScale = false;
            pressedZScale = false;
            pressedScaleFactor = false;
            pressedXTranslation = false;
            pressedYTranslation = false;
            pressedZTranslation = false;
        }
        if (selectedEntity == s.getYModelScale()) {
            pressedYScale = true;
            pressedXScale = false;
            pressedZScale = false;
            pressedScaleFactor = false;
            pressedXTranslation = false;
            pressedYTranslation = false;
            pressedZTranslation = false;
        }
        if (selectedEntity == s.getZModelScale()) {
            pressedZScale = true;
            pressedXScale = false;
            pressedYScale = false;
            pressedScaleFactor = false;
            pressedXTranslation = false;
            pressedYTranslation = false;
            pressedZTranslation = false;
        }
        if(selectedEntity == s.getScalingCube()){
            pressedScaleFactor = true;
            pressedZScale = false;
            pressedXScale = false;
            pressedYScale = false;
            pressedXTranslation = false;
            pressedYTranslation = false;
            pressedZTranslation = false;
        }
    }
}