package OxyEngine.Events;

import OxyEngine.Core.Layers.SceneLayer;
import OxyEngine.Core.Layers.UILayer;
import OxyEngine.Scene.Objects.Model.OxyModel;
import OxyEngine.Scene.SceneRuntime;

import static OxyEngine.System.OxyEventSystem.keyEventDispatcher;
import static OxyEngine.Scene.Scene.*;
import static OxyEngineEditor.UI.Gizmo.OxySelectHandler.entityContext;
import static org.lwjgl.glfw.GLFW.*;

public class OxyKeyEvent extends OxyEvent {

    public OxyKeyEvent() {
        super("Key Event");
    }

    @Override
    public EventType getEventType() {
        return EventType.KeyEvent;
    }

    private static boolean cPressed = false;

    public void onKeyPressed() {

        if (keyEventDispatcher.getKeys()[GLFW_KEY_DELETE] && entityContext != null) {
            SceneRuntime.ACTIVE_SCENE.removeEntity(entityContext);
            SceneRuntime.stop();
            SceneLayer.getInstance().updateAllEntities();
            entityContext = null;
            System.gc();
        }

        if (keyEventDispatcher.getKeys()[GLFW_KEY_LEFT_CONTROL] && keyEventDispatcher.getKeys()[GLFW_KEY_C] &&
                entityContext instanceof OxyModel m && !cPressed) {
            m.copyMe();
            SceneLayer.getInstance().updateAllEntities();
            cPressed = true;
            System.gc();
        }
        if (!keyEventDispatcher.getKeys()[GLFW_KEY_LEFT_CONTROL] && !keyEventDispatcher.getKeys()[GLFW_KEY_C])
            cPressed = false;

        if (glfwGetKey(UILayer.windowHandle.getPointer(), GLFW_KEY_LEFT_CONTROL) == GLFW_PRESS &&
                glfwGetKey(UILayer.windowHandle.getPointer(), GLFW_KEY_O) == GLFW_PRESS) {
            openScene();
            System.gc();
        }

        if (glfwGetKey(UILayer.windowHandle.getPointer(), GLFW_KEY_LEFT_CONTROL) == GLFW_PRESS &&
                glfwGetKey(UILayer.windowHandle.getPointer(), GLFW_KEY_S) == GLFW_PRESS) {
            saveScene();
        }

        if (glfwGetKey(UILayer.windowHandle.getPointer(), GLFW_KEY_LEFT_CONTROL) == GLFW_PRESS &&
                glfwGetKey(UILayer.windowHandle.getPointer(), GLFW_KEY_N) == GLFW_PRESS) {
            newScene();
            System.gc();
        }

        if (glfwGetKey(UILayer.windowHandle.getPointer(), GLFW_KEY_LEFT_CONTROL) == GLFW_PRESS &&
                glfwGetKey(UILayer.windowHandle.getPointer(), GLFW_KEY_G) == GLFW_PRESS) {
            SceneLayer.getInstance().recompileShader();
        }
    }
}
