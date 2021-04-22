package OxyEngine.Core.Renderer;

import java.util.ArrayList;
import java.util.List;

public final class RenderQueue {

    private static final List<RenderFunc> renderFuncs = new ArrayList<>();

    public void submit(RenderFunc func){
        renderFuncs.add(func);
    }
    
    static void runQueue(){
        for(RenderFunc f : renderFuncs){
            f.func();
        }
    }
}
