package OxyEngine.Core.Renderer.Shader;

import OxyEngine.System.OxyDisposable;

import static org.lwjgl.opengl.GL20.glDeleteProgram;

public class OxyShader implements OxyDisposable {

    public static final int VERTICES = 0;
    public static final int TEXTURE_COORDS = 1;
    public static final int NORMALS = 2;
    public static final int BITANGENT = 3;
    public static final int TANGENT = 4;
    public static final int OBJECT_ID = 5;
    public static final int BONEIDS = 6;
    public static final int WEIGHTS = 7;

    private final int program;
    private final String name;

    public static OxyShader createShader(String name, String glslPath){
        ShaderLibrary.removeShaderIfExist(name);
        OxyShader s = new OxyShader(name, glslPath);
        ShaderLibrary.addShaders(s);
        return s;
    }

    private OxyShader(String name, String glslPath) {
        this.name = name;
        String loadedString = ShaderUtil.loadAsString(glslPath);
        program = ShaderUtil.create(ShaderUtil.getVertex(loadedString).trim(), ShaderUtil.getFragment(loadedString).trim());
    }

    public int getProgram() {
        return program;
    }

    public String getName() {
        return name;
    }

    @Override
    public void dispose() {
        ShaderLibrary.removeShaderIfExist(name);
        glDeleteProgram(program);
    }
}