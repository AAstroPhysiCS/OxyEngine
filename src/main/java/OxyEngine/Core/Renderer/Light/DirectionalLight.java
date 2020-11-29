package OxyEngine.Core.Renderer.Light;

import OxyEngine.Components.TransformComponent;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngineEditor.Scene.Objects.Model.OxyMaterial;
import OxyEngineEditor.Scene.OxyEntity;
import OxyEngineEditor.UI.Panels.GUIProperty;
import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;
import org.joml.Vector3f;

import static OxyEngineEditor.UI.Selector.OxySelectHandler.entityContext;

public class DirectionalLight extends Light {

    public DirectionalLight(Vector3f ambient, Vector3f specular) {
        super(ambient, specular);
    }

    @Override
    public void update(OxyEntity e, int i) {
        OxyShader shader = e.get(OxyShader.class);
        OxyMaterial material = e.get(OxyMaterial.class);
        shader.enable();
        shader.setUniform1f("currentLightIndex", 1);
        shader.setUniformVec3("d_Light.position", e.get(TransformComponent.class).position);
        shader.setUniformVec3("d_Light.direction", e.get(TransformComponent.class).rotation);
//        shader.setUniformVec3("d_Light.ambient", ambient);
//        shader.setUniformVec3("d_Light.specular", specular);
        shader.setUniformVec3("d_Light.diffuse", new Vector3f(material.albedoColor.getNumbers()).mul(colorIntensity));
        shader.disable();
    }

    private static final float[] colorIntensityArr = new float[1];
    public static final GUIProperty guiNode = () -> {
        if (ImGui.collapsingHeader("Directional Light", ImGuiTreeNodeFlags.DefaultOpen)) {

            ImGui.columns(2, "env column");
            ImGui.alignTextToFramePadding();
            ImGui.text("Color intensity:");
            ImGui.nextColumn();
            ImGui.pushItemWidth(ImGui.getContentRegionAvailWidth());
            colorIntensityArr[0] = entityContext.get(DirectionalLight.class).colorIntensity;
            ImGui.sliderFloat("###hidelabel intensity d", colorIntensityArr, 0, 255);
//            entityContext.get(PointLight.class).ambient.set(ambientArr[0], ambientArr[1], ambientArr[2]);
//            entityContext.get(PointLight.class).specular.set(specularArr[0], specularArr[1], specularArr[2]);
            entityContext.get(DirectionalLight.class).colorIntensity = colorIntensityArr[0];
            ImGui.popItemWidth();
            ImGui.columns(1);
        }
    };
}

