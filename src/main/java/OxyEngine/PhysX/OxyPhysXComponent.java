package OxyEngine.PhysX;

import OxyEngine.Components.EntityComponent;
import OxyEngine.Components.TransformComponent;
import OxyEngine.Core.Context.Scene.OxyEntity;
import OxyEngine.System.OxyDisposable;
import OxyEngineEditor.UI.Panels.GUINode;
import org.joml.Vector3f;

import java.util.List;

import static OxyEngine.Core.Context.Scene.SceneRuntime.entityContext;


public final class OxyPhysXComponent implements EntityComponent, OxyDisposable {

    private OxyPhysXActor actor;
    private OxyPhysXGeometry geometry;

    public OxyPhysXComponent() {
    }

    public OxyPhysXComponent(OxyEntity e, String actorType, String geometryType) {
        Class<?> targetedGeometryType = OxyPhysXGeometry.getClassBasedOnType(geometryType);
        List<GUINode> nodeList = e.getGUINodes();
        if (OxyPhysXGeometry.Box.class.equals(targetedGeometryType)) {
            this.geometry = new OxyPhysXGeometry.Box(e);
            nodeList.add(OxyPhysXGeometry.Box.guiNode);
        }
        else if (OxyPhysXGeometry.Sphere.class.equals(targetedGeometryType)) {
            this.geometry = new OxyPhysXGeometry.Sphere(e);
            nodeList.add(OxyPhysXGeometry.Sphere.guiNode);
        }
        else if (OxyPhysXGeometry.TriangleMesh.class.equals(targetedGeometryType)) {
            this.geometry = new OxyPhysXGeometry.TriangleMesh(e);
            nodeList.add(OxyPhysXGeometry.TriangleMesh.guiNode);
        }
        else throw new IllegalStateException("No support for the given geometry type");

        this.actor = new OxyPhysXActor(PhysXRigidBodyType.valueOf(actorType), e);
        nodeList.add(OxyPhysXActor.guiNode);
    }

    public OxyPhysXComponent(OxyPhysXComponent other, OxyEntity newEntity) {
        if (other.actor == null || other.geometry == null)
            throw new IllegalStateException("Actor and/or geometry null");
        if (other.geometry instanceof OxyPhysXGeometry.Box b)
            this.geometry = new OxyPhysXGeometry.Box(new Vector3f(b.getPxHalfScalar()), newEntity);
        else if (other.geometry instanceof OxyPhysXGeometry.Sphere s)
            this.geometry = new OxyPhysXGeometry.Sphere(s.getRadius(), newEntity);
        else if (other.geometry instanceof OxyPhysXGeometry.TriangleMesh)
            this.geometry = new OxyPhysXGeometry.TriangleMesh(newEntity); //TODO: IMPLEMENT IT
        else throw new IllegalStateException("No copy implementation for the given geometry");
        this.actor = new OxyPhysXActor(other.actor.getBodyType(), newEntity);
    }

    @Override
    public void dispose() {
        geometry.dispose();
        actor.dispose();
    }

    public void setGeometryAs(OxyPhysXGeometry geometry) {
        this.geometry = geometry;
    }

    public void setRigidBodyAs(OxyPhysXActor rigidBody) {
        this.actor = rigidBody;
    }

    public OxyPhysXActor getActor() {
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
