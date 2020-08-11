package OxyEngineEditor.Sandbox.OxyComponents;

import org.joml.Vector3f;

public record BoundingBoxComponent(Vector3f pos, Vector3f min, Vector3f max) implements EntityComponent {
}

