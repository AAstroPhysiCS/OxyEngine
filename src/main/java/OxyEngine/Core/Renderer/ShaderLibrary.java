package OxyEngine.Core.Renderer;

import java.util.ArrayList;
import java.util.List;

import static OxyEngine.System.OxySystem.logger;

final class ShaderLibrary {

    private static final List<Shader> shaderLibrary = new ArrayList<>();

    private ShaderLibrary(){}

    static Shader get(String shaderName){
        for(Shader s : shaderLibrary){
            if(s.getName().equals(shaderName)) return s;
        }
        throw new IllegalStateException("No Shader named: " + shaderName);
    }

    static boolean has(String shaderName){
        for(Shader s : shaderLibrary){
            if(s.getName().equals(shaderName)) return true;
        }
        return false;
    }

    static void addShaders(Shader shader){
        if(!check(shader)) {
            logger.severe("Adding shaders that is already in the library");
            return;
        }
        shaderLibrary.add(shader);
    }

    private static boolean check(Shader shader){
        for(Shader s : shaderLibrary){
            if(s.getName().equals(shader.getName())){
                return false;
            }
        }
        return true;
    }

    static void removeShaderIfExist(String name) {
        shaderLibrary.removeIf(s -> s.getName().equals(name));
    }

    static List<Shader> getAllShaders() {
        return shaderLibrary;
    }
}
