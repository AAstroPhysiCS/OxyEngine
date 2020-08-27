package OxyEngineEditor.Scene.Model;

import OxyEngine.System.OxySystem;

public enum ModelType {
    Plane(OxySystem.FileSystem.getResourceByPath("/models/Native/plane.obj")),
    Sphere(OxySystem.FileSystem.getResourceByPath("/models/Native/sphere.obj")),
    Cone(OxySystem.FileSystem.getResourceByPath("/models/Native/cone.obj")),
    Cube(OxySystem.FileSystem.getResourceByPath("/models/Native/cube.obj"));

    private final String path;

    ModelType(String path){
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
