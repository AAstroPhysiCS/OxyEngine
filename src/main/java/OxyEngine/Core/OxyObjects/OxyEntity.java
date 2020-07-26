package OxyEngine.Core.OxyObjects;

import OxyEngine.Events.OxyEventListener;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static OxyEngineEditor.UI.OxyUISystem.OxyEventSystem.dispatcherThread;

public abstract class OxyEntity {

    protected float[] vertices, tcs;
    protected int[] indices;

    public boolean selected;

    protected Vector3f position, rotation;
    protected float scale;
    protected Matrix4f transform;

    public OxyEntity() {
    }

    public abstract void updateData();

    public void addEventListener(OxyEventListener listener) {
        dispatcherThread.addDispatchersToThread(this, listener);
    }

    public float[] getVertices() {
        return vertices;
    }

    public void setVertices(float[] vertices) {
        this.vertices = vertices;
    }

    public int[] getIndices() {
        return indices;
    }

    public float[] getTcs() {
        return tcs;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getRotation() {
        return rotation;
    }

    public Matrix4f getTransform() {
        return transform;
    }

    public float getScale() {
        return scale;
    }
}
