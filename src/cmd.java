import java.io.IOException;
import java.util.Scanner;

public class cmd {

	public static String model = "color";
	public static String pathOfDataset = "/tmp/";
	public static String seedSetSize = "10";
	public static String monteCarloSims = "1";

	public static void main(String[] args) throws IOException {
		Scanner stdin = new Scanner(System.in);
		System.out.println("Enter the absolute path of the dataset. : ");
		pathOfDataset = stdin.nextLine();
		System.out.println("Enter a Model from the following [color, classic, ratings, tattle, tim] : ");
		model = stdin.nextLine();
		System.out.println("Enter a Seet Set Size : ");
		seedSetSize = stdin.nextLine();
		System.out.println("Enter Number of Monte-Carlo Simulations to be used : ");
		monteCarloSims = stdin.nextLine();
		String[] params = { pathOfDataset, model, seedSetSize, monteCarloSims };
		Main.main(params);
		stdin.close();

	}

}
