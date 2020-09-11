package OxyEngineEditor.Scene.Objects.Model;

import OxyEngine.System.OxySystem;

public enum ModelType {
    Plane(OxySystem.FileSystem.getResourceByPath("/models/native/plane.obj")),
    Sphere(OxySystem.FileSystem.getResourceByPath("/models/native/sphere.obj")),
    Cone(OxySystem.FileSystem.getResourceByPath("/models/native/cone.obj")),
    Cube(OxySystem.FileSystem.getResourceByPath("/models/native/cube.obj"));

    private final String path;

    ModelType(String path){
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
