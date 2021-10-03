package OxyEngine.Core.Renderer.Mesh;

import OxyEngine.Components.AnimationComponent;
import OxyEngine.Components.BoundingBox;
import OxyEngine.Components.EntityComponent;
import OxyEngine.Components.TransformComponent;
import OxyEngine.Core.Renderer.Pipeline;
import OxyEngine.Core.Renderer.Renderer;
import OxyEngine.Core.Scene.Material;
import OxyEngine.Core.Scene.Scene;
import OxyEngine.System.Disposable;
import OxyEngineEditor.UI.GUINode;
import OxyEngineEditor.UI.Panels.MaterialEditorPanel;
import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiTableFlags;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.type.ImString;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.assimp.*;

import java.io.File;
import java.nio.IntBuffer;
import java.util.*;

import static OxyEngine.Core.Renderer.Mesh.Vertex.MAX_BONE_INFLUENCE;
import static OxyEngine.Core.Scene.SceneRuntime.entityContext;
import static OxyEngine.System.FileSystem.openDialog;
import static OxyEngine.System.OxySystem.logger;
import static OxyEngine.Utils.*;
import static OxyEngineEditor.UI.Panels.ProjectPanel.dirAssetGrey;
import static org.lwjgl.assimp.Assimp.*;
import static org.lwjgl.opengl.GL45.*;

@SuppressWarnings("ConstantConditions")
public final class OpenGLMesh implements Disposable, EntityComponent {

    private VertexBuffer vertexBuffer;
    private IndexBuffer indexBuffer;

    private final Pipeline pipeline;
    private BufferUsage usage;

    private String path;

    private int vao;
    private RenderMode renderMode;

    private final Map<String, AnimationComponent.BoneInfo> boneInfoMap = new HashMap<>();
    private AIScene aiScene;
    private int boneCounter = 0;

    private String meshName;
    private int meshID;

    private final List<Vertex> vertexList = new ArrayList<>();
    private final List<int[]> faces = new ArrayList<>();

    private BoundingBox fullMeshAABB = new BoundingBox(new Vector3f(Float.MAX_VALUE), new Vector3f(Float.MIN_VALUE));

    public static final record Submesh(int assimpMaterialIndex, int baseVertex, int baseIndex, int vertexCount,
                                       int indexCount, TransformComponent t, String name, BoundingBox boundingBox) {
    }

    private List<Submesh> submeshes = new ArrayList<>();
    private List<Material> materials = new ArrayList<>();

    public OpenGLMesh(Pipeline pipeline, String path, BufferUsage usage) {
        this.pipeline = pipeline;
        this.path = path;
        this.renderMode = pipeline.getRenderPass().getRenderMode();
        this.usage = usage;

        importScene();
    }

    public OpenGLMesh(OpenGLMesh other) {
        this.vertexBuffer = VertexBuffer.create(other.vertexBuffer);
        this.indexBuffer = IndexBuffer.create(other.indexBuffer);
        this.pipeline = other.pipeline;
        this.path = other.path;
        this.renderMode = other.renderMode;
        this.submeshes = other.submeshes;
        this.meshID = ++Scene.OBJECT_ID_COUNTER;
        this.fullMeshAABB = new BoundingBox(other.fullMeshAABB);
        this.usage = other.usage;

//        for (OxyMaterial material : other.materials) this.materials.add(OxyMaterial.create(material));
        this.materials = other.materials;

        for (int i = 3; i < this.vertexBuffer.getData().length; i += 23) this.vertexBuffer.getData()[i] = meshID;
    }

    public OpenGLMesh(Pipeline pipeline, BufferUsage usage) {
        this.pipeline = pipeline;
        this.renderMode = pipeline.getRenderPass().getRenderMode();
        this.usage = usage;

        vertexBuffer = VertexBuffer.create(0, usage);
        indexBuffer = IndexBuffer.create(0);
    }

    public OpenGLMesh(Pipeline pipeline, int allocationSize, BufferUsage usage) {
        this.pipeline = pipeline;
        this.renderMode = this.pipeline.getRenderPass().getRenderMode();

        vertexBuffer = VertexBuffer.create(allocationSize, usage);
        indexBuffer = IndexBuffer.create(0);
    }

    public boolean empty() {
        return vao == 0;
    }

