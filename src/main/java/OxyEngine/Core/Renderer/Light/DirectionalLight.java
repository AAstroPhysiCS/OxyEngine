package OxyEngine.Core.Renderer.Light;

import OxyEngine.Components.TransformComponent;
import OxyEngine.Core.Renderer.Renderer;
import OxyEngine.Core.Renderer.Shader;
import OxyEngine.Core.Scene.Entity;
import OxyEngineEditor.UI.GUINode;
import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;
import org.joml.Matrix3f;
import org.joml.Vector3f;

import static OxyEngine.Core.Scene.SceneRuntime.entityContext;

public final class DirectionalLight extends Light {

    private Vector3f dir;
    private final Vector3f color = new Vector3f(1.0f, 1.0f, 1.0f);
    private boolean castShadows = true;

    public DirectionalLight(float colorIntensity) {
        this.colorIntensity = colorIntensity;
    }

    public DirectionalLight(DirectionalLight other){
        this.dir = new Vector3f(other.dir);
        this.colorIntensity = other.colorIntensity;
        this.color.set(other.color);
        this.castShadows = other.castShadows;
    }

    public DirectionalLight() {
        this(1);
    }

    public void update(Entity e) {
        TransformComponent t = e.get(TransformComponent.class);
        Matrix3f transform3x3 = new Matrix3f();
        t.transform.normalize3x3(transform3x3);
        dir = new Vector3f(transform3x3.transform(new Vector3f(1.0f))).negate();
        Shader pbrShader = Renderer.getShader("OxyPBR");
        pbrShader.begin();
        pbrShader.setUniformVec3("d_Light.direction", dir.x, dir.y, dir.z);
        pbrShader.setUniformVec3("d_Light.diffuse", new Vector3f(color).mul(colorIntensity));
        pbrShader.setUniform1i("d_Light.activeState", 1);
        pbrShader.end();
    }

    public Vector3f getDirection() {
        return dir;
    }

    public boolean isCastingShadows() {
        return castShadows;
    }

    private static final float[] colorIntensityArr = new float[1], radianceArr = new float[3];

    public static final GUINode guiNode = () -> {

        if (entityContext == null) return;
        if (!entityContext.has(DirectionalLight.class)) return;

        if (ImGui.treeNodeEx("Directional Light", ImGuiTreeNodeFlags.DefaultOpen)) {
            DirectionalLight dL = entityContext.get(DirectionalLight.class);

            ImGui.columns(2, "env column");
            ImGui.alignTextToFramePadding();
            ImGui.text("Cast Hard Shadows:");
            ImGui.alignTextToFramePadding();
            ImGui.text("Color:");
            ImGui.alignTextToFramePadding();
            ImGui.text("Color intensity:");
            ImGui.nextColumn();
            ImGui.pushItemWidth(ImGui.getContentRegionAvailX());

            if(ImGui.radioButton("##hideLabel dCastShadows", dL.castShadows)){
                dL.castShadows = !dL.castShadows;
            }

            Vector3f radiance = dL.color;
            radianceArr[0] = radiance.x;
            radianceArr[1] = radiance.y;
            radianceArr[2] = radiance.z;
            ImGui.colorEdit3("###hidelabel radiance", radianceArr);
            dL.color.set(radianceArr);

            colorIntensityArr[0] = dL.colorIntensity;
            ImGui.dragFloat("###hidelabel intensity d", colorIntensityArr, 0.1f, 0f, 1000f);
            dL.colorIntensity = colorIntensityArr[0];

            ImGui.popItemWidth();
            ImGui.columns(1);
            ImGui.separator();
            ImGui.treePop();
            ImGui.spacing();
        }
    };
}

