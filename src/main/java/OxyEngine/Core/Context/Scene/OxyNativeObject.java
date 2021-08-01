package OxyEngine.Core.Context.Scene;

import OxyEngine.Components.RenderableComponent;
import OxyEngine.Components.RenderingMode;
import OxyEngine.Components.TransformComponent;
import OxyEngine.Core.Context.Renderer.Mesh.OpenGLMesh;
import org.joml.Vector4f;

import static OxyEngine.OxyUtils.copy;

public class OxyNativeObject extends OxyEntity {

    OxyNativeObject(Scene scene) {
        super(scene);
    }

    public void pushVertexData(float[] vertices) {
        if(this.vertices.length == 0){
            this.vertices = vertices;
            return;
        }
        for(int i = 0; i < this.vertices.length; i++){
            this.vertices = copy(this.vertices, vertices);
        }
    }

    @Override
    public OxyEntity copyMe() {
        OxyNativeObject e = new OxyNativeObject(scene);
        e.addToScene();
        e.addComponent(new TransformComponent(), new RenderableComponent(RenderingMode.Normal));
        return e;
    }

    @Override
    public void updateData() {
        transformLocally();
        TransformComponent c = get(TransformComponent.class);
        int vertPtr = 0;
        for (int i = 0; i < vertices.length; i++) {
            Vector4f transformed = new Vector4f(vertices[i++], vertices[i++], vertices[i++], 1.0f).mul(c.transform);
            vertices[vertPtr++] = transformed.x;
            vertices[vertPtr++] = transformed.y;
            vertices[vertPtr++] = transformed.z;
        }
        if (has(OpenGLMesh.class)) get(OpenGLMesh.class).updateSingleEntityData(0, vertices);
    }
}