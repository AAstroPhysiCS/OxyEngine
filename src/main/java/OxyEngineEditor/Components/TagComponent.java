package OxyEngineEditor.Components;

public class TagComponent implements EntityComponent {
    private String tag;
    public TagComponent(String tag) { this.tag = tag; }
    public void setTag(String tag) {
        this.tag = tag;
    }
    public String tag() {
        return tag;
    }

    @Override
    public String toString(){
        return "TagComponent: " + tag;
    }
}
