package OxyEngineEditor.Scene;

import OxyEngine.Components.*;
import OxyEngine.Core.Layers.SceneLayer;
import OxyEngine.Core.Renderer.Light.DirectionalLight;
import OxyEngine.Core.Renderer.Light.PointLight;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Core.Renderer.Texture.OxyColor;
import OxyEngine.Scripting.OxyScript;
import OxyEngineEditor.Scene.Objects.Model.OxyMaterial;
import OxyEngineEditor.Scene.Objects.Model.OxyMaterialPool;
import OxyEngineEditor.Scene.Objects.Model.OxyModel;
import OxyEngineEditor.UI.Gizmo.OxySelectHandler;
import OxyEngineEditor.UI.Panels.EnvironmentPanel;
import org.joml.Vector3f;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public final class SceneSerializer {

    public static final String fileExtension = ".osc", extensionName = "osc";

    private static SceneWriter WRITER = null;
    private static SceneReader READER = null;
    private static final OxyJSON INSTANCE = OxyJSON.getInstance();

    public static void serializeScene(String path) {
        if (WRITER == null) WRITER = new SceneWriter();
        WRITER.writeScene(new File(path), SceneRuntime.ACTIVE_SCENE);
    }

    public static Scene deserializeScene(String path, SceneLayer layer, OxyShader shader) {
        if (READER == null) READER = new SceneReader();
        return READER.readScene(path, layer, shader);
    }

    private static final class SceneWriter {

        public void writeScene(File f, Scene scene) {
            INSTANCE.openWritingStream()
                    .file(f)
                    .createOxyJSONObject("Scene")
                    .putField("Scene Name", scene.getSceneName().split("\\.")[0])
                    .separate()
                    .createOxyJSONObject("Environment")
                    .putField("Environment Map", SceneLayer.hdrTexture != null ? SceneLayer.hdrTexture.getPath() : "null")
                    .putField("Environment Gamma Strength", String.valueOf(EnvironmentPanel.gammaStrength[0]))
                    .putField("Environment LOD", String.valueOf(EnvironmentPanel.mipLevelStrength[0]))
                    .putField("Environment Exposure", String.valueOf(EnvironmentPanel.exposure[0]));

            OxyJSON.OxyJSONWriterBuilder builder = INSTANCE.openWritingStream();

            var array = builder.createOxyJSONArray("Registry");
            List<OxyEntity> rootPooled = new ArrayList<>();
            for (OxyEntity e : scene.getEntities()) {
                if(e.isRoot()){
                    if(!rootPooled.contains(e)) rootPooled.add(e);
                }
            }
            for(OxyEntity root : rootPooled){
                if (!(root instanceof OxyModel)) continue;
                var obj = array.createOxyJSONObject(root.get(TagComponent.class).tag());
                root.dump(obj);
            }
            builder.build().writeAndCloseStream();
        }
    }

    private static final class SceneReader {

        public Scene readScene(String path, SceneLayer layer, OxyShader shader) {
            SceneRuntime.stop();

            var modelsJSON = new OxyJSON.OxyJSONArray();
            var sceneJSON = new OxyJSON.OxyJSONObject();
            var envJSON = new OxyJSON.OxyJSONObject();

            INSTANCE.openReadingStream()
                    .read(path)
                    .getOxyJSONArray("Registry", modelsJSON)
                    .getOxyJSONObject("Scene", sceneJSON)
                    .getOxyJSONObject("Environment", envJSON);

            String sceneName = sceneJSON.getField("Scene Name").value();

            Scene oldScene = SceneRuntime.ACTIVE_SCENE;
            oldScene.disposeAllModels();
            Scene scene = new Scene(sceneName, oldScene.getRenderer(), oldScene.getFrameBuffer());
            for (var n : oldScene.getEntityEntrySet()) {
                scene.put(n.getKey());
                scene.addComponent(n.getKey(), n.getValue().toArray(EntityComponent[]::new));
            }
            OxySelectHandler.entityContext = null;
            layer.clear();

            String envMapPath = envJSON.getField("Environment Map").value();
            layer.loadHDRTextureToScene(!envMapPath.equals("null") ? envMapPath : null, scene);

            EnvironmentPanel.gammaStrength = new float[]{Float.parseFloat(envJSON.getField("Environment Gamma Strength").value())};
            EnvironmentPanel.mipLevelStrength = new float[]{Float.parseFloat(envJSON.getField("Environment LOD").value())};
            EnvironmentPanel.exposure = new float[]{Float.parseFloat(envJSON.getField("Environment Exposure").value())};

            Scene.optimization_Path = "";

            for (var root : modelsJSON.getObjectList()) {
                var familyComponent = new FamilyComponent();
                Vector3f positionRoot = parseStringToVector3f(root.getField("Position").value());
                Vector3f rotRoot = parseStringToVector3f(root.getField("Rotation").value());
                Vector3f scaleRoot = parseStringToVector3f(root.getField("Scale").value());
                OxyEntity rootEntity = readFields(root, scene, shader, null);
                rootEntity.addComponent(new TagComponent(root.getName()), new TransformComponent(positionRoot, rotRoot, scaleRoot), familyComponent);
                rootEntity.setRoot(true);
                rootEntity.transformLocally();

                for (var ent : root.getInnerObjects()) {
                    if(!ent.getName().startsWith("OxyModel")) continue;
                    readFields(ent, scene, shader, rootEntity);
                }
            }
            SceneRuntime.onCreate();
            //I don't have to do this... but just to be sure
            System.gc();
            return scene;
        }


        private OxyModel readFields(OxyJSON.OxyJSONObject ent, Scene scene, OxyShader shader, OxyEntity rootEntity){
            String name = ent.getField("Name").value();
            String id = ent.getField("ID").value();
            int meshPos = Integer.parseInt(ent.getField("Mesh Position").value());
            boolean emitting = Boolean.parseBoolean(ent.getField("Emitting").value());
            String emittingType = ent.getField("Emitting Type").value();
            Vector3f position = parseStringToVector3f(ent.getField("Position").value());
            Vector3f rot = parseStringToVector3f(ent.getField("Rotation").value());
            Vector3f scale = parseStringToVector3f(ent.getField("Scale").value());
            Vector3f minB = parseStringToVector3f(ent.getField("Bounds Min").value());
            Vector3f maxB = parseStringToVector3f(ent.getField("Bounds Max").value());
            float[] color = parseStringToFloatArray(ent.getField("Color").value(), 4);
            String nameMaterial = ent.getField("Material Name").value();
            String albedoTPath = ent.getField("Albedo Texture").value();
            String normalMapTPath = ent.getField("Normal Map Texture").value();
            float normalMapStrength = Float.parseFloat(ent.getField("Normal Map Strength").value());
            String roughnessMapTPath = ent.getField("Roughness Map Texture").value();
            float roughnessMapStrength = Float.parseFloat(ent.getField("Roughness Map Strength").value());
            String aoMapTPath = ent.getField("AO Map Texture").value();
            float aoMapStrength = Float.parseFloat(ent.getField("AO Map Strength").value());
            String metallicMapTPath = ent.getField("Metallic Map Texture").value();
            float metallicMapStrength = Float.parseFloat(ent.getField("Metallic Map Strength").value());
            String meshPath = ent.getField("Mesh").value();

            OxyModel modelInstance;
            int index = OxyMaterialPool.addMaterial(nameMaterial, albedoTPath,
                    normalMapTPath, roughnessMapTPath, metallicMapTPath, aoMapTPath,
                    new OxyColor(color), normalMapStrength, aoMapStrength, roughnessMapStrength, metallicMapStrength);
            if (!meshPath.equals("null") && rootEntity != null) {
                modelInstance = scene.createModelEntity(meshPath, shader, meshPos, index, rootEntity);
                modelInstance.getGUINodes().add(OxyMaterial.guiNode);
            } else {
                modelInstance = scene.createEmptyModel(shader, meshPos);
            }
            if (emitting) {
                var lightAttributes = ent.getInnerObjectByName("Light Attributes");
                float colorIntensity = Float.parseFloat(lightAttributes.getField("Intensity").value());

                if (emittingType.equals(PointLight.class.getSimpleName())) {
                    float constant = Float.parseFloat(lightAttributes.getField("Constant").value());
                    float linear = Float.parseFloat(lightAttributes.getField("Linear").value());
                    float quadratic = Float.parseFloat(lightAttributes.getField("Quadratic").value());

                    modelInstance.addComponent(new PointLight(colorIntensity, constant, linear, quadratic));
                    modelInstance.getGUINodes().add(PointLight.guiNode);
                } else if (emittingType.equals(DirectionalLight.class.getSimpleName())) {
                    Vector3f dir = parseStringToVector3f(lightAttributes.getField("Direction").value());

                    modelInstance.addComponent(new DirectionalLight(colorIntensity, dir));
                    modelInstance.getGUINodes().add(DirectionalLight.guiNode);
                }
                modelInstance.addComponent(new OxyMaterialIndex(index));
                modelInstance.getGUINodes().add(OxyMaterial.guiNode);
            }

            TransformComponent t = new TransformComponent(position, rot, scale);
            modelInstance.importedFromFile = true;
            modelInstance.addComponent(new UUIDComponent(UUID.fromString(id)), new MeshPosition(meshPos), new TagComponent(name), t,
                    new SelectedComponent(false), SceneRuntime.currentBoundedCamera, new BoundingBoxComponent(minB, maxB));
            modelInstance.constructData();
            if(rootEntity != null){
                modelInstance.addComponent(rootEntity.get(FamilyComponent.class));
                t.transform.mulLocal(rootEntity.get(TransformComponent.class).transform);
            } else {
                modelInstance.setRoot(true);
            }
            modelInstance.updateData();

            var scripts = ent.getInnerObjectByName("Script");
            for (var f : scripts.getFieldList()) modelInstance.addScript(new OxyScript(f.value()));
            return modelInstance;
        }

    }

    private static Vector3f parseStringToVector3f(String sValue) {
        String[] splittedVector = sValue.replace("(", "").replace(")", "").split(" ");
        String[] valuesPos = new String[3];
        int ptr = 0;
        for (String s : splittedVector) {
            if (s.isBlank() || s.isEmpty()) continue;
            valuesPos[ptr++] = s;
        }
        return new Vector3f(Float.parseFloat(valuesPos[0]), Float.parseFloat(valuesPos[1]), Float.parseFloat(valuesPos[2]));
    }

    private static float[] parseStringToFloatArray(String sValue, int len) {
        float[] valuesPos = new float[len];
        if (sValue.equals("null")) {
            Arrays.fill(valuesPos, 0f);
            return valuesPos;
        }
        String[] splittedVector = sValue.replace("[", "").replace("]", "").split(", ");
        for (int i = 0; i < valuesPos.length; i++) {
            valuesPos[i] = Float.parseFloat(splittedVector[i]);
        }
        return valuesPos;
    }
}