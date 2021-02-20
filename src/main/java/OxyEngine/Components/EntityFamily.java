package OxyEngine.Components;

public record EntityFamily(EntityFamily root) {
    public EntityFamily() {
        this(null);
    }
}
