package OxyEngine.Scene;

import OxyEngine.Components.*;
import OxyEngine.Core.Camera.OxyCamera;
import OxyEngine.Core.Context.Renderer.Buffer.OpenGLMesh;
import OxyEngine.Core.Context.Renderer.Light.SkyLight;
import OxyEngine.Core.Context.Renderer.Pipeline.OxyShader;
import OxyEngine.Core.Context.Renderer.ShadowRenderer;
import OxyEngine.Core.Context.Renderer.Texture.HDRTexture;
import OxyEngine.Core.Context.Renderer.Texture.TextureSlot;
import OxyEngine.PhysX.OxyPhysXComponent;
import OxyEngine.Scripting.OxyScript;
import OxyEngineEditor.UI.Panels.GUINode;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

import static OxyEngine.Core.Context.Renderer.ShadowRenderer.NUMBER_CASCADES;
import static OxyEngine.Scene.SceneRuntime.ACTIVE_SCENE;
import static OxyEngine.Scene.SceneRuntime.currentBoundedSkyLight;
import static OxyEngine.Utils.toPrimitiveFloat;
import static OxyEngine.Utils.toPrimitiveInteger;
import static org.lwjgl.opengl.GL45.glBindTextureUnit;

public abstract class OxyEntity {

    private final List<OxyScript> scripts = new ArrayList<>();
    private final List<GUINode> guiNodes = new ArrayList<>();

    private EntityFamily family = new EntityFamily();

    protected float[] vertices, tcs, normals, tangents, biTangents;
    protected int[] indices;

    protected final Scene scene;

    protected boolean importedFromFile;
    protected int objectID = -10; //for selection

    OxyEntity(Scene scene) {
        this.scene = scene;
    }

    OxyEntity(OxyModel other) {
        this(other.scene);
        this.tangents = other.tangents.clone();
        this.biTangents = other.biTangents.clone();
        this.normals = other.normals.clone();
        this.vertices = other.vertices.clone();
        this.tcs = other.tcs.clone();
        this.indices = other.indices.clone();
    }

    public abstract void updateData();

    public abstract OxyEntity copyMe();

    public void unbindTextures() {
        for (int i = 1; i <= 5; i++) {
            glBindTextureUnit(i, 0);
        }
    }

    public void setFamily(EntityFamily component) {
        this.family = component;
    }

    public EntityFamily getFamily() {
        return family;
    }

    protected void addToScene() {
        scene.put(this);
    }

    public void transformLocally() {
        TransformComponent c = get(TransformComponent.class);
        c.transform = new Matrix4f()
                .translate(c.position)
                .rotateX(c.rotation.x)
                .rotateY(c.rotation.y)
                .rotateZ(c.rotation.z)
                .scale(c.scale);

        var root = getRoot();
        if (root != null)
            c.transform.mulLocal(root.get(TransformComponent.class).transform);

        c.transform.getTranslation(c.worldSpacePosition);
    }

    public void addComponent(EntityComponent... component) {
        scene.addComponent(this, component);
        for (EntityComponent c : component) {
            if (c instanceof OpenGLMesh m) {
                m.addToList(this);
            }

            //Camera position and rotation from the entity to the camera component
            if (c instanceof OxyCamera m) {
                TransformComponent t = this.get(TransformComponent.class);
                m.setPosition(t.worldSpacePosition);
                m.setRotation(t.rotation);
            }
        }
    }

    public List<OxyEntity> getEntitiesRelatedTo() {
        List<OxyEntity> related = new ArrayList<>();
        for (var ent : scene.getEntities()) {
            if (ent.equals(this)) continue;
            EntityFamily c = ent.getFamily();
            if (c == this.getFamily()) {
                related.add(ent);
            } else if (c.root() != null) { //bcs in case of null, null == null would be true... and we dont want that
                if (c.root() == this.getFamily()) related.add(ent);
            }
        }
        return related;
    }

    public static void addParentTransformToChildren(OxyEntity root) {
        List<OxyEntity> relatedEntities = root.getEntitiesRelatedTo();
        if (relatedEntities == null) return;
        if (relatedEntities.size() == 0) return;
        for (OxyEntity m : relatedEntities) {
            m.transformLocally();
            if (m.has(OxyPhysXComponent.class))
                m.get(OxyPhysXComponent.class).getActor().setGlobalPose(m.get(TransformComponent.class).transform);
            addParentTransformToChildren(m);
        }
    }

    @SafeVarargs
    public final void removeComponent(Class<? extends EntityComponent>... components) {
        for (var classes : components) {
            scene.removeComponent(this, this.get(classes));
        }
    }

    public void addComponent(List<EntityComponent> component) {
        for (EntityComponent c : component) {
            scene.addComponent(this, c);
            if (c instanceof OpenGLMesh m) {
                m.addToList(this);
            }
        }
    }

    public void addScript(OxyScript component) {
        component.setScene(scene);
        component.setEntity(this);
        component.loadAssembly();
        getGUINodes().add(component.guiNode);
        scripts.add(component);
    }

