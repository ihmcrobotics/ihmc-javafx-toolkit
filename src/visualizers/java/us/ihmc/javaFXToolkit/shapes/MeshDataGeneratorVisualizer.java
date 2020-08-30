package us.ihmc.javaFXToolkit.shapes;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.stage.Stage;
import us.ihmc.euclid.geometry.ConvexPolygon2D;
import us.ihmc.euclid.geometry.tools.EuclidGeometryRandomTools;
import us.ihmc.euclid.tuple2D.Point2D;
import us.ihmc.euclid.tuple2D.interfaces.Point2DReadOnly;
import us.ihmc.euclid.tuple3D.Point3D;
import us.ihmc.graphicsDescription.MeshDataGenerator;
import us.ihmc.javaFXToolkit.graphics.JavaFXMeshDataInterpreter;
import us.ihmc.javaFXToolkit.scenes.View3DFactory;
import us.ihmc.javaFXToolkit.starter.ApplicationRunner;

public class MeshDataGeneratorVisualizer
{
   private static final boolean USE_TEXTURE = true;

   public MeshDataGeneratorVisualizer(Stage primaryStage)
   {
      primaryStage.setTitle(getClass().getSimpleName());

      View3DFactory view3dFactory = new View3DFactory(600, 400);
      view3dFactory.addCameraController(true);
      view3dFactory.addWorldCoordinateSystem(0.4);

      PhongMaterial defaultMaterial;
      if (USE_TEXTURE)
      {
         defaultMaterial = new PhongMaterial();
         Image image = new Image(getClass().getClassLoader().getResourceAsStream("debugTextureGrid.jpg"));
         defaultMaterial.setDiffuseMap(image);
      }
      else
      {
         defaultMaterial = new PhongMaterial(Color.CYAN);
      }

      MeshView arcTorus = new MeshView(JavaFXMeshDataInterpreter.interpretMeshData(MeshDataGenerator.ArcTorus(0.0, 1.0 * Math.PI, 0.15, 0.05, 64)));
      arcTorus.setMaterial(defaultMaterial);
      arcTorus.setTranslateX(0.0);
      arcTorus.setTranslateY(0.0);
      view3dFactory.addNodeToView(arcTorus);

      MeshView cylinder = new MeshView(JavaFXMeshDataInterpreter.interpretMeshData(MeshDataGenerator.Cylinder(0.1, 0.3, 64, false)));
      cylinder.setMaterial(defaultMaterial);
      cylinder.setTranslateX(0.0);
      cylinder.setTranslateY(0.5);
      view3dFactory.addNodeToView(cylinder);

      MeshView flatRectangle = new MeshView(JavaFXMeshDataInterpreter.interpretMeshData(MeshDataGenerator.FlatRectangle(0.2, 0.3, 0.0)));
      flatRectangle.setMaterial(defaultMaterial);
      flatRectangle.setTranslateX(0.0);
      flatRectangle.setTranslateY(1.0);
      view3dFactory.addNodeToView(flatRectangle);

      MeshView cone = new MeshView(JavaFXMeshDataInterpreter.interpretMeshData(MeshDataGenerator.Cone(0.3, 0.1, 64)));
      cone.setMaterial(defaultMaterial);
      cone.setTranslateX(0.0);
      cone.setTranslateY(-0.5);
      view3dFactory.addNodeToView(cone);

      MeshView wedge = new MeshView(JavaFXMeshDataInterpreter.interpretMeshData(MeshDataGenerator.Wedge(0.3, 0.2, 0.2)));
      wedge.setMaterial(defaultMaterial);
      wedge.setTranslateX(0.0);
      wedge.setTranslateY(-1.0);
      view3dFactory.addNodeToView(wedge);

      MeshView sphere = new MeshView(JavaFXMeshDataInterpreter.interpretMeshData(MeshDataGenerator.Ellipsoid(0.15, 0.05, 0.2, 64, 64)));
      sphere.setMaterial(defaultMaterial);
      sphere.setTranslateX(0.5);
      sphere.setTranslateY(0.0);
      view3dFactory.addNodeToView(sphere);

      List<? extends Point2DReadOnly> polygonVertices = createPolygon();
      MeshView extrudedPolygon = new MeshView(JavaFXMeshDataInterpreter.interpretMeshData(MeshDataGenerator.ExtrudedPolygon(polygonVertices, 0.1)));
      extrudedPolygon.setMaterial(defaultMaterial);
      extrudedPolygon.setTranslateX(0.5);
      extrudedPolygon.setTranslateY(0.5);
      view3dFactory.addNodeToView(extrudedPolygon);

      MeshView line = new MeshView(JavaFXMeshDataInterpreter.interpretMeshData(MeshDataGenerator.Line(0.0, 0.0, 0.2, 0.0, 0.5, 0.0, 0.01)));
      line.setMaterial(defaultMaterial);
      line.setTranslateX(0.5);
      line.setTranslateY(1.0);
      view3dFactory.addNodeToView(line);

      MeshView pyramidCube = new MeshView(JavaFXMeshDataInterpreter.interpretMeshData(MeshDataGenerator.PyramidCube(0.1, 0.2, 0.1, 0.25)));
      pyramidCube.setMaterial(defaultMaterial);
      pyramidCube.setTranslateX(0.5);
      pyramidCube.setTranslateY(-0.5);
      view3dFactory.addNodeToView(pyramidCube);

      MeshView capsule = new MeshView(JavaFXMeshDataInterpreter.interpretMeshData(MeshDataGenerator.Capsule(0.3, 0.1, 0.05, 0.15, 16, 16)));
      capsule.setMaterial(defaultMaterial);
      capsule.setTranslateX(0.5);
      capsule.setTranslateY(-1.0);
      view3dFactory.addNodeToView(capsule);

      MeshView genTruncatedCone = new MeshView(JavaFXMeshDataInterpreter.interpretMeshData(MeshDataGenerator.GenTruncatedCone(0.3, 0.2, 0.07, 0.05, 0.1, 64)));
      genTruncatedCone.setMaterial(defaultMaterial);
      genTruncatedCone.setTranslateX(-0.5);
      genTruncatedCone.setTranslateY(0.0);
      view3dFactory.addNodeToView(genTruncatedCone);

      MeshView hemiEllipsoid = new MeshView(JavaFXMeshDataInterpreter.interpretMeshData(MeshDataGenerator.HemiEllipsoid(0.3, 0.1, 0.4, 16, 16)));
      hemiEllipsoid.setMaterial(defaultMaterial);
      hemiEllipsoid.setTranslateX(-0.5);
      hemiEllipsoid.setTranslateY(0.5);
      view3dFactory.addNodeToView(hemiEllipsoid);

      MeshView tetrahedron = new MeshView(JavaFXMeshDataInterpreter.interpretMeshData(MeshDataGenerator.Tetrahedron(0.3)));
      tetrahedron.setMaterial(defaultMaterial);
      tetrahedron.setTranslateX(-0.5);
      tetrahedron.setTranslateY(1.0);
      view3dFactory.addNodeToView(tetrahedron);

      MeshView polygon = new MeshView(JavaFXMeshDataInterpreter.interpretMeshData(MeshDataGenerator.PolygonCounterClockwise(null, polygonVertices)));
      polygon.setMaterial(defaultMaterial);
      polygon.setTranslateX(-0.5);
      polygon.setTranslateY(-0.5);
      view3dFactory.addNodeToView(polygon);

      MeshView cube = new MeshView(JavaFXMeshDataInterpreter.interpretMeshData(MeshDataGenerator.Cube(0.1, 0.2, 0.3, false)));
      cube.setMaterial(defaultMaterial);
      cube.setTranslateX(1.0);
      cube.setTranslateY(0.0);
      view3dFactory.addNodeToView(cube);

      MeshView polygon3D = new MeshView(JavaFXMeshDataInterpreter.interpretMeshData(MeshDataGenerator.PolygonCounterClockwise(polygonVertices.stream()
                                                                                                                                             .map(p2D ->
                                                                                                                                             {
                                                                                                                                                Point3D p3D = new Point3D(p2D);
                                                                                                                                                p3D.setZ(0.1
                                                                                                                                                      * (Math.random()
                                                                                                                                                            - 0.5));
                                                                                                                                                return p3D;
                                                                                                                                             })
                                                                                                                                             .collect(Collectors.toList()))));
      polygon3D.setMaterial(defaultMaterial);
      polygon3D.setTranslateX(1.0);
      polygon3D.setTranslateY(0.5);
      view3dFactory.addNodeToView(polygon3D);

      MeshView torus = new MeshView(JavaFXMeshDataInterpreter.interpretMeshData(MeshDataGenerator.Torus(0.15, 0.05, 64)));
      torus.setMaterial(defaultMaterial);
      torus.setTranslateX(1.0);
      torus.setTranslateY(-0.5);
      view3dFactory.addNodeToView(torus);

      primaryStage.setMaximized(true);
      primaryStage.setScene(view3dFactory.getScene());
      primaryStage.show();
   }

   private List<? extends Point2DReadOnly> createPolygon()
   {
      ConvexPolygon2D polygon = EuclidGeometryRandomTools.nextConvexPolygon2D(new Random(), 0.3, 10);
      List<Point2D> vertices = polygon.getPolygonVerticesView().stream().map(Point2D::new).collect(Collectors.toList());
      Collections.reverse(vertices);
      return vertices;
   }

   public static void main(String[] args)
   {
      ApplicationRunner.runApplication(MeshDataGeneratorVisualizer::new);
   }
}
