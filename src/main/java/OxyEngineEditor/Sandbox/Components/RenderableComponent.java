package OxyEngineEditor.Sandbox.Components;

public class RenderableComponent implements EntityComponent {
    public boolean renderable;
    public RenderableComponent(boolean renderable){
        this.renderable = renderable;
    }
}
