package OxyEngine.Components;

public class EntitySerializationInfo implements EntityComponent {

    private final boolean grouped, imported;

    public EntitySerializationInfo(boolean grouped, boolean imported){
        this.grouped = grouped;
        this.imported = imported;
    }

    public EntitySerializationInfo(EntitySerializationInfo s){
        this.grouped = s.grouped;
        this.imported = s.imported;
    }

    public boolean grouped() {
        return grouped;
    }

    public boolean imported() {
        return imported;
    }
}
