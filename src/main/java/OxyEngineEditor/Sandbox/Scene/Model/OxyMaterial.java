package OxyEngineEditor.Sandbox.Scene.Model;

import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Core.Renderer.Texture.ImageTexture;
import org.joml.Vector3f;
import org.joml.Vector4f;

public record OxyMaterial(ImageTexture texture, Vector4f ambientColor, Vector4f diffuseColor, Vector4f specularColor, float reflectance) {
    public void setValues(OxyShader shader){
        shader.enable();
        shader.setUniformVec3("material.ambient", new Vector3f(ambientColor.x, ambientColor.y, ambientColor.z));
        shader.setUniformVec3("material.specular", new Vector3f(specularColor.x, specularColor.y, specularColor.z));
        shader.setUniformVec3("material.diffuse", new Vector3f(diffuseColor.x, diffuseColor.y, diffuseColor.z));
        shader.setUniform1f("material.reflectance", reflectance);
        shader.disable();
    }
}
