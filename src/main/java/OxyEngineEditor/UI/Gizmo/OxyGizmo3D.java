package OxyEngineEditor.UI.Gizmo;

import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngineEditor.Scene.OxyEntity;
import OxyEngineEditor.Scene.Scene;

import static org.lwjgl.opengl.GL11.*;

public class OxyGizmo3D {

    private static OxyGizmo3D INSTANCE = null;

    public static OxyGizmo3D getInstance(Scene scene) {
        if (INSTANCE == null) INSTANCE = new OxyGizmo3D(scene);
        return INSTANCE;
    }

    public static OxyGizmo3D getInstance() {
        return INSTANCE;
    }

    GizmoMode mode;

    private OxyGizmo3D(Scene scene) {
        OxyShader shader = new OxyShader("shaders/OxyGizmo.glsl");
        GizmoMode.Translation.init(scene, shader, this);
        GizmoMode.Scale.init(scene, shader, this);

        mode = GizmoMode.None; // default
    }

    public void scaleAll(OxyEntity e) {
        GizmoMode.Translation.gizmoComponent.scaleIt(e);
        GizmoMode.Scale.gizmoComponent.scaleIt(e);
    }

    public void renderAllGizmos(float ts) {
        glDisable(GL_DEPTH_TEST);
        GizmoMode.Translation.gizmoComponent.render(ts);
        GizmoMode.Scale.gizmoComponent.render(ts);
        glEnable(GL_DEPTH_TEST);
    }
}