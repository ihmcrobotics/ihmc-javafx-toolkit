package us.ihmc.javaFXToolkit.graphing;

import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import javax.imageio.ImageIO;

import gnu.trove.map.hash.TObjectIntHashMap;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Group;
import javafx.scene.ParallelCamera;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import us.ihmc.commons.Epsilons;
import us.ihmc.commons.MathTools;
import us.ihmc.euclid.transform.AffineTransform;
import us.ihmc.euclid.tuple2D.Point2D;
import us.ihmc.graphicsDescription.color.Gradient;
import us.ihmc.graphicsDescription.graphInterfaces.GraphIndicesHolder;
import us.ihmc.graphicsDescription.graphInterfaces.SelectedVariableHolder;
import us.ihmc.javaFXToolkit.JavaFXTools;
import us.ihmc.yoVariables.buffer.interfaces.YoBufferVariableEntryHolder;
import us.ihmc.yoVariables.buffer.interfaces.YoBufferVariableEntryReader;
import us.ihmc.yoVariables.buffer.interfaces.YoTimeBufferHolder;
import us.ihmc.yoVariables.registry.YoRegistry;
import us.ihmc.yoVariables.variable.YoDouble;

public class JavaFXHeatmapGraph
{
   private final JFXPanel javaFXPanel;
   private Group rootGroup;
   private final GraphIndicesHolder graphIndicesHolder;
   private final YoBufferVariableEntryHolder dataEntryHolder;

   private final Point2D focusPoint;
   private final AffineTransform transformToCanvasSpace;

   private TObjectIntHashMap<Point2D> heatmap;

   private YoDouble x;
   private YoDouble y;
   private GraphicsContext graphicsContext;
   private Scene scene;

   private Optional<Point2D> adjustingViewRangeMax;
   private Optional<Point2D> adjustingViewRangeMin;
   private JavaFXGraphColors colors;
   private Canvas canvas;
   private ParallelCamera parallelCamera;
   private Point2D gridCenter;
   private Point2D plotPencil;
   private Point2D viewRange;

   private final java.awt.Color[] rainbow = Gradient.createRainbow(500);

   public JavaFXHeatmapGraph(YoRegistry registry, GraphIndicesHolder graphIndicesHolder, SelectedVariableHolder selectedVariableHolder,
                             YoBufferVariableEntryHolder dataEntryHolder, YoTimeBufferHolder dataBuffer)
   {
      javaFXPanel = new JFXPanel();
      this.graphIndicesHolder = graphIndicesHolder;
      this.dataEntryHolder = dataEntryHolder;

      heatmap = new TObjectIntHashMap<>(dataBuffer.getTimeBuffer().length);

      adjustingViewRangeMax = Optional.empty();
      adjustingViewRangeMin = Optional.empty();

      focusPoint = new Point2D(1.0, 1.0);
      transformToCanvasSpace = new AffineTransform();
      transformToCanvasSpace.appendScale(50.0, 50.0, 1.0);

      gridCenter = new Point2D();
      plotPencil = new Point2D();
      viewRange = new Point2D();

      colors = JavaFXGraphColors.javaFXStyle();

      Platform.runLater(new Runnable()
      {
         @Override
         public void run()
         {
            rootGroup = new Group();
            canvas = new Canvas();
            graphicsContext = canvas.getGraphicsContext2D();
            rootGroup.getChildren().add(canvas);

            scene = new Scene(rootGroup);
            javaFXPanel.setScene(scene);

            parallelCamera = new ParallelCamera();
            scene.setCamera(parallelCamera);
         }
      });
   }

   public void update()
   {
      Platform.runLater(new Runnable()
      {
         @Override
         public void run()
         {
            // Update canvas for panel resize
            canvas.setWidth(javaFXPanel.getWidth());
            canvas.setHeight(javaFXPanel.getHeight());

            transformToCanvasSpace.setTranslationX((javaFXPanel.getWidth() / 2.0) - (focusPoint.getX() * transformToCanvasSpace.getLinearTransform().getScaleX()));
            transformToCanvasSpace.setTranslationY((javaFXPanel.getHeight() / 2.0) - (focusPoint.getY() * transformToCanvasSpace.getLinearTransform().getScaleY()));

            // save graphics context
            graphicsContext.save();

            // background
            graphicsContext.setFill(colors.getBackgroundColor());
            graphicsContext.fillRect(0.0, 0.0, javaFXPanel.getWidth(), javaFXPanel.getHeight());

            // grid lines
            graphicsContext.setStroke(colors.getGridAxisColor());
            gridCenter.set(0.0, 0.0);
            transformToCanvasSpace.transform(gridCenter);
            graphicsContext.strokeLine(0, gridCenter.getY(), javaFXPanel.getWidth(), gridCenter.getY());
            graphicsContext.strokeLine(gridCenter.getX(), 0.0, gridCenter.getX(), javaFXPanel.getHeight());

            plotXYHeatmap();

            drawGridLines();

            graphicsContext.setStroke(colors.getLabelColor());
            graphicsContext.strokeText(x.getName(), canvas.getWidth() / 2, canvas.getHeight() - 5);
            graphicsContext.rotate(-90.0);
            graphicsContext.strokeText(y.getName(), -canvas.getHeight() / 2, 10);
            graphicsContext.rotate(90.0);

            // restore context for next draw
            graphicsContext.restore();
         }
      });
   }

