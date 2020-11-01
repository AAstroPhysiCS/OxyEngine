package OxyEngine.Core.Renderer.Light;

import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngineEditor.Components.TransformComponent;
import OxyEngineEditor.Scene.Objects.Model.OxyMaterial;
import OxyEngineEditor.Scene.OxyEntity;
import org.joml.Vector3f;

public class DirectionalLight extends Light {

    public DirectionalLight(Vector3f ambient, Vector3f specular) {
        super(ambient, specular);
    }

    @Override
    public void update(OxyEntity e, int i) {
        OxyShader shader = e.get(OxyShader.class);
        OxyMaterial material = e.get(OxyMaterial.class);
        shader.enable();
        shader.setUniform1f("currentLightIndex", 1);
        shader.setUniformVec3("d_Light.direction", e.get(TransformComponent.class).position);
        shader.setUniformVec3("d_Light.ambient", ambient);
        shader.setUniformVec3("d_Light.specular", specular);
        shader.setUniformVec3("d_Light.diffuse", new Vector3f(material.albedoColor.getNumbers()));
        shader.disable();
    }
}

