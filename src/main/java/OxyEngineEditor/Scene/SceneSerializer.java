package OxyEngineEditor.Scene;

import OxyEngineEditor.Components.ModelMesh;
import OxyEngineEditor.Components.TagComponent;
import OxyEngineEditor.Components.TransformComponent;
import OxyEngineEditor.Scene.Objects.Model.OxyMaterial;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import static OxyEngine.System.OxySystem.oxyAssert;

public final class SceneSerializer {

    private static final String fileExtension = ".osc";

    private static final String objTemplate = """
            \t\t\tObject %s {
                   \tName: %s
                   \tPos: %s
                   \tRot: %s
                   \tScale: %s
                   \tColor: %s
                   \tAlbedo Texture: %s
                   \tNormal Map Texture: %s
                   \tRoughness Map Texture: %s
                   \tAO Map Texture: %s
                   \tMetallic Map Texture: %s
                   \tMesh: %s
               }
               \t**objpl**""".trim();

    public static void serializeScene(Scene scene) {
        try (SceneWriter writer = new SceneWriter(new File(scene.getSceneName() + fileExtension))) {
            writer.writeScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Scene deserializeScene(File f) {
        try (SceneReader reader = new SceneReader(f)) {
            return reader.readScene();
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
            OxySerializable s = scene.getClass().getAnnotation(OxySerializable.class);
            String formattedOriginal = s.getInfo().formatted(scene.getSceneName(), scene.getShapeCount());
            StringBuilder info = new StringBuilder(formattedOriginal);
            int ptr = 1;

            for (OxyEntity e : scene.getEntities()) {

                String tag = "No tag given";
                TransformComponent transform = e.get(TransformComponent.class);
                String albedoColor = "No color given";
                String albedoTexture = "No albedo texture given";
                String normalTexture = "No normal texture given";
                String roughnessTexture = "No roughness texture given";
                String metallicTexture = "No metallic texture given";
                String aoTexture = "No ao texture given";
                String mesh = "No mesh given";

                if (e.has(TagComponent.class)) tag = e.get(TagComponent.class).tag();
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

                String formatObjTemplate = objTemplate.formatted(ptr++, tag, transform.position, transform.rotation,
                        transform.scale, albedoColor, albedoTexture, normalTexture, roughnessTexture, aoTexture, metallicTexture, mesh).trim();

                int insertionStart = info.indexOf("**objpl**");
                info.delete(insertionStart - 1, info.length());
                info.insert(insertionStart - 1, formatObjTemplate);
            }
            try {
                writer.write(info.toString().replace("**objpl**", "").trim() + "\n}");
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

    private static final class SceneReader implements AutoCloseable {

        private FileReader reader;

        public SceneReader(File f) {
            try {
                reader = new FileReader(f);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public Scene readScene() {
            return null;
        }

        @Override
        public void close() throws Exception {
            reader.close();
        }
    }
}
