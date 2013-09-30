package com.silvestre.Cinema;

public class FilmsSelection {
	public long whichYear;
	public boolean orderByDate;  //TODO orderbydate, orderasc à definir
	public boolean orderAsc;
	public boolean searchMode;	// bascule entre le when et le searchLikeFilm
	public String searchLikeFilm; // on garde en mémire le resultat de la recherche
	public FilmsSelection() {
		whichYear = CinemaProvider.year.WHENEVER;
		orderByDate = true;
		orderAsc = false; // les plus récents en premier
		searchMode = false;
		searchLikeFilm = null;
	}
}