   private void drawGridLines()
   {
      //      // change grid line scale from 1m to 10cm ehn below 10m
      //      Point2D gridSize = new Point2D();
      //      gridSize.set(calculateGridSizePixels(transformToCanvasSpace.getScaleX()), calculateGridSizePixels(transformToCanvasSpace.getScaleY()));
      //      
      //      upperLeftCorner.changeFrame(pixelsFrame);
      //      lowerRightCorner.changeFrame(pixelsFrame);
      //
      //      focusPoint.changeFrame(pixelsFrame);
      //      double gridStart = (Math.round(focusPoint.getX() / gridSize.getX()) - 20.0) * gridSize.getX();
      //      double gridEnd = (Math.round(focusPoint.getX() / gridSize.getX()) + 20.0) * gridSize.getX();
      //
      //      for (double gridX = gridStart; gridX < gridEnd; gridX += gridSize.getX())
      //      {
      //         gridLinePencil.setIncludingFrame(pixelsFrame, gridX, 0.0);
      //
      //         gridLinePencil.changeFrame(metersFrame);
      //         gridSize.changeFrame(metersFrame);
      //         int nthGridLineFromOrigin = (int) (Math.abs(gridLinePencil.getX()) / gridSize.getX());
      //         if (MathTools.epsilonEquals(Math.abs(gridLinePencil.getX()) % gridSize.getX(), gridSize.getX(), 1e-7))
      //         {
      //            nthGridLineFromOrigin++;
      //         }
      //         applyParametersForGridline(graphics2d, nthGridLineFromOrigin);
      //
      //         gridLinePencil.changeFrame(pixelsFrame);
      //         gridSize.changeFrame(pixelsFrame);
      //         tempGridLine.set(gridLinePencil.getX(), gridLinePencil.getY(), 0.0, 1.0);
      //         graphics2d.drawLine(pixelsFrame, tempGridLine);
      //      }
   }

   private double calculateGridSizePixels(double pixelsPerMeter)
   {
      double medianGridWidthInPixels = Toolkit.getDefaultToolkit().getScreenResolution();
      double desiredMeters = medianGridWidthInPixels / pixelsPerMeter;
      double decimalPlace = Math.log10(desiredMeters);
      double orderOfMagnitude = Math.floor(decimalPlace);
      double nextOrderOfMagnitude = Math.pow(10, orderOfMagnitude + 1);
      double percentageToNextOrderOfMagnitude = desiredMeters / nextOrderOfMagnitude;

      double remainder = percentageToNextOrderOfMagnitude % 0.5;
      double roundToNearestPoint5 = remainder >= 0.25 ? percentageToNextOrderOfMagnitude + (0.5 - remainder) : percentageToNextOrderOfMagnitude - remainder;

      double gridSizeMeters;
      if (roundToNearestPoint5 > 0.0)
      {
         gridSizeMeters = nextOrderOfMagnitude * roundToNearestPoint5;
      }
      else
      {
         gridSizeMeters = Math.pow(10, orderOfMagnitude);
      }
      double gridSizePixels = gridSizeMeters * pixelsPerMeter;

      return gridSizePixels;
   }

