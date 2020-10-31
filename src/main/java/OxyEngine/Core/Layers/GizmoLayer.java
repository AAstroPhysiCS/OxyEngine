package OxyEngine.Core.Layers;

import OxyEngineEditor.UI.Selector.OxyGizmo3D;
import OxyEngineEditor.UI.Selector.OxySelectHandler;

import static OxyEngineEditor.Scene.SceneRuntime.ACTIVE_SCENE;

public class GizmoLayer extends Layer {

    private final OxyGizmo3D gizmo3D;

    public GizmoLayer() {
        gizmo3D = OxyGizmo3D.getInstance(ACTIVE_SCENE);
    }

    @Override
    public void build() {
        OxySelectHandler.init(ACTIVE_SCENE, gizmo3D);
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
        ACTIVE_SCENE.getFrameBuffer().bind();
        gizmo3D.renderAllGizmos(ts, deltaTime);
        ACTIVE_SCENE.getFrameBuffer().unbind();
    }
}