    public void addScript(List<OxyScript> components) {
        for (OxyScript s : components) addScript(s);
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
    public <T extends EntityComponent> T get(Class<T> destClass) {
        return scene.get(this, destClass);
    }

    public OxyEntity getRoot() {
        return scene.getRoot(this);
    }

    public static float[] sumAllVertices(List<OxyEntity> arr) {
        List<Float> allVertices = new ArrayList<>();
        for (OxyEntity oxyObj : arr) {
            for (int i = 0; i < oxyObj.vertices.length; i++) {
                allVertices.add(oxyObj.vertices[i]);
            }
        }
        return toPrimitiveFloat(allVertices);
    }

    public static int[] sumAllIndices(List<OxyEntity> arr) {
        List<Integer> allIndices = new ArrayList<>();
        for (OxyEntity oxyObj : arr) {
            if (oxyObj.indices == null) continue;
            for (int i = 0; i < oxyObj.indices.length; i++) {
                allIndices.add(oxyObj.indices[i]);
            }
        }
        return toPrimitiveInteger(allIndices);
    }

    public float[] getVertices() {
        return vertices;
    }

    public int[] getIndices() {
        return indices;
    }

    public float[] getTcs() {
        return tcs;
    }

    public int getObjectId() {
        return objectID;
    }

    public List<GUINode> getGUINodes() {
        return guiNodes;
    }

    public boolean familyHasRoot() {
        return getFamily().root() != null;
    }

    public List<OxyScript> getScripts() {
        return scripts;
    }

    public boolean hasMaterial() {
        return get(OxyMaterialIndex.class).index() != -1;
    }

    public void update() {
        OxyMaterial material = null;
        if (has(OxyMaterialIndex.class)) //noinspection OptionalGetWithoutIsPresent
            material = OxyMaterialPool.getMaterial(this).get();

        if (material == null)
            throw new IllegalStateException("Material is null. Entities that does not have any material should not be allowed to render!");

        OxyShader shader = material.getShader();
        shader.begin();

        //ANIMATION UPDATE
        shader.setUniform1i("animatedModel", 0);
        if (has(AnimationComponent.class)) {
            AnimationComponent animComp = get(AnimationComponent.class);
            if (ACTIVE_SCENE.STATE == SceneState.RUNNING) {
                shader.setUniform1i("animatedModel", 1);
                animComp.updateAnimation(SceneRuntime.TS);
                List<Matrix4f> matrix4fList = animComp.getFinalBoneMatrices();
                for (int j = 0; j < matrix4fList.size(); j++) {
                    shader.setUniformMatrix4fv("finalBonesMatrices[" + j + "]", matrix4fList.get(j), false);
                }
            } else animComp.setTime(0);
        }

        TransformComponent c = get(TransformComponent.class);
        shader.setUniformMatrix4fv("model", c.transform, false);
        int iblSlot = TextureSlot.UNUSED.getValue(), prefilterSlot = TextureSlot.UNUSED.getValue(), brdfLUTSlot = TextureSlot.UNUSED.getValue();

        shader.setUniform1f("hdrIntensity", 1.0f);
        shader.setUniform1f("gamma", ACTIVE_SCENE.gammaStrength);
        shader.setUniform1f("exposure", ACTIVE_SCENE.exposure);

        if (currentBoundedSkyLight != null) {
            SkyLight skyLightComp = currentBoundedSkyLight.get(SkyLight.class);
            HDRTexture hdrTexture = null;
            if (skyLightComp != null) {
                shader.setUniform1f("hdrIntensity", skyLightComp.intensity[0]);
                hdrTexture = skyLightComp.getHDRTexture();
            }
            if (hdrTexture != null) {
                iblSlot = hdrTexture.getIBLSlot();
                prefilterSlot = hdrTexture.getPrefilterSlot();
                brdfLUTSlot = hdrTexture.getBDRFSlot();
            }
        }

        shader.setUniform1i("iblMap", iblSlot);
        shader.setUniform1i("prefilterMap", prefilterSlot);
        shader.setUniform1i("brdfLUT", brdfLUTSlot);

        boolean castShadows = ShadowRenderer.castShadows();
        shader.setUniform1i("castShadows", castShadows ? 1 : 0);
        if (castShadows) {
            if (ShadowRenderer.cascadeIndicatorToggle)
                shader.setUniform1i("cascadeIndicatorToggle", 1);
            else shader.setUniform1i("cascadeIndicatorToggle", 0);

            for (int i = 0; i < NUMBER_CASCADES; i++) {
                if (ShadowRenderer.ready(i)) {
                    glBindTextureUnit(TextureSlot.CSM.getValue() + i, ShadowRenderer.getShadowMap(i));
                    shader.setUniform1i("shadowMap[" + i + "]", TextureSlot.CSM.getValue() + i);
                    shader.setUniformMatrix4fv("lightSpaceMatrix[" + i + "]", ShadowRenderer.getShadowViewMatrix(i), false);
                    shader.setUniform1f("cascadeSplits[" + i + "]", ShadowRenderer.getCascadeSplits(i));
                }
            }
        }
        material.push(shader);
        shader.end();
    }
}