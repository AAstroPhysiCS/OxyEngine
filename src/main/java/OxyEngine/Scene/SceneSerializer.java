package OxyEngine.Scene;

import OxyEngine.Components.*;
import OxyEngine.Core.Context.Renderer.Buffer.OpenGLMesh;
import OxyEngine.Core.Context.Renderer.Light.DirectionalLight;
import OxyEngine.Core.Context.Renderer.Light.Light;
import OxyEngine.Core.Context.Renderer.Light.PointLight;
import OxyEngine.Core.Context.Renderer.Light.SkyLight;
import OxyEngine.Core.Context.Renderer.Texture.HDRTexture;
import OxyEngine.Core.Context.Renderer.Texture.OxyColor;
import OxyEngine.Scripting.OxyScript;
import org.joml.Vector3f;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static OxyEngine.System.OxySystem.parseStringToFloatArray;
import static OxyEngine.System.OxySystem.parseStringToVector3f;

public final class SceneSerializer {

    public static final String fileExtension = ".osc", extensionName = "osc";

    private static SceneWriter WRITER = null;
    private static SceneReader READER = null;
    private static final OxyJSON INSTANCE = OxyJSON.getInstance();

    public static void serializeScene(String path) {
        if (WRITER == null) WRITER = new SceneWriter();
        WRITER.writeScene(new File(path), SceneRuntime.ACTIVE_SCENE);
    }

    public static Scene deserializeScene(String path) {
        if (READER == null) READER = new SceneReader();
        return READER.readScene(path);
    }

    private static final class SceneWriter {

        public void writeScene(File f, Scene scene) {
            INSTANCE.openWritingStream()
                    .file(f)
                    .createOxyJSONObject("Scene")
                    .putField("Scene Name", scene.getSceneName())
                    .putField("Scene Working Directory", scene.getWorkingDirectory())
                    .putField("Scene Gamma Strength", String.valueOf(scene.gammaStrength))
                    .putField("Scene Exposure", String.valueOf(scene.exposure))
                    .separate();

            OxyJSON.OxyJSONWriterBuilder builder = INSTANCE.openWritingStream();

            var array = builder.createOxyJSONArray("Registry");
            List<OxyEntity> rootPooled = new ArrayList<>();
            for (OxyEntity e : scene.getEntities()) {
                if (!e.familyHasRoot()) {
                    if (!rootPooled.contains(e)) rootPooled.add(e);
                }
            }
            for (OxyEntity root : rootPooled) {
                if (!(root instanceof OxyModel) & !root.has(SkyLight.class)) continue;
                var obj = array.createOxyJSONObject(root.get(TagComponent.class).tag());
                int meshPosRoot = -1;
                String idRoot = root.get(UUIDComponent.class).getUUIDString();
                String tagRoot = "null";
                if (root.has(MeshPosition.class)) meshPosRoot = root.get(MeshPosition.class).meshPos();
                if (root.has(TagComponent.class)) tagRoot = root.get(TagComponent.class).tag();
                boolean emittingRoot = root.has(Light.class);

                obj.putField("ID", idRoot)
                        .putField("Mesh Position", String.valueOf(meshPosRoot))
                        .putField("Name", tagRoot)
                        .putField("Emitting", String.valueOf(emittingRoot));
                addCommonFields(obj, emittingRoot, root);
                dump(obj, root.getEntitiesRelatedTo());
            }
            builder.build().writeAndCloseStream();
        }


        private static void dump(OxyJSON.OxyJSONObject arr, List<OxyEntity> relatedToList) {
            if (relatedToList.size() == 0) return;

            for (OxyEntity e : relatedToList) {
                if (!(e instanceof OxyModel)) continue;
                int meshPos = -1;
                String id = e.get(UUIDComponent.class).getUUIDString();
                String tag = "null";
                if (e.has(MeshPosition.class)) meshPos = e.get(MeshPosition.class).meshPos();
                if (e.has(TagComponent.class)) tag = e.get(TagComponent.class).tag();
                boolean emitting = e.has(Light.class);

                var obj = arr.createInnerObject(e.get(TagComponent.class).tag())
                        .putField("ID", id)
                        .putField("Mesh Position", String.valueOf(meshPos))
                        .putField("Name", tag)
                        .putField("Emitting", String.valueOf(emitting));

                addCommonFields(obj, emitting, e);

                List<OxyEntity> related = e.getEntitiesRelatedTo();
                if (related.size() != 0) {
                    dump(obj, related);
                }
            }
        }

