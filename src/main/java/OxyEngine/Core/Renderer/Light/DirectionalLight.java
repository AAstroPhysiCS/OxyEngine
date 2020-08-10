package OxyEngine.Core.Renderer.Light;

import OxyEngine.Core.Renderer.Shader.OxyShader;

import static OxyEngine.System.OxySystem.oxyAssert;

public class DirectionalLight extends Light {

    public DirectionalLight() {

    }

    @Override
    public void update(OxyShader shader) {
        assert direction != null : oxyAssert("Directional Lights should have a direction");
        shader.enable();
        shader.setUniform1f("currentLightIndex", 1);
        shader.setUniformVec3("d_Light.direction", direction);
        shader.setUniformVec3("d_Light.ambient", ambient);
        shader.setUniformVec3("d_Light.specular", specular);
        shader.setUniformVec3("d_Light.diffuse", diffuse);
        shader.disable();
    }
}

