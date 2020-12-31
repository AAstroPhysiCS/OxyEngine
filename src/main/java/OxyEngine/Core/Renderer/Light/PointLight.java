package OxyEngine.Core.Renderer.Light;

import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Components.TransformComponent;
import OxyEngineEditor.Scene.Objects.Model.OxyMaterial;
import OxyEngineEditor.Scene.OxyEntity;
import OxyEngineEditor.UI.Panels.GUINode;
import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;
import org.joml.Vector3f;

import static OxyEngineEditor.UI.Gizmo.OxySelectHandler.entityContext;

public class PointLight extends Light {

    private float constant;
    private float linear;
    private float quadratic;

    public PointLight(Vector3f ambient, Vector3f specular, float constant, float linear, float quadratic) {
        super(ambient, specular);
        this.constant = constant;
        this.linear = linear;
        this.quadratic = quadratic;
    }

    private OxyEntity e;
    private int index;

    @Override
    public void update(OxyEntity e, int i) {
        this.e = e;
        index = i;
        OxyShader shader = e.get(OxyShader.class);
        OxyMaterial material = e.get(OxyMaterial.class);
        shader.enable();
        shader.setUniformVec3("p_Light[" + i + "].position", e.get(TransformComponent.class).position);
//        shader.setUniformVec3("p_Light[" + i + "].ambient", ambient);
//        shader.setUniformVec3("p_Light[" + i + "].specular", specular);
        shader.setUniformVec3("p_Light[" + i + "].diffuse", new Vector3f(material.albedoColor.getNumbers()).mul(colorIntensity));
        shader.setUniform1f("p_Light[" + i + "].constant", constant);
        shader.setUniform1f("p_Light[" + i + "].linear", linear);
        shader.setUniform1f("p_Light[" + i + "].quadratic", quadratic);
        shader.disable();
    }

    private static final float[] constantArr = new float[1], linearArr = new float[1], quadraticArr = new float[1], colorIntensityArr = new float[1];
//    private static final float[] ambientArr = new float[3], specularArr = new float[3];
    public static final GUINode guiNode = () -> {
        if (ImGui.collapsingHeader("Point Light", ImGuiTreeNodeFlags.DefaultOpen)) {

            ImGui.columns(2, "env column");
            ImGui.alignTextToFramePadding();
            ImGui.text("Color intensity:");
            ImGui.alignTextToFramePadding();
            ImGui.text("Constant value:");
            ImGui.alignTextToFramePadding();
            ImGui.text("Linear value:");
            ImGui.alignTextToFramePadding();
            ImGui.text("Quadratic value:");
            ImGui.nextColumn();
            ImGui.pushItemWidth(ImGui.getContentRegionAvailWidth());
            constantArr[0] = entityContext.get(PointLight.class).constant;
            linearArr[0] = entityContext.get(PointLight.class).linear;
            quadraticArr[0] = entityContext.get(PointLight.class).quadratic;
            colorIntensityArr[0] = entityContext.get(PointLight.class).colorIntensity;
            ImGui.dragFloat("###hidelabel intensity", colorIntensityArr, 0.1f, 0f, 1000f);
            ImGui.dragFloat("###hidelabel constant", constantArr, 0.1f, 0, 10);
            ImGui.dragFloat("###hidelabel linear", linearArr,0.05f, 0, 1);
            ImGui.dragFloat("###hidelabel quadratic", quadraticArr, 0.05f, 0, 1);
            entityContext.get(PointLight.class).constant = constantArr[0];
            entityContext.get(PointLight.class).linear = linearArr[0];
            entityContext.get(PointLight.class).quadratic = quadraticArr[0];
//            entityContext.get(PointLight.class).ambient.set(ambientArr[0], ambientArr[1], ambientArr[2]);
//            entityContext.get(PointLight.class).specular.set(specularArr[0], specularArr[1], specularArr[2]);
            entityContext.get(PointLight.class).colorIntensity = colorIntensityArr[0];
            ImGui.popItemWidth();
            ImGui.columns(1);
        }
    };

    @Override
    public void dispose() {
        OxyShader shader = e.get(OxyShader.class);
        shader.enable();
        shader.setUniformVec3("p_Light[" + index + "].position", new Vector3f(0, 0, 0));
//        shader.setUniformVec3("p_Light[" + i + "].ambient", ambient);
//        shader.setUniformVec3("p_Light[" + i + "].specular", specular);
        shader.setUniformVec3("p_Light[" + index + "].diffuse", new Vector3f(0, 0, 0));
        shader.setUniform1f("p_Light[" + index + "].constant", 0);
        shader.setUniform1f("p_Light[" + index + "].linear", 0);
        shader.setUniform1f("p_Light[" + index + "].quadratic", 0);
        shader.disable();
    }
}
