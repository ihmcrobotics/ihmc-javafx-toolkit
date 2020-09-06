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
import us.ihmc.graphicsDescription.appearance.AppearanceDefinition;
import us.ihmc.graphicsDescription.appearance.YoAppearanceRGBColor;
import us.ihmc.graphicsDescription.instructions.Graphics3DAddExtrusionInstruction;
import us.ihmc.graphicsDescription.instructions.Graphics3DAddHeightMapInstruction;
import us.ihmc.graphicsDescription.instructions.Graphics3DAddMeshDataInstruction;
import us.ihmc.graphicsDescription.instructions.Graphics3DAddModelFileInstruction;
import us.ihmc.graphicsDescription.instructions.Graphics3DInstruction;
import us.ihmc.graphicsDescription.instructions.Graphics3DInstructionExecutor;
import us.ihmc.graphicsDescription.instructions.Graphics3DPrimitiveInstruction;
import us.ihmc.graphicsDescription.instructions.primitives.Graphics3DRotateInstruction;
import us.ihmc.graphicsDescription.instructions.primitives.Graphics3DScaleInstruction;
import us.ihmc.graphicsDescription.instructions.primitives.Graphics3DTranslateInstruction;
import us.ihmc.graphicsDescription.mesh.MeshDataHolder;
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
}
