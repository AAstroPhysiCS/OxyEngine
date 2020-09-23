package OxyEngineEditor.Scene.Objects.Model;

import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Core.Renderer.Texture.ImageTexture;
import OxyEngine.Core.Renderer.Texture.OxyColor;
import OxyEngineEditor.Components.EntityComponent;
import OxyEngineEditor.UI.Panels.PropertiesPanel;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class OxyMaterial implements EntityComponent {

    public ImageTexture albedoTexture, normalTexture, roughnessTexture, metallicTexture, aoTexture, heightTexture;
    public final OxyColor ambientColor;
    public final OxyColor diffuseColor;
    public final OxyColor specularColor;
    public final float reflectance;

    public OxyMaterial(ImageTexture albedoTexture, ImageTexture normalTexture, ImageTexture roughnessTexture, ImageTexture metallicTexture, ImageTexture aoTexture,
                       ImageTexture heightTexture, OxyColor ambientColor, OxyColor diffuseColor, OxyColor specularColor, float reflectance) {
        this.albedoTexture = albedoTexture;
        this.roughnessTexture = roughnessTexture;
        this.metallicTexture = metallicTexture;
        this.aoTexture = aoTexture;
        this.normalTexture = normalTexture;
        this.heightTexture = heightTexture;
        this.ambientColor = ambientColor;
        this.diffuseColor = diffuseColor;
        this.specularColor = specularColor;
        this.reflectance = reflectance;
    }

    public OxyMaterial(OxyColor ambientColor, OxyColor diffuseColor, OxyColor specularColor) {
        this(null, null, null, null, null, null, ambientColor, diffuseColor, specularColor, 1.0f);
    }

    public OxyMaterial(Vector4f ambientColor, Vector4f diffuseColor, Vector4f specularColor) {
        this(null, null, null, null, null, null, new OxyColor(ambientColor), new OxyColor(diffuseColor), new OxyColor(specularColor), 1.0f);
    }

    public OxyMaterial(Vector4f diffuseColor) {
        this(null, null, null, null, null, null, new OxyColor(new Vector4f(1.0f, 1.0f, 1.0f, 1.0f)), new OxyColor(diffuseColor), new OxyColor(new Vector4f(1.0f, 1.0f, 1.0f, 1.0f)), 1.0f);
    }

    public OxyMaterial(float r, float g, float b, float a) {
        this(null, null, null, null, null, null, new OxyColor(new Vector4f(1.0f, 1.0f, 1.0f, 1.0f)), new OxyColor(new Vector4f(r, g, b, a)), new OxyColor(new Vector4f(1.0f, 1.0f, 1.0f, 1.0f)), 1.0f);
    }

    public OxyMaterial(OxyMaterial other) {
        this.roughnessTexture = other.roughnessTexture;
        this.aoTexture = other.aoTexture;
        this.normalTexture = other.normalTexture;
        this.reflectance = other.reflectance;
        this.albedoTexture = other.albedoTexture;
        this.metallicTexture = other.metallicTexture;
        this.heightTexture = other.heightTexture;
        this.diffuseColor = new OxyColor(other.diffuseColor.getNumbers().clone());
        this.ambientColor = new OxyColor(other.ambientColor.getNumbers().clone());
        this.specularColor = new OxyColor(other.specularColor.getNumbers().clone());
    }

    public void push(OxyShader shader) {
        shader.enable();
        if(diffuseColor != null){
            shader.setUniformVec3("material.diffuse", new Vector3f(diffuseColor.getNumbers()[0], diffuseColor.getNumbers()[1], diffuseColor.getNumbers()[2]));
            shader.setUniformVec3("colorOut", new Vector3f(diffuseColor.getNumbers()[0], diffuseColor.getNumbers()[1], diffuseColor.getNumbers()[2]));
            shader.setUniformVec4("colorOut4f", new Vector4f(diffuseColor.getNumbers()[0], diffuseColor.getNumbers()[1], diffuseColor.getNumbers()[2], diffuseColor.getNumbers()[3]));
        }
        if (normalTexture != null) {
            shader.setUniform1i("normalMapSlot", normalTexture.getTextureSlot());
            shader.setUniform1f("normalMapStrength", PropertiesPanel.normalMapStrength[0]);
        } else {
            shader.setUniform1i("normalMapSlot", 0);
        }
        if (metallicTexture != null) {
            shader.setUniform1i("metallicSlot", metallicTexture.getTextureSlot());
        } else {
            shader.setUniform1i("metallicSlot", 0);
            shader.setUniform1f("metallicFloat", PropertiesPanel.metalness[0]);
        }
        if (aoTexture != null) {
            shader.setUniform1i("aoSlot", aoTexture.getTextureSlot());
        } else {
            shader.setUniform1f("aoFloat", PropertiesPanel.aoStrength[0]);
            shader.setUniform1i("aoSlot", 0);
        }
        if (roughnessTexture != null) {
            shader.setUniform1i("roughnessSlot", roughnessTexture.getTextureSlot());
        } else {
            shader.setUniform1f("roughnessFloat", PropertiesPanel.roughness[0]);
            shader.setUniform1i("roughnessSlot", 0);
        }
        if (heightTexture != null) {
            shader.setUniform1i("heightSlot", heightTexture.getTextureSlot());
            shader.setUniform1f("heightScale", PropertiesPanel.heightScale[0]);
        } else {
            shader.setUniform1i("heightSlot", 0);
        }
        shader.setUniform1f("gamma", PropertiesPanel.gammaStrength[0]);
        shader.disable();
    }
}
