package OxyEngine.Core.Layers;

import OxyEngineEditor.UI.Gizmo.OxyGizmo3D;
import OxyEngineEditor.UI.Gizmo.OxySelectHandler;

import static OxyEngineEditor.Scene.SceneRuntime.ACTIVE_SCENE;

public class GizmoLayer extends Layer {

    private final OxyGizmo3D gizmo3D;

    private static GizmoLayer INSTANCE;

    public static GizmoLayer getInstance(){
        if(INSTANCE == null) return INSTANCE = new GizmoLayer();
        return INSTANCE;
    }

    private GizmoLayer() {
        gizmo3D = OxyGizmo3D.getInstance(ACTIVE_SCENE);
    }

    @Override
    public void build() {
        OxySelectHandler.init(gizmo3D);
    }

    @Override
    public void rebuild() {

    }

    @Override
    public void update(float ts) {
        OxySelectHandler.controlRenderableStates();
    }

    @Override
    public void render(float ts) {
        ACTIVE_SCENE.getFrameBuffer().bind();
        gizmo3D.renderAllGizmos(ts);
        ACTIVE_SCENE.getFrameBuffer().unbind();
    }
}
