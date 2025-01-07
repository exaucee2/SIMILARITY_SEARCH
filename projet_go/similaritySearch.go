package main

import (
	"encoding/csv"
	"fmt"
	"image"
	"io/ioutil"
	"os"
	"path/filepath"
	"sort"
	"sync"
	"time"

	_ "image/jpeg"
)

type Histo struct {
	Name       string
	H          []float64
	Similarity float64
}

func computeHistogram(imagePath string, depth int) (Histo, error) {
	file, err := os.Open(imagePath)
	if err != nil {
		return Histo{}, err
	}
	defer file.Close()

	img, _, err := image.Decode(file)
	if err != nil {
		return Histo{}, err
	}
	histogram := Histo{Name: filepath.Base(imagePath), H: make([]float64, 1<<(3*depth))}

	bounds := img.Bounds()
	width, height := bounds.Max.X, bounds.Max.Y

	// Calcul de l'histogramme
	for y := 0; y < height; y++ {
		for x := 0; x < width; x++ {
			r, g, b, _ := img.At(x, y).RGBA()
			r >>= 8
			g >>= 8
			b >>= 8

			r >>= uint(8 - depth)
			g >>= uint(8 - depth)
			b >>= uint(8 - depth)

			index := (r << (2 * depth)) + (g << depth) + b
			histogram.H[index]++
		}
	}

	// Normalisation de l'histogramme
	totalPixels := float64(width * height)
	for i := range histogram.H {
		histogram.H[i] /= totalPixels
	}

	return histogram, nil
}

func computeHistograms(imagePaths []string, depth int, hChan chan<- Histo, wg *sync.WaitGroup) {
	defer wg.Done()
	for _, imagePath := range imagePaths {
		histo, err := computeHistogram(imagePath, depth)
		if err == nil {
			hChan <- histo
		}
	}
}

func main() {
	if len(os.Args) < 3 {
		fmt.Println("Usage: go run similaritySearch.go queryImageFilename imageDatasetDirectory")
		return
	}

	queryImagePath := os.Args[1]
	imageDir := os.Args[2]
	configs := []int{1, 2, 4, 16, 64, 256, 1048}

	for _, k := range configs {
		startTime := time.Now()

		imagePaths, err := getImagePaths(imageDir)
		if err != nil {
			fmt.Println("Error fetching image paths:", err)
			return
		}

		imageSlices := splitImagePaths(imagePaths, k)

		var wg sync.WaitGroup
		wg.Add(len(imageSlices))
		hChan := make(chan Histo)

		for _, slice := range imageSlices {
			go computeHistograms(slice, 3, hChan, &wg)
		}

		go func() {
			wg.Wait()
			close(hChan)
		}()

		queryHistogram, err := computeHistogram(queryImagePath, 3)
		if err != nil {
			fmt.Println("Error computing query image histogram:", err)
			return
		}

		var similarImages []Histo

		for histo := range hChan {
			similarity := compareHistograms(queryHistogram.H, histo.H)
			histo.Similarity = similarity
			similarImages = append(similarImages, histo)
		}

		sort.Slice(similarImages, func(i, j int) bool {
			return similarImages[i].Similarity > similarImages[j].Similarity
		})

		fmt.Println("Les 5 images les plus similaires à l'image de requête avec K =", k, "sont :")
		for i := 0; i < 5 && i < len(similarImages); i++ {
			fmt.Println(similarImages[i].Name)
		}

		endTime := time.Now()
		executionTime := endTime.Sub(startTime)
		fmt.Printf("Temps d'exécution pour K=%d : %v\n", k, executionTime)

		// Écrire les résultats dans un fichier CSV
		if err := writeResultsToCSV(queryImagePath, similarImages, k, executionTime); err != nil {
			fmt.Println("Error writing results to CSV:", err)
			return
		}
	}
}

func getImagePaths(imageDir string) ([]string, error) {
	var imagePaths []string
	files, err := ioutil.ReadDir(imageDir)
	if err != nil {
		return nil, err
	}
	for _, file := range files {
		if !file.IsDir() {
			imagePath := filepath.Join(imageDir, file.Name())
			imagePaths = append(imagePaths, imagePath)
		}
	}
	return imagePaths, nil
}

func splitImagePaths(imagePaths []string, K int) [][]string {
	var imageSlices [][]string
	numImages := len(imagePaths)
	sliceSize := (numImages + K - 1) / K
	for i := 0; i < numImages; i += sliceSize {
		end := i + sliceSize
		if end > numImages {
			end = numImages
		}
		imageSlices = append(imageSlices, imagePaths[i:end])
	}
	return imageSlices
}

func compareHistograms(hist1, hist2 []float64) float64 {
	sumProd := 0.0
	sumSq1 := 0.0
	sumSq2 := 0.0
	for i := 0; i < len(hist1); i++ {
		sumProd += hist1[i] * hist2[i]
		sumSq1 += hist1[i] * hist1[i]
		sumSq2 += hist2[i] * hist2[i]
	}
	
	return sumProd / (sumSq1 * sumSq2)
}

func writeResultsToCSV(queryImagePath string, similarImages []Histo, k int, executionTime time.Duration) error {
	file, err := os.Create(fmt.Sprintf("similar_images_K_%d.csv", k))
	if err != nil {
		return err
	}
	defer file.Close()

	writer := csv.NewWriter(file)
	defer writer.Flush()

	// Écrire l'en-tête CSV
	headers := []string{"Query Image", "Similar Image"}
	if err := writer.Write(headers); err != nil {
		return err
	}

	// Écrire les données des images similaires dans le fichier CSV
	queryImageName := filepath.Base(queryImagePath)
	for i := 0; i < 5 && i < len(similarImages); i++ {
		data := []string{queryImageName, similarImages[i].Name}
		if err := writer.Write(data); err != nil {
			return err
		}
	}

	// Ajouter une ligne pour le temps d'exécution
	if err := writer.Write([]string{"Execution Time", executionTime.String()}); err != nil {
		return err
	}

	return nil
}
