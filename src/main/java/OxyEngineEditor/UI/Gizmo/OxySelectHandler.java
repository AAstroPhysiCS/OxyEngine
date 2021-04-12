package OxyEngineEditor.UI.Gizmo;

import OxyEngine.Components.*;
import OxyEngine.Core.Layers.SceneLayer;
import OxyEngine.Core.Renderer.Buffer.Platform.OpenGLFrameBuffer;
import OxyEngine.Core.Renderer.Mesh.ModelMeshOpenGL;
import OxyEngine.Core.Renderer.OxyRenderer;
import OxyEngine.Core.Renderer.Pipeline.OxyPipeline;
import OxyEngine.Scene.Objects.Model.OxyMaterial;
import OxyEngine.Scene.OxyEntity;
import OxyEngineEditor.UI.Panels.ScenePanel;
import org.joml.Matrix4f;
import org.joml.Vector2f;

import java.util.List;
import java.util.Set;

import static OxyEngine.Core.Renderer.Context.OxyRenderCommand.rendererAPI;
import static OxyEngine.Scene.SceneRuntime.ACTIVE_SCENE;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL44.glClearTexImage;

public class OxySelectHandler {

    public static OxyEntity entityContext;
    public static OxyMaterial materialContext;

    public static void startPicking() {
        Set<OxyEntity> allModelEntities = SceneLayer.getInstance().allModelEntities;

        if (allModelEntities.size() == 0) return;
        glBindFramebuffer(GL_READ_FRAMEBUFFER, 0);
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, 0);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        OpenGLFrameBuffer pickingBuffer = ACTIVE_SCENE.getPickingBuffer();

        //ID RENDER PASS
        if (pickingBuffer.getBufferId() != 0) {
            int[] clearValue = {-1};
            pickingBuffer.bind();
            rendererAPI.clearBuffer();
            rendererAPI.clearColor(32, 32, 32, 1.0f);
            OxyPipeline geometryPipeline = SceneLayer.getInstance().getGeometryPipeline();
            glClearTexImage(pickingBuffer.getColorAttachmentTexture(1)[0], 0, pickingBuffer.getTextureFormat(1).getStorageFormat(), GL_INT, clearValue);
            for (OxyEntity e : allModelEntities) {
                if (!e.has(SelectedComponent.class)) continue;
                RenderableComponent renderableComponent = e.get(RenderableComponent.class);
                if (renderableComponent.mode != RenderingMode.Normal) continue;
                geometryPipeline.begin();
                //ANIMATION UPDATE
                geometryPipeline.setUniform1i("animatedModel", 0);
                if (e.has(AnimationComponent.class)) {
                    geometryPipeline.setUniform1i("animatedModel", 1);
                    AnimationComponent animComp = e.get(AnimationComponent.class);
                    List<Matrix4f> matrix4fList = animComp.getFinalBoneMatrices();
                    for (int j = 0; j < matrix4fList.size(); j++) {
                        geometryPipeline.setUniformMatrix4fv("finalBonesMatrices[" + j + "]", matrix4fList.get(j), false);
                    }
                }
                geometryPipeline.setUniformMatrix4fv("model", e.get(TransformComponent.class).transform, false);
                OxyRenderer.renderMesh(geometryPipeline, e.get(ModelMeshOpenGL.class));
                geometryPipeline.end();
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
        pickingBuffer.unbind();
    }

    private static int getEntityID() {
        Vector2f mousePos = new Vector2f(
                ScenePanel.mousePos.x - ScenePanel.windowPos.x - ScenePanel.offset.x,
                ScenePanel.mousePos.y - ScenePanel.windowPos.y - ScenePanel.offset.y);
        mousePos.y = ACTIVE_SCENE.getFrameBuffer().getHeight() - mousePos.y;
        glReadBuffer(GL_COLOR_ATTACHMENT1);
        int[] entityID = new int[1];
        glReadPixels((int) mousePos.x, (int) mousePos.y, 1, 1, GL_RED_INTEGER, GL_INT, entityID);
        return entityID[0];
    }
}