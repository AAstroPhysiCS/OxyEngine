package OxyEngine.Core.Context.Renderer.Light;

import OxyEngine.Core.Context.Renderer.Renderer;
import OxyEngine.Core.Context.Renderer.Texture.EnvironmentTexture;
import OxyEngine.Core.Context.Scene.Entity;
import OxyEngineEditor.UI.GUINode;
import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.type.ImString;
import org.joml.Math;

import java.util.Set;

import static OxyEngine.Core.Context.Scene.SceneRuntime.sceneContext;
import static OxyEngine.Core.Context.Scene.SceneRuntime.entityContext;
import static OxyEngine.System.FileSystem.openDialog;
import static OxyEngine.System.OxySystem.*;
import static OxyEngineEditor.UI.Panels.ProjectPanel.dirAssetGrey;

public abstract class SkyLight extends Light {

    public float[] intensity = new float[]{1.0f};

    protected boolean primary;

    public SkyLight() {

    }

    @Override
    public void update(Entity e, int i) {
    }

    private static final ImString guiNodePath = new ImString(100);

    private static void guiEnvMapLoad() {
        String path = openDialog("hdr", null);
        SkyLight skyLightComp = entityContext.get(SkyLight.class);
        if (skyLightComp instanceof HDREnvironmentMap envMap && path != null) {
            if (envMap.loadEnvironmentMap(path)) guiNodePath.set(path);
        }
    }

    public abstract void bind();

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    private static final Set<Class<? extends SkyLight>> subClasses = getSubClasses(SkyLight.class);

    public static final GUINode guiNode = () -> {

        if (entityContext == null) return;
        if (!entityContext.has(SkyLight.class)) return;
        SkyLight comp = entityContext.get(SkyLight.class);

        String currentSkyLightType = comp.getClass().getSimpleName();
        ImGui.pushItemWidth(ImGui.getContentRegionAvailX());
        if (ImGui.beginCombo("##hideLabelSkyLight", currentSkyLightType)) {
            for (Class<? extends SkyLight> classesToPick : subClasses) {
                String name = classesToPick.getSimpleName();
                boolean isSelected = (currentSkyLightType.equals(name));
                if (ImGui.selectable(name, isSelected)) {
                    entityContext.removeComponent(SkyLight.class);
                    try {
                        entityContext.addComponent(classesToPick.getDeclaredConstructor().newInstance());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Renderer.submitSkyLight(entityContext.get(SkyLight.class));
                }
            }
            ImGui.endCombo();
        }
        ImGui.popItemWidth();

        if (comp instanceof HDREnvironmentMap envMap) {

            EnvironmentTexture environmentTexture = envMap.getEnvironmentTexture();
            if(environmentTexture != null) guiNodePath.set(environmentTexture.getPath());

            ImGui.spacing();
            ImGui.alignTextToFramePadding();
            ImGui.indent(5);
            ImGui.text("HDR Path:");
            ImGui.unindent(5);
            ImGui.sameLine();
            ImGui.pushItemWidth(ImGui.getContentRegionAvailX() - 30f);
            if (ImGui.inputText("##hidelabel", guiNodePath, ImGuiInputTextFlags.EnterReturnsTrue))
                guiEnvMapLoad();
            ImGui.popItemWidth();
            ImGui.sameLine();
            if (ImGui.imageButton(dirAssetGrey.getTextureId(), 20, 20, 0, 1, 1, 0, 0))
                guiEnvMapLoad();
            ImGui.spacing();

            ImGui.columns(2, "env column");
            ImGui.setColumnOffset(0, -90f);
            ImGui.alignTextToFramePadding();
            ImGui.text("Intensity: ");
            ImGui.alignTextToFramePadding();
            ImGui.text("Environment LOD:");
            ImGui.nextColumn();
            ImGui.pushItemWidth(ImGui.getContentRegionAvailX());
            ImGui.sliderFloat("###hidelabel intensitySkyLight", envMap.intensity, 0, 10);
            ImGui.sliderFloat("###hidelabel lod", envMap.mipLevelStrength, 0, 5);
            ImGui.popItemWidth();
            ImGui.columns(1);

            if (ImGui.radioButton("Use", envMap.primary)) {
                envMap.primary = !envMap.primary;
                //RESETTING ALL THE OTHER SKYLIGHTS
                sceneContext.view(SkyLight.class).stream().map(e -> e.get(SkyLight.class)).filter(e -> !e.equals(comp)).forEach(e -> e.primary = false);
            }

            ImGui.separator();
        } else if (comp instanceof DynamicSky envMap) {

            ImGui.columns(2, "env column");
            ImGui.setColumnOffset(0, -90f);
            ImGui.alignTextToFramePadding();
            ImGui.text("Intensity: ");
            ImGui.nextColumn();
            ImGui.pushItemWidth(ImGui.getContentRegionAvailX());
            ImGui.sliderFloat("###hidelabel intensitySkyLight", envMap.intensity, 0, 10);
            ImGui.popItemWidth();
            ImGui.columns(1);

            if (ImGui.sliderFloat("Inclimation", envMap.inclination, 0, 10)) {
                envMap.dynamicSkySunDir.set(Math.sin(envMap.inclination[0]) * Math.cos(envMap.azimuth[0]), Math.cos(envMap.inclination[0]), Math.sin(envMap.inclination[0]) * Math.sin(envMap.azimuth[0]));
                if (envMap.dynamicSkyTexture != null) envMap.dynamicSkyTexture.dispose();
                envMap.dynamicSkyTexture = null;
                envMap.load();
            }
            if (ImGui.sliderFloat("Azimuth", envMap.azimuth, 0, 10)) {
                envMap.dynamicSkySunDir.set(Math.sin(envMap.inclination[0]) * Math.cos(envMap.azimuth[0]), Math.cos(envMap.inclination[0]), Math.sin(envMap.inclination[0]) * Math.sin(envMap.azimuth[0]));
                if (envMap.dynamicSkyTexture != null) envMap.dynamicSkyTexture.dispose();
                envMap.dynamicSkyTexture = null;
                envMap.load();
            }

            if (ImGui.sliderFloat("Turbidity", envMap.turbidity, 1.7f, 10)) {
                if (envMap.dynamicSkyTexture != null) envMap.dynamicSkyTexture.dispose();
                envMap.dynamicSkyTexture = null;
                envMap.load();
            }

            if (ImGui.radioButton("Use", envMap.primary)) {
                envMap.primary = !envMap.primary;
                if (!envMap.primary) envMap.dispose();
                else envMap.load();

                //RESETTING ALL THE OTHER SKYLIGHTS
                sceneContext.view(SkyLight.class).stream().map(e -> e.get(SkyLight.class)).filter(e -> !e.equals(comp)).forEach(e -> e.primary = false);
            }

            ImGui.separator();
        }
    };
}
