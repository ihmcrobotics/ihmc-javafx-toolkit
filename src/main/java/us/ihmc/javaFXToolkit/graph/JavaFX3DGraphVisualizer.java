package us.ihmc.javaFXToolkit.graph;

import com.sun.javafx.application.PlatformImpl;

import javafx.stage.Stage;
import us.ihmc.yoVariables.buffer.interfaces.YoBufferVariableEntryHolder;
import us.ihmc.yoVariables.buffer.interfaces.YoBufferVariableEntryReader;
import us.ihmc.yoVariables.variable.YoVariable;

public class JavaFX3DGraphVisualizer
{
   public JavaFX3DGraphVisualizer(Stage primaryStage)
   {
      primaryStage.setTitle(getClass().getSimpleName());
      primaryStage.setWidth(1920);
      primaryStage.setHeight(1080);

      YoBufferVariableEntryHolder dataEntryHolder = new YoBufferVariableEntryHolder()
      {
         @Override
         public YoBufferVariableEntryReader getEntry(YoVariable yoVariable)
         {
            return null;
         }
      };
//      TimeDataHolder timeDataHolder = new MinimalTimeDataHolder(200);
//      SelectedVariableHolder selectedVariableHolder = new SelectedVariableHolder();
//      GraphIndicesHolder graphIndicesHolder = new MinimalGraphIndicesHolder();
//
//      JavaFX3DGraph javaFX3DGraph = new JavaFX3DGraph(graphIndicesHolder, selectedVariableHolder, dataEntryHolder, timeDataHolder);
//
//      primaryStage.setScene(javaFX3DGraph.getPanel().getScene());
//      primaryStage.show();
//
//      primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>()
//      {
//         @Override
//         public void handle(WindowEvent event)
//         {
//            System.exit(0);
//         }
//      });
   }

   public static void main(String[] args)
   {
      PlatformImpl.startup(() -> new JavaFX3DGraphVisualizer(new Stage()));
   }
}
