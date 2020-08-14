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

    private final List<OxyModel> gizmo;

    private static OxyGizmo3D INSTANCE = null;

    public static OxyGizmo3D getInstance(Scene scene, OxyShader shader) {
        if (INSTANCE == null) INSTANCE = new OxyGizmo3D(scene, shader);
        return INSTANCE;
    }

    public static OxyGizmo3D getInstance() {
        return INSTANCE;
    }

    private OxyGizmo3D(Scene scene, OxyShader shader) {

        gizmo = scene.createModelEntities(OxySystem.FileSystem.getResourceByPath("/models/intern/oxygizmo.obj"), shader);

        gizmo.get(0).addComponent(new TransformComponent(new Vector3f(0, -30, 0), 10f), new SelectedComponent(false, true));
        gizmo.get(1).addComponent(new TransformComponent(new Vector3f(0, -30, 0), 10f), new SelectedComponent(false, true));
        gizmo.get(2).addComponent(new TransformComponent(new Vector3f(0, -30, 0), 10f), new SelectedComponent(false, true));

        gizmo.get(0).updateData();
        gizmo.get(1).updateData();
        gizmo.get(2).updateData();

        gizmo.get(0).addEventListener(new OxyGizmoController(scene, this));
        gizmo.get(1).addEventListener(new OxyGizmoController(scene, this));
        gizmo.get(2).addEventListener(new OxyGizmoController(scene, this));
    }

    public void scaleIt(){
        scale(gizmo.get(0));
        scale(gizmo.get(1));
        scale(gizmo.get(2));
    }

    public void recalculateBoundingBox(){

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
    }

    private void scale(OxyModel model){
        if(model.get(SelectedComponent.class).fixedValue) model.get(TransformComponent.class).scale = zoom * 0.03f;
    }

    public OxyModel getXModel() {
        return gizmo.get(1);
    }

    public OxyModel getYModel() {
        return gizmo.get(0);
    }

    public OxyModel getZModel() {
        return gizmo.get(2);
    }
}
