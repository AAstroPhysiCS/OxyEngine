package OxyEngine.Scene.Objects.Model;

import OxyEngine.System.OxySystem;

public enum ModelType {
    Plane(OxySystem.FileSystem.getResourceByPath("/models/plane.obj")),
    Sphere(OxySystem.FileSystem.getResourceByPath("/models/sphere.obj")),
    Cone(OxySystem.FileSystem.getResourceByPath("/models/cone.obj")),
    Cube(OxySystem.FileSystem.getResourceByPath("/models/cube.obj"));

    private final String path;

    ModelType(String path){
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
