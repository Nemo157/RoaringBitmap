package org.roaringbitmap.realdata.wrapper;

import org.roaringbitmap.FastAggregation;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Iterator;

final class RoaringRSBitmapWrapper implements Bitmap {
   private final Pointer bitmap;

   RoaringRSBitmapWrapper(int[] data) {
      this.bitmap = RoaringRS.roaring_rs_collect(data, data.length);
   }

   RoaringRSBitmapWrapper(Pointer bitmap) {
      this.bitmap = bitmap;
   }

   @Override
   public boolean contains(int i) {
      return RoaringRS.roaring_rs_contains(bitmap, i);
   }

   @Override
   public int last() {
      return RoaringRS.roaring_rs_iter_last(RoaringRS.roaring_rs_iter(bitmap));
   }

   @Override
   public int cardinality() {
      return RoaringRS.roaring_rs_len(bitmap);
   }

   @Override
   public BitmapIterator iterator() {
      return new RoaringRSIteratorWrapper(bitmap);
   }

   @Override
   public BitmapIterator reverseIterator() {
      return new RoaringRSRevIteratorWrapper(bitmap);
   }

   @Override
   public Bitmap and(Bitmap other) {
      return new RoaringRSBitmapWrapper(RoaringRS.roaring_rs_and(bitmap, ((RoaringRSBitmapWrapper) other).bitmap));
   }

   @Override
   public Bitmap or(Bitmap other) {
      return new RoaringRSBitmapWrapper(RoaringRS.roaring_rs_or(bitmap, ((RoaringRSBitmapWrapper) other).bitmap));
   }

   @Override
   public Bitmap flip(int rangeStart, int rangeEnd) {
      throw new RuntimeException();
   }

   @Override
   public Bitmap xor(Bitmap other) {
      return new RoaringRSBitmapWrapper(RoaringRS.roaring_rs_xor(bitmap, ((RoaringRSBitmapWrapper) other).bitmap));
   }

   @Override
   public Bitmap andNot(Bitmap other) {
      return new RoaringRSBitmapWrapper(RoaringRS.roaring_rs_sub(bitmap, ((RoaringRSBitmapWrapper) other).bitmap));
   }

   @Override
   public BitmapAggregator naiveAndAggregator() {
      return new BitmapAggregator() {
         @Override
         public Bitmap aggregate(Iterable<Bitmap> bitmaps) {
            Iterator<Bitmap> it = bitmaps.iterator();
            RoaringRSBitmapWrapper bitmap = (RoaringRSBitmapWrapper)it.next();
            while (it.hasNext()) {
               bitmap = (RoaringRSBitmapWrapper)bitmap.and(it.next());
            }
            return bitmap;
         }
      };
   }

   @Override
   public BitmapAggregator naiveOrAggregator() {
      return new BitmapAggregator() {
         @Override
         public Bitmap aggregate(Iterable<Bitmap> bitmaps) {
            Iterator<Bitmap> it = bitmaps.iterator();
            RoaringRSBitmapWrapper bitmap = (RoaringRSBitmapWrapper)it.next();
            while (it.hasNext()) {
               bitmap = (RoaringRSBitmapWrapper)bitmap.or(it.next());
            }
            return bitmap;
         }
      };
   }

   @Override
   public BitmapAggregator priorityQueueOrAggregator() {
      throw new RuntimeException();
   }

   @Override
   public void serialize(DataOutputStream dos) throws IOException {
      throw new RuntimeException();
   }

   protected void finalize() throws Throwable {
      RoaringRS.roaring_rs_delete(bitmap);
      super.finalize();
   }

   private static class RoaringRS {
      public static native Pointer roaring_rs_new();
      public static native Pointer roaring_rs_collect(int[] data, int length);
      // Consumes bitmap
      public static native void roaring_rs_delete(Pointer bitmap);

      public static native boolean roaring_rs_insert(Pointer bitmap, int i);
      public static native boolean roaring_rs_contains(Pointer bitmap, int i);
      public static native int roaring_rs_len(Pointer bitmap);
      public static native Pointer roaring_rs_and(Pointer bitmap, Pointer other);
      public static native Pointer roaring_rs_or(Pointer bitmap, Pointer other);
      public static native Pointer roaring_rs_xor(Pointer bitmap, Pointer other);
      public static native Pointer roaring_rs_sub(Pointer bitmap, Pointer other);

      public static native Pointer roaring_rs_iter(Pointer bitmap);
      // Consumes iter
      public static native Pointer roaring_rs_iter_delete(Pointer iter);
      // Consumes iter
      public static native int roaring_rs_iter_last(Pointer iter);
      public static native int roaring_rs_iter_next(Pointer iter);
      // Consumes iter
      public static native Pointer roaring_rs_iter_rev(Pointer iter);
      // Consumes iter
      public static native Pointer roaring_rs_iter_rev_delete(Pointer revIter);
      public static native int roaring_rs_iter_rev_next(Pointer revIter);

      static {
         Native.register("roaringrs");
      }
   }

   private static class RoaringRSIteratorWrapper implements BitmapIterator {
      private final Pointer iter;
      private int next;

      RoaringRSIteratorWrapper(Pointer bitmap) {
         this.iter = RoaringRS.roaring_rs_iter(bitmap);
         this.next = RoaringRS.roaring_rs_iter_next(this.iter);
      }

      @Override
      public boolean hasNext() {
         return next != 0xFFFFFFFF;
      }

      @Override
      public int next() {
         int current = next;
         next = RoaringRS.roaring_rs_iter_next(iter);
         return current;
      }

      protected void finalize() throws Throwable {
         RoaringRS.roaring_rs_iter_delete(iter);
         super.finalize();
      }
   }

   private static class RoaringRSRevIteratorWrapper implements BitmapIterator {
      private final Pointer iter;
      private int next;

      RoaringRSRevIteratorWrapper(Pointer bitmap) {
         this.iter = RoaringRS.roaring_rs_iter_rev(RoaringRS.roaring_rs_iter(bitmap));
         this.next = RoaringRS.roaring_rs_iter_rev_next(this.iter);
      }

      @Override
      public boolean hasNext() {
         return next != 0xFFFFFFFF;
      }

      @Override
      public int next() {
         int current = next;
         next = RoaringRS.roaring_rs_iter_rev_next(iter);
         return current;
      }

      protected void finalize() throws Throwable {
         RoaringRS.roaring_rs_iter_rev_delete(iter);
         super.finalize();
      }
   }
}
