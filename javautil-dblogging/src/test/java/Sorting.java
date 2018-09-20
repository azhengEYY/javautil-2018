import java.util.Arrays;

public class Sorting {
  public static void main (String [] args) {
   int [] array = {45,12,85,32,89,39,69,44,42,1,6,8};
   Arrays.sort(array);
   for (int i = 0; i < array.length; i++) {
   System.out.println(array[i]);
   };
  }
}