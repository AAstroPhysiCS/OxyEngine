package OxyEngineEditor.UI.Selector;

import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Core.Renderer.Texture.OxyColor;
import OxyEngineEditor.Sandbox.OxyComponents.SelectedComponent;
import OxyEngineEditor.Sandbox.Scene.ModelFileType;
import OxyEngineEditor.Sandbox.Scene.OxyModel;
import OxyEngineEditor.Sandbox.Scene.Scene;

import static OxyEngineEditor.Sandbox.Sandbox3D.camera;

public class OxyGizmo3D {

    private final OxyModel xModel;
    private final OxyModel yModel;
    private final OxyModel zModel;

    private static OxyGizmo3D INSTANCE = null;

    public static OxyGizmo3D getInstance(Scene scene) {
        if (INSTANCE == null) INSTANCE = new OxyGizmo3D(scene);
        return INSTANCE;
    }

    private OxyGizmo3D(Scene scene) {

        xModel = scene.createModelEntity(ModelFileType.OBJ, "src/main/resources/models/arrow.obj", "src/main/resources/models/arrow.mtl");
        yModel = scene.createModelEntity(ModelFileType.OBJ, "src/main/resources/models/arrow.obj", "src/main/resources/models/arrow.mtl");
        zModel = scene.createModelEntity(ModelFileType.OBJ, "src/main/resources/models/arrow.obj", "src/main/resources/models/arrow.mtl");

        xModel.addComponent(camera, new SelectedComponent(false, true), new OxyColor(new float[]{1f, 0f, 0f, 0.8f}));
        yModel.addComponent(camera, new SelectedComponent(false, true), new OxyColor(new float[]{0f, 1f, 0f, 0.8f}));
        zModel.addComponent(camera, new SelectedComponent(false, true), new OxyColor(new float[]{0f, 0f, 1f, 0.8f}));

        xModel.updateData();
        yModel.updateData();
        zModel.updateData();

        xModel.addEventListener(new OxyGizmoController(scene, xModel, yModel, zModel));
        yModel.addEventListener(new OxyGizmoController(scene, xModel, yModel, zModel));
        zModel.addEventListener(new OxyGizmoController(scene, xModel, yModel, zModel));
    }

    public OxyModel getXModel() {
        return xModel;
    }

    public OxyModel getZModel() {
        return zModel;
    }

    public OxyModel getYModel() {
        return yModel;
    }
}
