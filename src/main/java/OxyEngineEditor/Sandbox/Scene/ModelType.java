package OxyEngineEditor.Sandbox.Scene;

import OxyEngine.System.OxySystem;

public enum ModelType {
    Plane(OxySystem.FileSystem.getResourceByPath("/models/intern/plane.obj")),
    Sphere(OxySystem.FileSystem.getResourceByPath("/models/intern/sphere.obj")),
    Cone(OxySystem.FileSystem.getResourceByPath("/models/intern/cone.obj")),
    Cube(OxySystem.FileSystem.getResourceByPath("/models/intern/cube.obj"));

    private final String path;

    ModelType(String path){
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
