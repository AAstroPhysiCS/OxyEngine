package OxyEngineEditor.UI.Selector;

import OxyEngine.Core.Camera.OxyCamera;
import OxyEngine.Core.OxyObjects.Model.OxyModel;
import OxyEngine.Core.OxyObjects.Model.OxyModelLoader;
import OxyEngine.Core.Renderer.OxyRenderer3D;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Core.Renderer.Texture.OxyColor;

import static org.lwjgl.opengl.GL11.*;

public class OxyGizmo3D {

    private final OxyModel xModel;
    private final OxyModel yModel;
    private final OxyModel zModel;

    private static OxyGizmo3D INSTANCE = null;

    public static OxyGizmo3D getInstance(OxyRenderer3D renderer) {
        if (INSTANCE == null) INSTANCE = new OxyGizmo3D(renderer, renderer.getShader());
        return INSTANCE;
    }

    private OxyGizmo3D(OxyRenderer3D oxyRenderer, OxyShader oxyShader) {
        xModel = OxyModelLoader.load(oxyRenderer, new OxyModel.ModelSpec("src/main/resources/models/arrow.obj", new OxyColor(1f, 0f, 0f, 0.8f, oxyShader)));
        yModel = OxyModelLoader.load(oxyRenderer, new OxyModel.ModelSpec("src/main/resources/models/arrow.obj", new OxyColor(0f, 1f, 0f, 0.8f, oxyShader)));
        zModel = OxyModelLoader.load(oxyRenderer, new OxyModel.ModelSpec("src/main/resources/models/arrow.obj", new OxyColor(0f, 0f, 1f, 0.8f, oxyShader)));

        xModel.addEventListener(new OxyGizmoController(xModel, yModel, zModel));
        yModel.addEventListener(new OxyGizmoController(xModel, yModel, zModel));
        zModel.addEventListener(new OxyGizmoController(xModel, yModel, zModel));
    }

    public void render(OxyCamera camera) {
        glEnable(GL_CULL_FACE);
        xModel.render(camera);
        yModel.render(camera);
        zModel.render(camera);
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
