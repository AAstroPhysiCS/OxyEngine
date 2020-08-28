package OxyEngineEditor.Components;

import OxyEngine.Core.Renderer.RenderingMode;

public class RenderableComponent implements EntityComponent {
    public RenderingMode mode;
    public RenderableComponent(RenderingMode mode){
        this.mode = mode;
    }
}
