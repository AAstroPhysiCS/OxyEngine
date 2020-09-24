package OxyEngine.Core.Renderer.Light;

import OxyEngine.Core.Renderer.Shader.OxyShader;

import static OxyEngine.System.OxySystem.oxyAssert;

public class PointLight extends Light {

    private final float constant, linear, quadratic;

    public PointLight(float constant, float linear, float quadratic) {
        this.constant = constant;
        this.linear = linear;
        this.quadratic = quadratic;
    }

    @Override
    public void update(OxyShader shader, int i) {
        assert direction == null : oxyAssert("Point Lights should not have a direction");
        shader.enable();
        shader.setUniform1f("currentLightIndex", 0);
        shader.setUniformVec3("p_Light["+i+"].position", position);
        shader.setUniformVec3("p_Light["+i+"].ambient", ambient);
        shader.setUniformVec3("p_Light["+i+"].specular", specular);
        shader.setUniformVec3("p_Light["+i+"].diffuse", diffuse);
        shader.setUniform1f("p_Light["+i+"].constant", constant);
        shader.setUniform1f("p_Light["+i+"].linear", linear);
        shader.setUniform1f("p_Light["+i+"].quadratic", quadratic);
        shader.disable();
    }
}
