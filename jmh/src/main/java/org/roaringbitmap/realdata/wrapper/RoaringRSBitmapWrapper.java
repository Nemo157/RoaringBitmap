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
      throw new RuntimeException();
   }

   @Override
   public int cardinality() {
      return RoaringRS.roaring_rs_len(bitmap);
   }

   @Override
   public BitmapIterator iterator() {
      throw new RuntimeException();
   }

   @Override
   public BitmapIterator reverseIterator() {
      throw new RuntimeException();
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
      throw new RuntimeException();
   }

   @Override
   public BitmapAggregator naiveAndAggregator() {
      throw new RuntimeException();
   }

   @Override
   public BitmapAggregator naiveOrAggregator() {
      throw new RuntimeException();
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
      public static native void roaring_rs_delete(Pointer bitmap);
      public static native boolean roaring_rs_insert(Pointer bitmap, int i);
      public static native boolean roaring_rs_contains(Pointer bitmap, int i);
      public static native int roaring_rs_len(Pointer bitmap);
      public static native Pointer roaring_rs_and(Pointer bitmap, Pointer other);
      public static native Pointer roaring_rs_or(Pointer bitmap, Pointer other);
      public static native Pointer roaring_rs_xor(Pointer bitmap, Pointer other);
      public static native Pointer roaring_rs_sub(Pointer bitmap, Pointer other);

      static {
         Native.register("/Users/nemo157/sources/roaring-rs/bindings/target/release/libroaringrs.dylib");
      }
   }
}
