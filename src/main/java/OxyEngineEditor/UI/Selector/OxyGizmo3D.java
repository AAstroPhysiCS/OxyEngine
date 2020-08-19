package OxyEngineEditor.UI.Selector;

import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Core.Window.WindowHandle;
import OxyEngine.System.OxySystem;
import OxyEngineEditor.Sandbox.OxyComponents.BoundingBoxComponent;
import OxyEngineEditor.Sandbox.OxyComponents.SelectedComponent;
import OxyEngineEditor.Sandbox.OxyComponents.TransformComponent;
import OxyEngineEditor.Sandbox.Scene.Model.OxyModel;
import OxyEngineEditor.Sandbox.Scene.Scene;
import org.joml.Vector3f;

import java.util.List;

import static OxyEngineEditor.Sandbox.OxyComponents.PerspectiveCamera.zoom;

public class OxyGizmo3D {

    private final List<OxyModel> gizmoTranslate;
    private final List<OxyModel> gizmoRotate;

    private static OxyGizmo3D INSTANCE = null;

    public static OxyGizmo3D getInstance(WindowHandle windowHandle, Scene scene) {
        if (INSTANCE == null) INSTANCE = new OxyGizmo3D(windowHandle, scene);
        return INSTANCE;
    }

    public static OxyGizmo3D getInstance() {
        return INSTANCE;
    }

    private OxyGizmo3D(WindowHandle windowHandle, Scene scene) {
        OxyShader shader = new OxyShader("shaders/gizmo.glsl");
        gizmoTranslate = scene.createModelEntities(OxySystem.FileSystem.getResourceByPath("/models/intern/oxygizmo.obj"), shader);
        gizmoRotate = scene.createModelEntities(OxySystem.FileSystem.getResourceByPath("/models/intern/oxygizmoRotation.obj"), shader);

        for (OxyModel gizmoT : gizmoTranslate) {
            gizmoT.addComponent(new TransformComponent(new Vector3f(0, 0, 0), 3f), new SelectedComponent(false, true));
            gizmoT.addEventListener(new OxyGizmoController(windowHandle, scene, this));
        }
        recalculateBoundingBox();
        for (OxyModel gizmoT : gizmoTranslate) {
            gizmoT.updateData();
        }

        for (OxyModel gizmoR : gizmoRotate) {
            gizmoR.addComponent(new TransformComponent(new Vector3f(0, -20, 0), 3f), new SelectedComponent(false));
        }
        //recalculateForRotation
        for (OxyModel gizmoR : gizmoRotate) {
            gizmoR.updateData();
        }
    }

    public void scaleIt() {
        for (OxyModel gizmoT : gizmoTranslate) {
            scale(gizmoT);
        }
    }

    public void recalculateBoundingBox() {

        OxyModel xModel = getXModelTranslation();
        OxyModel yModel = getYModelTranslation();
        OxyModel zModel = getZModelTranslation();

        TransformComponent xC = xModel.get(TransformComponent.class);
        TransformComponent yC = yModel.get(TransformComponent.class);
        TransformComponent zC = zModel.get(TransformComponent.class);

        BoundingBoxComponent xCB = xModel.get(BoundingBoxComponent.class);
        BoundingBoxComponent yCB = yModel.get(BoundingBoxComponent.class);
        BoundingBoxComponent zCB = zModel.get(BoundingBoxComponent.class);

        xCB.pos().set(new Vector3f(xC.position).add(new Vector3f(xCB.originPos()).mul(xC.scale, 1, 1)));
        yCB.pos().set(new Vector3f(yC.position).add(new Vector3f(yCB.originPos()).mul(1, yC.scale, 1)));
        zCB.pos().set(new Vector3f(zC.position).add(new Vector3f(zCB.originPos()).mul(1, 1, zC.scale)));

        for (OxyModel gizmoT : gizmoTranslate) {
            gizmoT.updateData();
        }
    }

    private void scale(OxyModel model) {
        if (model.get(SelectedComponent.class).fixedValue && zoom >= 60)
            model.get(TransformComponent.class).scale = zoom * 0.05f;
    }

    public OxyModel getXModelTranslation() {
        return gizmoTranslate.get(2);
    }

    public OxyModel getYModelTranslation() {
        return gizmoTranslate.get(0);
    }

    public OxyModel getZModelTranslation() {
        return gizmoTranslate.get(1);
    }

    public OxyModel getRedGizmoRotation() {
        return gizmoRotate.get(0);
    }

    public OxyModel getBlueGizmoRotation() {
        return gizmoRotate.get(1);
    }

    public OxyModel getGreenGizmoRotation() {
        return gizmoRotate.get(2);
    }
}