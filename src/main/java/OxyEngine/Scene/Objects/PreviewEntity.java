package OxyEngine.Scene.Objects;

import OxyEngine.Components.TransformComponent;
import OxyEngine.Core.Renderer.Buffer.Platform.BufferProducer;
import OxyEngine.Core.Renderer.Buffer.Platform.FrameBufferSpecification;
import OxyEngine.Core.Renderer.Buffer.Platform.FrameBufferTextureFormat;
import OxyEngine.Core.Renderer.Buffer.Platform.OpenGLFrameBuffer;
import OxyEngine.Scene.Objects.Model.ModelType;
import OxyEngine.Scene.Objects.Model.OxyModel;

import static OxyEngine.Scene.SceneRuntime.ACTIVE_SCENE;
import static OxyEngineEditor.EditorApplication.oxyShader;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
//TODO: Will be used once i implemented shadows
@SuppressWarnings("unused")
public class PreviewEntity {

    public static final OpenGLFrameBuffer previewBuffer = BufferProducer.createFrameBuffer(400, 200,
            OpenGLFrameBuffer.createNewSpec(FrameBufferSpecification.class)
                    .setAttachmentIndex(0)
                    .setFormats(FrameBufferTextureFormat.RGBA8)
                    .setFilter(GL_LINEAR, GL_LINEAR));

    public static final OxyModel previewSphereEntity = ACTIVE_SCENE.createModelEntity(ModelType.Sphere, oxyShader);

    static {
        previewSphereEntity.addComponent(oxyShader);
        previewSphereEntity.get(TransformComponent.class).scale.set(15);
        previewSphereEntity.transformLocally();
        previewSphereEntity.updateVertexData();
    }

    private PreviewEntity(){}
}
