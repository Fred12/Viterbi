package hmm;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class ReadData {
	Scanner scanner = new Scanner(System.in); 
	String zeile = "";
	  
	public ReadData() throws IOException {
		System.out.println("Bitte den Namen der Datei (falls im selben Programmverzeichnis), oder den absoluten Pfad zum Dateinamen eingeben  : ");
		String dateiname = scanner.next();
		
		FileReader fr = new FileReader(dateiname);		
		BufferedReader br = new BufferedReader(fr);
		
		
		while( (zeile = br.readLine()) != null ) {
		if (zeile.equals("")) {			  
			  continue;
		  }	
		}
	}
}
