package OxyEngine.Core.Layers;

import OxyEngineEditor.Scene.Scene;
import OxyEngineEditor.UI.Selector.OxyGizmo3D;
import OxyEngineEditor.UI.Selector.OxySelectHandler;

public class GizmoLayer extends Layer {

    private final OxyGizmo3D gizmo3D;

    public GizmoLayer(Scene scene) {
        super(scene);
        gizmo3D = OxyGizmo3D.getInstance(scene);
    }

    @Override
    public void build() {
        OxySelectHandler.init(scene, gizmo3D);
    }

    @Override
    public void rebuild() {

    }

    @Override
    public void update(float ts, float deltaTime) {
        OxySelectHandler.controlRenderableStates();
    }

    @Override
    public void render(float ts, float deltaTime) {
        scene.getFrameBuffer().bind();
        gizmo3D.renderAllGizmos(ts, deltaTime);
        scene.getFrameBuffer().unbind();
    }
}
