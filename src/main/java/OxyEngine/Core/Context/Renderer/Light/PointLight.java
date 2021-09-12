package OxyEngine.Core.Context.Renderer.Light;

import OxyEngine.Core.Context.Renderer.Renderer;
import OxyEngine.Core.Context.Renderer.Shader;
import OxyEngine.Core.Context.Scene.Entity;
import OxyEngineEditor.UI.GUINode;
import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;
import org.joml.Vector3f;

import static OxyEngine.Core.Context.Scene.SceneRuntime.entityContext;


public final class PointLight extends Light {

    private float constant;
    private float linear;
    private float quadratic;

    private final Vector3f color = new Vector3f(1.0f, 1.0f, 1.0f);

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
    public void update(Entity e, int i) {
        Shader pbrShader = Renderer.getShader("OxyPBR");

        Vector3f translate = new Vector3f();
        e.getTransform().getTranslation(translate);

        pbrShader.begin();
        pbrShader.setUniformVec3("p_Light[" + i + "].position", translate);
        pbrShader.setUniformVec3("p_Light[" + i + "].diffuse", new Vector3f(color).mul(colorIntensity));
        pbrShader.setUniform1f("p_Light[" + i + "].constant", constant);
        pbrShader.setUniform1f("p_Light[" + i + "].linear", linear);
        pbrShader.setUniform1f("p_Light[" + i + "].quadratic", quadratic);
        pbrShader.setUniform1i("p_Light[" + i + "].activeState", 1);
        pbrShader.end();
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

    private static final float[] constantArr = new float[1], linearArr = new float[1], quadraticArr = new float[1], colorIntensityArr = new float[1], radianceArr = new float[3];
    public static final GUINode guiNode = () -> {
        if (entityContext == null) return;
        if (!entityContext.has(PointLight.class)) return;

        if (ImGui.treeNodeEx("Point Light", ImGuiTreeNodeFlags.DefaultOpen)) {

            ImGui.columns(2, "env column");
            ImGui.alignTextToFramePadding();
            ImGui.text("Color:");
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

            Vector3f radiance = entityContext.get(PointLight.class).color;
            radianceArr[0] = radiance.x;
            radianceArr[1] = radiance.y;
            radianceArr[2] = radiance.z;

            ImGui.colorEdit3("###hidelabel radiance", radianceArr);
            ImGui.dragFloat("###hidelabel intensity", colorIntensityArr, 0.1f, 0f, 1000f);
            ImGui.dragFloat("###hidelabel constant", constantArr, 0.1f, 0, 10);
            ImGui.dragFloat("###hidelabel linear", linearArr, 0.05f, 0, 1);
            ImGui.dragFloat("###hidelabel quadratic", quadraticArr, 0.05f, 0, 1);

            entityContext.get(PointLight.class).constant = constantArr[0];
            entityContext.get(PointLight.class).linear = linearArr[0];
            entityContext.get(PointLight.class).quadratic = quadraticArr[0];
            entityContext.get(PointLight.class).colorIntensity = colorIntensityArr[0];
            entityContext.get(PointLight.class).color.set(radianceArr);

            ImGui.popItemWidth();
            ImGui.columns(1);
            ImGui.separator();
            ImGui.treePop();
            ImGui.spacing();
        }
    };
}
