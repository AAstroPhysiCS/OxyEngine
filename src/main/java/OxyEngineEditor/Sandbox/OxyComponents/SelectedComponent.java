package OxyEngineEditor.Sandbox.OxyComponents;

public class SelectedComponent implements EntityComponent {
    public boolean selected;
    public boolean fixedValue;

    public SelectedComponent(boolean selected){
        this.selected = selected;
    }

    public SelectedComponent(boolean selected, boolean fixedValue){
        this.selected = selected;
        this.fixedValue = fixedValue;
    }
}
