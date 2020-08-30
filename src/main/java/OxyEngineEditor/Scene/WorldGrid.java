package OxyEngineEditor.Scene;

import OxyEngine.Core.Renderer.Buffer.BufferTemplate;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngineEditor.Components.NativeObjectMesh;
import OxyEngineEditor.Components.TransformComponent;
import OxyEngineEditor.Scene.Model.OxyMaterial;
import OxyEngineEditor.Scene.NativeObjects.GridFactory;
import OxyEngineEditor.Scene.NativeObjects.OxyNativeObject;
import org.joml.Vector3f;
import org.joml.Vector4f;

import static OxyEngineEditor.Components.NativeObjectMesh.*;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_LINES;

public class WorldGrid {

    private static final BufferTemplate.Attributes attributesVert = new BufferTemplate.Attributes(OxyShader.VERTICES, 3, GL_FLOAT, false, 10 * Float.BYTES, 0);
    private static final BufferTemplate.Attributes attributesTXCoords = new BufferTemplate.Attributes(OxyShader.TEXTURE_COORDS, 2, GL_FLOAT, false, 10 * Float.BYTES, 3 * Float.BYTES);
    private static final BufferTemplate.Attributes attributesTXSlot = new BufferTemplate.Attributes(OxyShader.TEXTURE_SLOT, 1, GL_FLOAT, false, 10 * Float.BYTES, 5 * Float.BYTES);
    private static final BufferTemplate.Attributes attributesColors = new BufferTemplate.Attributes(OxyShader.COLOR, 4, GL_FLOAT, false, 10 * Float.BYTES, 6 * Float.BYTES);

    private final Scene scene;
    private final NativeObjectMesh worldGridMesh;

    public WorldGrid(Scene scene, int size, OxyShader shader) {
        this.scene = scene;
        worldGridMesh = new NativeMeshBuilderImpl()
                .setShader(shader)
                .setMode(GL_LINES)
                .setUsage(BufferTemplate.Usage.STATIC)
                .setVerticesBufferAttributes(attributesVert, attributesTXCoords, attributesTXSlot, attributesColors)
                .create();
        add(size);
        worldGridMesh.initList();
    }

    private void add(int size) {
        for (int x = -size; x < size; x++) {
            for (int z = -size; z < size; z++) {
                OxyNativeObject e = scene.createNativeObjectEntity();
                e.addComponent(worldGridMesh, new GridFactory(), new OxyMaterial(new Vector4f(1.0f, 1.0f, 1.0f, 0.2f)), new TransformComponent(new Vector3f(x, 0, z), new Vector3f(0, 0, 0), 20f));
                e.initData();
            }
        }
    }
}