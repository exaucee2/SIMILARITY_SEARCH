#lang racket

(define (read-hist-file filename)
  (let ([corrected-filename (string-replace filename "\\" "/")])
    (with-input-from-file corrected-filename
      (lambda ()
        (let read-loop ()
          (let ((x (read)))
            (if (eof-object? x)
                '()
                (cons x (read-loop)))))))))

(define (histogram-intersection h1 h2)
  (let loop ((h1 h1) (h2 h2) (sum 0))
    (if (or (null? h1) (null? h2))
        sum
        (loop (cdr h1) (cdr h2) (+ sum (min (car h1) (car h2)))))))
(define (similaritySearch queryHistogramFilename imageDatasetDirectory)
  (let ((query-hist (read-hist-file queryHistogramFilename))
        (image-files (list-text-files-in-directory imageDatasetDirectory)))
    (let ((similarities
           (map (lambda (image-file)
                  (let ((image-hist (read-hist-file image-file)))
                    (cons image-file (histogram-intersection query-hist image-hist))))
                image-files)))
      (map car (take (sort similarities (lambda (a b) (> (cdr a) (cdr b)))) 5)))))
(define (list-text-files-in-directory directory-path)
  (filter (lambda (file) (string-suffix? file ".txt"))
          (map path->string (directory-list directory-path))))
