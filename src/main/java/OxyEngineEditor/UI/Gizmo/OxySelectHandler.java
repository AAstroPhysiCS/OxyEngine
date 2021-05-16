package OxyEngineEditor.UI.Gizmo;

import OxyEngine.Components.*;
import OxyEngine.Core.Renderer.Buffer.FrameBuffer;
import OxyEngine.Core.Renderer.Buffer.Platform.FrameBufferSpecification;
import OxyEngine.Core.Renderer.Buffer.Platform.FrameBufferTextureFormat;
import OxyEngine.Core.Renderer.Buffer.Platform.OpenGLFrameBuffer;
import OxyEngine.Core.Renderer.Mesh.ModelMeshOpenGL;
import OxyEngine.Core.Renderer.OxyRenderPass;
import OxyEngine.Core.Renderer.OxyRenderer;
import OxyEngine.Core.Renderer.Pipeline.OxyPipeline;
import OxyEngine.Core.Renderer.Pipeline.OxyShader;
import OxyEngine.Scene.Objects.Model.OxyMaterial;
import OxyEngine.Scene.OxyEntity;
import OxyEngine.Scene.SceneRenderer;
import OxyEngineEditor.UI.Panels.ScenePanel;
import org.joml.Matrix4f;
import org.joml.Vector2f;

import java.util.List;
import java.util.Set;

import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL44.glClearTexImage;

public class OxySelectHandler {

    public static OxyEntity entityContext;
    public static OxyMaterial materialContext;

    private static OpenGLFrameBuffer pickingFrameBuffer;
    private static OxyRenderPass pickingRenderPass;

    public static void init(int width, int height){
        pickingFrameBuffer = FrameBuffer.create(width, height,
                OpenGLFrameBuffer.createNewSpec(FrameBufferSpecification.class)
                        .setTextureCount(1)
                        .setAttachmentIndex(0)
                        .setFormats(FrameBufferTextureFormat.RGBA8)
                        .setFilter(GL_LINEAR, GL_LINEAR),
                OpenGLFrameBuffer.createNewSpec(FrameBufferSpecification.class)
                        .setTextureCount(1)
                        .setAttachmentIndex(1)
                        .setFormats(FrameBufferTextureFormat.R32I)
                        .setFilter(GL_NEAREST, GL_NEAREST),
                OpenGLFrameBuffer.createNewSpec(FrameBufferSpecification.class)
                        .setTextureCount(1)
                        .setStorage(true, 1));
        pickingFrameBuffer.drawBuffers(0, 1);
        pickingRenderPass = OxyRenderPass.createBuilder(pickingFrameBuffer).create();
    }

    public static void resizePickingBuffer(float width, float height){
        pickingFrameBuffer.resize(width, height);
    }

    public static void startPicking() {
        Set<OxyEntity> allModelEntities = SceneRenderer.getInstance().allModelEntities;

        if (allModelEntities.size() == 0) return;
        glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        //ID RENDER PASS
        if (pickingFrameBuffer.getBufferId() != 0) {
            int[] clearValue = {-1};

            pickingFrameBuffer.flush();

            pickingRenderPass.beginRenderPass();

            OxyPipeline geometryPipeline = SceneRenderer.getInstance().getGeometryPipeline();
            OxyShader pbrShader = geometryPipeline.getShader();

            glClearTexImage(pickingFrameBuffer.getColorAttachmentTexture(1)[0], 0, pickingFrameBuffer.getTextureFormat(1).getStorageFormat(), GL_INT, clearValue);

            for (OxyEntity e : allModelEntities) {
                if (!e.has(SelectedComponent.class)) continue;
                RenderableComponent renderableComponent = e.get(RenderableComponent.class);
                if (renderableComponent.mode != RenderingMode.Normal) continue;
                pbrShader.begin();
                //ANIMATION UPDATE
                pbrShader.setUniform1i("animatedModel", 0);
                if (e.has(AnimationComponent.class)) {
                    pbrShader.setUniform1i("animatedModel", 1);
                    AnimationComponent animComp = e.get(AnimationComponent.class);
                    List<Matrix4f> matrix4fList = animComp.getFinalBoneMatrices();
                    for (int j = 0; j < matrix4fList.size(); j++) {
                        pbrShader.setUniformMatrix4fv("finalBonesMatrices[" + j + "]", matrix4fList.get(j), false);
                    }
                }
                pbrShader.setUniformMatrix4fv("model", e.get(TransformComponent.class).transform, false);
                OxyRenderer.renderMesh(geometryPipeline, e.get(ModelMeshOpenGL.class));
                pbrShader.end();
            }
        }
        int id = getEntityID();
//        System.out.println("ENTITY ID: " + id);
        if (id == -1) {
            //IF NO ENTITY IS BEING SELECTED (SELECTING THE "AIR") => ENTITY == NULL
            //if (entityContext != null) entityContext.get(SelectedComponent.class).selected = false;
            //entityContext = null;
            //OxySelectHandler.materialContext = null;
        } else {
            for (OxyEntity e : allModelEntities) {
                if (e.getObjectId() == id) {
                    if (entityContext != null) entityContext.get(SelectedComponent.class).selected = false;
                    entityContext = e;
                    entityContext.get(SelectedComponent.class).selected = true;
                    break;
                }
            }
        }

        pickingRenderPass.endRenderPass();
    }

    private static int getEntityID() {
        Vector2f mousePos = new Vector2f(
                ScenePanel.mousePos.x - ScenePanel.windowPos.x - ScenePanel.offset.x,
                ScenePanel.mousePos.y - ScenePanel.windowPos.y - ScenePanel.offset.y);
        mousePos.y = SceneRenderer.getInstance().getFrameBuffer().getHeight() - mousePos.y;
        glReadBuffer(GL_COLOR_ATTACHMENT1);
        int[] entityID = new int[1];
        glReadPixels((int) mousePos.x, (int) mousePos.y, 1, 1, GL_RED_INTEGER, GL_INT, entityID);
        return entityID[0];
    }
}