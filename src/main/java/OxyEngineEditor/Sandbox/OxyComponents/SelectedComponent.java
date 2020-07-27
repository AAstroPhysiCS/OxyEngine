package OxyEngineEditor.Sandbox.OxyComponents;

public class SelectedComponent implements EntityComponent {
    public boolean selected = false;
    public SelectedComponent(boolean selected){
        this.selected = selected;
    }
}
