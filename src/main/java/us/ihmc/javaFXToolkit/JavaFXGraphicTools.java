package us.ihmc.javaFXToolkit;

import javafx.scene.Node;
import javafx.scene.transform.Affine;
import us.ihmc.euclid.axisAngle.AxisAngle;
import us.ihmc.euclid.geometry.interfaces.BoundingBox3DReadOnly;
import us.ihmc.euclid.geometry.interfaces.ConvexPolygon2DReadOnly;
import us.ihmc.euclid.geometry.interfaces.Pose3DReadOnly;
import us.ihmc.euclid.orientation.interfaces.Orientation3DReadOnly;
import us.ihmc.euclid.shape.primitives.Box3D;
import us.ihmc.euclid.shape.primitives.interfaces.Box3DReadOnly;
import us.ihmc.euclid.transform.RigidBodyTransform;
import us.ihmc.euclid.transform.interfaces.RigidBodyTransformReadOnly;
import us.ihmc.euclid.tuple2D.interfaces.Point2DReadOnly;
import us.ihmc.euclid.tuple3D.Point3D;
import us.ihmc.euclid.tuple3D.Vector3D;
import us.ihmc.euclid.tuple3D.interfaces.Point3DReadOnly;
import us.ihmc.euclid.tuple3D.interfaces.Tuple3DReadOnly;
import us.ihmc.euclid.tuple4D.Quaternion;
import us.ihmc.javaFXToolkit.shapes.JavaFXMeshBuilder;

import java.util.ArrayList;
import java.util.List;

public class JavaFXGraphicTools
{
   public static void setNodeTransformFromPose(Node node, Pose3DReadOnly pose)
   {
      setNodeTransformFromPose(node, pose, 1.0);
   }

   public static void setNodeTransformFromPose(Node node, Pose3DReadOnly pose, double scale)
   {
      node.getTransforms().clear();
      RigidBodyTransform rigidBodyTransform = new RigidBodyTransform();
      rigidBodyTransform.set(pose.getOrientation(), pose.getPosition());
      Affine affine = JavaFXTools.createRigidBodyTransformToAffine(rigidBodyTransform);
      if (scale != 1.0)
         affine.appendScale(scale, scale);
      node.getTransforms().add(affine);
   }

   public static void setNodePosition(Node node, Tuple3DReadOnly position)
   {
      node.setTranslateX(position.getX());
      node.setTranslateY(position.getY());
      node.setTranslateZ(position.getZ());
   }

   public static void drawPlanarRegion(JavaFXMeshBuilder meshBuilder,
                                       RigidBodyTransformReadOnly transformToWorld,
                                       List<? extends Point2DReadOnly> concaveHull,
                                       List<? extends ConvexPolygon2DReadOnly> convexPolygons,
                                       double lineWidth)
   {
      meshBuilder.addMultiLine(transformToWorld, concaveHull, lineWidth, true);

      for (ConvexPolygon2DReadOnly convexPolygon : convexPolygons)
      {
         meshBuilder.addPolygon(transformToWorld, convexPolygon);
      }
   }

   public static void drawArrow(JavaFXMeshBuilder meshBuilder,
                                Tuple3DReadOnly position,
                                Orientation3DReadOnly orientation,
                                double length,
                                double radius,
                                double cylinderToConeLengthRatio,
                                double coneDiameterMultiplier)
   {
      double cylinderLength = cylinderToConeLengthRatio * length;
      double coneLength = (1.0 - cylinderToConeLengthRatio) * length;
      double coneRadius = coneDiameterMultiplier * radius;

      AxisAngle axisAngle = new AxisAngle(orientation);
      meshBuilder.addCylinder(cylinderLength, radius, position, axisAngle);

      Vector3D coneBaseTranslation = new Vector3D(0.0, 0.0, 1.0);
      orientation.transform(coneBaseTranslation);
      coneBaseTranslation.scale(length);
      coneBaseTranslation.scale(cylinderToConeLengthRatio);
      Point3D coneBase = new Point3D(position);
      coneBase.add(coneBaseTranslation);

      meshBuilder.addCone(coneLength, coneRadius, coneBase, axisAngle);
   }

   public static void drawBoxEdges(JavaFXMeshBuilder meshBuilder, BoundingBox3DReadOnly boundingBox, double lineWidth)
   {
      drawBoxEdges(meshBuilder, convertBoundingBox3DToBox3D(boundingBox), lineWidth);
   }

   /**
    * Creates a 3D Euclid Box (a Shape) out of a 3D Bounding Box. Allocates a new Box3D.
    *
    * @param boundingBox
    * @return box
    */
   public static Box3D convertBoundingBox3DToBox3D(BoundingBox3DReadOnly boundingBox)
   {
      Point3DReadOnly minPoint = boundingBox.getMinPoint();
      Point3DReadOnly maxPoint = boundingBox.getMaxPoint();

      Point3D boxCenter = new Point3D();
      boxCenter.interpolate(minPoint, maxPoint, 0.5);
      Vector3D size = new Vector3D();
      size.sub(maxPoint, minPoint);

      return new Box3D(boxCenter, new Quaternion(), size.getX(), size.getY(), size.getZ());
   }

   public static void drawBoxEdges(JavaFXMeshBuilder meshBuilder, Box3DReadOnly box, double lineWidth)
   {
      ArrayList<Point3DReadOnly> orderedVertices = new ArrayList<>();

      orderedVertices.add(box.getVertex(0)); // x+y+z+  draw top
      orderedVertices.add(box.getVertex(1)); // x-y-z+
      orderedVertices.add(box.getVertex(3)); // x-y+z+
      orderedVertices.add(box.getVertex(2)); // x+y-z+
      orderedVertices.add(box.getVertex(0)); // x+y+z+

      orderedVertices.add(box.getVertex(4)); // x+y+z-  go down

      orderedVertices.add(box.getVertex(5)); // x-y-z-  leg 1
      orderedVertices.add(box.getVertex(1)); // x-y-z+
      orderedVertices.add(box.getVertex(5)); // x-y-z-

      orderedVertices.add(box.getVertex(7)); // x-y+z-  leg 2
      orderedVertices.add(box.getVertex(3)); // x-y+z+
      orderedVertices.add(box.getVertex(7)); // x-y+z-

      orderedVertices.add(box.getVertex(6)); // x+y-z-  leg 3
      orderedVertices.add(box.getVertex(2)); // x+y-z+
      orderedVertices.add(box.getVertex(6)); // x+y-z-

      orderedVertices.add(box.getVertex(4)); // x+y+z-  leg 4

      meshBuilder.addMultiLine(orderedVertices, lineWidth, false);
   }
}