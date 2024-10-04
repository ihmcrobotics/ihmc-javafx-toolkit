package us.ihmc.javaFXToolkit;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import javafx.application.Platform;

import com.sun.javafx.application.PlatformImpl;

import javafx.stage.Stage;
import org.apache.commons.lang3.mutable.MutableObject;
import us.ihmc.euclid.axisAngle.AxisAngle;
import us.ihmc.euclid.axisAngle.interfaces.AxisAngleReadOnly;
import us.ihmc.euclid.matrix.RotationMatrix;
import us.ihmc.euclid.matrix.interfaces.RotationMatrixBasics;
import us.ihmc.euclid.matrix.interfaces.RotationMatrixReadOnly;
import us.ihmc.euclid.orientation.interfaces.Orientation3DReadOnly;
import us.ihmc.euclid.transform.AffineTransform;
import us.ihmc.euclid.transform.RigidBodyTransform;
import us.ihmc.euclid.tuple3D.interfaces.Point3DBasics;
import us.ihmc.euclid.tuple3D.interfaces.Tuple3DBasics;
import us.ihmc.euclid.tuple3D.interfaces.Tuple3DReadOnly;
import us.ihmc.euclid.tuple3D.interfaces.Vector3DBasics;
import us.ihmc.log.LogTools;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

public abstract class JavaFXTools
{
   public static void convertRotationMatrixToAffine(RotationMatrixReadOnly rotation, Affine affineToModify)
   {
      affineToModify.setMxx(rotation.getM00());
      affineToModify.setMxy(rotation.getM01());
      affineToModify.setMxz(rotation.getM02());
      affineToModify.setMyx(rotation.getM10());
      affineToModify.setMyy(rotation.getM11());
      affineToModify.setMyz(rotation.getM12());
      affineToModify.setMzx(rotation.getM20());
      affineToModify.setMzy(rotation.getM21());
      affineToModify.setMzz(rotation.getM22());
   }

   public static void convertEuclidAffineToJavaFXAffine(AffineTransform euclidAffine, Affine javaFxAffineToPack)
   {
      javaFxAffineToPack.setMxx(euclidAffine.getM00());
      javaFxAffineToPack.setMxy(euclidAffine.getM01());
      javaFxAffineToPack.setMxz(euclidAffine.getM02());
      javaFxAffineToPack.setMyx(euclidAffine.getM10());
      javaFxAffineToPack.setMyy(euclidAffine.getM11());
      javaFxAffineToPack.setMyz(euclidAffine.getM12());
      javaFxAffineToPack.setMzx(euclidAffine.getM20());
      javaFxAffineToPack.setMzy(euclidAffine.getM21());
      javaFxAffineToPack.setMzz(euclidAffine.getM22());

      javaFxAffineToPack.setTx(euclidAffine.getM03());
      javaFxAffineToPack.setTy(euclidAffine.getM13());
      javaFxAffineToPack.setTz(euclidAffine.getM23());
   }

   /**
    * @deprecated Use {@link #convertOrientation3DToAffine(Orientation3DReadOnly,Affine)} instead
    */
   @Deprecated
   public static void convertAxisAngleToAffine(Orientation3DReadOnly orientation, Affine affineToPack)
   {
      convertOrientation3DToAffine(orientation, affineToPack);
   }

   public static void convertOrientation3DToAffine(Orientation3DReadOnly orientation, Affine affineToPack)
   {
      convertRotationMatrixToAffine(new RotationMatrix(orientation), affineToPack);
   }

   /**
    * @deprecated Use {@link #createRigidBodyTransformToAffine(RigidBodyTransform)} instead
    */
   @Deprecated
   public static Affine convertRigidBodyTransformToAffine(RigidBodyTransform rigidBodyTransform)
   {
      return createRigidBodyTransformToAffine(rigidBodyTransform);
   }

   public static void convertRigidBodyTransformToAffine(RigidBodyTransform rigidBodyTransform, Affine affineToPack)
   {
      affineToPack.setMxx(rigidBodyTransform.getM00());
      affineToPack.setMxy(rigidBodyTransform.getM01());
      affineToPack.setMxz(rigidBodyTransform.getM02());
      affineToPack.setMyx(rigidBodyTransform.getM10());
      affineToPack.setMyy(rigidBodyTransform.getM11());
      affineToPack.setMyz(rigidBodyTransform.getM12());
      affineToPack.setMzx(rigidBodyTransform.getM20());
      affineToPack.setMzy(rigidBodyTransform.getM21());
      affineToPack.setMzz(rigidBodyTransform.getM22());

      affineToPack.setTx(rigidBodyTransform.getM03());
      affineToPack.setTy(rigidBodyTransform.getM13());
      affineToPack.setTz(rigidBodyTransform.getM23());
   }

