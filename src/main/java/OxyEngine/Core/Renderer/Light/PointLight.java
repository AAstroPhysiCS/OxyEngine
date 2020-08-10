package OxyEngine.Core.Renderer.Light;

import OxyEngine.Core.Renderer.Shader.OxyShader;

import static OxyEngine.System.OxySystem.oxyAssert;

public class PointLight extends Light {

    public PointLight() {

    }

    @Override
    public void update(OxyShader shader) {
        assert direction == null : oxyAssert("Point Lights should not have a direction");
        shader.enable();
        shader.setUniform1f("currentLightIndex", 0);
        shader.setUniformVec3("p_Light.position", position);
        shader.setUniformVec3("p_Light.ambient", ambient);
        shader.setUniformVec3("p_Light.specular", specular);
        shader.setUniformVec3("p_Light.diffuse", diffuse);
        shader.setUniform1f("p_Light.constant", 1.0f);
        shader.setUniform1f("p_Light.linear", 0.0075f);
        shader.setUniform1f("p_Light.quadratic", 0.045f);
        shader.disable();
    }
}
