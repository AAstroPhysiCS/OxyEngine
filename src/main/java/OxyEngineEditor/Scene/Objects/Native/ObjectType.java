package OxyEngineEditor.Scene.Objects.Native;

public enum ObjectType {
    Grid(32, 6);

    private final int n_Vertices;
    private final int n_Indices;

    ObjectType(int n_Vertices, int n_Indices) {
        this.n_Indices = n_Indices;
        this.n_Vertices = n_Vertices;
    }

    public int n_Indices() {
        return n_Indices;
    }

    public int n_Vertices() {
        return n_Vertices;
    }
}
