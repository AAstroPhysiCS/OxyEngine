package OxyEngine.Core.Layers;

import OxyEngine.Core.Window.WindowHandle;
import OxyEngineEditor.Scene.Scene;
import OxyEngineEditor.UI.Selector.OxyGizmo3D;
import OxyEngineEditor.UI.Selector.OxySelectSystem;

public class GizmoLayer extends Layer {

    private final OxyGizmo3D gizmo3D;
    private final OxySelectSystem selectSystem;

    public GizmoLayer(Scene scene, WindowHandle windowHandle) {
        super(scene);
        gizmo3D = OxyGizmo3D.getInstance(windowHandle, scene);
        selectSystem = OxySelectSystem.getInstance(scene, gizmo3D);
    }

    @Override
    public void build() {

    }

    @Override
    public void rebuild() {

    }

    @Override
    public void update(float ts, float deltaTime) {
        selectSystem.controlRenderableStates();
    }

    @Override
    public void render(float ts, float deltaTime) {
        scene.getFrameBuffer().bind();
        gizmo3D.renderAllGizmos(ts, deltaTime);
        scene.getFrameBuffer().unbind();
    }
}
