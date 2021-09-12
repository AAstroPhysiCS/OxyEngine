package OxyEngine.Core.Context.Scene;

import OxyEngine.Core.Context.Renderer.Renderer;
import OxyEngine.Core.Context.Renderer.Shader;
import OxyEngine.Core.Context.Renderer.Texture.*;
import OxyEngine.System.Disposable;
import org.joml.Vector4f;
import org.lwjgl.assimp.AIColor4D;
import org.lwjgl.assimp.AIMaterial;
import org.lwjgl.assimp.AIString;

import java.nio.IntBuffer;

import static OxyEngine.System.OxySystem.logger;
import static OxyEngineEditor.UI.UIAssetManager.DEFAULT_TEXTURE_PARAMETER;
import static org.lwjgl.assimp.Assimp.*;

public final class Material implements Disposable {

    private static int UNKNOWN_MATERIAL_COUNT = 0;

    private int assimpMaterialIndex;

    private Shader shader;

    public Image2DTexture albedoTexture, normalTexture, roughnessTexture, metallicTexture, aoTexture, emissiveTexture;
    public Color albedoColor;

    public String name;

    public float[] metalness, roughness, aoStrength, emissiveStrength;
    public float[] normalStrength;

    public static Material create(int assimpMaterialIndex) {
        return create(assimpMaterialIndex, Renderer.getShader("OxyPBR"));
    }

    public static Material create(int assimpMaterialIndex, Shader shader) {
        Material material = new Material();
        material.assimpMaterialIndex = assimpMaterialIndex;
        material.albedoColor = new Color("#FFFFFFF");
        material.shader = shader;
        material.normalStrength = new float[]{1.0f};
        material.aoStrength = new float[]{1.0f};
        material.emissiveStrength = new float[]{1.0f};
        material.roughness = new float[]{0.0f};
        material.metalness = new float[]{0.0f};
        material.name = "Basic Material";
        return material;
    }

    public static Material create(Material other) {
        Material material = new Material();
        if (other.roughnessTexture != null) material.roughnessTexture = Texture.loadImage(other.roughnessTexture);
        if (other.aoTexture != null) material.aoTexture = Texture.loadImage(other.aoTexture);
        if (other.normalTexture != null) material.normalTexture = Texture.loadImage(other.normalTexture);
        if (other.albedoTexture != null) material.albedoTexture = Texture.loadImage(other.albedoTexture);
        if (other.metallicTexture != null) material.metallicTexture = Texture.loadImage(other.metallicTexture);
        if (other.emissiveTexture != null) material.emissiveTexture = Texture.loadImage(other.emissiveTexture);
        material.shader = other.shader;
        material.albedoColor = new Color(other.albedoColor.getNumbers().clone());
        material.name = other.name + " Copy";
        material.metalness = other.metalness.clone();
        material.roughness = other.roughness.clone();
        material.aoStrength = other.aoStrength.clone();
        material.normalStrength = other.normalStrength.clone();
        material.emissiveStrength = other.emissiveStrength.clone();
        material.assimpMaterialIndex = other.assimpMaterialIndex;
        return material;
    }

    public static Material create(String meshPath, Shader shader, AIMaterial aiMaterial, int assimpMaterialIndex) {
        return new Material(meshPath, shader, aiMaterial, assimpMaterialIndex);
    }

    public static Material create(String meshPath, AIMaterial aiMaterial, int assimpMaterialIndex) {
        return new Material(meshPath, aiMaterial, assimpMaterialIndex);
    }

    private Material() {
    }

    private Material(String meshPath, AIMaterial aiMaterial, int assimpMaterialIndex) {
        this(meshPath, Renderer.getShader("OxyPBR"), aiMaterial, assimpMaterialIndex);
    }

