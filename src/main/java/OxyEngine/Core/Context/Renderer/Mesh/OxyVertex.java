package OxyEngine.Core.Context.Renderer.Mesh;

import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.Arrays;

public final class OxyVertex {

    public static final int MAX_BONES = 100;
    public static final int MAX_BONE_INFLUENCE = 4;

    public final Vector3f vertices, normals, tangents, biTangents;
    public final Vector2f textureCoords;
    public final int[] m_BoneIDs;
    public final float[] m_Weights;

    public OxyVertex(Vector3f vertices, Vector2f textureCoords, Vector3f normals, Vector3f tangents, Vector3f biTangents, int[] m_BoneIDs, float[] m_Weights){
        this.vertices = vertices;
        this.textureCoords = textureCoords;
        this.normals = normals;
        this.tangents = tangents;
        this.biTangents = biTangents;
        this.m_Weights = m_Weights;
        this.m_BoneIDs = m_BoneIDs;
        Arrays.fill(m_BoneIDs, -1);
    }

    public OxyVertex(Vector3f position, Vector2f textureCoords, Vector3f normals, Vector3f tangents, Vector3f biTangents){
        this(position, textureCoords, normals, tangents, biTangents, new int[MAX_BONE_INFLUENCE], new float[MAX_BONE_INFLUENCE]);
    }

    public OxyVertex(){
        this(new Vector3f(), new Vector2f(), new Vector3f(), new Vector3f(), new Vector3f(), new int[MAX_BONE_INFLUENCE], new float[MAX_BONE_INFLUENCE]);
    }
}
