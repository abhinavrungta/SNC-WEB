public class MovieRating {
	private long movieId;
	private double ratingValue;
	private long ratingTime;

	public MovieRating(long movieId, double ratingValue, long ratingTime) {
		this.movieId = movieId;
		this.ratingValue = ratingValue;
		this.ratingTime = ratingTime;
	}

	public long getMovieId() {
		return this.movieId;
	}

	public double getRatingValue() {
		return this.ratingValue;
	}

	public long getRatingTime() {
		return this.ratingTime;
	}
}