    public void updateData(int pos, float[] newData) {
        vertexBuffer.updateData(pos, newData);
    }

    public void setAABB(Vector3f minB, Vector3f maxB) {
        fullMeshAABB.min().set(minB);
        fullMeshAABB.max().set(maxB);
    }

    public String getPath() {
        return path;
    }

    public BoundingBox getAABB() {
        return fullMeshAABB;
    }

    public void addMaterial(Material material) {
        materials.add(material);
    }

    private void importScene() {
        importScene(path);
    }

    public void importScene(String path) {
        this.path = path;

        final int DEFAULT_ASSIMP_FLAG = aiProcess_CalcTangentSpace |
                aiProcess_GenSmoothNormals |
                aiProcess_FixInfacingNormals |
                aiProcess_JoinIdenticalVertices |
                aiProcess_ImproveCacheLocality |
                aiProcess_LimitBoneWeights |
                aiProcess_RemoveRedundantMaterials |
                aiProcess_ValidateDataStructure |
                aiProcess_Triangulate |
                aiProcess_GenUVCoords |
//                aiProcess_PreTransformVertices | (animations won't work, if you enable this)
                aiProcess_SplitLargeMeshes |
                aiProcess_OptimizeMeshes;

        aiScene = aiImportFile(path, DEFAULT_ASSIMP_FLAG);
        if (aiScene == null) {
            logger.warning("Scene could not be imported!");
            return;
        }

        meshName = aiScene.mRootNode().mName().dataString();
        loadMeshes(aiScene);

        for (Submesh submesh : submeshes) {
            BoundingBox aabb = submesh.boundingBox;

            fullMeshAABB.min().x = Math.min(aabb.min().x, fullMeshAABB.min().x);
            fullMeshAABB.min().y = Math.min(aabb.min().y, fullMeshAABB.min().y);
            fullMeshAABB.min().z = Math.min(aabb.min().z, fullMeshAABB.min().z);

            fullMeshAABB.max().x = Math.max(aabb.max().x, fullMeshAABB.max().x);
            fullMeshAABB.max().y = Math.max(aabb.max().y, fullMeshAABB.max().y);
            fullMeshAABB.max().z = Math.max(aabb.max().z, fullMeshAABB.max().z);
        }

        int totalVertexSize = vertexList.size();

        float[] vertices = new float[totalVertexSize * 23];

        List<Integer> indicesArr = new ArrayList<>();
        int vertPtr = 0;

        for (int[] face : faces) {
            for (int i : face) {
                indicesArr.add(i);
            }
        }

        meshID = ++Scene.OBJECT_ID_COUNTER;

        for (Vertex o : vertexList) {
            vertices[vertPtr++] = o.vertices.x;
            vertices[vertPtr++] = o.vertices.y;
            vertices[vertPtr++] = o.vertices.z;
            vertices[vertPtr++] = meshID;

            vertices[vertPtr++] = o.boneIDs[0];
            vertices[vertPtr++] = o.boneIDs[1];
            vertices[vertPtr++] = o.boneIDs[2];
            vertices[vertPtr++] = o.boneIDs[3];

            vertices[vertPtr++] = o.weights[0];
            vertices[vertPtr++] = o.weights[1];
            vertices[vertPtr++] = o.weights[2];
            vertices[vertPtr++] = o.weights[3];

            vertices[vertPtr++] = o.normals.x;
            vertices[vertPtr++] = o.normals.y;
            vertices[vertPtr++] = o.normals.z;

            vertices[vertPtr++] = o.textureCoords.x;
            vertices[vertPtr++] = o.textureCoords.y;

            vertices[vertPtr++] = o.tangents.x;
            vertices[vertPtr++] = o.tangents.y;
            vertices[vertPtr++] = o.tangents.z;

            vertices[vertPtr++] = o.biTangents.x;
            vertices[vertPtr++] = o.biTangents.y;
            vertices[vertPtr++] = o.biTangents.z;
        }

        int[] indices = toPrimitiveInteger(indicesArr);

        vertexBuffer = VertexBuffer.create(vertices, usage);
        indexBuffer = IndexBuffer.create(indices);

        //cleanup
        vertexList.clear();
    }


