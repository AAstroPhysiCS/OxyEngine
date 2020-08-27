package OxyEngineEditor.UI.Selector;

import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Core.Window.WindowHandle;
import OxyEngine.System.OxySystem;
import OxyEngineEditor.Components.RenderableComponent;
import OxyEngineEditor.Components.SelectedComponent;
import OxyEngineEditor.Components.TransformComponent;
import OxyEngineEditor.Scene.Model.OxyModel;
import OxyEngineEditor.Scene.OxyEntity;
import OxyEngineEditor.Scene.Scene;
import org.joml.Vector3f;

import java.util.List;

import static OxyEngineEditor.Components.PerspectiveCamera.zoom;

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

        private float oldZoom;

        public Component(String path, WindowHandle windowHandle, Scene scene, OxyShader shader, OxyGizmo3D gizmo3D) {
            models = scene.createModelEntities(OxySystem.FileSystem.getResourceByPath(path), shader);
            for (OxyModel m : models) {
                m.addComponent(new TransformComponent(3f), new SelectedComponent(false, true), new RenderableComponent(true, true));
                m.addEventListener(new OxyGizmoController(windowHandle, scene, gizmo3D));
            }
            oldZoom = zoom; //default
        }

        public void switchRenderableState(boolean value) {
            for (OxyModel model : models) {
                model.get(RenderableComponent.class).renderable = value;
                model.updateComponents();
            }
        }

        public void scaleIt(OxyEntity e) {
            if (zoom != oldZoom) {
                for (OxyModel gizmoT : models) {
                    scale(gizmoT, e);
                }
                oldZoom = zoom;
            }
        }

        private void scale(OxyModel model, OxyEntity e) {
            if (model.get(SelectedComponent.class).fixedValue && zoom >= 150) {
                model.get(TransformComponent.class).scale.set(zoom * 0.05f);
                update(model, e);
            }
        }

        abstract void update(OxyModel model, OxyEntity e);
    }

    static class Translation extends Component {

        Translation(WindowHandle windowHandle, Scene scene, OxyShader shader, OxyGizmo3D gizmo3D) {
            super("/models/native/oxygizmo.obj", windowHandle, scene, shader, gizmo3D);
        }

        @Override
        void update(OxyModel model, OxyEntity e) {
            TransformComponent xC = model.get(TransformComponent.class);
            xC.position.set(new Vector3f(model.originPos).mul(xC.scale).add(e.get(TransformComponent.class).position));
            model.updateData();
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
            super("/models/native/oxygizmoScale2.obj", windowHandle, scene, shader, gizmo3D);
        }

        @Override
        void update(OxyModel model, OxyEntity e) {
            TransformComponent xC = model.get(TransformComponent.class);
            xC.position.set(new Vector3f(model.originPos).mul(xC.scale).add(e.get(TransformComponent.class).position));
            model.updateData();
        }

        public OxyModel getXModelScale() {
            return models.get(2);
        }

        public OxyModel getYModelScale() {
            return models.get(1);
        }

        public OxyModel getZModelScale() {
            return models.get(0);
        }

        public OxyModel getScalingCube() {
            return models.get(3);
        }
    }

    GizmoMode mode;

    private OxyGizmo3D(WindowHandle windowHandle, Scene scene) {
        OxyShader shader = new OxyShader("shaders/gizmo.glsl");
        GizmoMode.Translation.init(windowHandle, scene, shader, this);
        GizmoMode.Scale.init(windowHandle, scene, shader, this);

        mode = GizmoMode.Translation; // default
    }

    public void scaleAll(OxyEntity e) {
        GizmoMode.Translation.component.scaleIt(e);
        GizmoMode.Scale.component.scaleIt(e);
    }
}