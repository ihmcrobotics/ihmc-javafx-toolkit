package us.ihmc.javaFXToolkit.node;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Affine;
import javafx.scene.transform.MatrixType;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import us.ihmc.euclid.matrix.interfaces.RotationMatrixReadOnly;
import us.ihmc.euclid.tuple3D.Vector3D;
import us.ihmc.graphicsDescription.Graphics3DObject;
import us.ihmc.graphicsDescription.MeshDataGenerator;
import us.ihmc.graphicsDescription.MeshDataHolder;
import us.ihmc.graphicsDescription.appearance.AppearanceDefinition;
import us.ihmc.graphicsDescription.appearance.YoAppearanceRGBColor;
import us.ihmc.graphicsDescription.instructions.ArcTorusGraphics3DInstruction;
import us.ihmc.graphicsDescription.instructions.CapsuleGraphics3DInstruction;
import us.ihmc.graphicsDescription.instructions.ConeGraphics3DInstruction;
import us.ihmc.graphicsDescription.instructions.CubeGraphics3DInstruction;
import us.ihmc.graphicsDescription.instructions.CylinderGraphics3DInstruction;
import us.ihmc.graphicsDescription.instructions.EllipsoidGraphics3DInstruction;
import us.ihmc.graphicsDescription.instructions.ExtrudedPolygonGraphics3DInstruction;
import us.ihmc.graphicsDescription.instructions.Graphics3DAddExtrusionInstruction;
import us.ihmc.graphicsDescription.instructions.Graphics3DAddHeightMapInstruction;
import us.ihmc.graphicsDescription.instructions.Graphics3DAddMeshDataInstruction;
import us.ihmc.graphicsDescription.instructions.Graphics3DAddModelFileInstruction;
import us.ihmc.graphicsDescription.instructions.Graphics3DInstruction;
import us.ihmc.graphicsDescription.instructions.Graphics3DInstructionExecutor;
import us.ihmc.graphicsDescription.instructions.Graphics3DPrimitiveInstruction;
import us.ihmc.graphicsDescription.instructions.HemiEllipsoidGraphics3DInstruction;
import us.ihmc.graphicsDescription.instructions.PolygonGraphics3DInstruction;
import us.ihmc.graphicsDescription.instructions.PrimitiveGraphics3DInstruction;
import us.ihmc.graphicsDescription.instructions.PyramidCubeGraphics3DInstruction;
import us.ihmc.graphicsDescription.instructions.SphereGraphics3DInstruction;
import us.ihmc.graphicsDescription.instructions.TruncatedConeGraphics3DInstruction;
import us.ihmc.graphicsDescription.instructions.WedgeGraphics3DInstruction;
import us.ihmc.graphicsDescription.instructions.primitives.Graphics3DRotateInstruction;
import us.ihmc.graphicsDescription.instructions.primitives.Graphics3DScaleInstruction;
import us.ihmc.graphicsDescription.instructions.primitives.Graphics3DTranslateInstruction;
import us.ihmc.javaFXToolkit.graphics.JAssImpJavaFXTools;
import us.ihmc.javaFXToolkit.graphics.JavaFXMeshDataInterpreter;

public class JavaFXGraphicsObject extends Graphics3DInstructionExecutor
{
   private final Group parentGroup = new Group();
   private Group currentGroup = parentGroup;

   public JavaFXGraphicsObject(Graphics3DObject graphics3dObject)
   {
      this(graphics3dObject, null);
   }

   public JavaFXGraphicsObject(Graphics3DObject graphics3dObject, AppearanceDefinition appearance)
   {
      if (graphics3dObject != null)
      {
         List<Graphics3DPrimitiveInstruction> graphics3dInstructions = graphics3dObject.getGraphics3DInstructions();
         if (graphics3dInstructions != null)
         {
            for (Graphics3DPrimitiveInstruction instruction : graphics3dInstructions)
            {
               if (instruction instanceof Graphics3DInstruction)
               {
                  Graphics3DInstruction graphicsInstruction = (Graphics3DInstruction) instruction;
                  if (appearance != null)
                     graphicsInstruction.setAppearance(appearance);
               }
            }
            setUpGraphicsFromDefinition(graphics3dInstructions);
         }
      }
   }

   @Override
   protected void doAddMeshDataInstruction(Graphics3DAddMeshDataInstruction graphics3DAddMeshData)
   {
      graphics3DAddMeshData.getMeshData().getVertices();
      TriangleMesh outputMesh = interpretMeshData(graphics3DAddMeshData.getMeshData());
      Material outputMaterial = convertMaterial(graphics3DAddMeshData.getAppearance());

      MeshView meshView = new MeshView();
      meshView.setMesh(outputMesh);
      meshView.setMaterial(outputMaterial);
      Group meshGroup = new Group(meshView);
      currentGroup.getChildren().add(meshGroup);
      currentGroup = meshGroup;
   }

