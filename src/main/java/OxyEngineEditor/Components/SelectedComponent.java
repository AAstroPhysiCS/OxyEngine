package OxyEngineEditor.Components;

public class SelectedComponent implements EntityComponent {
    public boolean selected;
    public boolean fixedValue;

    public SelectedComponent(boolean selected){
        this.selected = selected;
    }

    public SelectedComponent(SelectedComponent other){
        this.selected = other.selected;
        this.fixedValue = other.fixedValue;
    }

    public SelectedComponent(boolean selected, boolean fixedValue){
        this.selected = selected;
        this.fixedValue = fixedValue;
    }
}
