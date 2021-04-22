package OxyEngine.Core.Renderer.Light;

import OxyEngine.Components.TransformComponent;
import OxyEngine.Core.Renderer.Pipeline.OxyShader;
import OxyEngine.Core.Renderer.Pipeline.ShaderLibrary;
import OxyEngine.Scene.Objects.Model.OxyMaterial;
import OxyEngine.Scene.Objects.Model.OxyMaterialPool;
import OxyEngine.Scene.OxyEntity;
import OxyEngineEditor.UI.Panels.GUINode;
import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;
import org.joml.Matrix3f;
import org.joml.Vector3f;

import static OxyEngineEditor.UI.Gizmo.OxySelectHandler.entityContext;

public class DirectionalLight extends Light {

    private Vector3f dir = null;

    public DirectionalLight(float colorIntensity) {
        this.colorIntensity = colorIntensity;
    }

    public DirectionalLight() {
        this(1);
    }

    @Override
    public void update(OxyEntity e, int i) {
        OxyMaterial material = OxyMaterialPool.getMaterial(e);
        TransformComponent t = e.get(TransformComponent.class);

        Matrix3f transform3x3 = new Matrix3f();
        t.transform.normalize3x3(transform3x3);
        dir = transform3x3.transform(new Vector3f(1.0f));

        OxyShader pbrShader = ShaderLibrary.get("OxyPBRAnimation");
        pbrShader.begin();
        pbrShader.setUniformVec3("d_Light[" + i + "].direction", dir.x, dir.y, dir.z);
        pbrShader.setUniformVec3("d_Light[" + i + "].diffuse", new Vector3f(material.albedoColor.getNumbers()).mul(colorIntensity));
        pbrShader.setUniform1i("d_Light[" + i + "].activeState", 1);
        pbrShader.end();
    }

    public Vector3f getDirection() {
        return dir;
    }

    private boolean castShadows = true;

    public boolean isCastingShadows() {
        return castShadows;
    }

    final float[] colorIntensityArr = new float[1];
    public static final GUINode guiNode = () -> {
        if (ImGui.treeNodeEx("Directional Light", ImGuiTreeNodeFlags.DefaultOpen)) {
            DirectionalLight dL = entityContext.get(DirectionalLight.class);
            ImGui.columns(2, "env column");
            ImGui.alignTextToFramePadding();
            ImGui.text("Cast Hard Shadows:");
            ImGui.text("Color intensity:");
            ImGui.nextColumn();
            ImGui.pushItemWidth(ImGui.getContentRegionAvailX());

            if(ImGui.radioButton("##hideLabel dCastShadows", dL.castShadows)){
                dL.castShadows = !dL.castShadows;
            }

            dL.colorIntensityArr[0] = dL.colorIntensity;
            ImGui.dragFloat("###hidelabel intensity d", dL.colorIntensityArr, 0.1f, 0f, 1000f);
            dL.colorIntensity = dL.colorIntensityArr[0];

            ImGui.popItemWidth();
            ImGui.columns(1);
            ImGui.separator();
            ImGui.treePop();
            ImGui.spacing();
        }
    };
}

