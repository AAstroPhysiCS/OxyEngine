package OxyEngine.Core.Renderer.Light;

import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Scene.Objects.Model.OxyMaterial;
import OxyEngine.Scene.Objects.Model.OxyMaterialPool;
import OxyEngine.Scene.OxyEntity;
import OxyEngineEditor.UI.Panels.GUINode;
import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;
import org.joml.Vector3f;

import static OxyEngineEditor.UI.Gizmo.OxySelectHandler.entityContext;

public class DirectionalLight extends Light {

    private final Vector3f dir;

    public DirectionalLight(float colorIntensity, Vector3f dir) {
        this.dir = dir;
        this.colorIntensity = colorIntensity;
    }

    public DirectionalLight() {
        this(1, new Vector3f());
    }

    @Override
    public void update(OxyEntity e, int i) {
        OxyShader shader = e.get(OxyShader.class);
        OxyMaterial material = OxyMaterialPool.getMaterial(e);
        shader.enable();
        shader.setUniformVec3("d_Light[" + i + "].direction", dir.x, dir.y, dir.z);
        shader.setUniformVec3("d_Light[" + i + "].diffuse", new Vector3f(material.albedoColor.getNumbers()).mul(colorIntensity));
        shader.setUniform1i("d_Light[" + i + "].activeState", 1);
        shader.disable();
    }

    public Vector3f getDirection() {
        return dir;
    }

    final float[] colorIntensityArr = new float[1];
    final float[] dirArr = new float[3];
    public static final GUINode guiNode = () -> {
        if (ImGui.treeNodeEx("Directional Light", ImGuiTreeNodeFlags.DefaultOpen)) {
            DirectionalLight dL = entityContext.get(DirectionalLight.class);
            ImGui.columns(2, "env column");
            ImGui.alignTextToFramePadding();
            ImGui.text("Color intensity:");
            ImGui.text("Light Direction:");
            ImGui.nextColumn();
            ImGui.pushItemWidth(ImGui.getContentRegionAvailX());
            dL.colorIntensityArr[0] = dL.colorIntensity;
            dL.dirArr[0] = dL.dir.x;
            dL.dirArr[1] = dL.dir.y;
            dL.dirArr[2] = dL.dir.z;
            ImGui.dragFloat("###hidelabel intensity d", dL.colorIntensityArr, 0.1f, 0f, 1000f);
            ImGui.dragFloat3("##hidelabel direction d", dL.dirArr);
            dL.colorIntensity = dL.colorIntensityArr[0];
            dL.dir.set(dL.dirArr[0], dL.dirArr[1], dL.dirArr[2]);
            ImGui.popItemWidth();
            ImGui.columns(1);
            ImGui.separator();
            ImGui.treePop();
            ImGui.spacing();
        }
    };
}

