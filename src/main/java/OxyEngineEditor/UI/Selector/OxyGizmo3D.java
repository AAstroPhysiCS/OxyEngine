package OxyEngineEditor.UI.Selector;

import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Core.Window.WindowHandle;
import OxyEngine.System.OxySystem;
import OxyEngineEditor.Sandbox.Components.BoundingBoxComponent;
import OxyEngineEditor.Sandbox.Components.SelectedComponent;
import OxyEngineEditor.Sandbox.Components.TransformComponent;
import OxyEngineEditor.Sandbox.Scene.Model.OxyModel;
import OxyEngineEditor.Sandbox.Scene.Scene;
import org.joml.Vector3f;

import java.util.List;

import static OxyEngineEditor.Sandbox.Components.PerspectiveCamera.zoom;

public class OxyGizmo3D {

    private static OxyGizmo3D INSTANCE = null;

    public static OxyGizmo3D getInstance(WindowHandle windowHandle, Scene scene) {
        if (INSTANCE == null) INSTANCE = new OxyGizmo3D(windowHandle, scene);
        return INSTANCE;
    }

    public static OxyGizmo3D getInstance() {
        return INSTANCE;
    }

    enum GizmoMode {
        None(), Translation(), Rotation(), Scale();

        Component component;

        void init(WindowHandle windowHandle, Scene scene, OxyShader shader, OxyGizmo3D gizmo3D) {
            switch (this) {
                case Translation -> component = new Translation(windowHandle, scene, shader, gizmo3D);
                case Scale -> component = new Scaling(windowHandle, scene, shader, gizmo3D);
            }
        }
    }

    abstract static class Component {

        final List<OxyModel> models;

        public Component(String path, WindowHandle windowHandle, Scene scene, OxyShader shader, OxyGizmo3D gizmo3D) {
            models = scene.createModelEntities(OxySystem.FileSystem.getResourceByPath(path), shader);
            for (OxyModel m : models) {
                m.addComponent(new TransformComponent(new Vector3f(0, 0, 0), 3f), new SelectedComponent(false, true));
                m.addEventListener(new OxyGizmoController(windowHandle, scene, gizmo3D));
            }
            recalculate();
        }

        abstract void scaleIt();

        abstract void recalculate();
    }

    static class Translation extends Component {

        Translation(WindowHandle windowHandle, Scene scene, OxyShader shader, OxyGizmo3D gizmo3D) {
            super("/models/native/oxygizmo.obj", windowHandle, scene, shader, gizmo3D);
        }

        @Override
        public void scaleIt() {
            for (OxyModel gizmoT : models) {
                scale(gizmoT);
            }
        }

        @Override
        public void recalculate() {
            recalculateBoundingBox(getXModelTranslation(), getYModelTranslation(), getZModelTranslation());
        }

        public OxyModel getXModelTranslation() {
            return models.get(2);
        }

        public OxyModel getYModelTranslation() {
            return models.get(0);
        }

        public OxyModel getZModelTranslation() {
            return models.get(1);
        }
    }

    static class Scaling extends Component {

        Scaling(WindowHandle windowHandle, Scene scene, OxyShader shader, OxyGizmo3D gizmo3D) {
            super("/models/native/oxygizmoScale.obj", windowHandle, scene, shader, gizmo3D);
        }

        @Override
        public void scaleIt() {
            for (OxyModel gizmoS : models) {
                scale(gizmoS);
            }
        }

        @Override
        public void recalculate() {
            recalculateBoundingBox(getXModelScale(), getYModelScale(), getZModelScale());
        }

        public OxyModel getXModelScale() {
            return models.get(1);
        }

        public OxyModel getYModelScale() {
            return models.get(2);
        }

        public OxyModel getZModelScale() {
            return models.get(0);
        }
    }

    GizmoMode mode;

    private OxyGizmo3D(WindowHandle windowHandle, Scene scene) {
        OxyShader shader = new OxyShader("shaders/gizmo.glsl");
        GizmoMode.Translation.init(windowHandle, scene, shader, this);
        GizmoMode.Scale.init(windowHandle, scene, shader, this);

        mode = GizmoMode.Translation; // default
    }

    public void scaleAll() {
        GizmoMode.Translation.component.scaleIt();
        GizmoMode.Scale.component.scaleIt();
    }

    public void recalculateAll() {
        GizmoMode.Translation.component.recalculate();
        GizmoMode.Scale.component.recalculate();
    }

    public static void recalculateBoundingBox(OxyModel xModel, OxyModel yModel, OxyModel zModel) {

        TransformComponent xC = xModel.get(TransformComponent.class);
        TransformComponent yC = yModel.get(TransformComponent.class);
        TransformComponent zC = zModel.get(TransformComponent.class);

        BoundingBoxComponent xCB = xModel.get(BoundingBoxComponent.class);
        BoundingBoxComponent yCB = yModel.get(BoundingBoxComponent.class);
        BoundingBoxComponent zCB = zModel.get(BoundingBoxComponent.class);

        xCB.pos().set(new Vector3f(xC.position).add(new Vector3f(xCB.originPos()).mul(xC.scale)));
        yCB.pos().set(new Vector3f(yC.position).add(new Vector3f(yCB.originPos()).mul(yC.scale)));
        zCB.pos().set(new Vector3f(zC.position).add(new Vector3f(zCB.originPos()).mul(zC.scale)));

        xModel.updateData();
        yModel.updateData();
        zModel.updateData();
    }

    private static void scale(OxyModel model) {
        if (model.get(SelectedComponent.class).fixedValue && zoom >= 150)
            model.get(TransformComponent.class).scale.set(zoom * 0.05f);
    }
}