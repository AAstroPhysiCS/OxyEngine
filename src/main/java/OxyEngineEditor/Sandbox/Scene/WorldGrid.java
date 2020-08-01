package OxyEngineEditor.Sandbox.Scene;

import OxyEngine.Core.Renderer.Buffer.BufferTemplate;
import OxyEngineEditor.Sandbox.OxyComponents.GameObjectMesh;
import OxyEngineEditor.Sandbox.OxyComponents.TransformComponent;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

import static OxyEngine.Core.Renderer.OxyRenderer.MeshSystem.worldGridMesh;
import static OxyEngineEditor.Sandbox.OxyComponents.GameObjectMesh.*;
import static OxyEngineEditor.Sandbox.Sandbox3D.camera;
import static org.lwjgl.opengl.GL11.GL_LINES;

public class WorldGrid {

    private static final List<OxyGameObject> worldGrids = new ArrayList<>();

    private final Scene scene;

    public WorldGrid(Scene scene, int size) {
        this.scene = scene;
        worldGridMesh.obj = new GameObjectMesh.GameObjectMeshBuilderImpl()
                .setMode(GL_LINES)
                .setUsage(BufferTemplate.Usage.STATIC)
                .setVerticesBufferAttributes(attributesVert, attributesTXCoords, attributesTXSlots)
                .create();
        add(size);
        worldGridMesh.obj.add(worldGrids);
    }

    private void add(int size) {
        for (int x = -size; x < size; x++) {
            for (int z = -size; z < size; z++) {
                OxyGameObject e = scene.createGameObjectEntity();
                e.addComponent(camera, worldGridMesh.obj, new GridTemplate(scene.getRenderer().getShader()), new TransformComponent(new Vector3f(x, 0, z), new Vector3f(0, 0, 0), 20f));
                e.initData();
                worldGrids.add(e);
            }
        }
    }

    public void render() {
//        sandBoxMesh.obj.getFrameBuffer().bind();
        GridTemplate.getColor().init();
//        scene.render(worldGridMesh.obj);
//        sandBoxMesh.obj.getFrameBuffer().unbind();
    }
}