   private void plotXYHeatmap()
   {
      YoBufferVariableEntryReader xDataEntry = dataEntryHolder.getEntry(x);
      YoBufferVariableEntryReader yDataEntry = dataEntryHolder.getEntry(y);

      double discreteX = 0.09;
      double discreteY = 0.3;

      for (int i = graphIndicesHolder.getInPoint(); i < graphIndicesHolder.getIndex(); i++)
      {
         double roundedX = MathTools.roundToPrecision(xDataEntry.getBuffer()[i], discreteX);
         double roundedY = MathTools.roundToPrecision(yDataEntry.getBuffer()[i], discreteY);

         plotPencil.set(roundedX, roundedY);

         heatmap.adjustOrPutValue(plotPencil, 1, 1);
         int heat = heatmap.get(plotPencil);

         adjustViewRange(plotPencil.getX(), plotPencil.getY());

         transformToCanvasSpace.transform(plotPencil);

         graphicsContext.setFill(getHeatColor(heat));
         fillRect(plotPencil.getX(), plotPencil.getY(), discreteX * transformToCanvasSpace.getLinearTransform().getScaleX(), discreteY * transformToCanvasSpace.getLinearTransform().getScaleY());
      }

      heatmap.clear();
   }

   private Color getHeatColor(int heat)
   {
      double maxHeat = 30.0;
      int heatIndex = (int) MathTools.roundToPrecision(MathTools.clamp((heat / maxHeat) * 500.0, 0.0, 499.0), 1.0);
      return JavaFXTools.awtToJfx(rainbow[heatIndex]);
   }

   private void fillRect(double x, double y, double width, double height)
   {
      double upperLeftX = x - (width / 2.0);
      double upperLeftY = y + (height / 2.0);
      graphicsContext.fillRect(upperLeftX, upperLeftY, width, height);
   }

   public void setXVariable(YoDouble x)
   {
      this.x = x;
   }

   public void setYVariable(YoDouble y)
   {
      this.y = y;
   }

   public Scene getScene()
   {
      return new Scene(rootGroup);
   }

   public JFXPanel getPanel()
   {
      return javaFXPanel;
   }

   private void setViewRange(double viewRangeXMeters, double viewRangeYMeters)
   {
      transformToCanvasSpace.getLinearTransform().setScale(javaFXPanel.getWidth() / viewRangeXMeters, javaFXPanel.getHeight() / viewRangeYMeters, 1.0);
   }

   public BufferedImage snapshot()
   {
      WritableImage snapshot = scene.snapshot(null);
      BufferedImage fromFXImage = SwingFXUtils.fromFXImage(snapshot, null);
      BufferedImage pngImage = null;
      byte[] imageInByte;
      try
      {
         ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
         ImageIO.write(fromFXImage, "png", byteArrayOutputStream);
         byteArrayOutputStream.flush();
         imageInByte = byteArrayOutputStream.toByteArray();
         byteArrayOutputStream.close();
         InputStream in = new ByteArrayInputStream(imageInByte);
         pngImage = ImageIO.read(in);
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
      return pngImage;
   }

   private void adjustViewRange(double xInMeters, double yInMeters)
   {
      viewRange.set(xInMeters, yInMeters);

      if (!adjustingViewRangeMax.isPresent())
      {
         adjustingViewRangeMax = Optional.of(new Point2D(viewRange.getX() + Epsilons.ONE_THOUSANDTH, viewRange.getY() + Epsilons.ONE_THOUSANDTH));
         adjustingViewRangeMin = Optional.of(new Point2D(viewRange.getX() - Epsilons.ONE_THOUSANDTH, viewRange.getY() - Epsilons.ONE_THOUSANDTH));
         focusPoint.setX(xInMeters);
         focusPoint.setY(yInMeters);
         return;
      }

      if (xInMeters > adjustingViewRangeMax.get().getX())
      {
         adjustingViewRangeMax.get().setX(xInMeters);
      }
      if (yInMeters > adjustingViewRangeMax.get().getY())
      {
         adjustingViewRangeMax.get().setY(yInMeters);
      }
      if (xInMeters < adjustingViewRangeMin.get().getX())
      {
         adjustingViewRangeMin.get().setX(xInMeters);
      }
      if (yInMeters < adjustingViewRangeMin.get().getY())
      {
         adjustingViewRangeMin.get().setY(yInMeters);
      }

      focusPoint.setX(adjustingViewRangeMin.get().getX() + ((adjustingViewRangeMax.get().getX() - adjustingViewRangeMin.get().getX()) / 2.0));
      focusPoint.setY(adjustingViewRangeMin.get().getY() + ((adjustingViewRangeMax.get().getY() - adjustingViewRangeMin.get().getY()) / 2.0));

      setViewRange((adjustingViewRangeMax.get().getX() - adjustingViewRangeMin.get().getX()) * 1.10,
                   (adjustingViewRangeMax.get().getY() - adjustingViewRangeMin.get().getY()) * 1.10);
   }
}
