package OxyEngine.Core.Context.Renderer.Light;

import OxyEngine.Components.TransformComponent;
import OxyEngine.Core.Context.Renderer.Pipeline.OxyShader;
import OxyEngine.Core.Context.Renderer.Pipeline.ShaderLibrary;
import OxyEngine.Core.Context.Scene.OxyMaterial;
import OxyEngine.Core.Context.Scene.OxyMaterialPool;
import OxyEngine.Core.Context.Scene.OxyEntity;
import OxyEngineEditor.UI.Panels.GUINode;
import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;
import org.joml.Matrix3f;
import org.joml.Vector3f;

import java.util.Optional;

import static OxyEngine.Core.Context.Scene.SceneRuntime.entityContext;

public class DirectionalLight extends Light {

    private Vector3f dir;

    public DirectionalLight(float colorIntensity) {
        this.colorIntensity = colorIntensity;
    }

    public DirectionalLight() {
        this(1);
    }

    @Override
    public void update(OxyEntity e, int i) {
        Optional<OxyMaterial> materialOpt = OxyMaterialPool.getMaterial(e);
        materialOpt.ifPresent(material -> {
            TransformComponent t = e.get(TransformComponent.class);

            Matrix3f transform3x3 = new Matrix3f();
            t.transform.normalize3x3(transform3x3);
            dir = new Vector3f(transform3x3.transform(new Vector3f(1.0f))).negate();

            OxyShader pbrShader = ShaderLibrary.get("OxyPBR");
            pbrShader.begin();
            pbrShader.setUniformVec3("d_Light[" + i + "].direction", dir.x, dir.y, dir.z);
            pbrShader.setUniformVec3("d_Light[" + i + "].diffuse", new Vector3f(material.albedoColor.getNumbers()).mul(colorIntensity));
            pbrShader.setUniform1i("d_Light[" + i + "].activeState", 1);
            pbrShader.end();
        });
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

