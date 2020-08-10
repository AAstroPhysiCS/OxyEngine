package OxyEngine.Core.Renderer.Light;

import OxyEngine.Core.Renderer.Shader.OxyShader;

import static OxyEngine.System.OxySystem.logOut;

public class DirectionalLight extends Light {

    public DirectionalLight() {

    }

    @Override
    public void update(OxyShader shader) {
        assert direction != null : logOut("Directional Lights should have a direction");
        shader.enable();
        shader.setUniformVec3("d_Light.direction", direction);
        shader.setUniformVec3("d_Light.ambient", ambient);
        shader.setUniformVec3("d_Light.specular", specular);
        shader.setUniformVec3("d_Light.diffuse", diffuse);
        shader.disable();
    }
}

