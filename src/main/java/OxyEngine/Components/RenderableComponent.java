package OxyEngine.Components;

public class RenderableComponent implements EntityComponent {
    public final RenderingMode mode;
    public RenderableComponent(RenderingMode mode){
        this.mode = mode;
    }
}
