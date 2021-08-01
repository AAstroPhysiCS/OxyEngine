package OxyEngine.Core.Context.Scene;

import OxyEngine.System.OxyFileSystem;

public enum DefaultModelType {
    Plane(OxyFileSystem.getResourceByPath("/models/plane.fbx")),
    Sphere(OxyFileSystem.getResourceByPath("/models/sphere.fbx")),
    Cone(OxyFileSystem.getResourceByPath("/models/cone.fbx")),
    Cube(OxyFileSystem.getResourceByPath("/models/cube.fbx"));

    private final String path;

    DefaultModelType(String path){
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