        private static void addCommonFields(OxyJSON.OxyJSONObject obj, boolean emitting, OxyEntity e) {

            TransformComponent transform = e.get(TransformComponent.class);
            var ref = new Object() {
                String albedoColor = "null";
                String albedoTexture = "null";
                String normalTexture = "null", normalTextureStrength = "0";
                String roughnessTexture = "null", roughnessTextureStrength = "0";
                String metallicTexture = "null", metalnessTextureStrength = "0";
                String aoTexture = "null", aoTextureStrength = "0";
                String emissiveTexture = "null", emissiveTextureStrength = "0";
                String mesh = "null";
                String materialName = "null";
            };

            Vector3f minBound = new Vector3f(0, 0, 0), maxBound = new Vector3f(0, 0, 0);
            if (e.has(BoundingBoxComponent.class)) {
                minBound = e.get(BoundingBoxComponent.class).min();
                maxBound = e.get(BoundingBoxComponent.class).max();
            }
            if (e.has(OxyMaterialIndex.class)) {
                OxyMaterialPool.getMaterial(e).ifPresent((m) -> {
                    ref.materialName = m.name;
                    if (m.albedoColor != null) ref.albedoColor = Arrays.toString(m.albedoColor.getNumbers());
                    if (m.albedoTexture != null) ref.albedoTexture = m.albedoTexture.getPath();
                    if (m.normalTexture != null) ref.normalTexture = m.normalTexture.getPath();
                    ref.normalTextureStrength = String.valueOf(m.normalStrength[0]);
                    if (m.roughnessTexture != null) ref.roughnessTexture = m.roughnessTexture.getPath();
                    ref.roughnessTextureStrength = String.valueOf(m.roughness[0]);
                    if (m.metallicTexture != null) ref.metallicTexture = m.metallicTexture.getPath();
                    ref.metalnessTextureStrength = String.valueOf(m.metalness[0]);
                    if (m.aoTexture != null) ref.aoTexture = m.aoTexture.getPath();
                    ref.aoTextureStrength = String.valueOf(m.aoStrength[0]);
                    if (m.emissiveTexture != null) ref.emissiveTexture = m.emissiveTexture.getPath();
                    ref.emissiveTextureStrength = String.valueOf(m.emissiveStrength[0]);
                });
            }
            if (e.has(OpenGLMesh.class)) ref.mesh = e.get(OpenGLMesh.class).getPath();
            if (e.has(Light.class)) emitting = true;

            if (emitting) {
                Light l = e.get(Light.class);
                obj = obj.createInnerObject("Light Attributes")
                        .putField("Intensity", String.valueOf(l.getColorIntensity()));
                if (l instanceof PointLight p) {
                    obj.putField("Constant", String.valueOf(p.getConstantValue()));
                    obj.putField("Linear", String.valueOf(p.getLinearValue()));
                    obj.putField("Quadratic", String.valueOf(p.getQuadraticValue()));
                } else if (l instanceof DirectionalLight) {
                    obj.putField("Direction", e.get(TransformComponent.class).rotation.toString());
                } else if (l instanceof SkyLight s) {
                    HDRTexture hdrTexture = s.getHDRTexture();
                    if (hdrTexture != null) obj.putField("Environment Map", s.getHDRTexture().getPath());
                    else obj.putField("Environment Map", "null");

                    obj.putField("Environment LOD", String.valueOf(s.mipLevelStrength[0]));
                    obj.putField("Environment Intensity", String.valueOf(s.intensity[0]));
                    obj.putField("Environment Primary", String.valueOf(s.isPrimary()));
                }
                obj = obj.backToObject();
            }

            obj = obj.putField("Emitting Type", emitting ? e.get(Light.class).getClass().getSimpleName() : "null")
                    .putField("Position", transform.position.toString())
                    .putField("Rotation", transform.rotation.toString())
                    .putField("Scale", transform.scale.toString())
                    .putField("Bounds Min", minBound.toString())
                    .putField("Bounds Max", maxBound.toString())
                    .putField("Material Name", ref.materialName)
                    .putField("Color", ref.albedoColor)
                    .putField("Albedo Texture", ref.albedoTexture)
                    .putField("Normal Map Texture", ref.normalTexture)
                    .putField("Normal Map Strength", ref.normalTextureStrength)
                    .putField("Roughness Map Texture", ref.roughnessTexture)
                    .putField("Roughness Map Strength", ref.roughnessTextureStrength)
                    .putField("AO Map Texture", ref.aoTexture)
                    .putField("AO Map Strength", ref.aoTextureStrength)
                    .putField("Metallic Map Texture", ref.metallicTexture)
                    .putField("Metallic Map Strength", ref.metalnessTextureStrength)
                    .putField("Emissive Map Texture", ref.emissiveTexture)
                    .putField("Emissive Map Strength", ref.emissiveTextureStrength)
                    .putField("Mesh", ref.mesh)
                    .createInnerObject("Script");
            for (var scripts : e.getScripts()) obj.putField("Path", scripts.getPath());
        }
    }

    private static final class SceneReader {

