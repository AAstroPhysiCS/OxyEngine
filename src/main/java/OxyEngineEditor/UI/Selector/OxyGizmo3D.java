package OxyEngineEditor.UI.Selector;

import OxyEngine.Core.Camera.OxyCamera;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Core.Renderer.Texture.OxyColor;
import OxyEngineEditor.Sandbox.OxyObjects.ModelSpec;
import OxyEngineEditor.Sandbox.OxyObjects.ModelTemplate;
import OxyEngineEditor.Sandbox.OxyObjects.OxyEntity;
import OxyEngineEditor.Sandbox.OxyObjects.OxyModelLoader;
import OxyEngineEditor.Sandbox.Scene.Scene;

import static org.lwjgl.opengl.GL11.*;

public class OxyGizmo3D {

    private final OxyEntity xModel;
    private final OxyEntity yModel;
    private final OxyEntity zModel;

    private static OxyGizmo3D INSTANCE = null;

    private final Scene scene;

    public static OxyGizmo3D getInstance(Scene scene) {
        if (INSTANCE == null) INSTANCE = new OxyGizmo3D(scene, scene.getRenderer().getShader());
        return INSTANCE;
    }

    private OxyGizmo3D(Scene scene, OxyShader oxyShader) {
        this.scene = scene;
        xModel = OxyModelLoader.load(scene, new ModelSpec("src/main/resources/models/arrow.obj", new OxyColor(1f, 0f, 0f, 0.8f, oxyShader)));
        yModel = OxyModelLoader.load(scene, new ModelSpec("src/main/resources/models/arrow.obj", new OxyColor(0f, 1f, 0f, 0.8f, oxyShader)));
        zModel = OxyModelLoader.load(scene, new ModelSpec("src/main/resources/models/arrow.obj", new OxyColor(0f, 0f, 1f, 0.8f, oxyShader)));

        xModel.addEventListener(new OxyGizmoController(scene, xModel, yModel, zModel));
        yModel.addEventListener(new OxyGizmoController(scene, xModel, yModel, zModel));
        zModel.addEventListener(new OxyGizmoController(scene, xModel, yModel, zModel));
    }

    public void render(OxyCamera camera) {
        glEnable(GL_CULL_FACE);
        scene.render(((ModelTemplate)xModel.getTemplate()).getMesh());
        scene.render(((ModelTemplate)yModel.getTemplate()).getMesh());
        scene.render(((ModelTemplate)zModel.getTemplate()).getMesh());
        glDisable(GL_CULL_FACE);
    }

    public OxyEntity getXModel() {
        return xModel;
    }

    public OxyEntity getZModel() {
        return zModel;
    }

    public OxyEntity getYModel() {
        return yModel;
    }
}
