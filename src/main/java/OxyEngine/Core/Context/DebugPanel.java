package OxyEngine.Core.Context;

import OxyEngineEditor.UI.Panels.Panel;
import imgui.ImGui;
import imgui.flag.ImGuiTreeNodeFlags;

import static OxyEngine.Core.Context.OxyRenderer.NUMBER_CASCADES;
import static OxyEngine.Core.Context.SceneRenderer.cascadeIndicatorToggle;
import static OxyEngine.Core.Context.SceneRenderer.getShadowMap;

final class DebugPanel extends Panel {

    private static DebugPanel INSTANCE = null;

    static final float[] cascadeSplit = new float[]{
            OxyRenderer.cascadeSplit[0], OxyRenderer.cascadeSplit[1], OxyRenderer.cascadeSplit[2], OxyRenderer.cascadeSplit[3]
    };
    static final float[] nearPlaneOffset = new float[]{
            ShadowMapCamera.nearPlaneOffset
    };
    static final int[] shadowMapIndex = new int[]{0};

    protected static DebugPanel getInstance() {
        if (INSTANCE == null) INSTANCE = new DebugPanel();
        return INSTANCE;
    }

    @Override
    public void preload() {

    }

    @Override
    public void renderPanel() {
        ImGui.begin("Debug Panel");

        if (ImGui.treeNodeEx("Shadow Map Debugging", ImGuiTreeNodeFlags.DefaultOpen)) {
            ImGui.sliderInt("##hideLabel ShadowMapIndex", shadowMapIndex, 0, NUMBER_CASCADES - 1);

            for (int i = 0; i < NUMBER_CASCADES; i++) {
                int textureID = getShadowMap(i);
                if (shadowMapIndex[0] == i) {
                    ImGui.image(textureID, ImGui.getContentRegionAvailX(), ImGui.getContentRegionAvailY(), 0, 1, 1, 0);
                }
            }

            ImGui.dragFloat("##hideLabel ShadowMapNearPlaneOffset", nearPlaneOffset);
            ImGui.dragFloat4("##hideLabel ShadowMapSplit", cascadeSplit);

            if (ImGui.radioButton("##hideLabel ShadowMapCascadeToggle", cascadeIndicatorToggle)) {
                cascadeIndicatorToggle = !cascadeIndicatorToggle;
            }

            OxyRenderer.cascadeSplit[0] = cascadeSplit[0];
            OxyRenderer.cascadeSplit[1] = cascadeSplit[1];
            OxyRenderer.cascadeSplit[2] = cascadeSplit[2];
            OxyRenderer.cascadeSplit[3] = cascadeSplit[3];

            ShadowMapCamera.nearPlaneOffset = nearPlaneOffset[0];

            ImGui.treePop();
        }

        ImGui.end();
    }
}
