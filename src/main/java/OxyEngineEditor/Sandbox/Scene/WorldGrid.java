package OxyEngineEditor.Sandbox.Scene;

import OxyEngine.Core.Renderer.Buffer.BufferTemplate;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Core.Renderer.Texture.OxyColor;
import OxyEngineEditor.Sandbox.OxyComponents.TransformComponent;
import OxyEngineEditor.Sandbox.Scene.InternObjects.GridFactory;
import OxyEngineEditor.Sandbox.Scene.InternObjects.OxyInternObject;
import org.joml.Vector3f;

import static OxyEngine.Core.Renderer.OxyRenderer.MeshSystem.worldGridMesh;
import static OxyEngineEditor.Sandbox.OxyComponents.InternObjectMesh.*;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_LINES;

public class WorldGrid {

    private static final BufferTemplate.Attributes attributesVert = new BufferTemplate.Attributes(OxyShader.VERTICES, 3, GL_FLOAT, false, 10 * Float.BYTES, 0);
    private static final BufferTemplate.Attributes attributesTXCoords = new BufferTemplate.Attributes(OxyShader.TEXTURE_COORDS, 2, GL_FLOAT, false, 10 * Float.BYTES, 3 * Float.BYTES);
    private static final BufferTemplate.Attributes attributesTXSlot = new BufferTemplate.Attributes(OxyShader.TEXTURE_SLOT, 1, GL_FLOAT, false, 10 * Float.BYTES, 5 * Float.BYTES);
    private static final BufferTemplate.Attributes attributesColors = new BufferTemplate.Attributes(OxyShader.COLOR, 4, GL_FLOAT, false, 10 * Float.BYTES, 6 * Float.BYTES);

    private final Scene scene;

    public WorldGrid(Scene scene, int size, OxyShader shader) {
        this.scene = scene;
        worldGridMesh.obj = new InternMeshBuilderImpl()
                .setShader(shader)
                .setMode(GL_LINES)
                .setUsage(BufferTemplate.Usage.STATIC)
                .setVerticesBufferAttributes(attributesVert, attributesTXCoords, attributesTXSlot, attributesColors)
                .create();
        add(size);
        worldGridMesh.obj.initList();
    }

    private void add(int size) {
        for (int x = -size; x < size; x++) {
            for (int z = -size; z < size; z++) {
                OxyInternObject e = scene.createInternObjectEntity();
                e.addComponent(worldGridMesh.obj, new GridFactory(), new OxyColor(1.0f, 1.0f, 1.0f, 0.2f), new TransformComponent(new Vector3f(x, 0, z), new Vector3f(0, 0, 0), 20f));
                e.initData();
            }
        }
    }
}