    private Material(String meshPath, Shader shader, AIMaterial aiMaterial, int assimpMaterialIndex) {
        this.shader = shader;
        this.assimpMaterialIndex = assimpMaterialIndex;

        //loading values
        {
            AIColor4D color = AIColor4D.create();
            albedoColor = new Color(new Vector4f(1.0f, 1.0f, 1.0f, 1.0f));
            int result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_DIFFUSE, aiTextureType_NONE, 0, color);
            if (result == 0) {
                albedoColor = new Color(new Vector4f(color.r(), color.g(), color.b(), color.a()));
            }

            metalness = new float[]{0.0f};
            aiGetMaterialFloatArray(aiMaterial, AI_MATKEY_REFLECTIVITY, aiTextureType_NONE, 0, metalness, new int[]{1});

            roughness = new float[]{0.0f};
            aiGetMaterialFloatArray(aiMaterial, AI_MATKEY_SHININESS, aiTextureType_NONE, 0, roughness, new int[]{1});
            if (result == 0)
                roughness[0] = (float) (1.0f - Math.sqrt(roughness[0] / 100.0f));

            normalStrength = new float[]{1.0f};
            aoStrength = new float[]{1.0f};
            emissiveStrength = new float[]{1.0f};
        }


        //loading textures
        {
            AIString stringBuffer = AIString.calloc();

            aiGetMaterialString(aiMaterial, AI_MATKEY_NAME, aiTextureType_NONE, 0, stringBuffer);
            name = stringBuffer.dataString();
            stringBuffer.free();

            if (name.isEmpty() || name.isBlank()) name = "Basic Material (%s)".formatted(++UNKNOWN_MATERIAL_COUNT);

            stringBuffer = AIString.calloc();
            aiGetMaterialTexture(aiMaterial, aiTextureType_DIFFUSE, 0, stringBuffer, (IntBuffer) null, null, null, null, null, null);
            String textPathAlbedo = stringBuffer.dataString();
            stringBuffer.free();

            stringBuffer = AIString.calloc();
            aiGetMaterialTexture(aiMaterial, aiTextureType_NORMALS, 0, stringBuffer, (IntBuffer) null, null, null, null, null, null);
            String textPathNormals = stringBuffer.dataString();
            stringBuffer.free();

            stringBuffer = AIString.calloc();
            aiGetMaterialTexture(aiMaterial, aiTextureType_SHININESS, 0, stringBuffer, (IntBuffer) null, null, null, null, null, null);
            String textPathRoughness = stringBuffer.dataString();
            stringBuffer.free();

            stringBuffer = AIString.calloc();
            aiGetMaterialTexture(aiMaterial, aiTextureType_SPECULAR, 0, stringBuffer, (IntBuffer) null, null, null, null, null, null);
            String textPathMetallic = stringBuffer.dataString();
            stringBuffer.free();

            stringBuffer = AIString.calloc();
            final int aiTextureType_AMBIENT_OCCLUSION = 17;
            aiGetMaterialTexture(aiMaterial, aiTextureType_AMBIENT_OCCLUSION, 0, stringBuffer, (IntBuffer) null, null, null, null, null, null);
            String textPathAO = stringBuffer.dataString();
            stringBuffer.free();

            stringBuffer = AIString.calloc();
            aiGetMaterialTexture(aiMaterial, aiTextureType_EMISSIVE, 0, stringBuffer, (IntBuffer) null, null, null, null, null, null);
            String textPathEmissive = stringBuffer.dataString();
            stringBuffer.free();

            String warningString = "For the file: %s, ".formatted(meshPath);

            if (textPathAlbedo.isBlank() || textPathAlbedo.isEmpty()) {
                logger.warning(warningString + "Albedo map is empty!");
                textPathAlbedo = null;
            } else textPathAlbedo = meshPath + "\\" + textPathAlbedo;
            if (textPathNormals.isBlank() || textPathNormals.isEmpty()) {
                logger.warning(warningString + "Normal map is empty!");
                textPathNormals = null;
            } else textPathNormals = meshPath + "\\" + textPathNormals;
            if (textPathRoughness.isBlank() || textPathRoughness.isEmpty()) {
                logger.warning(warningString + "Roughness map is empty!");
                textPathRoughness = null;
            } else textPathRoughness = meshPath + "\\" + textPathRoughness;
            if (textPathMetallic.isBlank() || textPathMetallic.isEmpty()) {
                logger.warning(warningString + "Metallic map is empty!");
                textPathMetallic = null;
            } else textPathMetallic = meshPath + "\\" + textPathMetallic;
            if (textPathAO.isBlank() || textPathAO.isEmpty()) {
                logger.warning(warningString + "AO map is empty!");
                textPathAO = null;
            } else textPathAO = meshPath + "\\" + textPathAO;
            if (textPathEmissive.isBlank() || textPathEmissive.isEmpty()) {
                logger.warning(warningString + "Emissive map is empty!");
                textPathEmissive = null;
            } else textPathEmissive = meshPath + "\\" + textPathEmissive;

            albedoTexture = Texture.loadImage(TextureSlot.ALBEDO, textPathAlbedo, TexturePixelType.UByte, DEFAULT_TEXTURE_PARAMETER);
            normalTexture = Texture.loadImage(TextureSlot.NORMAL, textPathNormals, TexturePixelType.UByte, DEFAULT_TEXTURE_PARAMETER);
            roughnessTexture = Texture.loadImage(TextureSlot.ROUGHNESS, textPathRoughness, TexturePixelType.UByte, DEFAULT_TEXTURE_PARAMETER);
            metallicTexture = Texture.loadImage(TextureSlot.METALLIC, textPathMetallic, TexturePixelType.UByte, DEFAULT_TEXTURE_PARAMETER);
            aoTexture = Texture.loadImage(TextureSlot.AO, textPathAO, TexturePixelType.UByte, DEFAULT_TEXTURE_PARAMETER);
            emissiveTexture = Texture.loadImage(TextureSlot.EMISSIVE, textPathEmissive, TexturePixelType.UByte, DEFAULT_TEXTURE_PARAMETER);
        }
    }

    public int getAssimpMaterialIndex() {
        return assimpMaterialIndex;
    }

    private void bindTextures() {
        if (roughnessTexture != null)
            roughnessTexture.bind();
        if (metallicTexture != null)
            metallicTexture.bind();
        if (normalTexture != null)
            normalTexture.bind();
        if (aoTexture != null)
            aoTexture.bind();
        if (albedoTexture != null)
            albedoTexture.bind();
        if (emissiveTexture != null)
            emissiveTexture.bind();
    }

    public void bindMaterial() {
        shader.begin();
        bindTextures();

        if (albedoColor != null) {
            shader.setUniformVec4("Material.diffuse", albedoColor.getNumbers()[0], albedoColor.getNumbers()[1], albedoColor.getNumbers()[2], albedoColor.getNumbers()[3]);
        }
        if (albedoTexture != null) {
            shader.setUniform1i("Material.albedoMapSlot", albedoTexture.getTextureSlot());
        } else {
            shader.setUniform1i("Material.albedoMapSlot", 0);
        }

        if (normalTexture != null) {
            shader.setUniform1i("Material.normalMapSlot", normalTexture.getTextureSlot());
            shader.setUniform1f("Material.normalMapStrength", normalStrength[0]);
        } else {
            shader.setUniform1i("Material.normalMapSlot", 0);
        }
        if (metallicTexture != null) {
            shader.setUniform1i("Material.metallicSlot", metallicTexture.getTextureSlot());
        } else {
            shader.setUniform1i("Material.metallicSlot", 0);
            shader.setUniform1f("Material.metallicStrength", metalness[0]);
        }
        if (aoTexture != null) {
            shader.setUniform1i("Material.aoSlot", aoTexture.getTextureSlot());
        } else {
            shader.setUniform1f("Material.aoStrength", aoStrength[0]);
            shader.setUniform1i("Material.aoSlot", 0);
        }
        if (roughnessTexture != null) {
            shader.setUniform1i("Material.roughnessSlot", roughnessTexture.getTextureSlot());
        } else {
            shader.setUniform1f("Material.roughnessStrength", roughness[0]);
            shader.setUniform1i("Material.roughnessSlot", 0);
        }
        if (emissiveTexture != null) {
            shader.setUniform1f("Material.emissiveStrength", emissiveStrength[0]);
            shader.setUniform1i("Material.emissiveSlot", emissiveTexture.getTextureSlot());
        } else {
            shader.setUniform1i("Material.emissiveSlot", 0);
        }
    }

    public void unbindMaterial() {
        shader.end();
    }

    @Override
    public void dispose() {
        if (albedoTexture != null) albedoTexture.dispose();
        if (aoTexture != null) aoTexture.dispose();
        if (normalTexture != null) normalTexture.dispose();
        if (metallicTexture != null) metallicTexture.dispose();
        if (roughnessTexture != null) roughnessTexture.dispose();
        albedoTexture = null;
        aoTexture = null;
        normalTexture = null;
        metallicTexture = null;
        roughnessTexture = null;
    }

    public Shader getShader() {
        return shader;
    }
}
