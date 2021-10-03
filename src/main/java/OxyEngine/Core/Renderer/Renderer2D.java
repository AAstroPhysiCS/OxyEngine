package OxyEngine.Core.Renderer;

import OxyEngine.Components.BoundingBox;
import OxyEngine.Core.Renderer.Mesh.BufferUsage;
import OxyEngine.Core.Renderer.Mesh.OpenGLMesh;
import OxyEngine.Core.Renderer.Texture.Color;
import OxyEngine.Core.Scene.Material;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static OxyEngine.Core.Renderer.Renderer.lineRenderPass;

public final class Renderer2D {

    private static Pipeline linePipeline;
    private static Material lineMaterial;
    private static OpenGLMesh lineMesh;

    private static final int MAX_VERTICES = 72;

    private Renderer2D() {
    }

    static void initPipelines() {

        linePipeline = Pipeline.createNewPipeline(Pipeline.createNewSpecification()
                .setDebugName("Line Pipeline")
                .setRenderPass(lineRenderPass)
                .setVertexBufferLayout(Pipeline.createNewVertexBufferLayout()
                        .addShaderType(Shader.VERTICES, ShaderType.Float3)
                ));

        lineMesh = new OpenGLMesh(linePipeline, MAX_VERTICES, BufferUsage.DYNAMIC);
        lineMesh.loadGL();

        lineMaterial = Material.create(0, Renderer.getShader("OxyLine"));
        lineMaterial.albedoColor = new Color(0.5f, 1.0f, 0.7f, 0.5f);
    }

    public static void submitAABB(BoundingBox aabb, Matrix4f transform) {
        Vector4f[] corners = new Vector4f[]{
                new Vector4f(aabb.min().x, aabb.min().y, aabb.max().z, 1.0f).mul(transform),
                new Vector4f(aabb.min().x, aabb.max().y, aabb.max().z, 1.0f).mul(transform),
                new Vector4f(aabb.max().x, aabb.max().y, aabb.max().z, 1.0f).mul(transform),
                new Vector4f(aabb.max().x, aabb.min().y, aabb.max().z, 1.0f).mul(transform),

                new Vector4f(aabb.min().x, aabb.min().y, aabb.min().z, 1.0f).mul(transform),
                new Vector4f(aabb.min().x, aabb.max().y, aabb.min().z, 1.0f).mul(transform),
                new Vector4f(aabb.max().x, aabb.max().y, aabb.min().z, 1.0f).mul(transform),
                new Vector4f(aabb.max().x, aabb.min().y, aabb.min().z, 1.0f).mul(transform)
        };

        //DO LINES
        for (int i = 0; i < 4; i++) {
            Vector4f p1 = corners[i];
            Vector4f p2 = corners[(i + 1) % 4];
            submitLine(new Vector3f(p1.x, p1.y, p1.z), new Vector3f(p2.x, p2.y, p2.z));
        }
        for (int i = 0; i < 4; i++) {
            Vector4f p1 = corners[i + 4];
            Vector4f p2 = corners[((i + 1) % 4) + 4];
            submitLine(new Vector3f(p1.x, p1.y, p1.z), new Vector3f(p2.x, p2.y, p2.z));
        }
        for (int i = 0; i < 4; i++) {
            Vector4f p1 = corners[i];
            Vector4f p2 = corners[i + 4];
            submitLine(new Vector3f(p1.x, p1.y, p1.z), new Vector3f(p2.x, p2.y, p2.z));
        }

    }

    private static int vertexPos;

    public static void submitLine(Vector3f start, Vector3f end) {
        lineMesh.updateData(vertexPos, new float[]{start.x, start.y, start.z});
        vertexPos += 3;
        lineMesh.updateData(vertexPos, new float[]{end.x, end.y, end.z});
        vertexPos += 3;
    }

    static void beginScene() {
        vertexPos = 0;
        lineMesh.updateData(0, new float[MAX_VERTICES]);
    }

    static void endScene() {
        RenderCommand.disableDepth();
        if (!lineMesh.empty()) {
            //Bounding Box
            RenderPass linePass = linePipeline.getRenderPass();
            Renderer.beginRenderPass(linePass);
            lineMesh.bind();
            lineMaterial.bindMaterial();
            RenderCommand.drawArrays(linePass.getRenderMode().getModeID(), 0, lineMesh.getVertices().length);
            lineMaterial.unbindMaterial();
            lineMesh.unbind();
            Renderer.endRenderPass();
        }
        RenderCommand.enableDepth();
    }
}
