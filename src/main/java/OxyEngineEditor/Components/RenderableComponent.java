package OxyEngineEditor.Components;

public class RenderableComponent implements EntityComponent {
    public boolean renderable;
    public boolean noZBufferRendering;
    public RenderableComponent(boolean renderable, boolean noZBufferRendering){
        this.renderable = renderable;
        this.noZBufferRendering = noZBufferRendering;
    }
    public RenderableComponent(boolean renderable){
        this(renderable, false);
    }
}
