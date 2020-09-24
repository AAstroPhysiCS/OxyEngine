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
    public final OxyColor albedoColor;

    public float[] metalness;
    public float[] roughness;
    public float[] aoStrength;
    public final float[] normalStrength;
    public final float[] heightScale;

    public OxyMaterial(ImageTexture albedoTexture, ImageTexture normalTexture, ImageTexture roughnessTexture, ImageTexture metallicTexture, ImageTexture aoTexture,
                       ImageTexture heightTexture, OxyColor albedoColor) {
        this.albedoTexture = albedoTexture;
        this.roughnessTexture = roughnessTexture;
        this.metallicTexture = metallicTexture;
        this.aoTexture = aoTexture;
        this.normalTexture = normalTexture;
        this.heightTexture = heightTexture;
        this.albedoColor = albedoColor;
        metalness = new float[]{0.0f}; roughness = new float[]{1.0f}; aoStrength = new float[]{1.0f}; normalStrength = new float[]{1.0f}; heightScale = new float[]{1.0f};
    }

    public OxyMaterial(Vector4f albedoColor) {
        this(null, null, null, null, null, null, new OxyColor(albedoColor));
    }

    public OxyMaterial(float r, float g, float b, float a) {
        this(null, null, null, null, null, null, new OxyColor(new Vector4f(r, g, b, a)));
    }

    public OxyMaterial(Vector4f albedoColor, float metalness, float roughness, float aoStrength){
        this(null, null, null, null, null, null, new OxyColor(albedoColor));
        this.metalness = new float[]{metalness};
        this.roughness = new float[]{roughness};
        this.aoStrength = new float[]{aoStrength};
    }

    public OxyMaterial(OxyMaterial other) {
        this.roughnessTexture = other.roughnessTexture;
        this.aoTexture = other.aoTexture;
        this.normalTexture = other.normalTexture;
        this.albedoTexture = other.albedoTexture;
        this.metallicTexture = other.metallicTexture;
        this.heightTexture = other.heightTexture;
        this.albedoColor = new OxyColor(other.albedoColor.getNumbers().clone());
        this.metalness = other.metalness.clone();
        this.roughness = other.roughness.clone();
        this.aoStrength = other.aoStrength.clone();
        this.normalStrength = other.normalStrength.clone();
        this.heightScale = other.heightScale.clone();
    }

    public void push(OxyShader shader) {
        shader.enable();
        if(albedoColor != null){
            shader.setUniformVec3("material.diffuse", new Vector3f(albedoColor.getNumbers()[0], albedoColor.getNumbers()[1], albedoColor.getNumbers()[2]));
            shader.setUniformVec3("colorOut", new Vector3f(albedoColor.getNumbers()[0], albedoColor.getNumbers()[1], albedoColor.getNumbers()[2]));
            shader.setUniformVec4("colorOut4f", new Vector4f(albedoColor.getNumbers()[0], albedoColor.getNumbers()[1], albedoColor.getNumbers()[2], albedoColor.getNumbers()[3]));
        }
        if (normalTexture != null) {
            shader.setUniform1i("normalMapSlot", normalTexture.getTextureSlot());
            shader.setUniform1f("normalMapStrength", normalStrength[0]);
        } else {
            shader.setUniform1i("normalMapSlot", 0);
        }
        if (metallicTexture != null) {
            shader.setUniform1i("metallicSlot", metallicTexture.getTextureSlot());
        } else {
            shader.setUniform1i("metallicSlot", 0);
            shader.setUniform1f("metallicFloat", metalness[0]);
        }
        if (aoTexture != null) {
            shader.setUniform1i("aoSlot", aoTexture.getTextureSlot());
        } else {
            shader.setUniform1f("aoFloat", aoStrength[0]);
            shader.setUniform1i("aoSlot", 0);
        }
        if (roughnessTexture != null) {
            shader.setUniform1i("roughnessSlot", roughnessTexture.getTextureSlot());
        } else {
            shader.setUniform1f("roughnessFloat", roughness[0]);
            shader.setUniform1i("roughnessSlot", 0);
        }
        if (heightTexture != null) {
            shader.setUniform1i("heightSlot", heightTexture.getTextureSlot());
            shader.setUniform1f("heightScale", heightScale[0]);
        } else {
            shader.setUniform1i("heightSlot", 0);
        }
        shader.setUniform1f("gamma", PropertiesPanel.gammaStrength[0]);
        shader.disable();
    }
}
