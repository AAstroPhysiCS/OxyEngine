package OxyEngineEditor.UI.Selector;

import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.System.OxySystem;
import OxyEngineEditor.Sandbox.OxyComponents.SelectedComponent;
import OxyEngineEditor.Sandbox.OxyComponents.TransformComponent;
import OxyEngineEditor.Sandbox.Scene.Model.OxyModel;
import OxyEngineEditor.Sandbox.Scene.Scene;
import org.joml.Vector3f;

import java.util.List;

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

        gizmo.get(0).addComponent(new TransformComponent(new Vector3f(0, -30, 0), 0.8f), new SelectedComponent(false, true));
        gizmo.get(1).addComponent(new TransformComponent(new Vector3f(0, -30, 0), 0.8f), new SelectedComponent(false, true));
        gizmo.get(2).addComponent(new TransformComponent(new Vector3f(0, -30, 0), 0.8f), new SelectedComponent(false, true));

        gizmo.get(0).updateData();
        gizmo.get(1).updateData();
        gizmo.get(2).updateData();

        gizmo.get(0).addEventListener(new OxyGizmoController(scene, gizmo.get(0), gizmo.get(1), gizmo.get(2)));
        gizmo.get(1).addEventListener(new OxyGizmoController(scene, gizmo.get(0), gizmo.get(1), gizmo.get(2)));
        gizmo.get(2).addEventListener(new OxyGizmoController(scene, gizmo.get(0), gizmo.get(1), gizmo.get(2)));
    }

    public OxyModel getXModel() {
        return gizmo.get(0);
    }

    public OxyModel getZModel() {
        return gizmo.get(1);
    }

    public OxyModel getYModel() {
        return gizmo.get(2);
    }
}
