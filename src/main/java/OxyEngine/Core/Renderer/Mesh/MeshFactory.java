package OxyEngine.Core.Renderer.Mesh;

import OxyEngine.Core.Renderer.Renderer;
import OxyEngine.Core.Scene.Material;
import OxyEngine.TargetPlatform;

import java.util.ArrayList;
import java.util.List;

public final class MeshFactory {

    private MeshFactory() {
    }

    private static void calculateRing(int segments, float radius, float y, float dy, float height, float actualRadius, List<Float> vertices) {
        float segIncr = 1.0f / (float) (segments - 1);
        for (int s = 0; s < segments; s++) {
            float x = (float) (Math.cos(Math.PI * 2 * s * segIncr) * radius);
            float z = (float) (Math.sin(Math.PI * 2 * s * segIncr) * radius);
            vertices.add(actualRadius * x);
            vertices.add(actualRadius * y + height * dy);
            vertices.add(actualRadius * z);
        }
    }

    public static OpenGLMesh createCapsuleMesh(float radius, float height) {

        int subdivisionsHeight = 8;
        int ringsBody = subdivisionsHeight + 1;
        int ringsTotal = subdivisionsHeight + ringsBody;
        int numSegments = 12;
        float radiusModifier = 0.021f;

        List<Float> vertices = new ArrayList<>(numSegments * ringsTotal * 3);
        List<Integer> indices = new ArrayList<>((numSegments - 1) * (ringsTotal - 1) * 2 * 3);

        float bodyIncr = 1.0f / ringsBody - 1;
        float ringIncr = 1.0f / subdivisionsHeight - 1;

        for (int r = 0; r < subdivisionsHeight / 2; r++)
            calculateRing(numSegments, (float) Math.sin(Math.PI * r * ringIncr), (float) Math.sin(Math.PI * (r * ringIncr - 0.5f)), -0.5f, height, radius + radiusModifier, vertices);

        for (int r = 0; r < ringsBody; r++)
            calculateRing(numSegments, 1.0f, 0.0f, r * bodyIncr - 0.5f, height, radius + radiusModifier, vertices);

        for (int r = subdivisionsHeight / 2; r < subdivisionsHeight; r++)
            calculateRing(numSegments, (float) Math.sin(Math.PI * r * ringIncr), (float) Math.sin(Math.PI * (r * ringIncr - 0.5f)), 0.5f, height, radius + radiusModifier, vertices);

        for (int r = 0; r < ringsTotal - 1; r++) {
            for (int s = 0; s < numSegments - 1; s++) {
                indices.add(r * numSegments + s + 1);
                indices.add(r * numSegments + s);
                indices.add((r + 1) * numSegments + s + 1);

                indices.add((r + 1) * numSegments + s);
                indices.add((r + 1) * numSegments + s + 1);
                indices.add(r * numSegments + s);
            }
        }

        if (Renderer.getCurrentTargetPlatform() == TargetPlatform.OpenGL) {
            OpenGLMesh mesh = new OpenGLMesh(Renderer.getColliderPipeline(), BufferUsage.STATIC);
            mesh.addMaterial(Material.create(0));
            mesh.pushVertices(vertices);
            mesh.pushIndices(indices);
            mesh.loadGL();
            return mesh;
        }

        return null;
    }

}
