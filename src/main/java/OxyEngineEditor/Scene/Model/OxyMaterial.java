package OxyEngineEditor.Scene.Model;

import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Core.Renderer.Texture.ImageTexture;
import OxyEngine.Core.Renderer.Texture.OxyColor;
import OxyEngineEditor.Components.EntityComponent;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class OxyMaterial implements EntityComponent {

    public ImageTexture albedoTexture, normalTexture;
    public OxyColor ambientColor, diffuseColor, specularColor;
    public final float reflectance;

    public OxyMaterial(ImageTexture albedoTexture, ImageTexture normalTexture, OxyColor ambientColor, OxyColor diffuseColor, OxyColor specularColor,
                       float reflectance) {
        this.albedoTexture = albedoTexture;
        this.normalTexture = normalTexture;
        this.ambientColor = ambientColor;
        this.diffuseColor = diffuseColor;
        this.specularColor = specularColor;
        this.reflectance = reflectance;
    }

    public OxyMaterial(OxyColor ambientColor, OxyColor diffuseColor, OxyColor specularColor) {
        this(null, null, ambientColor, diffuseColor, specularColor, 1.0f);
    }

    public OxyMaterial(Vector4f ambientColor, Vector4f diffuseColor, Vector4f specularColor) {
        this(null, null, new OxyColor(ambientColor), new OxyColor(diffuseColor), new OxyColor(specularColor), 1.0f);
    }

    public OxyMaterial(Vector4f diffuseColor) {
        this(null, null, new OxyColor(new Vector4f(0.0f, 0.0f, 0.0f, 0.0f)), new OxyColor(diffuseColor), new OxyColor(new Vector4f(0.0f, 0.0f, 0.0f, 0.0f)), 1.0f);
    }

    public void push(OxyShader shader) {
        shader.enable();
        if(normalTexture != null) {
            shader.setUniform1i("normalMapTextureSlot", normalTexture.getTextureSlot());
        } else {
            shader.setUniform1i("normalMapTextureSlot", 0);
        }
        shader.setUniformVec3("material.ambient", new Vector3f(1.0f, 1.0f, 1.0f));
        shader.setUniformVec3("material.specular", new Vector3f(specularColor.getNumbers()[0], specularColor.getNumbers()[1], specularColor.getNumbers()[2]));
        shader.setUniformVec3("material.diffuse", new Vector3f(diffuseColor.getNumbers()[0], diffuseColor.getNumbers()[1], diffuseColor.getNumbers()[2]));
        shader.setUniform1f("material.reflectance", reflectance);
        shader.disable();
    }

    public void pop(OxyShader shader) {
        shader.enable();
        shader.setUniformVec3("material.ambient", new Vector3f(0, 0, 0));
        shader.setUniformVec3("material.specular", new Vector3f(0, 0, 0));
        shader.setUniformVec3("material.diffuse", new Vector3f(0, 0, 0));
        shader.setUniform1f("material.reflectance", 0);
        shader.disable();
    }
}
