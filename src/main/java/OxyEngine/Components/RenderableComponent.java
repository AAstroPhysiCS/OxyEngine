package OxyEngine.Components;

public class RenderableComponent implements EntityComponent {
    public RenderingMode mode;
    public RenderableComponent(RenderingMode mode){
        this.mode = mode;
    }
}
