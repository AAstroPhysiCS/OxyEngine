package OxyEngineEditor.UI.Selector;

import OxyEngine.Core.Camera.OxyCamera;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Core.Renderer.Texture.OxyColor;
import OxyEngineEditor.Sandbox.OxyComponents.SelectedComponent;
import OxyEngineEditor.Sandbox.Scene.ModelImportType;
import OxyEngineEditor.Sandbox.Scene.OxyModel;
import OxyEngineEditor.Sandbox.Scene.Scene;

import static org.lwjgl.opengl.GL11.*;

public class OxyGizmo3D {

    private final OxyModel xModel;
    private final OxyModel yModel;
    private final OxyModel zModel;

    private static OxyGizmo3D INSTANCE = null;

    private final Scene scene;

    public static OxyGizmo3D getInstance(Scene scene) {
        if (INSTANCE == null) INSTANCE = new OxyGizmo3D(scene, scene.getRenderer().getShader());
        return INSTANCE;
    }

    private OxyGizmo3D(Scene scene, OxyShader oxyShader) {
        this.scene = scene;

        xModel = scene.createModelEntity(ModelImportType.obj, "src/main/resources/models/arrow.obj", "src/main/resources/models/arrow.mtl");
        yModel = scene.createModelEntity(ModelImportType.obj, "src/main/resources/models/arrow.obj", "src/main/resources/models/arrow.mtl");
        zModel = scene.createModelEntity(ModelImportType.obj, "src/main/resources/models/arrow.obj", "src/main/resources/models/arrow.mtl");

        xModel.addComponent(new SelectedComponent(false), new OxyColor(new float[]{1f, 0f, 0f, 0.8f}, oxyShader));
        yModel.addComponent(new SelectedComponent(false), new OxyColor(new float[]{0f, 1f, 0f, 0.8f}, oxyShader));
        zModel.addComponent(new SelectedComponent(false), new OxyColor(new float[]{0f, 0f, 1f, 0.8f}, oxyShader));

        xModel.addEventListener(new OxyGizmoController(scene, xModel, yModel, zModel));
        yModel.addEventListener(new OxyGizmoController(scene, xModel, yModel, zModel));
        zModel.addEventListener(new OxyGizmoController(scene, xModel, yModel, zModel));
    }

    public void render(OxyCamera camera) {
        glEnable(GL_CULL_FACE);
        scene.render(xModel.getMesh(), camera);
        scene.render(yModel.getMesh(), camera);
        scene.render(zModel.getMesh(), camera);
        glDisable(GL_CULL_FACE);
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