   @Override
   protected void doAddHeightMapInstruction(Graphics3DAddHeightMapInstruction graphics3DAddHeightMap)
   {
      // not implemented yet
   }

   @Override
   protected void doAddExtrusionInstruction(Graphics3DAddExtrusionInstruction graphics3DAddText)
   {
      // not implemented yet
   }

   @Override
   protected void doAddModelFileInstruction(Graphics3DAddModelFileInstruction graphics3DAddModelFile)
   {
      MeshView[] outputModelMeshes = new MeshView[0];
      try
      {
         outputModelMeshes = JAssImpJavaFXTools.getJavaFxMeshes(graphics3DAddModelFile.getFileName(), graphics3DAddModelFile.getResourceClassLoader());
      }
      catch (URISyntaxException | IOException e)
      {
         e.printStackTrace();
      }

      if (graphics3DAddModelFile.getAppearance() != null)
      {
         Material outputMaterial = convertMaterial(graphics3DAddModelFile.getAppearance());
         for (int i = 0; i < outputModelMeshes.length; i++)
         {
            outputModelMeshes[i].setMaterial(outputMaterial);
         }
      }

      Group meshGroup = new Group(outputModelMeshes);
      currentGroup.getChildren().add(meshGroup);
      currentGroup = meshGroup;
   }

   @Override
   protected void doIdentityInstruction()
   {
      currentGroup = parentGroup;
   }

   @Override
   protected void doRotateInstruction(Graphics3DRotateInstruction rot)
   {
      RotationMatrixReadOnly mat = rot.getRotationMatrix();
      Affine outputRotation = new Affine(new double[] {mat.getM00(), mat.getM01(), mat.getM02(), 0, mat.getM10(), mat.getM11(), mat.getM12(), 0, mat.getM20(),
            mat.getM21(), mat.getM22(), 0, 0, 0, 0, 1}, MatrixType.MT_3D_4x4, 0);

      Group rotationGroup = new Group();
      rotationGroup.getTransforms().add(outputRotation);
      currentGroup.getChildren().add(rotationGroup);
      currentGroup = rotationGroup;
   }

   @Override
   protected void doScaleInstruction(Graphics3DScaleInstruction graphics3DScale)
   {
      Vector3D scale = graphics3DScale.getScaleFactor();
      Scale outputScale = new Scale(scale.getX(), scale.getY(), scale.getZ());

      Group scaleGroup = new Group();
      scaleGroup.getTransforms().add(outputScale);
      currentGroup.getChildren().add(scaleGroup);
      currentGroup = scaleGroup;
   }

   @Override
   protected void doTranslateInstruction(Graphics3DTranslateInstruction graphics3DTranslate)
   {
      Vector3D t = graphics3DTranslate.getTranslation();
      Translate outputTranslation = new Translate(t.getX(), t.getY(), t.getZ());

      Group translationGroup = new Group();
      translationGroup.getTransforms().add(outputTranslation);
      currentGroup.getChildren().add(translationGroup);
      currentGroup = translationGroup;
   }

   public Group getGroup()
   {
      return parentGroup;
   }

   private static Material convertMaterial(AppearanceDefinition appearance)
   {
      float r = appearance.getColor().getX();
      float g = appearance.getColor().getY();
      float b = appearance.getColor().getZ();
      double transparency = appearance.getTransparency();

      if (appearance instanceof YoAppearanceRGBColor)
      {
         transparency = 1.0 - transparency;
      }

      Color color = new Color(r, g, b, transparency);
      PhongMaterial res = new PhongMaterial(color);
      res.setSpecularColor(Color.WHITE);
      return res;
   }

   private static TriangleMesh interpretMeshData(MeshDataHolder meshData)
   {
      return JavaFXMeshDataInterpreter.interpretMeshData(meshData);
   }

