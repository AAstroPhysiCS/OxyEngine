package OxyEngineEditor.Sandbox.OxyObjects;

import OxyEngine.Core.Renderer.Buffer.Mesh;
import OxyEngine.Events.OxyEventListener;
import OxyEngineEditor.Sandbox.OxyComponents.EntityComponent;
import OxyEngineEditor.Sandbox.OxyComponents.GameObjectMesh;
import OxyEngineEditor.Sandbox.Scene.Scene;

import java.util.List;

import static OxyEngineEditor.UI.OxyUISystem.OxyEventSystem.dispatcherThread;

public class OxyEntity {

    float[] vertices, tcs, normals;
    int[] indices;

    private final Scene scene;
    private final ObjectTemplate template;

    public OxyEntity(Scene scene, ObjectTemplate template) {
        this.scene = scene;
        this.template = template;
    }

    public void initData(Mesh mesh){
        template.constructData(this);
        if(mesh instanceof GameObjectMesh m)
            template.initData(this, m);
    }

    public void updateData(){
        if(template instanceof ModelTemplate m)
            m.updateData(this);
    }

    public final void addComponent(EntityComponent... component) {
        scene.addComponent(this, component);
    }

    /*
     * returns true if the component is already in the set
     */
    public boolean has(Class<? extends EntityComponent> destClass) {
        return scene.has(this, destClass);
    }

    /*
     * gets the component from the set
     */
    public EntityComponent get(Class<? extends EntityComponent> destClass) {
        return scene.get(this, destClass);
    }

    public static float[] sumAllVertices(OxyEntity[] arr, GameObjectType type) {
        float[] allVertices = new float[arr.length * type.n_Vertices()];
        int ptr = 0;
        for (OxyEntity oxyObj : arr) {
            for (int i = 0; i < oxyObj.vertices.length; i++) {
                allVertices[ptr++] = oxyObj.vertices[i];
            }
        }
        return allVertices;
    }

    public static int[] sumAllIndices(OxyEntity[] arr, GameObjectType type) {
        int[] allIndices = new int[arr.length * type.n_Indices()];
        int ptr = 0;
        for (OxyEntity oxyObj : arr) {
            for (int i = 0; i < oxyObj.indices.length; i++) {
                allIndices[ptr++] = oxyObj.indices[i];
            }
        }
        return allIndices;
    }

    public static float[] sumAllVertices(List<OxyEntity> arr, GameObjectType type) {
        float[] allVertices = new float[arr.size() * type.n_Vertices()];
        int ptr = 0;
        for (OxyEntity oxyObj : arr) {
            for (int i = 0; i < oxyObj.vertices.length; i++) {
                allVertices[ptr++] = oxyObj.vertices[i];
            }
        }
        return allVertices;
    }

    public static int[] sumAllIndices(List<OxyEntity> arr, GameObjectType type) {
        int[] allIndices = new int[arr.size() * type.n_Indices()];
        int ptr = 0;
        for (OxyEntity oxyObj : arr) {
            for (int i = 0; i < oxyObj.indices.length; i++) {
                allIndices[ptr++] = oxyObj.indices[i];
            }
        }
        return allIndices;
    }

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

    public ObjectTemplate getTemplate() {
        return template;
    }

    public float[] getTcs() {
        return tcs;
    }
}
