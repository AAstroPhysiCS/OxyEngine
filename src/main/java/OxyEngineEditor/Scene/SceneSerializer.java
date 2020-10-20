package OxyEngineEditor.Scene;

import OxyEngine.Core.Layers.SceneLayer;
import OxyEngine.Core.Renderer.OxyRenderer;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Core.Renderer.Texture.OxyColor;
import OxyEngine.Core.Renderer.Texture.OxyTexture;
import OxyEngine.System.OxySystem;
import OxyEngineEditor.Components.*;
import OxyEngineEditor.Scene.Objects.Model.OxyMaterial;
import OxyEngineEditor.Scene.Objects.Model.OxyModel;
import OxyEngineEditor.UI.Panels.EnvironmentPanel;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static OxyEngine.System.OxySystem.oxyAssert;

public final class SceneSerializer {

    public static final String fileExtension = ".osc", extensionName = "osc";

    public static void serializeScene(SceneLayer sceneLayer) {
        serializeScene(sceneLayer, sceneLayer.getScene().getSceneName() + fileExtension);
    }

    public static void serializeScene(SceneLayer sceneLayer, String path) {
        try (SceneWriter writer = new SceneWriter(new File(path))) {
            writer.writeScene(sceneLayer.getScene());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Scene deserializeScene(String path, SceneLayer layer, OxyShader shader) {
        try (SceneReader reader = new SceneReader(path)) {
            Scene s = reader.readScene(layer, shader);
            layer.setScene(s);
            return s;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static final class SceneWriter implements AutoCloseable {

        private FileWriter writer;

        public SceneWriter(File f) {
            try {
                assert f.exists() || f.createNewFile() : oxyAssert("Failed to create a scene serialization file");
                writer = new FileWriter(f);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void writeScene(Scene scene) {
            OxySerializable sceneInfo = scene.getClass().getAnnotation(OxySerializable.class);
            String formattedOriginal = sceneInfo.info().formatted(scene.getSceneName(), SceneLayer.hdrTexture.getPath(),
                    EnvironmentPanel.gammaStrength[0], EnvironmentPanel.mipLevelStrength[0], EnvironmentPanel.exposure[0], scene.getShapeCount());
            StringBuilder info = new StringBuilder(formattedOriginal);
            int ptr = 1;

            for (OxyEntity e : scene.getEntities()) {

                String tag = "null";
                String grouped = "false";
                TransformComponent transform = e.get(TransformComponent.class);
                String albedoColor = "null";
                String albedoTexture = "null";
                String normalTexture = "null";
                String roughnessTexture = "null";
                String metallicTexture = "null";
                String aoTexture = "null";
                String mesh = "null";

                if (e.has(TagComponent.class)) tag = e.get(TagComponent.class).tag();
                if (e.has(EntitySerializationInfo.class))
                    grouped = String.valueOf(e.get(EntitySerializationInfo.class).grouped());
                if (e.has(OxyMaterial.class)) {
                    OxyMaterial m = e.get(OxyMaterial.class);
                    if (m.albedoColor != null) albedoColor = Arrays.toString(m.albedoColor.getNumbers());
                    if (m.albedoTexture != null) albedoTexture = m.albedoTexture.getPath();
                    if (m.normalTexture != null) normalTexture = m.normalTexture.getPath();
                    if (m.roughnessTexture != null) roughnessTexture = m.roughnessTexture.getPath();
                    if (m.metallicTexture != null) metallicTexture = m.metallicTexture.getPath();
                    if (m.aoTexture != null) aoTexture = m.aoTexture.getPath();
                }
                if (e.has(ModelMesh.class)) mesh = e.get(ModelMesh.class).getPath();

                OxySerializable objInfo = e.getClass().getAnnotation(OxySerializable.class);
                if (objInfo != null) {
                    String formatObjTemplate = objInfo.info().formatted(ptr++, tag, grouped, transform.position, transform.rotation,
                            transform.scale, albedoColor, albedoTexture, normalTexture, roughnessTexture, aoTexture, metallicTexture, mesh).trim();
                    info.append("\t").append(formatObjTemplate).append("\n");
                }
            }
            try {
                writer.write(info.toString() + "}");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void close() throws Exception {
            writer.flush();
            writer.close();
        }
    }

    static record SceneFileObject(Map<String, List<String>>map) {
    }

    private static final class SceneReader implements AutoCloseable {

        private final String loadedS;

        public SceneReader(String path) {
            loadedS = OxySystem.FileSystem.load(path);
        }

        @SuppressWarnings("DuplicateExpressions")
        public Scene readScene(SceneLayer layer, OxyShader shader) {
            String[] splitted = loadedS.split("\n");
            boolean objFound = false;
            String sceneName = null;
            List<SceneFileObject> objects = new ArrayList<>();
            for (int i = 0; i < splitted.length; i++) {
                String s = splitted[i];
                if (s.contains("Scene Name:")) sceneName = s.split(": ")[1].trim();
                SceneFileObject obj = null;
                String label = null;
                if (s.endsWith("{") && !s.contains("Registry")) {
                    label = s.replace("{", "").trim();
                    objFound = true;
                    obj = new SceneFileObject(new HashMap<>());
                }
                int ptr = i;
                while (objFound) {
                    String whileS = splitted[ptr++];
                    if (!obj.map.containsKey(label)) obj.map.put(label, new ArrayList<>());
                    else obj.map.get(label).add(whileS.replace("}", "").trim());
                    if (whileS.endsWith("}")) objFound = false;
                }
                if (obj != null) objects.add(obj);
            }
            Scene oldScene = layer.getScene();
            layer.clear();
            oldScene.dispose();
            Scene scene = new Scene(sceneName, oldScene.getRenderer(), oldScene.getFrameBuffer());
            scene.setUISystem(oldScene.getOxyUISystem());
            List<String> meshes = new ArrayList<>();
            for (var obj : objects) {
                for (var entrySet : obj.map.entrySet()) {
                    var key = entrySet.getKey();
                    var listOfValues = entrySet.getValue();
                    if (key.contains("Environment")) {
                        for (var values : listOfValues) {
                            if (values.isEmpty()) continue;
                            String[] split = values.split(": ");
                            String tag = split[0].trim();
                            String sValue = split[1].trim();
                            switch (tag) {
                                case "Environment Map" -> {
                                    if (SceneLayer.hdrTexture != null) SceneLayer.hdrTexture.dispose();
                                    SceneLayer.hdrTexture = OxyTexture.loadHDRTexture(sValue, layer.getScene());
                                    SceneLayer.hdrTexture.captureFaces(0);
                                }
                                case "Environment Gamma Strength" -> EnvironmentPanel.gammaStrength = new float[]{Float.parseFloat(sValue)};
                                case "Environment LOD" -> EnvironmentPanel.mipLevelStrength = new float[]{Float.parseFloat(sValue)};
                                case "Environment Exposure" -> EnvironmentPanel.exposure = new float[]{Float.parseFloat(sValue)};
                            }
                        }
                    } else if (key.contains("OxyModel")) {
                        String name = null, aT = null, nMT = null, rMT = null, aMT = null, mMT = null, mesh = null, grouped = null;
                        Vector3f pos = null, rot = null, scale = null;
                        Vector4f color = new Vector4f(1, 1, 1, 1);
                        for (var values : listOfValues) {
                            if (values.isEmpty()) continue;
                            String[] split = values.split(": ");
                            String tag = split[0].trim();
                            String sValue = split[1].trim();
                            switch (tag) {
                                case "Name" -> name = sValue;
                                case "Grouped" -> grouped = sValue;
                                case "Position" -> {
                                    CharSequence sequenceForVectors = sValue.subSequence(sValue.indexOf("(") + 2, sValue.indexOf(")"));
                                    String[] subSequence = Arrays.stream(((String) sequenceForVectors).trim().strip().split(" ")).filter(s -> !s.isBlank()).toArray(String[]::new);
                                    pos = new Vector3f((float) Double.parseDouble(subSequence[0]), (float) Double.parseDouble(subSequence[1]), (float) Double.parseDouble(subSequence[2]));
                                }
                                case "Rotation" -> {
                                    CharSequence sequenceForVectors = sValue.subSequence(sValue.indexOf("(") + 2, sValue.indexOf(")"));
                                    String[] subSequence = Arrays.stream(((String) sequenceForVectors).trim().strip().split(" ")).filter(s -> !s.isBlank()).toArray(String[]::new);
                                    rot = new Vector3f((float) Double.parseDouble(subSequence[0]), (float) Double.parseDouble(subSequence[1]), (float) Double.parseDouble(subSequence[2]));
                                }
                                case "Scale" -> {
                                    CharSequence sequenceForVectors = sValue.subSequence(sValue.indexOf("(") + 2, sValue.indexOf(")"));
                                    String[] subSequence = Arrays.stream(((String) sequenceForVectors).trim().strip().split(" ")).filter(s -> !s.isBlank()).toArray(String[]::new);
                                    scale = new Vector3f((float) Double.parseDouble(subSequence[0]), (float) Double.parseDouble(subSequence[1]), (float) Double.parseDouble(subSequence[2]));
                                }
                                case "Color" -> {
                                    CharSequence sequenceForArrays = sValue.subSequence(sValue.indexOf("[") + 1, sValue.indexOf("]"));
                                    String[] subSequence = ((String) sequenceForArrays).trim().strip().split(", ");
                                    color = new Vector4f((float) Double.parseDouble(subSequence[0]), (float) Double.parseDouble(subSequence[1]), (float) Double.parseDouble(subSequence[2]), (float) Double.parseDouble(subSequence[3]));
                                }
                                case "Albedo Texture" -> aT = sValue;
                                case "Normal Map Texture" -> nMT = sValue;
                                case "Roughness Map Texture" -> rMT = sValue;
                                case "AO Map Texture" -> aMT = sValue;
                                case "Metallic Map Texture" -> mMT = sValue;
                                case "Mesh" -> mesh = sValue;
                            }
                        }
                        if (grouped.equals("true") && !meshes.contains(mesh)) {
                            List<OxyModel> mList = scene.createModelEntities(mesh, shader);
                            meshes.add(mesh);
                            for (OxyModel m : mList) {
                                m.addComponent(new TagComponent(name), new TransformComponent(pos, rot, scale), new OxyMaterial(OxyTexture.loadImage(aT),
                                                OxyTexture.loadImage(nMT), OxyTexture.loadImage(rMT), OxyTexture.loadImage(mMT), OxyTexture.loadImage(aMT), null, new OxyColor(color)),
                                        new SelectedComponent(false), OxyRenderer.currentBoundedCamera
                                );
                                m.constructData();
                            }
                        } else {
                            OxyModel m = scene.createModelEntity(mesh, shader);
                            m.addComponent(new TagComponent(name), new TransformComponent(pos, rot, scale), new OxyMaterial(OxyTexture.loadImage(aT),
                                            OxyTexture.loadImage(nMT), OxyTexture.loadImage(rMT), OxyTexture.loadImage(mMT), OxyTexture.loadImage(aMT), null, new OxyColor(color)),
                                    new SelectedComponent(false), OxyRenderer.currentBoundedCamera
                            );
                            m.constructData();
                        }
                    }
                }
            }
            meshes.clear();
            return scene;
        }

        @Override
        public void close() {
        }
    }
}