   @Override
   protected void doAddPrimitiveInstruction(PrimitiveGraphics3DInstruction primitiveInstruction)
   {
      if (primitiveInstruction instanceof CubeGraphics3DInstruction)
      {
         CubeGraphics3DInstruction cubeInstruction = (CubeGraphics3DInstruction) primitiveInstruction;

         MeshDataHolder meshData = MeshDataGenerator.Cube(cubeInstruction.getLength(),
                                                          cubeInstruction.getWidth(),
                                                          cubeInstruction.getHeight(),
                                                          cubeInstruction.getCenteredInTheCenter(),
                                                          cubeInstruction.getTextureFaces());
         Graphics3DAddMeshDataInstruction meshDataInstruction = Graphics3DObject.createMeshDataInstruction(meshData, cubeInstruction.getAppearance());

         doAddMeshDataInstruction(meshDataInstruction);
      }
      else if (primitiveInstruction instanceof SphereGraphics3DInstruction)
      {
         SphereGraphics3DInstruction sphereInstruction = (SphereGraphics3DInstruction) primitiveInstruction;

         MeshDataHolder meshData = MeshDataGenerator.Sphere(sphereInstruction.getRadius(),
                                                            sphereInstruction.getResolution(),
                                                            sphereInstruction.getResolution());
         Graphics3DAddMeshDataInstruction meshDataInstruction = Graphics3DObject.createMeshDataInstruction(meshData, sphereInstruction.getAppearance());

         doAddMeshDataInstruction(meshDataInstruction);
      }
      else if (primitiveInstruction instanceof WedgeGraphics3DInstruction)
      {
         WedgeGraphics3DInstruction wedgeInstruction = (WedgeGraphics3DInstruction) primitiveInstruction;

         MeshDataHolder meshData = MeshDataGenerator.Wedge(wedgeInstruction.getLengthX(), wedgeInstruction.getWidthY(), wedgeInstruction.getHeightZ());
         Graphics3DAddMeshDataInstruction meshDataInstruction = Graphics3DObject.createMeshDataInstruction(meshData, wedgeInstruction.getAppearance());

         doAddMeshDataInstruction(meshDataInstruction);
      }
      else if (primitiveInstruction instanceof CapsuleGraphics3DInstruction)
      {
         CapsuleGraphics3DInstruction capsuleInstruction = (CapsuleGraphics3DInstruction) primitiveInstruction;

         MeshDataHolder meshData = MeshDataGenerator.Capsule(capsuleInstruction.getHeight(),
                                                             capsuleInstruction.getXRadius(),
                                                             capsuleInstruction.getYRadius(),
                                                             capsuleInstruction.getZRadius(),
                                                             capsuleInstruction.getResolution(),
                                                             capsuleInstruction.getResolution());
         Graphics3DAddMeshDataInstruction meshDataInstruction = Graphics3DObject.createMeshDataInstruction(meshData, capsuleInstruction.getAppearance());

         doAddMeshDataInstruction(meshDataInstruction);
      }
      else if (primitiveInstruction instanceof EllipsoidGraphics3DInstruction)
      {
         EllipsoidGraphics3DInstruction ellipsoidInstruction = (EllipsoidGraphics3DInstruction) primitiveInstruction;

         MeshDataHolder meshData = MeshDataGenerator.Ellipsoid(ellipsoidInstruction.getXRadius(),
                                                               ellipsoidInstruction.getYRadius(),
                                                               ellipsoidInstruction.getZRadius(),
                                                               ellipsoidInstruction.getResolution(),
                                                               ellipsoidInstruction.getResolution());
         Graphics3DAddMeshDataInstruction meshDataInstruction = Graphics3DObject.createMeshDataInstruction(meshData, ellipsoidInstruction.getAppearance());
         doAddMeshDataInstruction(meshDataInstruction);
      }
      else if (primitiveInstruction instanceof CylinderGraphics3DInstruction)
      {
         CylinderGraphics3DInstruction cylinderInstruction = (CylinderGraphics3DInstruction) primitiveInstruction;

         MeshDataHolder meshData = MeshDataGenerator.Cylinder(cylinderInstruction.getRadius(),
                                                              cylinderInstruction.getHeight(),
                                                              cylinderInstruction.getResolution());
         Graphics3DAddMeshDataInstruction meshDataInstruction = Graphics3DObject.createMeshDataInstruction(meshData, cylinderInstruction.getAppearance());
         doAddMeshDataInstruction(meshDataInstruction);
      }
      else if (primitiveInstruction instanceof ConeGraphics3DInstruction)
      {
         ConeGraphics3DInstruction coneInstruction = (ConeGraphics3DInstruction) primitiveInstruction;

         MeshDataHolder meshData = MeshDataGenerator.Cone(coneInstruction.getHeight(), coneInstruction.getRadius(), coneInstruction.getResolution());
         Graphics3DAddMeshDataInstruction meshDataInstruction = Graphics3DObject.createMeshDataInstruction(meshData, coneInstruction.getAppearance());
         doAddMeshDataInstruction(meshDataInstruction);
      }
      else if (primitiveInstruction instanceof TruncatedConeGraphics3DInstruction)
      {
         TruncatedConeGraphics3DInstruction truncatedConeInstruction = (TruncatedConeGraphics3DInstruction) primitiveInstruction;

         MeshDataHolder meshData = MeshDataGenerator.GenTruncatedCone(truncatedConeInstruction.getHeight(),
                                                                      truncatedConeInstruction.getXBaseRadius(),
                                                                      truncatedConeInstruction.getYBaseRadius(),
                                                                      truncatedConeInstruction.getXTopRadius(),
                                                                      truncatedConeInstruction.getYTopRadius(),
                                                                      truncatedConeInstruction.getResolution());
         Graphics3DAddMeshDataInstruction meshDataInstruction = Graphics3DObject.createMeshDataInstruction(meshData, truncatedConeInstruction.getAppearance());
         doAddMeshDataInstruction(meshDataInstruction);
      }
      else if (primitiveInstruction instanceof HemiEllipsoidGraphics3DInstruction)
      {
         HemiEllipsoidGraphics3DInstruction hemiEllipsoidInstruction = (HemiEllipsoidGraphics3DInstruction) primitiveInstruction;

         MeshDataHolder meshData = MeshDataGenerator.HemiEllipsoid(hemiEllipsoidInstruction.getXRadius(),
                                                                   hemiEllipsoidInstruction.getYRadius(),
                                                                   hemiEllipsoidInstruction.getZRadius(),
                                                                   hemiEllipsoidInstruction.getResolution(),
                                                                   hemiEllipsoidInstruction.getResolution());
         Graphics3DAddMeshDataInstruction meshDataInstruction = Graphics3DObject.createMeshDataInstruction(meshData, hemiEllipsoidInstruction.getAppearance());
         doAddMeshDataInstruction(meshDataInstruction);
      }
      else if (primitiveInstruction instanceof ArcTorusGraphics3DInstruction)
      {
         ArcTorusGraphics3DInstruction arcTorusInstruction = (ArcTorusGraphics3DInstruction) primitiveInstruction;

         MeshDataHolder meshData = MeshDataGenerator.ArcTorus(arcTorusInstruction.getStartAngle(),
                                                              arcTorusInstruction.getEndAngle(),
                                                              arcTorusInstruction.getMajorRadius(),
                                                              arcTorusInstruction.getMinorRadius(),
                                                              arcTorusInstruction.getResolution());
         Graphics3DAddMeshDataInstruction meshDataInstruction = Graphics3DObject.createMeshDataInstruction(meshData, arcTorusInstruction.getAppearance());
         doAddMeshDataInstruction(meshDataInstruction);
      }
      else if (primitiveInstruction instanceof PyramidCubeGraphics3DInstruction)
      {
         PyramidCubeGraphics3DInstruction pyramidInstruction = (PyramidCubeGraphics3DInstruction) primitiveInstruction;

         MeshDataHolder meshData = MeshDataGenerator.PyramidCube(pyramidInstruction.getLengthX(),
                                                                 pyramidInstruction.getWidthY(),
                                                                 pyramidInstruction.getHeightZ(),
                                                                 pyramidInstruction.getPyramidHeight());
         Graphics3DAddMeshDataInstruction meshDataInstruction = Graphics3DObject.createMeshDataInstruction(meshData, pyramidInstruction.getAppearance());
         doAddMeshDataInstruction(meshDataInstruction);
      }
      else if (primitiveInstruction instanceof PolygonGraphics3DInstruction)
      {
         PolygonGraphics3DInstruction polygonInstruction = (PolygonGraphics3DInstruction) primitiveInstruction;

         MeshDataHolder meshData = MeshDataGenerator.Polygon(polygonInstruction.getPolygonPoints());
         Graphics3DAddMeshDataInstruction meshDataInstruction = Graphics3DObject.createMeshDataInstruction(meshData, polygonInstruction.getAppearance());
         doAddMeshDataInstruction(meshDataInstruction);
      }
      else if (primitiveInstruction instanceof ExtrudedPolygonGraphics3DInstruction)
      {
         ExtrudedPolygonGraphics3DInstruction extrudedPolygonInstruction = (ExtrudedPolygonGraphics3DInstruction) primitiveInstruction;

         MeshDataHolder meshData = MeshDataGenerator.ExtrudedPolygon(extrudedPolygonInstruction.getPolygonPoints(),
                                                                     extrudedPolygonInstruction.getExtrusionHeight());
         Graphics3DAddMeshDataInstruction meshDataInstruction = Graphics3DObject.createMeshDataInstruction(meshData,
                                                                                                           extrudedPolygonInstruction.getAppearance());
         doAddMeshDataInstruction(meshDataInstruction);
      }
      else
      {
         throw new RuntimeException("Need to support that primitive type! primitiveInstruction = " + primitiveInstruction);
      }
   }
}