   public static void convertOrientation3DToRotate(Orientation3DReadOnly orientation3D, Rotate rotateToPack)
   {
      if (orientation3D instanceof AxisAngleReadOnly)
         convertAxisAngleToRotate((AxisAngleReadOnly) orientation3D, rotateToPack);
      else
         convertAxisAngleToRotate(new AxisAngle(orientation3D), rotateToPack);
   }

   public static void convertAxisAngleToRotate(AxisAngleReadOnly axisAngle, Rotate rotateToPack)
   {
      rotateToPack.setAngle(axisAngle.getAngle());
      rotateToPack.setPivotX(0.0);
      rotateToPack.setPivotY(0.0);
      rotateToPack.setPivotZ(0.0);
      rotateToPack.setAxis(new javafx.geometry.Point3D(axisAngle.getX(), axisAngle.getY(), axisAngle.getZ()));
   }

   public static void convertTransformToRotationMatrix(Transform transform, RotationMatrixBasics rotationToPack)
   {
      rotationToPack.set(transform.getMxx(),
                         transform.getMxy(),
                         transform.getMxz(),
                         transform.getMyx(),
                         transform.getMyy(),
                         transform.getMyz(),
                         transform.getMzx(),
                         transform.getMzy(),
                         transform.getMzz());
   }

   public static Affine createRigidBodyTransformToAffine(RigidBodyTransform rigidBodyTransform)
   {
      Affine ret = new Affine();
      convertRigidBodyTransformToAffine(rigidBodyTransform, ret);
      return ret;
   }

   public static Rotate createRotateFromOrientation3D(Orientation3DReadOnly orientation3D)
   {
      if (orientation3D instanceof AxisAngleReadOnly)
         return createRotateFromAxisAngle((AxisAngleReadOnly) orientation3D);
      else
         return createRotateFromAxisAngle(new AxisAngle(orientation3D));
   }

   public static Rotate createRotateFromAxisAngle(AxisAngleReadOnly axisAngle)
   {
      return new Rotate(axisAngle.getAngle(), new javafx.geometry.Point3D(axisAngle.getX(), axisAngle.getY(), axisAngle.getZ()));
   }

   public static javafx.geometry.Point3D createJavaFXPoint3D(Tuple3DReadOnly tuple3D)
   {
      return new javafx.geometry.Point3D(tuple3D.getX(), tuple3D.getY(), tuple3D.getZ());
   }

   /**
    * @deprecated Use
    *             {@link #createAffineFromOrientation3DAndTuple(Orientation3DReadOnly,Tuple3DReadOnly)}
    *             instead
    */
   @Deprecated
   public static Affine createAffineFromQuaternionAndTuple(Orientation3DReadOnly orientation, Tuple3DReadOnly translation)
   {
      return createAffineFromOrientation3DAndTuple(orientation, translation);
   }

   public static Affine createAffineFromOrientation3DAndTuple(Orientation3DReadOnly orientation3D, Tuple3DReadOnly translation)
   {
      return createRigidBodyTransformToAffine(new RigidBodyTransform(orientation3D, translation));
   }

   public static Affine createAffineFromAxisAngle(AxisAngleReadOnly axisAngle)
   {
      Affine affine = new Affine();
      convertOrientation3DToAffine(axisAngle, affine);
      return affine;
   }

   public static void applyTranform(Transform transform, Vector3DBasics vectorToTransform)
   {
      javafx.geometry.Point3D temporaryVector = transform.deltaTransform(vectorToTransform.getX(), vectorToTransform.getY(), vectorToTransform.getZ());
      vectorToTransform.set(temporaryVector.getX(), temporaryVector.getY(), temporaryVector.getZ());
   }

   public static void applyTranform(Transform transform, Point3DBasics pointToTransform)
   {
      javafx.geometry.Point3D temporaryVector = transform.transform(pointToTransform.getX(), pointToTransform.getY(), pointToTransform.getZ());
      pointToTransform.set(temporaryVector.getX(), temporaryVector.getY(), temporaryVector.getZ());
   }

   public static void applyInvertTranform(Transform transform, Vector3DBasics vectorToTransform)
   {
      javafx.geometry.Point3D temporaryVector = new javafx.geometry.Point3D(vectorToTransform.getX(), vectorToTransform.getY(), vectorToTransform.getZ());
      try
      {
         transform.inverseDeltaTransform(temporaryVector);
      }
      catch (NonInvertibleTransformException e)
      {
         e.printStackTrace();
      }
      vectorToTransform.set(temporaryVector.getX(), temporaryVector.getY(), temporaryVector.getZ());
   }

