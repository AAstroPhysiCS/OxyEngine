package OxyEngine.Core.Context.Scene;

import OxyEngine.System.FileSystem;

public enum DefaultModelType {
    Plane(FileSystem.getResourceByPath("/models/plane.fbx")),
    Sphere(FileSystem.getResourceByPath("/models/sphere.fbx")),
    Cone(FileSystem.getResourceByPath("/models/cone.fbx")),
    Cube(FileSystem.getResourceByPath("/models/cube.fbx"));

    private final String path;

    DefaultModelType(String path){
        this.path = path;
    }

    String getPath() {
        return path;
    }
}
