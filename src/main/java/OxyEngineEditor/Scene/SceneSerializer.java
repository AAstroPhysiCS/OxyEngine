package OxyEngineEditor.Scene;

import OxyEngine.Components.*;
import OxyEngine.Core.Layers.SceneLayer;
import OxyEngine.Core.Renderer.Light.DirectionalLight;
import OxyEngine.Core.Renderer.Light.PointLight;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Core.Renderer.Texture.OxyColor;
import OxyEngine.Core.Renderer.Texture.OxyTexture;
import OxyEngine.Scripting.OxyScript;
import OxyEngineEditor.Scene.Objects.Model.OxyMaterial;
import OxyEngineEditor.Scene.Objects.Model.OxyModel;
import OxyEngineEditor.UI.Panels.EnvironmentPanel;
import OxyEngineEditor.UI.Selector.OxySelectHandler;
import org.joml.Vector3f;

import java.io.File;
import java.util.Arrays;
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
                    .putField("Scene Name", scene.getSceneName())
                    .separate()
                    .createOxyJSONObject("Environment")
                    .putField("Environment Map", SceneLayer.hdrTexture != null ? SceneLayer.hdrTexture.getPath() : "null")
                    .putField("Environment Gamma Strength", String.valueOf(EnvironmentPanel.gammaStrength[0]))
                    .putField("Environment LOD", String.valueOf(EnvironmentPanel.mipLevelStrength[0]))
                    .putField("Environment Exposure", String.valueOf(EnvironmentPanel.exposure[0]));

            int ptr = 0;
            OxyJSON.OxyJSONWriterBuilder builder = INSTANCE.openWritingStream();
            var array = builder.createOxyJSONArray("Registry");
            for (OxyEntity e : scene.getEntities()) {
                if (!(e instanceof OxyModel)) continue;
                e.dump(ptr++, array);
            }
            builder.build().writeAndCloseStream();
        }
    }

    private static final class SceneReader {

        public Scene readScene(String path, SceneLayer layer, OxyShader shader) {
            SceneRuntime.stop();
            SceneRuntime.dispose();

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
            Scene scene = new Scene(sceneName, oldScene.getRenderer(), oldScene.getFrameBuffer());
            for (var n : oldScene.getNativeObjects()) {
                scene.put(n.getKey());
                scene.addComponent(n.getKey(), n.getValue().toArray(EntityComponent[]::new));
            }
            scene.setUISystem(oldScene.getOxyUISystem());
            OxySelectHandler.entityContext = null;
            layer.clear();
            oldScene.dispose();
            SceneRuntime.ACTIVE_SCENE = scene;

            String envMapPath = envJSON.getField("Environment Map").value();
            layer.loadHDRTextureToScene(!envMapPath.equals("null") ? envMapPath : null);

            EnvironmentPanel.gammaStrength = new float[]{Float.parseFloat(envJSON.getField("Environment Gamma Strength").value())};
            EnvironmentPanel.mipLevelStrength = new float[]{Float.parseFloat(envJSON.getField("Environment LOD").value())};
            EnvironmentPanel.exposure = new float[]{Float.parseFloat(envJSON.getField("Environment Exposure").value())};

            for (var models : modelsJSON.getObjectList()) {
                String id = models.getField("ID").value();
                int meshPos = Integer.parseInt(models.getField("Mesh Position").value());
                String name = models.getField("Name").value();
                boolean emitting = Boolean.parseBoolean(models.getField("Emitting").value());
                String emittingType = models.getField("Emitting Type").value();
                Vector3f position = parseStringToVector3f(models.getField("Position").value());
                Vector3f rot = parseStringToVector3f(models.getField("Rotation").value());
                Vector3f scale = parseStringToVector3f(models.getField("Scale").value());
                Vector3f minB = parseStringToVector3f(models.getField("Bounds Min").value());
                Vector3f maxB = parseStringToVector3f(models.getField("Bounds Max").value());
                float[] color = parseStringToFloatArray(models.getField("Color").value(), 4);
                String albedoTPath = models.getField("Albedo Texture").value();
                String normalMapTPath = models.getField("Normal Map Texture").value();
                float normalMapStrength = Float.parseFloat(models.getField("Normal Map Strength").value());
                String roughnessMapTPath = models.getField("Roughness Map Texture").value();
                float roughnessMapStrength = Float.parseFloat(models.getField("Roughness Map Strength").value());
                String aoMapTPath = models.getField("AO Map Texture").value();
                float aoMapStrength = Float.parseFloat(models.getField("AO Map Strength").value());
                String metallicMapTPath = models.getField("Metallic Map Texture").value();
                float metallicMapStrength = Float.parseFloat(models.getField("Metallic Map Strength").value());
                String meshPath = models.getField("Mesh").value();

                OxyModel modelInstance;
                if (!meshPath.equals("null")) {
                    modelInstance = scene.createModelEntity(meshPath, shader, true, meshPos);
                } else {
                    modelInstance = scene.createEmptyModel(shader, true, meshPos);
                }
                if (emitting) {
                    if (emittingType.equals(PointLight.class.getSimpleName())) {
                        modelInstance.addComponent(new PointLight(new Vector3f(2f, 2f, 2f), new Vector3f(1f, 1f, 1f), 1.0f, 0.027f, 0.0028f));
                        modelInstance.getGUINodes().add(PointLight.guiNode);
                    } else if (emittingType.equals(DirectionalLight.class.getSimpleName())) {
                        modelInstance.addComponent(new DirectionalLight(new Vector3f(2f, 2f, 2f), new Vector3f(1f, 1f, 1f)));
                        modelInstance.getGUINodes().add(DirectionalLight.guiNode);
                    }
                }

                modelInstance.originPos = new Vector3f(0, 0, 0);
                modelInstance.importedFromFile = true;
                modelInstance.addComponent(new UUIDComponent(UUID.fromString(id)), new MeshPosition(meshPos), new TagComponent(name), new TransformComponent(position, rot, scale), new OxyMaterial(OxyTexture.loadImage(1, albedoTPath),
                                OxyTexture.loadImage(2, normalMapTPath), OxyTexture.loadImage(3, roughnessMapTPath), OxyTexture.loadImage(4, metallicMapTPath), OxyTexture.loadImage(5, aoMapTPath), new OxyColor(color), normalMapStrength, aoMapStrength, roughnessMapStrength, metallicMapStrength),
                        new SelectedComponent(false), SceneRuntime.currentBoundedCamera, new BoundingBoxComponent(minB, maxB));
                modelInstance.constructData();

                var scripts = models.getInnerObjectByName("Script");
                for (var f : scripts.getFieldList()) modelInstance.addScript(new OxyScript(f.value()));
            }
            //I don't have to do this... but just to be sure
            System.gc();
            return SceneRuntime.ACTIVE_SCENE;
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