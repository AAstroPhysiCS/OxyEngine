package OxyEngineEditor.Components;

import OxyEngineEditor.UI.Panels.PropertyEntry;
import imgui.ImGui;
import org.joml.Vector3f;

public record EmittingComponent(Vector3f position, Vector3f direction, Vector3f ambient, Vector3f diffuse,
                                Vector3f specular) implements EntityComponent {
    static PropertyEntry node = () -> ImGui.text("THIS IS A EMITTING COMPONENT");
}
