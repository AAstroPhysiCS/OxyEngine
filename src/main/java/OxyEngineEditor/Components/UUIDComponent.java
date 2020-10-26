package OxyEngineEditor.Components;

import java.util.UUID;

public record UUIDComponent(UUID id) implements EntityComponent {
    public String getUUIDString() {
        return id.toString();
    }
}
