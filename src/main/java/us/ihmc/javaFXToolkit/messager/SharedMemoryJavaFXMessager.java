package us.ihmc.javaFXToolkit.messager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import us.ihmc.messager.MessagerAPIFactory.MessagerAPI;
import us.ihmc.messager.MessagerAPIFactory.Topic;
import us.ihmc.messager.SharedMemoryMessager;
import us.ihmc.messager.TopicListener;

/**
 * Implementation of {@code JavaFXMessager} using shared memory.
 *
 * @author Sylvain Bertrand
 */
public class SharedMemoryJavaFXMessager extends SharedMemoryMessager implements JavaFXMessager
{
   private final Map<Topic<?>, JavaFXSyncedTopicListeners> javaFXSyncedTopicListeners = new HashMap<>();
   private final AnimationTimer animationTimer;
   private boolean readingListeners = false;

   /**
    * Creates a new messager.
    *
    * @param messagerAPI the API to use with this messager.
    */
   public SharedMemoryJavaFXMessager(MessagerAPI messagerAPI)
   {
      super(messagerAPI);
      animationTimer = new AnimationTimer()
      {
         @Override
         public void handle(long now)
         {
            try
            {
               readingListeners = true;
               for (JavaFXSyncedTopicListeners listener : javaFXSyncedTopicListeners.values())
                  listener.notifyListeners();
            }
            catch (Exception e)
            {
               e.printStackTrace();
            }
            finally
            {
               readingListeners = false;
            }
         }
      };
   }

   /** {@inheritDoc} */
   @Override
   public <T> void registerJavaFXSyncedTopicListener(Topic<T> topic, TopicListener<T> listener)
   {
      JavaFXSyncedTopicListeners topicListeners = javaFXSyncedTopicListeners.get(topic);
      if (topicListeners == null)
      {
         JavaFXSyncedTopicListeners newTopicListeners = new JavaFXSyncedTopicListeners(topic);
         topicListeners = newTopicListeners;

         try
         {
            if (!readingListeners && Platform.isFxApplicationThread())
            { // It appears to not be enough to check for application thread somehow.
               javaFXSyncedTopicListeners.put(topic, newTopicListeners);
            }
            else // The following one can throw an exception if the JavaFX thread has not started yet.
            {
               Platform.runLater(() -> javaFXSyncedTopicListeners.put(topic, newTopicListeners));
            }
         }
         catch (IllegalStateException e)
         { // The JavaFX thread has not started yet, no need to invoke Platform.runLater(...).
            javaFXSyncedTopicListeners.put(topic, newTopicListeners);
         }
      }
      topicListeners.addListener(listener);
   }

   /** {@inheritDoc} */
   @Override
   public void startMessager()
   {
      super.startMessager();
      animationTimer.start();
   }

   /** {@inheritDoc} */
   @Override
   public void closeMessager()
   {
      super.closeMessager();
      animationTimer.stop();
   }

   @SuppressWarnings("unchecked")
   private class JavaFXSyncedTopicListeners
   {
      private final ConcurrentLinkedQueue<Object> inputQueue = new ConcurrentLinkedQueue<>();
      private final List<TopicListener<Object>> listeners = new ArrayList<>();

      private JavaFXSyncedTopicListeners(Topic<?> topic)
      {
         registerTopicListener(topic, message -> inputQueue.add(message));
      }

      private void addListener(TopicListener<?> listener)
      {
         listeners.add((TopicListener<Object>) listener);
      }

      private void notifyListeners()
      {
         while (!inputQueue.isEmpty())
         {
            Object newData = inputQueue.poll();
            listeners.forEach(listener -> listener.receivedMessageForTopic(newData));
         }
      }
   }
}
