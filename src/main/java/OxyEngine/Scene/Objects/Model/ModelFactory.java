package OxyEngine.Scene.Objects.Model;

import OxyEngine.Core.Renderer.Mesh.OxyVertex;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

import static OxyEngine.Globals.toPrimitiveInteger;

public class ModelFactory {

    private final List<int[]> faces;
    private final List<OxyVertex> vertexList;

    public ModelFactory(List<OxyVertex> vertexList, List<int[]> faces) {
        this.vertexList = vertexList;
        this.faces = faces;
    }

    public void constructData(OxyModel e) {
        e.vertices = new float[vertexList.size() * 12];
        e.normals = new float[vertexList.size() * 3];
        e.tcs = new float[vertexList.size() * 2];
        e.tangents = new float[vertexList.size() * 3];
        e.biTangents = new float[vertexList.size() * 3];
        List<Integer> indicesArr = new ArrayList<>();

        int vertPtr = 0;
        int nPtr = 0;
        int tcsPtr = 0;
        int tangentPtr = 0;
        int biTangentPtr = 0;
        for (OxyVertex o : vertexList) {
            e.vertices[vertPtr++] = o.vertices.x;
            e.vertices[vertPtr++] = o.vertices.y;
            e.vertices[vertPtr++] = o.vertices.z;
            e.vertices[vertPtr++] = e.getObjectId();

            e.vertices[vertPtr++] = o.m_BoneIDs[0];
            e.vertices[vertPtr++] = o.m_BoneIDs[1];
            e.vertices[vertPtr++] = o.m_BoneIDs[2];
            e.vertices[vertPtr++] = o.m_BoneIDs[3];

            e.vertices[vertPtr++] = o.m_Weights[0];
            e.vertices[vertPtr++] = o.m_Weights[1];
            e.vertices[vertPtr++] = o.m_Weights[2];
            e.vertices[vertPtr++] = o.m_Weights[3];

            Vector3f normals = o.normals;
            e.normals[nPtr++] = normals.x;
            e.normals[nPtr++] = normals.y;
            e.normals[nPtr++] = normals.z;

            Vector2f textureCoords = o.textureCoords;
            e.tcs[tcsPtr++] = textureCoords.x;
            e.tcs[tcsPtr++] = textureCoords.y;

            Vector3f tangents = o.tangents;
            e.tangents[tangentPtr++] = tangents.x;
            e.tangents[tangentPtr++] = tangents.y;
            e.tangents[tangentPtr++] = tangents.z;

            Vector3f biTangents = o.biTangents;
            e.biTangents[biTangentPtr++] = biTangents.x;
            e.biTangents[biTangentPtr++] = biTangents.y;
            e.biTangents[biTangentPtr++] = biTangents.z;
        }

        for (int[] face : faces) {
            for (int i : face) {
                indicesArr.add(i);
            }
        }
        e.indices = toPrimitiveInteger(indicesArr);
    }
}