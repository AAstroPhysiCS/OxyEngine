package OxyEngine.Core.Scene;

import OxyEngine.Components.*;
import OxyEngine.Core.Camera.Camera;
import OxyEngine.Core.Camera.SceneCamera;
import OxyEngine.Core.Renderer.Light.*;
import OxyEngine.Core.Renderer.Mesh.BufferUsage;
import OxyEngine.Core.Renderer.Mesh.OpenGLMesh;
import OxyEngine.Core.Renderer.Pipeline;
import OxyEngine.Core.Renderer.Renderer;
import OxyEngine.Core.Renderer.Texture.*;
import OxyEngine.Core.Scene.OxyJSON.OxyJSONArray;
import OxyEngine.Core.Scene.OxyJSON.OxyJSONObject;
import OxyEngine.Core.Scene.OxyJSON.OxyJSONWriterBuilder;
import OxyEngine.PhysX.PhysXActor;
import OxyEngine.PhysX.PhysXComponent;
import OxyEngine.PhysX.PhysXGeometry;
import OxyEngine.PhysX.PhysXMaterial;
import OxyEngine.Scripting.Script;
import org.joml.Vector3f;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static OxyEngine.System.OxySystem.parseStringToFloatArray;
import static OxyEngine.System.OxySystem.parseStringToVector3f;
import static OxyEngineEditor.UI.UIAssetManager.DEFAULT_TEXTURE_PARAMETER;

public final class SceneSerializer {

    public static final String fileExtension = ".osc", extensionName = "osc";

    private static SceneWriter WRITER = null;
    private static SceneReader READER = null;
    private static final OxyJSON INSTANCE = OxyJSON.getInstance();

    public static void serializeScene(String path) {
        if (WRITER == null) WRITER = new SceneWriter();
        WRITER.writeScene(new File(path), SceneRuntime.sceneContext);
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
                    .putField("Scene Gamma Strength", String.valueOf(scene.gammaStrength[0]))
                    .putField("Scene Exposure", String.valueOf(scene.exposure[0]))
                    .separate();

            OxyJSONWriterBuilder builder = INSTANCE.openWritingStream();

            var registryArray = builder.createOxyJSONArray("Registry");

            /*
             * sorting because otherwise, there may be a exception regarding the root.
             * if a entity has a root, that entity should be less prioritised than a entity without one
             * simple reason: root's cant have roots and so you ensure that all the root entities are being added first.
             */
            List<Entity> sortedEntityList = new ArrayList<>(scene.getEntities());
            sortedEntityList.sort((o1, o2) -> {
                if (!o1.familyHasRoot() && o2.familyHasRoot()) return -1;
                if (o1.familyHasRoot() && !o2.familyHasRoot()) return 1;
                if (!o1.familyHasRoot() && !o2.familyHasRoot()) return 0;
                if (o1.familyHasRoot() && o2.familyHasRoot()) return 0;
                return 0;
            });

            for (Entity e : sortedEntityList) {
                String tag = e.get(TagComponent.class).tag();
                var entityOxyJSON = registryArray.createOxyJSONObject(tag);
                String id = e.get(UUIDComponent.class).getUUID();
                boolean emitting = e.has(Light.class) || e.has(SkyLight.class);
                entityOxyJSON.putField("ID", id)
                        .putField("Name", tag)
                        .putField("Emitting", String.valueOf(emitting));
                addCommonFields(entityOxyJSON, emitting, e);
            }
            builder.build().writeAndCloseStream();
        }

