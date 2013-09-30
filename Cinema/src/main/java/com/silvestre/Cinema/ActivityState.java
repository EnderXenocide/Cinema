package com.silvestre.Cinema;

public class ActivityState {
		public int countSceances;
		public int countVisiblesSceances;
		public FilmsSelection selection;
		public long idFilmSelected;
		public ActivityState() {
			selection = new FilmsSelection();
			countSceances = 0;
			countVisiblesSceances = 0;
			idFilmSelected = CinemaProvider.NO_ID;
		}
}
