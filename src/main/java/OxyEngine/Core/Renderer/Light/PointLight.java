package OxyEngine.Core.Renderer.Light;

import OxyEngine.Core.Layers.SceneLayer;
import OxyEngine.Components.TransformComponent;
import OxyEngine.Core.Renderer.Pipeline.OxyPipeline;
import OxyEngine.Scene.Objects.Model.OxyMaterial;
import OxyEngine.Scene.Objects.Model.OxyMaterialPool;
import OxyEngine.Scene.OxyEntity;
import OxyEngineEditor.UI.Panels.GUINode;
import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;
import org.joml.Vector3f;

import static OxyEngineEditor.UI.Gizmo.OxySelectHandler.entityContext;

public class PointLight extends Light {

    private float constant;
    private float linear;
    private float quadratic;

    public PointLight(float constant, float linear, float quadratic) {
        this.constant = constant;
        this.linear = linear;
        this.quadratic = quadratic;
    }

    public PointLight(float colorIntensity, float constant, float linear, float quadratic) {
        this.colorIntensity = colorIntensity;
        this.constant = constant;
        this.linear = linear;
        this.quadratic = quadratic;
    }

    @Override
    public void update(OxyEntity e, int i) {
        OxyPipeline pbrPipeline = SceneLayer.getInstance().getGeometryPipeline();
        OxyMaterial material = OxyMaterialPool.getMaterial(e);
        pbrPipeline.begin();
        pbrPipeline.setUniformVec3("p_Light[" + i + "].position", e.get(TransformComponent.class).worldSpacePosition);
        pbrPipeline.setUniformVec3("p_Light[" + i + "].diffuse", new Vector3f(material.albedoColor.getNumbers()).mul(colorIntensity));
        pbrPipeline.setUniform1f("p_Light[" + i + "].constant", constant);
        pbrPipeline.setUniform1f("p_Light[" + i + "].linear", linear);
        pbrPipeline.setUniform1f("p_Light[" + i + "].quadratic", quadratic);
        pbrPipeline.setUniform1i("p_Light[" + i + "].activeState", 1);
        pbrPipeline.end();
    }

    public float getConstantValue() {
        return constant;
    }

    public float getLinearValue() {
        return linear;
    }

    public float getQuadraticValue() {
        return quadratic;
    }

    private static final float[] constantArr = new float[1], linearArr = new float[1], quadraticArr = new float[1], colorIntensityArr = new float[1];
    public static final GUINode guiNode = () -> {
        if(entityContext == null) return;
        if (ImGui.treeNodeEx("Point Light", ImGuiTreeNodeFlags.DefaultOpen)) {

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
            ImGui.pushItemWidth(ImGui.getContentRegionAvailX());
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
            entityContext.get(PointLight.class).colorIntensity = colorIntensityArr[0];
            ImGui.popItemWidth();
            ImGui.columns(1);
            ImGui.separator();
            ImGui.treePop();
            ImGui.spacing();
        }
    };
}
