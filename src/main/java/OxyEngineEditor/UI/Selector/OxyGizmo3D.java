package OxyEngineEditor.UI.Selector;

import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Core.Window.WindowHandle;
import OxyEngine.System.OxySystem;
import OxyEngineEditor.Sandbox.Components.RenderableComponent;
import OxyEngineEditor.Sandbox.Components.SelectedComponent;
import OxyEngineEditor.Sandbox.Components.TransformComponent;
import OxyEngineEditor.Sandbox.Scene.Model.OxyModel;
import OxyEngineEditor.Sandbox.Scene.OxyEntity;
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
                m.addComponent(new TransformComponent(3f), new SelectedComponent(false, true), new RenderableComponent(true, true));
                m.addEventListener(new OxyGizmoController(windowHandle, scene, gizmo3D));
            }
        }

        public void switchRenderableState(boolean value) {
            for (OxyModel model : models) model.get(RenderableComponent.class).renderable = value;
        }


        protected static void scale(OxyModel model) {
            if (model.get(SelectedComponent.class).fixedValue && zoom >= 150)
                model.get(TransformComponent.class).scale.set(zoom * 0.05f);
        }

        abstract void scaleIt();

        abstract void update(OxyEntity e);
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
        void update(OxyEntity e) {
            for (OxyModel gizmoT : models) {
                TransformComponent xC = gizmoT.get(TransformComponent.class);
                xC.position.set(new Vector3f(gizmoT.originPos).mul(xC.scale).add(e.get(TransformComponent.class).position));
                gizmoT.updateData();
            }
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
        public void scaleIt() {
            for (OxyModel gizmoS : models) {
                scale(gizmoS);
            }
        }

        @Override
        void update(OxyEntity e) {
            for (OxyModel gizmoS : models) {
                TransformComponent xC = gizmoS.get(TransformComponent.class);
                xC.position.set(new Vector3f(gizmoS.originPos).mul(xC.scale).add(e.get(TransformComponent.class).position));
                gizmoS.updateData();
            }
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

    public void scaleAll() {
        GizmoMode.Translation.component.scaleIt();
        GizmoMode.Scale.component.scaleIt();
    }

    public void updateAll(OxyEntity e) {
        GizmoMode.Translation.component.update(e);
        GizmoMode.Scale.component.update(e);
    }
}