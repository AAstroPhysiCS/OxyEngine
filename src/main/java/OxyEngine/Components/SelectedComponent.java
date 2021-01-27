package OxyEngine.Components;

public class SelectedComponent implements EntityComponent {
    public boolean selected;

    public SelectedComponent(boolean selected){
        this.selected = selected;
    }

    public SelectedComponent(SelectedComponent other){
        this.selected = other.selected;
    }
}
