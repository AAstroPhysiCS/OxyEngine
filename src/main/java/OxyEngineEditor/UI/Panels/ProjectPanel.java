package OxyEngineEditor.UI.Panels;

import imgui.ImGui;

public class ProjectPanel extends Panel {

    private static ProjectPanel INSTANCE = null;

    public static ProjectPanel getInstance(){
        if(INSTANCE == null) INSTANCE = new ProjectPanel();
        return INSTANCE;
    }

    private ProjectPanel(){

    }

    @Override
    public void preload() {

    }

    @Override
    public void renderPanel() {
        ImGui.begin("Project");

        ImGui.text("Test");

        ImGui.end();
    }
}
