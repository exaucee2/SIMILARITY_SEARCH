/* Auteur: Exaucee MBUYI
 * NUMERO ETUDIANT:300268093
 */

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.PriorityQueue;


public class SimilaritySearch {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Usage: java SimilaritySearch <queryImageFilename> <imageDatasetDirectory>");
            return;
        }
        

        String queryImageFilename = args[0];
        String imageDatasetDirectory = args[1];
        ColorImage queryImage = new ColorImage(queryImageFilename);
        queryImage.reduceColor(3); 
        ColorHistogram queryHistogram = new ColorHistogram(3);
        queryHistogram.setImage(queryImage);

        PriorityQueue<ColorHistogram> heap = new PriorityQueue<>(Comparator.comparingDouble(hist -> -queryHistogram.compare(hist)));
        
        File datasetDir = new File(imageDatasetDirectory);
        File[] datasetFiles = datasetDir.listFiles();
        for (File file : datasetFiles) {
            if (file.isFile() && file.getName().endsWith(".txt")) {
               
                
                ColorHistogram histogram = new ColorHistogram(file.getAbsolutePath());
                  
                    heap.add(histogram);   
            }    
        }

        int numOfTopKSimilarImages = 5;

        System.out.println("Top " + numOfTopKSimilarImages + " similar images : ");
        for (int i = 0; i < numOfTopKSimilarImages; i++) {
            System.out.println(heap.poll().getFilename());
        }


    queryHistogram.save("output.txt"); 

    }
}
    


