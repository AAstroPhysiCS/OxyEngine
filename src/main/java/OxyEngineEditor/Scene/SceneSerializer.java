package OxyEngineEditor.Scene;

import OxyEngine.Core.Layers.SceneLayer;
import OxyEngine.Core.Renderer.Light.Light;
import OxyEngine.Core.Renderer.Light.PointLight;
import OxyEngine.Core.Renderer.OxyRenderer;
import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Core.Renderer.Texture.OxyColor;
import OxyEngine.Core.Renderer.Texture.OxyTexture;
import OxyEngine.System.OxySystem;
import OxyEngineEditor.Components.*;
import OxyEngineEditor.Scene.Objects.Model.OxyMaterial;
import OxyEngineEditor.Scene.Objects.Model.OxyModel;
import OxyEngineEditor.UI.Panels.EnvironmentPanel;
import OxyEngineEditor.UI.Selector.OxySelectHandler;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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

                String nativeTag = "null";
                String tag = "null";
                String grouped = "false";
                StringBuilder scripts = new StringBuilder();
                TransformComponent transform = e.get(TransformComponent.class);
                Vector3f minBound = new Vector3f(0, 0, 0), maxBound = new Vector3f(0, 0, 0);
                String albedoColor = "null";
                String albedoTexture = "null";
                String normalTexture = "null";
                String roughnessTexture = "null";
                String metallicTexture = "null";
                String aoTexture = "null";
                String mesh = "null";
                String id = e.get(UUIDComponent.class).getUUIDString();
                boolean emitting = false;

                if (e.has(BoundingBoxComponent.class)) {
                    minBound = e.get(BoundingBoxComponent.class).min();
                    maxBound = e.get(BoundingBoxComponent.class).max();
                }

                if (e.has(TagComponent.class)) tag = e.get(TagComponent.class).tag();
                if (e.has(NativeTagComponent.class)) nativeTag = e.get(NativeTagComponent.class).originalTag();
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
                if (e.has(EmittingComponent.class)) emitting = true;

                for (ScriptingComponent c : e.getScripts()) {
                    scripts.append(" ").append(c.getPath()).append(",");
                }

                OxySerializable objInfo = e.getClass().getAnnotation(OxySerializable.class);
                if (objInfo != null) {
                    String formatObjTemplate = objInfo.info().formatted(ptr++, id, nativeTag, tag, grouped, emitting,
                            transform.position.x, transform.position.y, transform.position.z,
                            transform.rotation.x, transform.rotation.y, transform.rotation.z,
                            transform.scale.x, transform.scale.y, transform.scale.z,
                            minBound.x, minBound.y, minBound.z, maxBound.x, maxBound.y, maxBound.z,
                            albedoColor, scripts.toString(), albedoTexture, normalTexture, roughnessTexture, aoTexture, metallicTexture, mesh).trim();
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

        private String loadedS;

        private Scene oldScene;

        public SceneReader(String path) {
            if (path != null) loadedS = OxySystem.FileSystem.load(path);
        }

        @SuppressWarnings("DuplicateExpressions")
        public Scene readScene(SceneLayer layer, OxyShader shader) {
            if (loadedS == null) return layer.getScene();
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
            oldScene = layer.getScene();
            Scene scene = new Scene(sceneName, oldScene.getRenderer(), oldScene.getFrameBuffer());
            scene.setUISystem(oldScene.getOxyUISystem());
            OxySelectHandler.entityContext = null;
            layer.clear();
            oldScene.dispose();
            Map<String, List<List<EntityComponent>>> listOfEntries = new HashMap<>(objects.size());
            Map<String, List<ScriptingComponent>> listOfScripts = new HashMap<>(objects.size());
            List<NativeTagComponent> listOfNativeTags = new ArrayList<>(objects.size());
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
                                case "Environment Map" -> layer.loadHDRTextureToScene(sValue);
                                case "Environment Gamma Strength" -> EnvironmentPanel.gammaStrength = new float[]{Float.parseFloat(sValue)};
                                case "Environment LOD" -> EnvironmentPanel.mipLevelStrength = new float[]{Float.parseFloat(sValue)};
                                case "Environment Exposure" -> EnvironmentPanel.exposure = new float[]{Float.parseFloat(sValue)};
                            }
                        }
                    } else if (key.contains("OxyModel")) {
                        String id = null, name = null, nativeTag = null, aT = null, nMT = null, rMT = null, aMT = null, mMT = null, mesh = null, grouped = null;
                        Vector3f pos = null, rot = null, scale = null, min = null, max = null;
                        boolean emitting = false;
                        Vector4f color = new Vector4f(1, 1, 1, 1);
                        for (var values : listOfValues) {
                            if (values.isEmpty()) continue;
                            String[] split = values.split(": ");
                            String tag = split[0].trim();
                            String sValue = split[1].trim();
                            switch (tag) {
                                case "ID" -> id = sValue;
                                case "Native Name" -> nativeTag = sValue;
                                case "Name" -> name = sValue;
                                case "Grouped" -> grouped = sValue;
                                case "Position" -> pos = getVector3fFromString(sValue);
                                case "Bounds Min" -> min = getVector3fFromString(sValue);
                                case "Bounds Max" -> max = getVector3fFromString(sValue);
                                case "Rotation" -> rot = getVector3fFromString(sValue);
                                case "Scale" -> scale = getVector3fFromString(sValue);
                                case "Emitting" -> emitting = Boolean.parseBoolean(sValue);
                                case "Color" -> {
                                    CharSequence sequenceForArrays = sValue.subSequence(sValue.indexOf("[") + 1, sValue.indexOf("]"));
                                    String[] subSequence = ((String) sequenceForArrays).trim().strip().split(", ");
                                    color = new Vector4f((float) Double.parseDouble(subSequence[0]), (float) Double.parseDouble(subSequence[1]), (float) Double.parseDouble(subSequence[2]), (float) Double.parseDouble(subSequence[3]));
                                }
                                case "Scripts" -> {
                                    CharSequence sequenceForArrays = sValue.subSequence(sValue.indexOf("[") + 1, sValue.indexOf("]"));
                                    String[] subSequence = ((String) sequenceForArrays).trim().strip().split(",");
                                    if (!listOfScripts.containsKey(id)) {
                                        listOfScripts.put(id, new ArrayList<>());
                                    }
                                    for (String s : subSequence) {
                                        if (!s.isBlank()) listOfScripts.get(id).add(new ScriptingComponent(s.trim()));
                                    }
                                }
                                case "Albedo Texture" -> aT = sValue;
                                case "Normal Map Texture" -> nMT = sValue;
                                case "Roughness Map Texture" -> rMT = sValue;
                                case "AO Map Texture" -> aMT = sValue;
                                case "Metallic Map Texture" -> mMT = sValue;
                                case "Mesh" -> mesh = sValue;
                            }
                        }

                        if (grouped.equals("true") && !listOfEntries.containsKey(mesh)) {
                            listOfEntries.put(mesh, new ArrayList<>());
                        }
                        if (grouped.equals("true")) {
                            listOfNativeTags.add(new NativeTagComponent(nativeTag));
                            //list.of does not work
                            listOfEntries.get(mesh).add(Arrays.stream(new EntityComponent[]{new UUIDComponent(UUID.fromString(id)), new TagComponent(name), new TransformComponent(pos, rot, scale), new OxyMaterial(OxyTexture.loadImage(aT),
                                            OxyTexture.loadImage(nMT), OxyTexture.loadImage(rMT), OxyTexture.loadImage(mMT), OxyTexture.loadImage(aMT), null, new OxyColor(color)),
                                    new SelectedComponent(false), OxyRenderer.currentBoundedCamera, new EntitySerializationInfo(true, true), new BoundingBoxComponent(min, max)}).collect(Collectors.toList())
                            );
                        } else {
                            OxyModel m = scene.createModelEntity(mesh, shader);
                            m.originPos = new Vector3f(0, 0, 0);
                            m.addComponent(new UUIDComponent(UUID.fromString(id)), new NativeTagComponent(nativeTag), new TagComponent(name), new TransformComponent(pos, rot, scale), new OxyMaterial(OxyTexture.loadImage(aT),
                                            OxyTexture.loadImage(nMT), OxyTexture.loadImage(rMT), OxyTexture.loadImage(mMT), OxyTexture.loadImage(aMT), null, new OxyColor(color)),
                                    new SelectedComponent(false), OxyRenderer.currentBoundedCamera, new EntitySerializationInfo(false, true), new BoundingBoxComponent(min, max)
                            );
                            if (emitting) {
                                Light pointLightComponent = new PointLight(1.0f, 0.027f, 0.0028f);
                                m.addComponent(pointLightComponent, new EmittingComponent(
                                        new Vector3f(pos),
                                        null,
                                        new Vector3f(2f, 2f, 2f),
                                        new Vector3f(5f, 5f, 5f),
                                        new Vector3f(1f, 1f, 1f)));
                            }
                            m.constructData();
                        }
                    }
                }
            }

            List<OxyModel> m = null;
            List<String> meshValue = new ArrayList<>();
            for (var entrySet : listOfEntries.entrySet()) {
                String mesh = entrySet.getKey();
                List<List<EntityComponent>> components = entrySet.getValue();
                if (!meshValue.contains(mesh)) {
                    m = scene.createModelEntities(mesh, shader);
                    var iterator = m.iterator();
                    while(iterator.hasNext()){
                        OxyModel oxyModel = iterator.next();
                        String nativeTagComponentFromModel = oxyModel.get(NativeTagComponent.class).originalTag();
                        int counter = 0;
                        for (NativeTagComponent importedNativeTag : listOfNativeTags) {
                            if (!nativeTagComponentFromModel.equals(importedNativeTag.originalTag())) {
                                counter++;
                                if (counter == listOfNativeTags.size()) {
                                    iterator.remove();
                                    scene.removeEntity(oxyModel);
                                }
                            }
                        }
                    }
                    meshValue.add(mesh);
                }

                if (m != null) {
                    for (int i = 0; i < m.size(); i++) {
                        m.get(i).originPos = new Vector3f(0, 0, 0);
                        m.get(i).addComponent(components.get(i));
                        m.get(i).constructData();
                    }
                }
                m = null;
            }

            for (OxyEntity modelInScene : scene.getEntities()) {
                List<ScriptingComponent> components = listOfScripts.get(modelInScene.get(UUIDComponent.class).getUUIDString());
                modelInScene.addScript(components);
            }
            return scene;
        }

        private static Vector3f getVector3fFromString(String sValue) {
            String[] splittedVector = sValue.split(", ");
            String[] valuesPos = new String[3];
            int ptr = 0;
            for (String s : splittedVector) {
                String[] valuesVector = s.split(" ");
                valuesPos[ptr++] = valuesVector[1];
            }
            return new Vector3f(Float.parseFloat(valuesPos[0]), Float.parseFloat(valuesPos[1]), Float.parseFloat(valuesPos[2]));
        }

        //I don't have to do this... but just to be sure
        @Override
        public void close() {
            oldScene = null;
            System.gc();
        }
    }
}