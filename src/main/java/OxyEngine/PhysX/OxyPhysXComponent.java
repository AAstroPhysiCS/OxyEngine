package OxyEngine.PhysX;

import OxyEngine.Components.EntityComponent;
import OxyEngine.Components.TransformComponent;
import OxyEngine.Scene.OxyEntity;
import OxyEngine.System.OxyDisposable;
import org.joml.Vector3f;

import static OxyEngineEditor.UI.Gizmo.OxySelectHandler.entityContext;

public final class OxyPhysXComponent implements EntityComponent, OxyDisposable {

    private OxyPhysXActor actor;
    private OxyPhysXGeometry geometry;

    public OxyPhysXComponent(){
    }

    public OxyPhysXComponent(OxyPhysXComponent other, OxyEntity newEntity){
        if(other.actor == null || other.geometry == null) throw new IllegalStateException("Actor and/or geometry null");
        if(other.geometry instanceof OxyPhysXGeometry.Box b)
            this.geometry = new OxyPhysXGeometry.Box(new Vector3f(b.getPxHalfScalar()), newEntity);
        else if(other.geometry instanceof OxyPhysXGeometry.Sphere s)
            this.geometry = new OxyPhysXGeometry.Sphere(s.getRadius(), newEntity);
        else throw new IllegalStateException("No copy implementation for the given geometry");
        this.actor = new OxyPhysXActor(other.actor.getPhysXRigidBodyMode(), newEntity);
    }

    @Override
    public void dispose() {
        geometry.dispose();
        actor.dispose();
    }

    public void setGeometryAs(OxyPhysXGeometry geometry) {
        this.geometry = geometry;
    }

    public void setRigidBodyAs(OxyPhysXActor rigidBody){
        this.actor = rigidBody;
    }

    public OxyPhysXActor getActor(){
        return actor;
    }

    public OxyPhysXGeometry getGeometry() {
        return geometry;
    }

    public void update() {
        actor.setGlobalPose(entityContext.get(TransformComponent.class).transform);
        geometry.update();
    }
}
