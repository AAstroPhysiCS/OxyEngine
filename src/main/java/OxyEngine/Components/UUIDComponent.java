package OxyEngine.Components;

import java.util.UUID;

public final class UUIDComponent implements EntityComponent {

    private final String uuid;

    public UUIDComponent(){
        uuid = UUID.randomUUID().toString();
    }

    public UUIDComponent(String uuid){
        this.uuid = uuid;
    }

    public String getUUID() {
        return uuid;
    }
}
