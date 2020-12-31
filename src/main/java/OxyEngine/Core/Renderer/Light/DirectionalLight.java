package OxyEngine.Core.Renderer.Light;

import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngineEditor.Scene.Objects.Model.OxyMaterial;
import OxyEngineEditor.Scene.OxyEntity;
import OxyEngineEditor.UI.Panels.GUINode;
import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;
import org.joml.Vector3f;

import static OxyEngineEditor.UI.Gizmo.OxySelectHandler.entityContext;

public class DirectionalLight extends Light {

    private final Vector3f dir = new Vector3f(0, 0, 0);

    public DirectionalLight(Vector3f ambient, Vector3f specular) {
        super(ambient, specular);
    }

    private int index;
    private OxyEntity e;

    @Override
    public void update(OxyEntity e, int i) {
        this.e = e;
        index = i;
        OxyShader shader = e.get(OxyShader.class);
        OxyMaterial material = e.get(OxyMaterial.class);
        shader.enable();
        shader.setUniformVec3("d_Light[" + i + "].direction", dir);
//        shader.setUniformVec3("d_Light.ambient", ambient);
//        shader.setUniformVec3("d_Light.specular", specular);
        shader.setUniformVec3("d_Light[" + i + "].diffuse", new Vector3f(material.albedoColor.getNumbers()).mul(colorIntensity));
        shader.disable();
    }

    final float[] colorIntensityArr = new float[1];
    final float[] dirArr = new float[3];
    public static final GUINode guiNode = () -> {
        if (ImGui.collapsingHeader("Directional Light", ImGuiTreeNodeFlags.DefaultOpen)) {
            DirectionalLight dL = entityContext.get(DirectionalLight.class);
            ImGui.columns(2, "env column");
            ImGui.alignTextToFramePadding();
            ImGui.text("Color intensity:");
            ImGui.text("Light Direction:");
            ImGui.nextColumn();
            ImGui.pushItemWidth(ImGui.getContentRegionAvailWidth());
            dL.colorIntensityArr[0] = dL.colorIntensity;
            ImGui.dragFloat("###hidelabel intensity d", dL.colorIntensityArr, 0.1f, 0f, 1000f);
            ImGui.dragFloat3("##hidelabel direction d", dL.dirArr);
            dL.colorIntensity = dL.colorIntensityArr[0];
            dL.dir.set(dL.dirArr[0], dL.dirArr[1], dL.dirArr[2]);
            ImGui.popItemWidth();
            ImGui.columns(1);
        }
    };

    @Override
    public void dispose() {
        OxyShader shader = e.get(OxyShader.class);
        shader.enable();
        shader.setUniformVec3("d_Light[" + index + "].direction", new Vector3f(0, 0, 0));
        shader.setUniformVec3("d_Light[" + index + "].diffuse", new Vector3f(0, 0, 0));
        shader.disable();
    }
}

