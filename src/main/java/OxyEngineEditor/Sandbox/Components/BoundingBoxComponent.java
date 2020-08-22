package OxyEngineEditor.Sandbox.Components;

import OxyEngineEditor.Sandbox.Scene.Model.OxyModelLoader;
import org.joml.Vector3f;

import java.util.Arrays;

public record BoundingBoxComponent(OxyModelLoader.AssimpOxyMesh assimpMesh, Vector3f pos, Vector3f min, Vector3f max,
                                   Vector3f originPos) implements EntityComponent {

    //0 = x
    //1 = y
    //2 = z

    public static void calcPos(OxyModelLoader.AssimpOxyMesh oxyMesh, float[][] sortedVertices) {
        oxyMesh.pos = new Vector3f(
                (sortedVertices[0][0] + sortedVertices[0][sortedVertices[0].length - 1]) / 2,
                (sortedVertices[1][0] + sortedVertices[1][sortedVertices[1].length - 1]) / 2,
                (sortedVertices[2][0] + sortedVertices[2][sortedVertices[2].length - 1]) / 2
        );
    }

    public static void calcMin(OxyModelLoader.AssimpOxyMesh oxyMesh, float[][] sortedVertices) {
        oxyMesh.min = new Vector3f(
                oxyMesh.pos.x - sortedVertices[0][0],
                oxyMesh.pos.y - sortedVertices[1][0],
                oxyMesh.pos.z - sortedVertices[2][0]
        );
    }

    public static void calcMax(OxyModelLoader.AssimpOxyMesh oxyMesh, float[][] sortedVertices) {
        oxyMesh.max = new Vector3f(
                sortedVertices[0][sortedVertices[0].length - 1] - oxyMesh.pos.x,
                sortedVertices[1][sortedVertices[1].length - 1] - oxyMesh.pos.y,
                sortedVertices[2][sortedVertices[2].length - 1] - oxyMesh.pos.z
        );
    }

    public static float[][] sort(OxyModelLoader.AssimpOxyMesh oxyMesh) {
        float[] allVerticesX = new float[oxyMesh.vertices.size()];
        int ptr = 0;

        for (Vector3f v : oxyMesh.vertices) {
            allVerticesX[ptr++] = v.x;
        }
        Arrays.sort(allVerticesX);

        float[] allVerticesY = new float[oxyMesh.vertices.size()];
        int ptr2 = 0;
        for (Vector3f v : oxyMesh.vertices) {
            allVerticesY[ptr2++] = v.y;
        }
        Arrays.sort(allVerticesY);

        float[] allVerticesZ = new float[oxyMesh.vertices.size()];
        int ptr3 = 0;
        for (Vector3f v : oxyMesh.vertices) {
            allVerticesZ[ptr3++] = v.z;
        }
        Arrays.sort(allVerticesZ);
        return new float[][]{allVerticesX, allVerticesY, allVerticesZ};
    }

    public static float[][] sort(float[] vertices) {
        float[] allVerticesX = new float[vertices.length / 7];
        float[] allVerticesY = new float[vertices.length / 7];
        float[] allVerticesZ = new float[vertices.length / 7];

        int index = 0;
        for (int i = 0; i < vertices.length; ) {
            allVerticesX[index] = vertices[i++];
            allVerticesY[index] = vertices[i++];
            allVerticesZ[index] = vertices[i++];
            i += 5;
            index++;
        }
        Arrays.sort(allVerticesX);
        Arrays.sort(allVerticesY);
        Arrays.sort(allVerticesZ);
        return new float[][]{allVerticesX, allVerticesY, allVerticesZ};
    }
}

