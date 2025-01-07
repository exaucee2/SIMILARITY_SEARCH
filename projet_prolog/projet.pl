% Define dataset directory
dataset('C:/Users/HP/OneDrive - University of Ottawa/Applications/Hiver2024/CSI2520/projet_prolog/imageDataset2_15_20').

% directory_textfiles(DirectoryName, ListOfTextfiles)
% Produit la liste des fichiers texte dans un répertoire
directory_textfiles(D, Textfiles) :-
    directory_files(D, Files),
    include(isTextFile, Files, Textfiles).

isTextFile(Filename) :-
    file_name_extension(_, 'txt', Filename).

% read_hist_file(Filename, ListOfNumbers)
read_hist_file(Filename, Numbers) :-
    open(Filename, read, Stream),
    read_line_to_string(Stream, _), % Ignore the first line if necessary
    read_line_to_string(Stream, String),
    close(Stream),
    atomic_list_concat(List, ' ', String),
    atoms_numbers(List, Numbers).

% similarity_search(QueryFile, SimilarList)
similarity_search(QueryFile, SimilarList) :-
    dataset(D),
    atomic_list_concat([D, '/', QueryFile], QueryPath),
    read_hist_file(QueryPath, QueryHisto),
    directory_textfiles(D, DatasetFiles),
    compare_histograms(QueryHisto, D, DatasetFiles, Scores),
    sort(2, @>, Scores, Sorted),
    take(Sorted, 5, SimilarList).

% compare_histograms(QueryHisto, DatasetDirectory, DatasetFiles, Scores)
compare_histograms(_, _, [], []).
compare_histograms(QueryHisto, DatasetDirectory, [File | Files], [(File, Score) | Rest]) :-
    atomic_list_concat([DatasetDirectory, '/', File], FilePath),
    read_hist_file(FilePath, Histogram),
    histogram_intersection(QueryHisto, Histogram, Score),
    compare_histograms(QueryHisto, DatasetDirectory, Files, Rest).

% histogram_intersection(Histogram1, Histogram2, Score)
histogram_intersection([], [], 1.0).
histogram_intersection([H1 | T1], [H2 | T2], Score) :-
    histogram_intersection(T1, T2, RestScore),
    Min is min(H1, H2),
    Score is Min + RestScore.

% take(List, K, KList)
take(_, 0, []).
take([X | Xs], N, [X | Ys]) :-
    N > 0,
    M is N - 1,
    take(Xs, M, Ys).

% atoms_numbers(ListOfAtoms, ListOfNumbers)
atoms_numbers([], []).
atoms_numbers([X | L], [Y | T]) :-
    atom_number(X, Y),
    atoms_numbers(L, T).
atoms_numbers([X | L], T) :-
    \+ atom_number(X, _),
    atoms_numbers(L, T).