        public Scene readScene(String path) {
            SceneRuntime.stop();

            var modelsJSON = new OxyJSON.OxyJSONArray();
            var sceneJSON = new OxyJSON.OxyJSONObject();

            INSTANCE.openReadingStream()
                    .read(path)
                    .getOxyJSONArray("Registry", modelsJSON)
                    .getOxyJSONObject("Scene", sceneJSON);

            String sceneName = sceneJSON.getField("Scene Name").value();
            String sceneWorkingDir = sceneJSON.getField("Scene Working Directory").value();

            Scene oldScene = SceneRuntime.ACTIVE_SCENE;
            oldScene.disposeAllModels();

            Scene scene = new Scene(sceneName, sceneWorkingDir);
            scene.gammaStrength = Float.parseFloat(sceneJSON.getField("Scene Gamma Strength").value());
            scene.exposure = Float.parseFloat(sceneJSON.getField("Scene Exposure").value());

            for (var n : oldScene.getEntityEntrySet()) {
                OxyEntity key = n.getKey();
                scene.put(key);
                scene.addComponent(key, n.getValue().toArray(EntityComponent[]::new));
            }
            SceneRuntime.entityContext = null;
            SceneRuntime.currentBoundedSkyLight = null;
            SceneRenderer.getInstance().clear();

            Scene.optimization_Path = "";

            for (var root : modelsJSON.getObjectList()) {
                var familyComponentRoot = new EntityFamily();
                OxyEntity rootEntity = readFields(root, scene);
                rootEntity.setFamily(familyComponentRoot);
                rootEntity.transformLocally();
                readAllInnerObjects(root, scene, rootEntity);
            }

            //I don't have to do this... but just to be sure
            System.gc();
            return scene;
        }

        private static void readAllInnerObjects(OxyJSON.OxyJSONObject root, Scene scene, OxyEntity rootEntity) {
            for (var ent : root.getInnerObjects()) {
                if (ent.getName().startsWith("Script")) continue;
                if (ent.getName().startsWith("Light Attributes")) continue;

                var childFamilyComponent = new EntityFamily(rootEntity.getFamily());
                OxyEntity childEntity = readFields(ent, scene);
                childEntity.setFamily(childFamilyComponent);
                childEntity.transformLocally();

                readAllInnerObjects(ent, scene, childEntity);
            }
        }


        private static OxyEntity readFields(OxyJSON.OxyJSONObject ent, Scene scene) {
            String name = ent.getField("Name").value();
            String id = ent.getField("ID").value();
            int meshPos = Integer.parseInt(ent.getField("Mesh Position").value());

            boolean emitting = Boolean.parseBoolean(ent.getField("Emitting").value());
            String emittingType = ent.getField("Emitting Type").value();

            //SKYLIGHT
            if (emitting) {
                if (emittingType.equals(SkyLight.class.getSimpleName())) {
                    var lightAttributes = ent.getInnerObjectByName("Light Attributes");
                    String path = lightAttributes.getField("Environment Map").value();

                    OxyNativeObject skyLightEnt = scene.createSkyLight();
                    SkyLight skyLightComp = skyLightEnt.get(SkyLight.class);
                    if (!path.equals("null")) skyLightComp.loadHDR(path);

                    skyLightComp.mipLevelStrength = new float[]{Float.parseFloat(lightAttributes.getField("Environment LOD").value())};
                    skyLightComp.intensity = new float[]{Float.parseFloat(lightAttributes.getField("Environment Intensity").value())};
                    skyLightComp.setPrimary(Boolean.parseBoolean(lightAttributes.getField("Environment Primary").value()));

                    return skyLightEnt;
                }
            }

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
            String emissiveMapTPath = ent.getField("Emissive Map Texture").value();
            float emissiveMapStrength = Float.parseFloat(ent.getField("Emissive Map Strength").value());
            String meshPath = ent.getField("Mesh").value();

            OxyModel modelInstance;

            int index = OxyMaterialPool.addMaterial(nameMaterial, albedoTPath,
                    normalMapTPath, roughnessMapTPath, metallicMapTPath, aoMapTPath, emissiveMapTPath,
                    new OxyColor(color), normalMapStrength, aoMapStrength, roughnessMapStrength, metallicMapStrength, emissiveMapStrength);

            if (!meshPath.equals("null")) {
                modelInstance = scene.createModelEntity(meshPath, meshPos, index);
                modelInstance.getGUINodes().add(OxyMaterial.guiNode);
            } else {
                modelInstance = scene.createEmptyModel(meshPos);
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
                    modelInstance.addComponent(new DirectionalLight(colorIntensity));
                    modelInstance.getGUINodes().add(DirectionalLight.guiNode);
                }

                modelInstance.addComponent(new OxyMaterialIndex(index));

                if (!modelInstance.getGUINodes().contains(OxyMaterial.guiNode))
                    modelInstance.getGUINodes().add(OxyMaterial.guiNode);
            }

            TransformComponent t = new TransformComponent(position, rot, scale);
            modelInstance.importedFromFile = true;
            modelInstance.addComponent(new MeshPosition(meshPos), new TagComponent(name), t,
                    new SelectedComponent(false), new BoundingBoxComponent(minB, maxB));

            var scripts = ent.getInnerObjectByName("Script");
            for (var f : scripts.getFieldList()) modelInstance.addScript(new OxyScript(f.value()));

            return modelInstance;
        }
    }
}