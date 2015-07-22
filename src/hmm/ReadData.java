package hmm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class ReadData {
	Scanner scanner = new Scanner(System.in); 
	String zeile = "";
	File source;
	int zahl;
	int i = 0;
	
	
	  
	public ReadData() throws IOException {
		System.out.println("Bitte den Namen der Datei (falls im selben Programmverzeichnis), " + "\n" + "oder den absoluten Pfad zum Dateinamen eingeben (Pfad darf keine Leerzeichen enthalten) : ");
		String dateiname = scanner.next();
		
		source = new File(dateiname);
		FileReader fr = new FileReader(source);		
		BufferedReader br = new BufferedReader(fr);
		
		
		while( (zeile = br.readLine()) != null ) {
				
			if (zeile.equals("")) {			  
				  continue;
			}	
			
			for (char c : zeile.toCharArray()) {
		        if (Character.isDigit(c)) {
		            zahl = Character.getNumericValue(c);			            
		            
		        }	
		    }
	 
		}
	}
}
