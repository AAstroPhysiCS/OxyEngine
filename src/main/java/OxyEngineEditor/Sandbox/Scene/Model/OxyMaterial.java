package OxyEngineEditor.Sandbox.Scene.Model;

import OxyEngine.Core.Renderer.Texture.OxyTexture;
import org.joml.Vector4f;

public record OxyMaterial(OxyTexture texture, Vector4f ambientColor, Vector4f diffuseColor, Vector4f specularColor, float reflectance) {
}
