package us.ihmc.javaFXGraphics;

import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import us.ihmc.euclid.tuple3D.Vector3D;
import us.ihmc.graphicsDescription.Graphics3DObject;
import us.ihmc.graphicsDescription.appearance.AppearanceDefinition;
import us.ihmc.graphicsDescription.appearance.YoAppearance;
import us.ihmc.graphicsDescription.color.MutableColor;
import us.ihmc.graphicsDescription.instructions.Graphics3DInstruction;
import us.ihmc.graphicsDescription.instructions.Graphics3DPrimitiveInstruction;
import us.ihmc.javaFXToolkit.node.JavaFXGraphicsObject;

@Tag("gui")
public class JavaFXGraphicsTest
{
   private static final double CUBE_SIDE = 2.0;
   private static final double CUBE_X = 5.0;

   private static final AppearanceDefinition desiredAppearance = YoAppearance.Red();

   @Test
   public void testJavaFXGraphicsObject()
   {
      Graphics3DObject cubeGraphics = new Graphics3DObject();
      cubeGraphics.translate(new Vector3D(CUBE_X, 0.0, 0.0));
      cubeGraphics.addCube(CUBE_SIDE, CUBE_SIDE, CUBE_SIDE);

      JavaFXGraphicsObject javaFXGraphics = new JavaFXGraphicsObject(cubeGraphics, desiredAppearance);

      List<Graphics3DPrimitiveInstruction> graphics3dInstructions = cubeGraphics.getGraphics3DInstructions();
      for (Graphics3DPrimitiveInstruction primitive : graphics3dInstructions)
      {
         if (primitive instanceof Graphics3DInstruction)
         {
            Graphics3DInstruction graphicsInstruction = (Graphics3DInstruction) primitive;
            AppearanceDefinition appearance = graphicsInstruction.getAppearance();
            MutableColor actualColor = appearance.getColor();
            MutableColor expectedColor = desiredAppearance.getColor();
            assertTrue(actualColor.x == expectedColor.x);
            assertTrue(actualColor.y == expectedColor.y);
            assertTrue(actualColor.z == expectedColor.z);
         }
      }

      System.out.print(javaFXGraphics.getClass().getSimpleName() + " propely set up graphics from instructions.");
   }
}
