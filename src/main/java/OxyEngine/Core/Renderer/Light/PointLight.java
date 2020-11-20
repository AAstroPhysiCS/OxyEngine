package OxyEngine.Core.Renderer.Light;

import OxyEngine.Core.Renderer.Shader.OxyShader;
import OxyEngine.Components.TransformComponent;
import OxyEngineEditor.Scene.Objects.Model.OxyMaterial;
import OxyEngineEditor.Scene.OxyEntity;
import org.joml.Vector3f;

public class PointLight extends Light {

    private final float constant, linear, quadratic;

    public PointLight(Vector3f ambient, Vector3f specular, float constant, float linear, float quadratic) {
        super(ambient, specular);
        this.constant = constant;
        this.linear = linear;
        this.quadratic = quadratic;
    }

    @Override
    public void update(OxyEntity e, int i) {
        OxyShader shader = e.get(OxyShader.class);
        OxyMaterial material = e.get(OxyMaterial.class);
        shader.enable();
        shader.setUniform1f("currentLightIndex", 0);
        shader.setUniformVec3("p_Light[" + i + "].position", e.get(TransformComponent.class).position);
        shader.setUniformVec3("p_Light[" + i + "].ambient", ambient);
        shader.setUniformVec3("p_Light[" + i + "].specular", specular);
        shader.setUniformVec3("p_Light[" + i + "].diffuse", new Vector3f(material.albedoColor.getNumbers()));
        shader.setUniform1f("p_Light[" + i + "].constant", constant);
        shader.setUniform1f("p_Light[" + i + "].linear", linear);
        shader.setUniform1f("p_Light[" + i + "].quadratic", quadratic);
        shader.disable();
    }
}
