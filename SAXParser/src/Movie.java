import java.util.ArrayList;

public class Movie {

	private String title;

	private String director;
	
	private int year;
	
	private String id;
	
	private ArrayList<String> genresList;
	
	public Movie(){
		this.genresList = new ArrayList<String>();
	}
	
	public Movie(String title, String director, int year, String id) {
		this.title = title;
		this.director = director;
		this.year = year;
		this.id = id;
		this.genresList = new ArrayList<String>();
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}


	public String getDirector() {
		return director;
	}

	public void setDirector(String director) {
		this.director = director;
	}	
	
	public int getYear() {
		return year;
	}
	
	public void setYear(int year) {
		this.year = year;
	}
	
	public ArrayList<String> getGenres() {
		return genresList;
	}
	
	public void setGenres(String genre) {
		this.genresList.add(genre);
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Movie Details - ");
		sb.append("Id:" + getId());
		sb.append(", ");
		sb.append("Title:" + getTitle());
		sb.append(", ");
		sb.append("Director:" + getDirector());
		sb.append(", ");
		sb.append("Year:" + getYear());
		sb.append(", ");
		sb.append("Genres:" + getGenres());
		sb.append(".");
		
		return sb.toString();
	}
	
}
