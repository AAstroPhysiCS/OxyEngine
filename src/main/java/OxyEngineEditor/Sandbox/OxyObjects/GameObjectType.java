package OxyEngineEditor.Sandbox.OxyObjects;

public enum GameObjectType {
    //TCS: 48
    Cube(144, 36),
    Grid(24, 6);

    private final int n_Vertices;
    private final int n_Indices;

    GameObjectType(int n_Vertices, int n_Indices) {
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
