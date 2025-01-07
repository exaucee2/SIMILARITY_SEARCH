/* Auteur: Exaucee MBUYI
 * NUMERO ETUDIANT:300268093
 */


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;

public class ColorImage {
    private int[][][] pixels; 
    private int width;
    private int height;
    private int depth; 
   //un constructeur qui cree une image a partir d'un fichier texte 
    public ColorImage(String filename) throws IOException {
        try {
            File file = new File(filename);

            BufferedImage image = ImageIO.read(file);

            if (image != null) {
                width = image.getWidth();
                height = image.getHeight();

                depth = 24;
                pixels = new int[width][height][3];
                for (int i = 0; i < width; i++) {
                    for (int j = 0; j < height; j++) {
                        int rgb = image.getRGB(i, j);
                        pixels[i][j][0] = (rgb >> 16) & 0xFF; 
                        pixels[i][j][1] = (rgb >> 8) & 0xFF; 
                        pixels[i][j][2] = rgb & 0xFF; 
                    }
                }
            } else {
                System.err.println("Failed to load the image.");
                }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getDepth() {
        return depth;
    }
// methode qui retourne les pixels d'une image en fonction de leur position (i,j)
    public int[] getPixel(int i, int j) {
        return pixels[i][j];
    }
// methode qui reduit les espaces de couleur 
    public void reduceColor(int d) {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int r = pixels[i][j][0] >> (8 - d);
                int g = pixels[i][j][1] >> (8 - d);
                int b = pixels[i][j][2] >> (8 - d);
                pixels[i][j] = new int[] { r, g, b };
            }
        }
    }
}
