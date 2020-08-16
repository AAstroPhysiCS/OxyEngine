package OxyEngineEditor.UI.Selector;

import OxyEngine.Core.Renderer.Shader.OxyShader;
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
//    private final List<OxyModel> gizmoRotate;

    private static OxyGizmo3D INSTANCE = null;

    public static OxyGizmo3D getInstance(Scene scene) {
        if (INSTANCE == null) INSTANCE = new OxyGizmo3D(scene);
        return INSTANCE;
    }

    public static OxyGizmo3D getInstance() {
        return INSTANCE;
    }

    private OxyGizmo3D(Scene scene) {

        gizmoTranslate = scene.createModelEntities(OxySystem.FileSystem.getResourceByPath("/models/intern/oxygizmo.obj"), new OxyShader("shaders/gizmo.glsl"));

        gizmoTranslate.get(0).addComponent(new TransformComponent(new Vector3f(0, 0, 0), 3f), new SelectedComponent(false, true));
        gizmoTranslate.get(1).addComponent(new TransformComponent(new Vector3f(0, 0, 0), 3f), new SelectedComponent(false, true));
        gizmoTranslate.get(2).addComponent(new TransformComponent(new Vector3f(0, 0, 0), 3f), new SelectedComponent(false, true));

        /*gizmoRotate = scene.createModelEntities(OxySystem.FileSystem.getResourceByPath("/models/intern/oxygizmoRotation.obj"), shader);
        gizmoRotate.get(0).addComponent(new TransformComponent(new Vector3f(0, -30, 0), 3f), new SelectedComponent(false));
        gizmoRotate.get(1).addComponent(new TransformComponent(new Vector3f(0, -30, 0), 3f), new SelectedComponent(false));
        gizmoRotate.get(2).addComponent(new TransformComponent(new Vector3f(0, -30, 0), 3f), new SelectedComponent(false));*/

        gizmoTranslate.get(0).updateData();
        gizmoTranslate.get(1).updateData();
        gizmoTranslate.get(2).updateData();

        recalculateBoundingBox();

        /*gizmoRotate.get(0).updateData();
        gizmoRotate.get(1).updateData();
        gizmoRotate.get(2).updateData();*/

        gizmoTranslate.get(0).addEventListener(new OxyGizmoController(scene, this));
        gizmoTranslate.get(1).addEventListener(new OxyGizmoController(scene, this));
        gizmoTranslate.get(2).addEventListener(new OxyGizmoController(scene, this));
    }

    public void scaleIt() {
        scale(gizmoTranslate.get(0));
        scale(gizmoTranslate.get(1));
        scale(gizmoTranslate.get(2));
    }

    public void recalculateBoundingBox() {

        OxyModel xModel = getXModel();
        OxyModel yModel = getYModel();
        OxyModel zModel = getZModel();

        TransformComponent xC = xModel.get(TransformComponent.class);
        TransformComponent yC = yModel.get(TransformComponent.class);
        TransformComponent zC = zModel.get(TransformComponent.class);

        BoundingBoxComponent xCB = xModel.get(BoundingBoxComponent.class);
        BoundingBoxComponent yCB = yModel.get(BoundingBoxComponent.class);
        BoundingBoxComponent zCB = zModel.get(BoundingBoxComponent.class);

        xCB.pos().set(new Vector3f(xC.position).add(new Vector3f(xCB.originPos()).mul(xC.scale, 1, 1)));
        yCB.pos().set(new Vector3f(yC.position).add(new Vector3f(yCB.originPos()).mul(1, yC.scale, 1)));
        zCB.pos().set(new Vector3f(zC.position).add(new Vector3f(zCB.originPos()).mul(1, 1, zC.scale)));

        gizmoTranslate.get(0).updateData();
        gizmoTranslate.get(1).updateData();
        gizmoTranslate.get(2).updateData();
    }

    private void scale(OxyModel model) {
        if (model.get(SelectedComponent.class).fixedValue && zoom >= 60)
            model.get(TransformComponent.class).scale = zoom * 0.05f;
    }

    public OxyModel getXModel() {
        return gizmoTranslate.get(2);
    }

    public OxyModel getYModel() {
        return gizmoTranslate.get(0);
    }

    public OxyModel getZModel() {
        return gizmoTranslate.get(1);
    }
}
