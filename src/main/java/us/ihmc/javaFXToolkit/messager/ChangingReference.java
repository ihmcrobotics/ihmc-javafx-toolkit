package us.ihmc.javaFXToolkit.messager;

import java.util.concurrent.atomic.AtomicReference;

/**
 * An atomic reference with one frame of history to keep track if it
 * is newly changed. Useful for activation and deactivation of UI
 * components that need to register and remove listeners.
 *
 * @author Duncan Calvert
 */
public class ChangingReference<T>
{
   private final AtomicReference<T> atomicReference;
   private T lastValue;
   private boolean changed = false;

   /**
    * Create a new changing reference.
    *
    * @param atomicReference
    */
   public ChangingReference(AtomicReference<T> atomicReference)
   {
      this.atomicReference = atomicReference;
      lastValue = atomicReference.get();
   }

   /**
    * Get the value from atomicReference.get() and set the changed flag.
    *
    * @return value
    */
   public T get()
   {
      T newValue = atomicReference.get();
      changed = !newValue.equals(lastValue);
      lastValue = newValue;
      return newValue;
   }

   /**
    * Returns if the value changed the last time {@link #get()} was called.
    *
    * @return hasChanged
    */
   public boolean hasChanged()
   {
      return changed;
   }
}
