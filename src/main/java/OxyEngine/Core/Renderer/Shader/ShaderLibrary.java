package OxyEngine.Core.Renderer.Shader;

import java.util.ArrayList;
import java.util.List;

import static OxyEngine.System.OxySystem.logger;

public class ShaderLibrary {

    private static final List<OxyShader> shaderLibrary = new ArrayList<>();

    private ShaderLibrary(){}

    public static OxyShader get(String shaderName){
        for(OxyShader s : shaderLibrary){
            if(s.getName().equals(shaderName)) return s;
        }
        throw new IllegalStateException("No Shader named: " + shaderName);
    }

    static void addShaders(OxyShader shader){
        if(!check(shader)) {
            logger.severe("Adding shaders that is already in the library");
            return;
        }
        shaderLibrary.add(shader);
    }

    private static boolean check(OxyShader shader){
        for(OxyShader s : shaderLibrary){
            if(s.getName().equals(shader.getName())){
                return false;
            }
        }
        return true;
    }

    public static void removeShaderIfExist(String name) {
        shaderLibrary.removeIf(s -> s.getName().equals(name));
    }
}
