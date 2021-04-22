package OxyEngine.Core.Renderer.Light;

import OxyEngine.Core.Renderer.Mesh.NativeObjectMeshOpenGL;
import OxyEngine.Core.Renderer.Pipeline.OxyShader;
import OxyEngine.Core.Renderer.Texture.HDRTexture;
import OxyEngine.Core.Renderer.Texture.OxyTexture;
import OxyEngine.Scene.Objects.Native.NativeObjectFactory;
import OxyEngine.Scene.Objects.Native.OxyNativeObject;
import OxyEngine.Scene.OxyEntity;
import OxyEngine.Scene.SceneRenderer;
import OxyEngineEditor.UI.Panels.GUINode;
import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.type.ImString;
import org.lwjgl.stb.STBImage;

import static OxyEngine.Scene.SceneRuntime.ACTIVE_SCENE;
import static OxyEngine.System.OxySystem.FileSystem.openDialog;
import static OxyEngine.System.OxySystem.logger;
import static OxyEngineEditor.UI.Gizmo.OxySelectHandler.entityContext;
import static OxyEngineEditor.UI.Panels.ProjectPanel.dirAssetGrey;

public class SkyLight extends Light {

    public static final NativeObjectMeshOpenGL skyLightMesh =
            new NativeObjectMeshOpenGL(SceneRenderer.getInstance().getHDRPipeline());

    private boolean primary;

    private HDRTexture hdrTexture;

    static {
        OxyShader.createShader("OxySkybox", "shaders/OxySkybox.glsl");
    }

    public float[] gammaStrength = new float[]{2.2f};
    public float[] exposure = new float[]{1.0f};

    public float[] intensity = new float[]{1.0f};
    public float[] mipLevelStrength = new float[]{0.0f};

    public SkyLight() {
    }

    public void loadHDR(String pathToHDR) {
        if (pathToHDR == null) return;
        if (!STBImage.stbi_is_hdr(pathToHDR)) {
            logger.severe("Image is not HDR");
            return;
        }

        if (hdrTexture != null) hdrTexture.dispose();
        hdrTexture = OxyTexture.loadHDRTexture(pathToHDR);
    }

    @Override
    public void update(OxyEntity e, int i) {
        //do nothing
    }

    public HDRTexture getHDRTexture() {
        return hdrTexture;
    }

    private final ImString guiNodePath = new ImString(100);

    private static void guiNodeLoad() {
        String path = openDialog("hdr", null);
        SkyLight skyLightComp = entityContext.get(SkyLight.class);
        if (path != null) {
            skyLightComp.guiNodePath.set(path);
            skyLightComp.loadHDR(path);
        }
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public static final class Factory implements NativeObjectFactory {

        private static final float[] skyboxVertices = {
                -1.0f, -1.0f, -1.0f,
                1.0f, 1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,
                1.0f, 1.0f, -1.0f,
                -1.0f, -1.0f, -1.0f,
                -1.0f, 1.0f, -1.0f,
                // front face
                -1.0f, -1.0f, 1.0f,
                1.0f, -1.0f, 1.0f,
                1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, 1.0f,
                -1.0f, 1.0f, 1.0f,
                -1.0f, -1.0f, 1.0f,
                // left face
                -1.0f, 1.0f, 1.0f,
                -1.0f, 1.0f, -1.0f,
                -1.0f, -1.0f, -1.0f,
                -1.0f, -1.0f, -1.0f,
                -1.0f, -1.0f, 1.0f,
                -1.0f, 1.0f, 1.0f,
                // right face
                1.0f, 1.0f, 1.0f,
                1.0f, -1.0f, -1.0f,
                1.0f, 1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,
                1.0f, 1.0f, 1.0f,
                1.0f, -1.0f, 1.0f,
                // bottom face
                -1.0f, -1.0f, -1.0f,
                1.0f, -1.0f, -1.0f,
                1.0f, -1.0f, 1.0f,
                1.0f, -1.0f, 1.0f,
                -1.0f, -1.0f, 1.0f,
                -1.0f, -1.0f, -1.0f,
                // top face
                -1.0f, 1.0f, -1.0f,
                1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, -1.0f,
                1.0f, 1.0f, 1.0f,
                -1.0f, 1.0f, -1.0f,
                -1.0f, 1.0f, 1.0f,
        };

        private static final int[] indices = new int[skyboxVertices.length];

        static {
            for (int i = 0; i < skyboxVertices.length; i++) {
                indices[i] = i;
            }
        }

        @Override
        public void constructData(OxyNativeObject e, int size) {
            if (e.vertices == null) {
                e.vertices = skyboxVertices;
            }
        }

        public void initData(OxyNativeObject e, NativeObjectMeshOpenGL mesh) {
            e.indices = indices;
            mesh.addToBuffer(SceneRenderer.getInstance().getHDRPipeline());
        }
    }

    public static final GUINode guiNode = () -> {

        if (entityContext == null) return;
        if (!entityContext.has(SkyLight.class)) return;
        SkyLight comp = entityContext.get(SkyLight.class);
        HDRTexture hdrTexture = comp.getHDRTexture();
        if (hdrTexture != null) comp.guiNodePath.set(hdrTexture.getPath());

        ImGui.spacing();
        ImGui.alignTextToFramePadding();
        ImGui.indent(5);
        ImGui.text("HDR Path:");
        ImGui.unindent(5);
        ImGui.sameLine();
        ImGui.pushItemWidth(ImGui.getContentRegionAvailX() - 30f);
        if (ImGui.inputText("##hidelabel", comp.guiNodePath, ImGuiInputTextFlags.EnterReturnsTrue))
            guiNodeLoad();
        ImGui.popItemWidth();
        ImGui.sameLine();
        if (ImGui.imageButton(dirAssetGrey.getTextureId(), 20, 20, 0, 1, 1, 0, 0))
            guiNodeLoad();
        ImGui.spacing();

        ImGui.columns(2, "env column");
        ImGui.setColumnOffset(0, -90f);
        ImGui.alignTextToFramePadding();
        ImGui.text("Intensity: ");
        ImGui.alignTextToFramePadding();
        ImGui.text("Gamma strength:");
        ImGui.alignTextToFramePadding();
        ImGui.text("Environment LOD:");
        ImGui.alignTextToFramePadding();
        ImGui.text("Exposure: ");
        ImGui.nextColumn();
        ImGui.pushItemWidth(ImGui.getContentRegionAvailX());
        ImGui.sliderFloat("###hidelabel intensitySkyLight", comp.intensity, 0, 10);
        ImGui.sliderFloat("###hidelabel g", comp.gammaStrength, 0, 10);
        ImGui.sliderFloat("###hidelabel lod", comp.mipLevelStrength, 0, 5);
        ImGui.sliderFloat("###hidelabel exposure", comp.exposure, 0, 10);
        ImGui.popItemWidth();
        ImGui.columns(1);

        if (ImGui.radioButton("Use", comp.primary)) {
            comp.primary = !comp.primary;
            //RESETTING ALL THE OTHER SKYLIGHTS
            ACTIVE_SCENE.view(SkyLight.class).stream().map(e -> e.get(SkyLight.class)).filter(e -> !e.equals(comp)).forEach(e -> e.primary = false);
        }

        ImGui.separator();
    };

}
