package OxyEngine.Scene.Objects.Model;

import OxyEngine.System.OxySystem;

public enum DefaultModelType {
    Plane(OxySystem.FileSystem.getResourceByPath("/models/plane.fbx")),
    Sphere(OxySystem.FileSystem.getResourceByPath("/models/sphere.fbx")),
    Cone(OxySystem.FileSystem.getResourceByPath("/models/cone.fbx")),
    Cube(OxySystem.FileSystem.getResourceByPath("/models/cube.fbx"));

    private final String path;

    DefaultModelType(String path){
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
