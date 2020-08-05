package OxyEngineEditor.UI.Selector;

import OxyEngine.Core.Renderer.Texture.OxyColor;
import OxyEngine.System.OxySystem;
import OxyEngineEditor.Sandbox.OxyComponents.SelectedComponent;
import OxyEngineEditor.Sandbox.OxyComponents.TransformComponent;
import OxyEngineEditor.Sandbox.Scene.Model.OxyModel;
import OxyEngineEditor.Sandbox.Scene.Scene;
import org.joml.Vector3f;

import java.util.List;

import static OxyEngineEditor.Sandbox.Sandbox3D.camera;

//TODO: REFACTOR IT
public class OxyGizmo3D {

    private final List<OxyModel> gizmo;

    private static OxyGizmo3D INSTANCE = null;

    public static OxyGizmo3D getInstance(Scene scene) {
        if (INSTANCE == null) INSTANCE = new OxyGizmo3D(scene);
        return INSTANCE;
    }

    private OxyGizmo3D(Scene scene) {

        gizmo = scene.createModelEntity(OxySystem.FileSystem.getResourceByPath("/models/oxygizmo.obj"));

        gizmo.get(0).addComponent(new TransformComponent(new Vector3f(0, 0, 0), new Vector3f((float) Math.toRadians(180), 0, 0), 0.5f), camera, new SelectedComponent(false, true), new OxyColor(new float[]{0f, 1f, 0f, 0.8f}));
        gizmo.get(1).addComponent(new TransformComponent(0.5f), camera, new SelectedComponent(false, true), new OxyColor(new float[]{1f, 0f, 0f, 0.8f}));
        gizmo.get(2).addComponent(new TransformComponent(0.5f), camera, new SelectedComponent(false, true), new OxyColor(new float[]{0f, 0f, 1f, 0.8f}));

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
