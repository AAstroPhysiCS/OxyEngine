package OxyEngineEditor.Sandbox.OxyObjects;

import OxyEngine.Core.Renderer.Buffer.BufferTemplate;
import OxyEngineEditor.Sandbox.OxyComponents.GameObjectMesh;
import OxyEngineEditor.Sandbox.OxyComponents.TransformComponent;
import OxyEngineEditor.Sandbox.Scene.Scene;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

import static OxyEngine.Core.Renderer.OxyRenderer.MeshSystem.sandBoxMesh;
import static OxyEngine.Core.Renderer.OxyRenderer.MeshSystem.worldGridMesh;
import static OxyEngineEditor.Sandbox.OxyComponents.GameObjectMesh.*;
import static org.lwjgl.opengl.GL11.GL_LINES;

public class WorldGrid {

    private static final List<OxyEntity> worldGrids = new ArrayList<>();

    private final Scene scene;

    public WorldGrid(Scene scene, int size) {
        this.scene = scene;
        worldGridMesh.obj = new GameObjectMesh.GameObjectMeshBuilderImpl()
                .setMode(GL_LINES)
                .setUsage(BufferTemplate.Usage.STATIC)
                .setVerticesBufferAttributes(attributesVert, attributesTXCoords, attributesTXSlots)
                .setGameObjectType(GameObjectType.Grid)
                .create();
        add(size);
        worldGridMesh.obj.add(worldGrids);
    }

    private void add(int size) {
        for (int x = -size; x < size; x++) {
            for (int z = -size; z < size; z++) {
                OxyEntity e = scene.createEntity(new GridTemplate(scene.getRenderer().getShader()));
                e.addComponent(new TransformComponent(new Vector3f(x, 0, z), new Vector3f(0, 0, 0), 20f));
                e.initData(worldGridMesh.obj);
                worldGrids.add(e);
            }
        }
    }

    public void render() {
        sandBoxMesh.obj.getFrameBuffer().bind();
        GridTemplate.getColor().init();
        scene.render(worldGridMesh.obj);
        sandBoxMesh.obj.getFrameBuffer().unbind();
    }
}