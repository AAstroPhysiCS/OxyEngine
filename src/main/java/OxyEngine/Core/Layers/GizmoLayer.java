package OxyEngine.Core.Layers;

public class GizmoLayer extends Layer {

    private static GizmoLayer INSTANCE;

    public static GizmoLayer getInstance(){
        if(INSTANCE == null) return INSTANCE = new GizmoLayer();
        return INSTANCE;
    }

    private GizmoLayer() {

    }

    @Override
    public void build() {

    }

    @Override
    public void rebuild() {

    }

    @Override
    public void update(float ts) {

    }

    @Override
    public void render(float ts) {
        /*ACTIVE_SCENE.getFrameBuffer().bind();
        gizmo3D.renderAllGizmos(ts);
        ACTIVE_SCENE.getFrameBuffer().unbind();*/
    }
}
