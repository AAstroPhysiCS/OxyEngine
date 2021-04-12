package OxyEngine.Components;

public final class EntityFamily {

    private EntityFamily root;

    public EntityFamily(EntityFamily root) {
        this.root = root;
    }

    public EntityFamily() {
        this(null);
    }

    public EntityFamily root() {
        return root;
    }

    public void setRoot(EntityFamily root) {
        this.root = root;
    }
}
