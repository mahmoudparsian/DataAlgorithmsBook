package org.dataalgorithms.bonus.wordcount.spark;

import scala.Tuple2;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Description:
 *
 *    SparkWordCount: Counting the words if their size is  
 *                    greater than or equal N, where N > 1.
 *
 * @author Mahmoud Parsian
 *
 */
public class SparkWordCount {

   private SparkWordCount() {
   }

   public static void main(String[] args) throws Exception {
       if (args.length != 3) {
          System.err.println("Usage: SparkWordCount <N> <input> <output>");
          System.exit(1);
       }

       // handle input parameters
       final int N = Integer.parseInt(args[0]);
       final String inputPath = args[1];
       final String outputPath = args[2];

       // create a context object, which is used 
       // as a factory for creating new RDDs
       JavaSparkContext ctx = new JavaSparkContext();

       // read input and create the first RDD
       JavaRDD<String> lines = ctx.textFile(inputPath, 1);

       //      output                                            input   output
       JavaRDD<String> words = lines.flatMap(new FlatMapFunction<String, String>() {
          //              output       input
          public Iterable<String> call(String s) {
             if ((s == null) || (s.length() < N)) {
                return Collections.emptyList();
             }
             
             String[] tokens = s.split(" ");
             List<String> list = new ArrayList<String>();
             for (String  tok : tokens) {
                if (tok.matches(".*[,.;]$")) {
                   // remove the special char from the end
                   tok = tok.substring(0, tok.length() -1); 
                }
           
                if (tok.length() < N) {
                   continue;
                }
           
                list.add(tok);
             }
             return list;
          }
       });

        //           K       V    
       JavaPairRDD<String, Integer> ones = 
                            //                   input    K       V
               words.mapToPair(new PairFunction<String, String, Integer>() {
         //            K       V             input
         public Tuple2<String, Integer> call(String s) {
           //                K       V
           return new Tuple2<String, Integer>(s, 1);
         }
       });

       // find the total count for each unique word
       JavaPairRDD<String, Integer> counts = 
            ones.reduceByKey(new Function2<Integer, Integer, Integer>() {
          public Integer call(Integer i1, Integer i2) {
             return i1 + i2;
          }
       });

       // save the final output 
       counts.saveAsTextFile(outputPath);

       // close the context and we are done
       ctx.close();
       System.exit(0);
    }
    
}