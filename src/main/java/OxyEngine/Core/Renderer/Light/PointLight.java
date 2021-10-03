package OxyEngine.Core.Renderer.Light;

import OxyEngine.Core.Scene.Entity;
import OxyEngineEditor.UI.GUINode;
import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;
import org.joml.Vector3f;

import static OxyEngine.Core.Scene.SceneRuntime.entityContext;

public final class PointLight extends Light {

    private float radius, cutoff;

    private final Vector3f color = new Vector3f(1.0f, 1.0f, 1.0f);
    private final Vector3f position = new Vector3f();

    public PointLight(float radius, float cutoff) {
        this.radius = radius;
        this.cutoff = cutoff;
    }

    public PointLight(float colorIntensity, float radius, float cutoff) {
        this.colorIntensity = colorIntensity;
        this.radius = radius;
        this.cutoff = cutoff;
    }

    public PointLight(PointLight other) {
        this.color.set(other.color);
        this.radius = other.radius;
        this.cutoff = other.cutoff;
        this.colorIntensity = other.colorIntensity;
    }

    public void update(Entity e) {
        e.getTransform().getTranslation(position);
    }

    public float getRadius() {
        return radius;
    }

    public float getCutoff() {
        return cutoff;
    }

    public Vector3f getColor() {
        return color;
    }

    public Vector3f getPosition() {
        return position;
    }

    private static final float[] radiusArr = new float[1], cutoffArr = new float[1], colorIntensityArr = new float[1], radianceArr = new float[3];
    public static final GUINode guiNode = () -> {
        if (entityContext == null) return;
        if (!entityContext.has(PointLight.class)) return;

        if (ImGui.treeNodeEx("Point Light", ImGuiTreeNodeFlags.DefaultOpen)) {

            ImGui.columns(2, "env column");
            ImGui.alignTextToFramePadding();
            ImGui.text("Color");
            ImGui.alignTextToFramePadding();
            ImGui.text("Color intensity");
            ImGui.alignTextToFramePadding();
            ImGui.text("Radius");
            ImGui.alignTextToFramePadding();
            ImGui.text("Cutoff");
            ImGui.nextColumn();
            ImGui.pushItemWidth(ImGui.getContentRegionAvailX());

            radiusArr[0] = entityContext.get(PointLight.class).radius;
            cutoffArr[0] = entityContext.get(PointLight.class).cutoff;
            colorIntensityArr[0] = entityContext.get(PointLight.class).colorIntensity;

            Vector3f radiance = entityContext.get(PointLight.class).color;
            radianceArr[0] = radiance.x;
            radianceArr[1] = radiance.y;
            radianceArr[2] = radiance.z;

            ImGui.colorEdit3("###hidelabel radiance", radianceArr);
            ImGui.dragFloat("###hidelabel intensity", colorIntensityArr, 0.1f, 0f, 1000f);
            ImGui.dragFloat("###hidelabel radius", radiusArr, 0.01f, 0, 1000f);
            ImGui.dragFloat("###hidelabel cutoff", cutoffArr, 0.01f, 0, 1f);

            entityContext.get(PointLight.class).radius = radiusArr[0];
            entityContext.get(PointLight.class).cutoff = cutoffArr[0];
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
