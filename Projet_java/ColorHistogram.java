/* Auteur: Exaucee MBUYI
 * NUMERO ETUDIANT:300268093
 */


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
class ColorHistogram {
    private double[] histogram;
    private int depth;
    private ColorImage colorImage;
    private String filename;

    // le constructeur qui creent une instance d'une image a d bits 

    public ColorHistogram(int d) {
        depth = d;
        histogram = new double[(int) Math.pow(2, depth * 3)];
        this.filename = "";
    }
    //un constructeur qui creent une une  image a partir d'un fichier txt 

    public ColorHistogram(String filename) throws IOException {
        File  file = new File(filename);
        Scanner scanner = new Scanner(file);
        int size = scanner.nextInt();
        histogram = new double[size];
        this.filename = filename;

        double sum = 0.0;
        for (int i = 0; i < size; i++) {
            histogram[i] = scanner.nextDouble();
            sum += histogram[i];
        }
        for (int i = 0; i < size; i++) {
            histogram[i] /= sum;
        }

        scanner.close();
    }  

    // la methode qui associe une image avec un histogramme
    public void setImage(ColorImage image) {
        colorImage = image;
        int imageWidth = image.getWidth();  
        int imageHeight = image.getHeight(); 
        long totalPixels = (long) imageWidth * imageHeight;
        for (int i = 0; i < imageWidth; i++) {
            for (int j = 0; j < imageHeight; j++) {
                int[] pixel = image.getPixel(i, j);
                int index = (pixel[0]<<(2*depth)) + (pixel[1]<<depth) + pixel[2];
                histogram[index]++;
            }
        }
    
        for (int i = 0; i < histogram.length; i++) {
            histogram[i] = (double) histogram[i] / totalPixels;
        }
    }
// la methode qui retourne le histogramme  normalise d'une image
    public double[] getHistogram() {
        return histogram;
    }
// la methode qui retourne le nom de l'image
    public String getFilename() {
        return filename;
    }
    // la methode qui compare  l'interdection entre deux histogrammes
    public double compare(ColorHistogram hist) {
        double[] hist1 = this.getHistogram();
        double[] hist2 = hist.getHistogram();


        double intersection = 0.0;
        for (int i = 0; i < hist1.length; i++) {
            intersection += Math.min(hist1[i], hist2[i]);
        }
        return intersection;
    }
    // la methode qui sauvegarde les histogrammes dans un fichier texte 
    public void save(String filename) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        writer.write(String.valueOf(depth));
        writer.newLine();
        for (double value : histogram) {
            writer.write(String.valueOf(value) + " ");
        }
        writer.close();
    }
    

}