    private void loadMeshes(AIScene aiScene) {

        int baseVertexCount = 0;
        int baseIndexCount = 0;

        String parentPath = new File(path).getParent();

        int assimpMaterialIndex;

        boolean sceneHasMaterialsAndShouldBeAdded = aiScene.mNumMaterials() > 0 && materials.size() == 0; //assimp materials should be then added when we did not add any via addMaterial method (for Serialization)

        for (int i = 0; i < aiScene.mNumMeshes(); i++) {
            AIMesh mesh = AIMesh.create(aiScene.mMeshes().get(i));
            AIAABB aiaabb = mesh.mAABB();

            assimpMaterialIndex = mesh.mMaterialIndex();

            if (sceneHasMaterialsAndShouldBeAdded) {
                AIMaterial aiMaterial = AIMaterial.create(Objects.requireNonNull(aiScene.mMaterials()).get(assimpMaterialIndex));
                materials.add(Material.create(parentPath, aiMaterial, assimpMaterialIndex));
            }

            Submesh subMesh = new Submesh(assimpMaterialIndex, baseVertexCount, baseIndexCount, mesh.mNumVertices(), mesh.mNumFaces() * 3,
                    new TransformComponent(), mesh.mName().dataString(),
                    new BoundingBox(new Vector3f(aiaabb.mMin().x(), aiaabb.mMin().y(), aiaabb.mMin().z()),
                            new Vector3f(aiaabb.mMax().x(), aiaabb.mMax().y(), aiaabb.mMax().z())));
            baseVertexCount += subMesh.vertexCount;
            baseIndexCount += subMesh.indexCount;

            submeshes.add(subMesh);
        }

        traverseNodes(aiScene.mRootNode(), new Matrix4f().identity());

        for (int i = 0; i < aiScene.mNumMeshes(); i++) {

            AIMesh mesh = AIMesh.create(aiScene.mMeshes().get(i));
            Submesh subMesh = submeshes.get(i);

            AIVector3D.Buffer bufferVert = mesh.mVertices();
            AIVector3D.Buffer bufferNor = mesh.mNormals();
            AIVector3D.Buffer textCoords = mesh.mTextureCoords(0);
            AIVector3D.Buffer tangent = mesh.mTangents();
            AIVector3D.Buffer bitangent = mesh.mBitangents();

            String meshName = mesh.mName().dataString();

            for (int j = 0; j < mesh.mNumVertices(); j++) {
                AIVector3D vertices = bufferVert.get(j);
                Vertex vertex = new Vertex();

                Vector3f v = new Vector3f(vertices.x(), vertices.y(), vertices.z());
                vertex.vertices.set(v);

                Vector4f transformedVertices = new Vector4f(vertex.vertices, 1.0f).mul(subMesh.t.transform);

                BoundingBox aabb = subMesh.boundingBox;
                aabb.min().x = Math.min(transformedVertices.x, aabb.min().x);
                aabb.min().y = Math.min(transformedVertices.y, aabb.min().y);
                aabb.min().z = Math.min(transformedVertices.z, aabb.min().z);

                aabb.max().x = Math.max(transformedVertices.x, aabb.max().x);
                aabb.max().y = Math.max(transformedVertices.y, aabb.max().y);
                aabb.max().z = Math.max(transformedVertices.z, aabb.max().z);

                if (bufferNor != null) {
                    AIVector3D normals3 = bufferNor.get(j);
                    vertex.normals.set(normals3.x(), normals3.y(), normals3.z());
                } else logger.info("Model: " + meshName + " has no normals");

                if (textCoords != null) {
                    AIVector3D textCoord = textCoords.get(j);
                    vertex.textureCoords.set(textCoord.x(), textCoord.y());
                } else logger.info("Model: " + meshName + " has no texture coordinates");

                if (tangent != null) {
                    AIVector3D tangentC = tangent.get(j);
                    vertex.tangents.set(tangentC.x(), tangentC.y(), tangentC.z());
                } else logger.info("Model: " + meshName + " has no tangent");

                if (bitangent != null) {
                    AIVector3D biTangentC = bitangent.get(j);
                    vertex.biTangents.set(biTangentC.x(), biTangentC.y(), biTangentC.z());
                } else logger.info("Model: " + meshName + " has no bitangent");

                vertexList.add(vertex);
            }

            int numFaces = mesh.mNumFaces();
            AIFace.Buffer aiFaces = mesh.mFaces();
            for (int j = 0; j < numFaces; j++) {
                AIFace aiFace = aiFaces.get(j);
                IntBuffer buffer = aiFace.mIndices();
                while (buffer.hasRemaining()) {
                    int f1 = buffer.get();
                    int f2 = buffer.get();
                    int f3 = buffer.get();
                    faces.add(new int[]{f1, f2, f3});
                }
            }

            if (mesh.mBones() != null) {
                for (int boneIndex = 0; boneIndex < mesh.mNumBones(); boneIndex++) {
                    int boneID;

                    AIBone bone = AIBone.create(Objects.requireNonNull(mesh.mBones()).get(boneIndex));
                    String boneName = bone.mName().dataString();

                    if (!boneInfoMap.containsKey(boneName)) {
                        AnimationComponent.BoneInfo newInfo = new AnimationComponent.BoneInfo(boneCounter, convertAIMatrixToJOMLMatrix(bone.mOffsetMatrix()));
                        boneInfoMap.put(boneName, newInfo);
                        boneID = boneCounter;
                        boneCounter++;
                    } else {
                        boneID = boneInfoMap.get(boneName).id();
                    }

                    AIVertexWeight.Buffer weights = bone.mWeights();
                    int numWeights = bone.mNumWeights();

                    for (int weightIndex = 0; weightIndex < numWeights; weightIndex++) {
                        int vertexId = subMesh.baseVertex + weights.get(weightIndex).mVertexId();
                        float weight = weights.get(weightIndex).mWeight();
                        for (int j = 0; j < MAX_BONE_INFLUENCE; j++) {
                            if (vertexList.get(vertexId).boneIDs[j] == -1) {
                                vertexList.get(vertexId).weights[j] = weight;
                                vertexList.get(vertexId).boneIDs[j] = boneID;
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    private void traverseNodes(AINode node, Matrix4f parentTransform) {
        Matrix4f localTransform = convertAIMatrixToJOMLMatrix(node.mTransformation());
        Matrix4f transform = new Matrix4f(parentTransform).mul(localTransform);

        //Submeshes
        for (int i = 0; i < node.mNumMeshes(); i++) {
            Submesh submesh = submeshes.get(node.mMeshes().get(i));
            submesh.t.set(transform);
        }

        //Children of a submesh
        for (int i = 0; i < node.mNumChildren(); i++) {
            AINode childrenNode = AINode.create(node.mChildren().get(i));
            traverseNodes(childrenNode, transform);
        }
    }

    public void loadGL() {
        if (vao == 0) vao = glCreateVertexArrays();
        glBindVertexArray(vao);

        glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer.getBufferId());
        pipeline.processVertexBufferLayout();
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        vertexBuffer.load();
        indexBuffer.load();

        if (vertexBuffer.getUsage() == BufferUsage.DYNAMIC) {
            glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer.getBufferId());
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertexBuffer.getData());
        }
    }

    public void bind() {
        glBindVertexArray(vao);
        if (indexBuffer != null) glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer.bufferId);
    }

    public void unbind() {
        if (indexBuffer != null) glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        Renderer.Stats.totalVertexCount += vertexBuffer.getData().length;
        Renderer.Stats.totalIndicesCount += indexBuffer != null && indexBuffer.getData() != null ? indexBuffer.getData().length : 0;
    }

    public void pushVertices(float[] vertices) {
        vertexBuffer.addToBuffer(vertices);
    }

    public void pushIndices(int[] indices) {
        indexBuffer.addToBuffer(indices);
    }

    public void pushVertices(List<Float> vertices) {
        vertexBuffer.addToBuffer(toPrimitiveFloat(vertices));
    }

    public void pushIndices(List<Integer> indices) {
        indexBuffer.addToBuffer(toPrimitiveInteger(indices));
    }

    public float[] getVertices() {
        return vertexBuffer.getData();
    }

    public Map<String, AnimationComponent.BoneInfo> getBoneInfoMap() {
        return boneInfoMap;
    }

    public AIScene getAIScene() {
        return aiScene;
    }

    public List<Submesh> getSubmeshes() {
        return submeshes;
    }

    public List<Material> getMaterials() {
        return materials;
    }

    public Pipeline getPipeline() {
        return pipeline;
    }

    public Optional<Material> getMaterial(int index) {
        for (Material m : materials) {
            if (m.getAssimpMaterialIndex() == index) {
                return Optional.of(m);
            }
        }
        return Optional.empty();
    }

    public int[] getIndices() {
        return indexBuffer.getData();
    }

    public int getMeshID() {
        return meshID;
    }

    public RenderMode getRenderMode() {
        return renderMode;
    }

    public void setRenderMode(RenderMode mode) {
        this.renderMode = mode;
    }

    public String getMeshName() {
        return meshName;
    }

    @Override
    public void dispose() {
        vertexBuffer.dispose();
        indexBuffer.dispose();
        glDeleteVertexArrays(vao);
        vao = 0;

        for (Material material : materials)
            material.dispose();
    }

    private static ImString meshPath = new ImString("");

    public static final GUINode guiNode = () -> {
        if (entityContext == null) return;

        {
            if (ImGui.collapsingHeader("Mesh Renderer", ImGuiTreeNodeFlags.DefaultOpen)) {
                if (entityContext.has(OpenGLMesh.class))
                    meshPath = new ImString(entityContext.get(OpenGLMesh.class).getPath());
                else meshPath.set("");

                ImGui.setNextItemOpen(true);
                ImGui.columns(2, "myColumns");
                ImGui.setColumnOffset(0, -120f);
                ImGui.alignTextToFramePadding();
                ImGui.text("Mesh:");
                ImGui.nextColumn();
                ImGui.pushItemWidth(ImGui.getContentRegionAvailX() - 30f);
                ImGui.inputText("##hidelabel", meshPath, ImGuiInputTextFlags.ReadOnly);
                ImGui.popItemWidth();
                ImGui.sameLine();
                if (ImGui.imageButton(dirAssetGrey.getTextureId(), 20, 20, 0, 1, 1, 0, 0)) {
                    String path = openDialog("", null);
                    if (path != null) {
                        if (entityContext != null) {
                            Pipeline pipeline = Renderer.getGeometryPipeline();
                            BufferUsage usage = BufferUsage.STATIC;
                            if (entityContext.has(OpenGLMesh.class)) {
                                OpenGLMesh mesh = entityContext.get(OpenGLMesh.class);
                                mesh.dispose();
                                Renderer.removeFromCommand(mesh);
                                pipeline = mesh.pipeline;
                                usage = mesh.vertexBuffer.usage;
                            }
                            entityContext.addComponent(new OpenGLMesh(pipeline, path, usage));
                            Renderer.submitMesh(entityContext.get(OpenGLMesh.class), entityContext.get(TransformComponent.class), entityContext.get(AnimationComponent.class));
                            meshPath = new ImString(path);
                        }
                    }
                }
                ImGui.columns(1);
                ImGui.spacing();
            }

            if (ImGui.collapsingHeader("Materials", ImGuiTreeNodeFlags.DefaultOpen) && entityContext.has(OpenGLMesh.class)) {

                List<Material> materialList = entityContext.get(OpenGLMesh.class).getMaterials();

                if (ImGui.beginTable("materialTable", 2, ImGuiTableFlags.BordersH | ImGuiTableFlags.BordersInner | ImGuiTableFlags.Resizable)) {

                    for (int i = 0; i < materialList.size(); i++) {
                        ImGui.tableNextColumn();

                        Material m = materialList.get(i);
                        ImGui.pushID(i);

                        ImGui.setCursorPosX(25f);
                        ImGui.setCursorPosY(ImGui.getCursorPosY() + 15f);
                        ImGui.text("Material " + i);

                        ImGui.tableNextColumn();
                        //TODO: Delete this and make a sphere that gets rendered on to a fb and show that fb
                        int id = -1;
                        if (m.albedoTexture != null) id = m.albedoTexture.getTextureId();
                        if (ImGui.imageButton(id, 65, 60, 0, 1, 1, 0, 1)) {
                            //open materialeditor
                            var INSTANCE = MaterialEditorPanel.getInstance();
                            INSTANCE.pushMaterialToShow(m);
                            INSTANCE.setShow(true);
                        }
                        ImGui.sameLine();
                        if (ImGui.beginCombo("##hideLabelMaterials", m.name)) {
                            //all elements
                            ImGui.endCombo();
                        }
                        ImGui.popID();

                        if (i != materialList.size() - 1) ImGui.tableNextRow();
                    }

                    ImGui.endTable();
                }
            }
        }
    };
}
