package us.ihmc.javaFXToolkit.node;

import java.util.ArrayList;

import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Transform;
import us.ihmc.euclid.transform.AffineTransform;
import us.ihmc.graphicsDescription.appearance.AppearanceDefinition;
import us.ihmc.graphicsDescription.structure.Graphics3DNode;
import us.ihmc.javaFXToolkit.JavaFXTools;

public class JavaFXGraphics3DNode extends Group
{
   private final Graphics3DNode graphicsNode;
   private final JavaFXGraphicsObject javaFXGraphicsObject;
   private final ArrayList<JavaFXGraphics3DNode> updatables = new ArrayList<>();

   public JavaFXGraphics3DNode(Graphics3DNode graphicsNode)
   {
      this(graphicsNode, null);
   }

   public JavaFXGraphics3DNode(Graphics3DNode graphicsNode, AppearanceDefinition appearance)
   {
      this.graphicsNode = graphicsNode;

      javaFXGraphicsObject = new JavaFXGraphicsObject(graphicsNode.getGraphics3DObject(), appearance);
      getChildren().add(javaFXGraphicsObject.getGroup());
   }

   public void update()
   {
      ObservableList<Transform> transforms = getTransforms();
      transforms.clear();
      Affine javaFxAffineTransform = new Affine();

      AffineTransform euclidAffineTransform = graphicsNode.getTransform();
      JavaFXTools.convertEuclidAffineToJavaFXAffine(euclidAffineTransform, javaFxAffineTransform);
      transforms.add(javaFxAffineTransform);

      for (int i = 0; i < updatables.size(); i++)
      {
         updatables.get(i).update();
      }
   }

   public void addChild(JavaFXGraphics3DNode child)
   {
      getChildren().add(child);
      updatables.add(child);
   }
}
