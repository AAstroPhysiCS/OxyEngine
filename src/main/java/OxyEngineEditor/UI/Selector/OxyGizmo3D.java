package OxyEngineEditor.UI.Selector;

import OxyEngine.Core.Camera.OxyCamera;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Core.Renderer.Texture.OxyColor;
import OxyEngineEditor.Sandbox.OxyComponents.TransformComponent;
import OxyEngineEditor.Sandbox.OxyObjects.ModelSpec;
import OxyEngineEditor.Sandbox.OxyObjects.ModelTemplate;
import OxyEngineEditor.Sandbox.OxyObjects.OxyEntity;
import OxyEngineEditor.Sandbox.OxyObjects.OxyModelLoader;
import OxyEngineEditor.Sandbox.Scene.Scene;
import org.joml.Vector3f;

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

        TransformComponent xC = (TransformComponent) xModel.get(TransformComponent.class);
        TransformComponent yC = (TransformComponent) xModel.get(TransformComponent.class);
        TransformComponent zC = (TransformComponent) xModel.get(TransformComponent.class);

        xC.rotation.set(180, 0, 0);
        yC.rotation.set(-90, -180, 0);
        zC.rotation.set(0, -90, 0);

        xModel.initData(((ModelTemplate)xModel.getTemplate()).getMesh());
        yModel.initData(((ModelTemplate)yModel.getTemplate()).getMesh());
        zModel.initData(((ModelTemplate)zModel.getTemplate()).getMesh());

        xModel.addEventListener(new OxyGizmoController(scene, xModel, yModel, zModel));
        yModel.addEventListener(new OxyGizmoController(scene, xModel, yModel, zModel));
        zModel.addEventListener(new OxyGizmoController(scene, xModel, yModel, zModel));
    }

    public void render(OxyCamera camera) {
        glEnable(GL_CULL_FACE);
        OxyColor cX = (OxyColor) xModel.get(OxyColor.class);
        cX.init();
        scene.render(((ModelTemplate)xModel.getTemplate()).getMesh(), camera);
        OxyColor cY = (OxyColor) yModel.get(OxyColor.class);
        cY.init();
        scene.render(((ModelTemplate)yModel.getTemplate()).getMesh(), camera);
        OxyColor cZ = (OxyColor) zModel.get(OxyColor.class);
        cZ.init();
        scene.render(((ModelTemplate)zModel.getTemplate()).getMesh(), camera);
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
