package OxyEngineEditor.Components;

public class RenderableComponent implements EntityComponent {
    public boolean renderable;
    public boolean maskedRendering;
    public RenderableComponent(boolean renderable, boolean maskedRendering){
        this.renderable = renderable;
        this.maskedRendering = maskedRendering;
    }
    public RenderableComponent(boolean renderable){
        this(renderable, false);
    }
}