        private static void addCommonFields(OxyJSONObject entityOxyJSON, boolean emitting, Entity e) {

            TransformComponent transformComponent = e.get(TransformComponent.class);
            OpenGLMesh mesh = null;

            Vector3f minBound = new Vector3f(0, 0, 0), maxBound = new Vector3f(0, 0, 0);
            if (e.has(OpenGLMesh.class)) {
                mesh = e.get(OpenGLMesh.class);
                BoundingBox boundingBox = mesh.getAABB();
                minBound = boundingBox.min();
                maxBound = boundingBox.max();
            }
            if (e.has(Light.class) || e.has(SkyLight.class)) emitting = true;

            if (emitting) {
                Light l = e.get(Light.class);
                if(l != null) {
                    entityOxyJSON = entityOxyJSON.createInnerObject("Light Attributes")
                            .putField("Intensity", String.valueOf(l.getColorIntensity()));
                    if (l instanceof PointLight p) {
                        entityOxyJSON.putField("Radius", String.valueOf(p.getRadius()));
                        entityOxyJSON.putField("Cutoff", String.valueOf(p.getCutoff()));
                    } else if (l instanceof DirectionalLight) {
                        entityOxyJSON.putField("Direction", e.getRotation().toString());
                    }
                }

                SkyLight skyLight = e.get(SkyLight.class);
                if(skyLight != null) {
                    entityOxyJSON = entityOxyJSON.createInnerObject("Light Attributes")
                            .putField("Intensity", String.valueOf(skyLight.intensity[0]));
                    if (skyLight instanceof HDREnvironmentMap s) {
                        EnvironmentTexture environmentTexture = s.getEnvironmentTexture();
                        if (environmentTexture != null)
                            entityOxyJSON.putField("Environment Map", environmentTexture.getPath());
                        else entityOxyJSON.putField("Environment Map", "null");

                        entityOxyJSON.putField("Environment LOD", String.valueOf(s.mipLevelStrength[0]));
                        entityOxyJSON.putField("Environment Intensity", String.valueOf(s.intensity[0]));
                        entityOxyJSON.putField("Environment Primary", String.valueOf(s.isPrimary()));
                    } else if (skyLight instanceof DynamicSky s) {
                        entityOxyJSON.putField("Dynamic Sky Turbidity", String.valueOf(s.getTurbidity()[0]));
                        entityOxyJSON.putField("Dynamic Sky Azimuth", String.valueOf(s.getAzimuth()[0]));
                        entityOxyJSON.putField("Dynamic Sky Inclination", String.valueOf(s.getInclination()[0]));
                        entityOxyJSON.putField("Dynamic Sky Sun Direction", s.getDynamicSkySunDir().toString());
                        entityOxyJSON.putField("Environment Intensity", String.valueOf(s.intensity[0]));
                        entityOxyJSON.putField("Environment Primary", String.valueOf(s.isPrimary()));
                    }
                }
                entityOxyJSON = entityOxyJSON.backToObject();
            }

            entityOxyJSON = entityOxyJSON.putField("Hidden", String.valueOf(e.has(HiddenComponent.class)));

            if (e.has(PhysXComponent.class)) {
                PhysXComponent physXComponent = e.get(PhysXComponent.class);

                PhysXActor actor = physXComponent.getActor();
                PhysXGeometry geometry = physXComponent.getGeometry();
                PhysXMaterial material = physXComponent.getMaterial();

                entityOxyJSON = entityOxyJSON.createInnerObject("PhysX")
                        .putField("Rigid Body Type", actor.getBodyType().toString())
                        .putField("Collider Type", geometry.getColliderType())
                        .putField("Dynamic Friction", String.valueOf(material.dynamicFriction[0]))
                        .putField("Static Friction", String.valueOf(material.staticFriction[0]))
                        .putField("Restitution", String.valueOf(material.restitution[0]));

                entityOxyJSON = entityOxyJSON.backToObject();
            }


            entityOxyJSON.putField("Camera", String.valueOf(e.has(Camera.class)));

            Entity root = e.getRoot();
            entityOxyJSON = entityOxyJSON.createInnerObject("Family")
                    .putField("Root", root != null ? root.get(UUIDComponent.class).getUUID() : "null")
                    .backToObject();

            String emittingType = "null";
            if(e.has(Light.class)) {
                emittingType = e.get(Light.class).getClass().getSimpleName();
            } else if (e.has(SkyLight.class)){
                emittingType = e.get(SkyLight.class).getClass().getSimpleName();
            }

            entityOxyJSON = entityOxyJSON.putField("Emitting Type", emittingType)
                    .putField("Position", transformComponent.position.toString())
                    .putField("Rotation", transformComponent.rotation.toString())
                    .putField("Scale", transformComponent.scale.toString())
                    .putField("Bounds Min", minBound.toString())
                    .putField("Bounds Max", maxBound.toString())
                    .putField("Mesh Path", mesh != null ? mesh.getPath() : "null")
                    .createInnerObject("Script");
            for (var scripts : e.getScripts()) entityOxyJSON.putField("Path", scripts.getPath());
            entityOxyJSON = entityOxyJSON.backToObject();

            entityOxyJSON.putField("Submesh Indices", mesh != null ? mesh.getSubmeshes().stream().map(OpenGLMesh.Submesh::assimpMaterialIndex).sorted().toList().toString() : "");

            List<Material> materials = mesh != null ? mesh.getMaterials() : null;
            if (materials != null) {
                for (int i = 0; i < materials.size(); i++) {

                    Material material = materials.get(i);

                    Color albedoColor = material.albedoColor;

                    Image2DTexture albedoTexture = material.albedoTexture;
                    Image2DTexture normalTexture = material.normalTexture;
                    Image2DTexture roughnessTexture = material.roughnessTexture;
                    Image2DTexture aoTexture = material.aoTexture;
                    Image2DTexture emissiveTexture = material.emissiveTexture;
                    Image2DTexture metallicTexture = material.metallicTexture;

                    float[] metalnessStrength = material.metalness;
                    float[] roughnessStrength = material.roughness;
                    float[] aoStrength = material.aoStrength;
                    float[] emissiveStrength = material.emissiveStrength;
                    float[] normalStrength = material.normalStrength;

                    entityOxyJSON = entityOxyJSON.createInnerObject("Material " + i)
                            .putField("Material Index", String.valueOf(material.getAssimpMaterialIndex()))
                            .putField("Material Name", material.name)
                            .putField("Color", albedoColor != null ? Arrays.toString(albedoColor.getNumbers()) : "null")
                            .putField("Albedo Texture", albedoTexture != null ? albedoTexture.getPath() : "null")
                            .putField("Normal Map Texture", normalTexture != null ? normalTexture.getPath() : "null")
                            .putField("Normal Map Strength", String.valueOf(normalStrength[0]))
                            .putField("Roughness Map Texture", roughnessTexture != null ? roughnessTexture.getPath() : "null")
                            .putField("Roughness Map Strength", String.valueOf(roughnessStrength[0]))
                            .putField("AO Map Texture", aoTexture != null ? aoTexture.getPath() : "null")
                            .putField("AO Map Strength", String.valueOf(aoStrength[0]))
                            .putField("Metallic Map Texture", metallicTexture != null ? metallicTexture.getPath() : "null")
                            .putField("Metallic Map Strength", String.valueOf(metalnessStrength[0]))
                            .putField("Emissive Map Texture", emissiveTexture != null ? emissiveTexture.getPath() : "null")
                            .putField("Emissive Map Strength", String.valueOf(emissiveStrength[0]))
                            .backToObject();
                }
            }
        }
    }

    private static final class SceneReader {

        public Scene readScene(String path) {
            SceneRuntime.runtimeStop();

            var modelsJSON = new OxyJSONArray();
            var sceneJSON = new OxyJSONObject();

            INSTANCE.openReadingStream()
                    .read(path)
                    .getOxyJSONArray("Registry", modelsJSON)
                    .getOxyJSONObject("Scene", sceneJSON);

            String sceneName = sceneJSON.getField("Scene Name").value();
            String sceneWorkingDir = sceneJSON.getField("Scene Working Directory").value();

            Scene oldScene = SceneRuntime.sceneContext;

            Scene newScene = new Scene(sceneName, sceneWorkingDir);
            newScene.gammaStrength = new float[]{Float.parseFloat(sceneJSON.getField("Scene Gamma Strength").value())};
            newScene.exposure = new float[]{Float.parseFloat(sceneJSON.getField("Scene Exposure").value())};

            oldScene.disposeAllEntities(); //disposing all entities (exception editor camera)

            SceneRuntime.entityContext = null;
            SceneRuntime.skyLightEntityContext = null;

            for (var jsonObject : modelsJSON.getObjectList()) {
                Entity entity = readFields(jsonObject, newScene);
                readPhysXFields(jsonObject, entity);
                entity.updateTransform();
            }

            //I don't have to do this... but just to be sure
            System.gc();
            return newScene;
        }

        private static void readPhysXFields(OxyJSONObject ent, Entity entity) {
            OxyJSONObject physXObject = ent.getInnerObjectByName("PhysX");
            if (physXObject != null) {

                float dynamicFr = Float.parseFloat(physXObject.getField("Dynamic Friction").value());
                float staticFr = Float.parseFloat(physXObject.getField("Static Friction").value());
                float restitution = Float.parseFloat(physXObject.getField("Restitution").value());

                String rigidBodyType = physXObject.getField("Rigid Body Type").value();
                String colliderType = physXObject.getField("Collider Type").value();

                PhysXComponent physXComponent = new PhysXComponent(entity, new PhysXMaterial(staticFr, dynamicFr, restitution),
                        rigidBodyType, colliderType);
                entity.addComponent(physXComponent);
            }
        }

        private static Entity readFields(OxyJSONObject ent, Scene scene) {

            Entity entity = scene.createEmptyEntity();

            String meshPath = ent.getField("Mesh Path").value();
            String name = ent.getField("Name").value();
            String id = ent.getField("ID").value();
            Vector3f position = parseStringToVector3f(ent.getField("Position").value());
            Vector3f rot = parseStringToVector3f(ent.getField("Rotation").value());
            Vector3f scale = parseStringToVector3f(ent.getField("Scale").value());
            Vector3f minB = parseStringToVector3f(ent.getField("Bounds Min").value());
            Vector3f maxB = parseStringToVector3f(ent.getField("Bounds Max").value());

            entity.addComponent(new TagComponent(name), new TransformComponent(position, rot, scale), new SelectedComponent(false),
                    new UUIDComponent(id));

            boolean isCamera = Boolean.parseBoolean(ent.getField("Camera").value());
            if (isCamera) {
                entity.addComponent(new SceneCamera(entity.getTransform()));
                entity.getGUINodes().add(SceneCamera.guiNode);
            }

            if (ent.getField("Hidden").value().equals("true")) entity.addComponent(new HiddenComponent());

            var scripts = ent.getInnerObjectByName("Script");
            for (var f : scripts.getFieldList()) entity.addScript(new Script(f.value()));

            boolean emitting = Boolean.parseBoolean(ent.getField("Emitting").value());
            String emittingType = ent.getField("Emitting Type").value();

            if (emitting) {
                var lightAttributes = ent.getInnerObjectByName("Light Attributes");
                if (emittingType.equals(HDREnvironmentMap.class.getSimpleName())) {
                    String path = lightAttributes.getField("Environment Map").value();

                    HDREnvironmentMap envMap = new HDREnvironmentMap();
                    entity.addComponent(envMap);
                    if (!path.equals("null")) {
                        envMap.loadEnvironmentMap(path);
                        envMap.mipLevelStrength = new float[]{Float.parseFloat(lightAttributes.getField("Environment LOD").value())};
                    }

                    envMap.intensity = new float[]{Float.parseFloat(lightAttributes.getField("Environment Intensity").value())};
                    envMap.setPrimary(Boolean.parseBoolean(lightAttributes.getField("Environment Primary").value()));
                    entity.getGUINodes().add(SkyLight.guiNode);

                    Renderer.submitSkyLight(envMap);

                } else if (emittingType.equals(DynamicSky.class.getSimpleName())) {

                    DynamicSky envMap = new DynamicSky();
                    entity.addComponent(envMap);

                    envMap.setTurbidity(Float.parseFloat(lightAttributes.getField("Dynamic Sky Turbidity").value()));
                    envMap.setAzimuth(Float.parseFloat(lightAttributes.getField("Dynamic Sky Azimuth").value()));
                    envMap.setInclination(Float.parseFloat(lightAttributes.getField("Dynamic Sky Inclination").value()));
                    envMap.setDynamicSkySunDir(parseStringToVector3f(lightAttributes.getField("Dynamic Sky Sun Direction").value()));
                    envMap.load();

                    envMap.intensity = new float[]{Float.parseFloat(lightAttributes.getField("Environment Intensity").value())};
                    envMap.setPrimary(Boolean.parseBoolean(lightAttributes.getField("Environment Primary").value()));
                    entity.getGUINodes().add(SkyLight.guiNode);

                    Renderer.submitSkyLight(envMap);

                } else if (emittingType.equals(PointLight.class.getSimpleName())) {
                    float colorIntensity = Float.parseFloat(lightAttributes.getField("Intensity").value());
                    float radius = Float.parseFloat(lightAttributes.getField("Radius").value());
                    float cutoff = Float.parseFloat(lightAttributes.getField("Cutoff").value());

                    PointLight pointLight = new PointLight(colorIntensity, radius, cutoff);

                    entity.addComponent(pointLight);
                    entity.getGUINodes().add(PointLight.guiNode);

                    Renderer.submitPointLight(pointLight);

                } else if (emittingType.equals(DirectionalLight.class.getSimpleName())) {
                    float colorIntensity = Float.parseFloat(lightAttributes.getField("Intensity").value());
                    DirectionalLight directionalLight = new DirectionalLight(colorIntensity);
                    entity.addComponent(directionalLight);
                    entity.getGUINodes().add(DirectionalLight.guiNode);

                    Renderer.submitDirectionalLight(directionalLight);
                }
            }

            if (!meshPath.isEmpty() && !meshPath.equals("null")) {
                Pipeline geometryPipeline = Renderer.getGeometryPipeline();
                OpenGLMesh mesh = new OpenGLMesh(geometryPipeline, BufferUsage.STATIC);
                mesh.setAABB(minB, maxB);
                //adding materials
                for (int i = 0; ; i++) {
                    var materials = ent.getInnerObjectByName("Material " + i);
                    if (materials == null) break; //loop, unless there's no more material available
                    int materialIndex = Integer.parseInt(materials.getField("Material Index").value());
                    float[] color = parseStringToFloatArray(materials.getField("Color").value(), 4);
                    String nameMaterial = materials.getField("Material Name").value();
                    String albedoTPath = materials.getField("Albedo Texture").value();
                    String normalMapTPath = materials.getField("Normal Map Texture").value();
                    float normalMapStrength = Float.parseFloat(materials.getField("Normal Map Strength").value());
                    String roughnessMapTPath = materials.getField("Roughness Map Texture").value();
                    float roughnessMapStrength = Float.parseFloat(materials.getField("Roughness Map Strength").value());
                    String aoMapTPath = materials.getField("AO Map Texture").value();
                    float aoMapStrength = Float.parseFloat(materials.getField("AO Map Strength").value());
                    String metallicMapTPath = materials.getField("Metallic Map Texture").value();
                    float metallicMapStrength = Float.parseFloat(materials.getField("Metallic Map Strength").value());
                    String emissiveMapTPath = materials.getField("Emissive Map Texture").value();
                    float emissiveMapStrength = Float.parseFloat(materials.getField("Emissive Map Strength").value());

                    Material material = Material.create(materialIndex);
                    material.name = nameMaterial;
                    material.albedoColor = new Color(color);
                    material.albedoTexture = Texture.loadImage(TextureSlot.ALBEDO, albedoTPath, TexturePixelType.UByte, DEFAULT_TEXTURE_PARAMETER);
                    material.normalTexture = Texture.loadImage(TextureSlot.NORMAL, normalMapTPath, TexturePixelType.UByte, DEFAULT_TEXTURE_PARAMETER);
                    material.roughnessTexture = Texture.loadImage(TextureSlot.ROUGHNESS, roughnessMapTPath, TexturePixelType.UByte, DEFAULT_TEXTURE_PARAMETER);
                    material.metallicTexture = Texture.loadImage(TextureSlot.METALLIC, metallicMapTPath, TexturePixelType.UByte, DEFAULT_TEXTURE_PARAMETER);
                    material.aoTexture = Texture.loadImage(TextureSlot.AO, aoMapTPath, TexturePixelType.UByte, DEFAULT_TEXTURE_PARAMETER);
                    material.emissiveTexture = Texture.loadImage(TextureSlot.EMISSIVE, emissiveMapTPath, TexturePixelType.UByte, DEFAULT_TEXTURE_PARAMETER);
                    material.roughness[0] = roughnessMapStrength;
                    material.metalness[0] = metallicMapStrength;
                    material.normalStrength[0] = normalMapStrength;
                    material.aoStrength[0] = aoMapStrength;
                    material.emissiveStrength[0] = emissiveMapStrength;
                    mesh.addMaterial(material);
                }
                mesh.importScene(meshPath); //import the assimp scene
                if (mesh.getAIScene().mNumAnimations() > 0)
                    entity.addComponent(new AnimationComponent(mesh.getAIScene(), mesh.getBoneInfoMap())); //deletes aiScene
                entity.addComponent(mesh);
                entity.getGUINodes().add(OpenGLMesh.guiNode);
                Renderer.submitMesh(mesh, entity.get(TransformComponent.class), entity.get(AnimationComponent.class));
            }

            var family = ent.getInnerObjectByName("Family");
            if (family != null) {
                String val = family.getField("Root").value();
                if (!val.equals("null") && !val.isEmpty() && !val.isBlank()) {
                    Entity rootEntity = scene.getEntityByUUID(val);
                    entity.setFamily(new EntityFamily(rootEntity.getFamily()));
                }
            }

            return entity;
        }
    }
}