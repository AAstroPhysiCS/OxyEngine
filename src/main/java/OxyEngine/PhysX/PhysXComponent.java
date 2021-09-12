package OxyEngine.PhysX;

import OxyEngine.Components.EntityComponent;
import OxyEngine.Core.Context.Scene.Entity;
import OxyEngine.System.Disposable;
import OxyEngineEditor.UI.GUINode;

import java.util.List;


public final class PhysXComponent implements EntityComponent, Disposable {

    private final PhysXActor actor;
    private PhysXGeometry geometry;
    private PhysXMaterial material;

    /*
     * Automatically creates an actor
     */
    public PhysXComponent(Entity e) {
        material = new PhysXMaterial(0.5f, 0.5f, 0.5f);
        this.actor = new PhysXActor(PhysXRigidBodyType.Static, e);
        e.getGUINodes().add(PhysXActor.guiNode);
    }

    public PhysXComponent(Entity e, PhysXMaterial material, String actorType, String geometryType) {
        this.material = material;
        Class<?> targetedGeometryType = PhysXGeometry.getClassBasedOnType(geometryType);
        List<GUINode> nodeList = e.getGUINodes();
        if (PhysXGeometry.Box.class.equals(targetedGeometryType)) {
            this.geometry = new PhysXGeometry.Box(e);
        }
        else if (PhysXGeometry.Sphere.class.equals(targetedGeometryType)) {
            this.geometry = new PhysXGeometry.Sphere(e);
        }
        else if (PhysXGeometry.TriangleMesh.class.equals(targetedGeometryType)) {
            this.geometry = new PhysXGeometry.TriangleMesh(e);
        }
        else throw new IllegalStateException("No support for the given geometry type");

        this.actor = new PhysXActor(PhysXRigidBodyType.valueOf(actorType), e);
        nodeList.add(PhysXGeometry.guiNode);
        nodeList.add(PhysXActor.guiNode);
    }

    public PhysXComponent(PhysXComponent other, Entity newEntity) {
        if (other.actor == null || other.geometry == null)
            throw new IllegalStateException("Actor and/or geometry null");
        if (other.geometry instanceof PhysXGeometry.Box)
            this.geometry = new PhysXGeometry.Box(newEntity);
        else if (other.geometry instanceof PhysXGeometry.Sphere)
            this.geometry = new PhysXGeometry.Sphere(newEntity);
        else if (other.geometry instanceof PhysXGeometry.TriangleMesh)
            this.geometry = new PhysXGeometry.TriangleMesh(newEntity);
        else throw new IllegalStateException("No copy implementation for the given geometry");

        this.actor = new PhysXActor(other.actor.getBodyType(), newEntity);
        this.material = other.material;
    }

    public void reset(){
        OxyPhysX.getPhysXEnv().removeActorFromScene(actor);
        actor.dispose();
    }

    @Override
    public void dispose() {
        OxyPhysX.getPhysXEnv().removeActorFromScene(actor);
        material.dispose();
        actor.dispose();
    }

    public void setGeometryAs(PhysXGeometry geometry) {
        this.geometry = geometry;
    }

    public PhysXActor getActor() {
        return actor;
    }

    public PhysXGeometry getGeometry() {
        return geometry;
    }

    public PhysXMaterial getMaterial() {
        return material;
    }

    public void setMaterial(PhysXMaterial material) {
        this.material = material;
    }
}