   public static void addEquals(Translate translateToModify, Tuple3DBasics offset)
   {
      translateToModify.setX(translateToModify.getX() + offset.getX());
      translateToModify.setY(translateToModify.getY() + offset.getY());
      translateToModify.setZ(translateToModify.getZ() + offset.getZ());
   }

   public static void subEquals(Translate translateToModify, Tuple3DBasics offset)
   {
      translateToModify.setX(translateToModify.getX() - offset.getX());
      translateToModify.setY(translateToModify.getY() - offset.getY());
      translateToModify.setZ(translateToModify.getZ() - offset.getZ());
   }

   public static java.awt.Color jfxToAwt(javafx.scene.paint.Color jfxColor)
   {
      return new java.awt.Color((float) jfxColor.getRed(), (float) jfxColor.getGreen(), (float) jfxColor.getBlue(), (float) jfxColor.getOpacity());
   }

   public static javafx.scene.paint.Color awtToJfx(java.awt.Color awtColor)
   {
      return javafx.scene.paint.Color.rgb(awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue(), awtColor.getAlpha() / 255.0);
   }

   public static void startup()
   {
      PlatformImpl.startup(() ->
                           {
                              // Just to make sure JavaFX thread has started.
                           });
   }

   public static void runNFramesLater(int numberOfFramesToWait, Runnable runnable)
   {
      new AnimationTimer()
      {
         int counter = 0;

         @Override
         public void handle(long now)
         {
            if (counter++ > numberOfFramesToWait)
            {
               runnable.run();
               stop();
            }
         }
      }.start();
   }

   public static void runApplication(ApplicationNoModule application)
   {
      runApplication(application, null);
   }

   public static void runApplication(ApplicationNoModule application, Runnable initialize)
   {
      Runnable runnable = () ->
      {
         try
         {
            application.start(new Stage());
            if (initialize != null)
               initialize.run();
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      };

      PlatformImpl.startup(() ->
                           {
                              Platform.runLater(runnable);
                           });
      PlatformImpl.setImplicitExit(false);
   }

   public static void runApplication(Application application)
   {
      runApplication(application, null);
   }

   public static void runApplication(Application application, Runnable initialize)
   {
      Runnable runnable = () ->
      {
         try
         {
            application.start(new Stage());
            if (initialize != null)
               initialize.run();
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      };

      PlatformImpl.startup(() ->
                           {
                              Platform.runLater(runnable);
                           });
      PlatformImpl.setImplicitExit(false);
   }

   /**
    * - Controller class name must match the .fxml file name. - Must not set fx:contoller in FXML file
    * - This method expects .fxml to be in same package path as controller class
    */
   public static <T> T loadFromFXML(Object controller)
   {
      FXMLLoader loader = new FXMLLoader();
      loader.setController(controller);
      URL resource = controller.getClass().getResource(controller.getClass().getSimpleName() + ".fxml");
      if (resource == null)
      {
         throw new RuntimeException("Cannot find " + controller.getClass().getSimpleName() + ".fxml, please check the resource path.");
      }
      loader.setLocation(resource);

      try
      {
         return loader.load();
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
   }

   public static void runAndWait(final Runnable runnable)
   {
      if (Platform.isFxApplicationThread())
      {
         try
         {
            runnable.run();
         }
         catch (Throwable t)
         {
            LogTools.error("Exception in runnable");
            t.printStackTrace();
         }
      }
      else
      {
         final CountDownLatch doneLatch = new CountDownLatch(1);

         Platform.runLater(() ->
                           {
                              try
                              {
                                 runnable.run();
                              }
                              finally
                              {
                                 doneLatch.countDown();
                              }
                           });

         try
         {
            doneLatch.await();
         }
         catch (InterruptedException ex)
         {
            ex.printStackTrace();
         }
      }
   }

   public static <R> R runAndWait(final Callable<R> callable)
   {
      if (Platform.isFxApplicationThread())
      {
         try
         {
            return callable.call();
         }
         catch (Throwable t)
         {
            LogTools.error("Exception in callable");
            t.printStackTrace();
            return null;
         }
      }
      else
      {
         final CountDownLatch doneLatch = new CountDownLatch(1);
         final MutableObject<R> result = new MutableObject<>();

         Platform.runLater(() ->
                           {
                              try
                              {
                                 result.setValue(callable.call());
                              }
                              catch (Exception e)
                              {
                                 e.printStackTrace();
                              }
                              finally
                              {
                                 doneLatch.countDown();
                              }
                           });

         try
         {
            doneLatch.await();
            return result.getValue();
         }
         catch (InterruptedException ex)
         {
            ex.printStackTrace();
            return null;
         }
      }
   }
}
