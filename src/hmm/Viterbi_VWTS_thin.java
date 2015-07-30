/*
 * Die Klasse Viterbi_VWTS_thin.java implementiert den Viterbi-, und den Vorw�rts-Algorithmus, 
 * und fragt zu Beginn dynamisch �bergangs-, Emissions-, und Zustandswahrscheinlichkeiten 
 * f�r die W�rfelereignisse ab.
 * zudem ist diese Version von unn�tigen Kommentaren und �berfl�ssigen Eingaben bereinigt worden,
 * und die Methode zum Eingeben der Konfiguration per Hand wurde weggelassen.
 * 
 *  * 
 * @author Marc Ludovici
 * @course Bioinformatik
 * @date   30.07.2015
 * @version 0.3
 */

package hmm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
	
	
	public class Viterbi_VWTS_thin {		
		private double q0Fair;  //WSK, im Anfangszustand den fairen Wuerfel zu waehlen
		private double q0Fairlog;  //log. Werte
		private double q0Unfair;  //WSK, im Anfangszustand den unfairen Wuerfel zu waehlen
		private double q0Unfairlog;

		private double FF;   //WSK des Wechsels von fairem zu fairem Wuerfel
		private double FFlog;	//log.Werte
		private double FU;	 //WSK des Wechsels von fairem zu unfairem Wuerfel
		private double FUlog;
		private double UU;	 //WSK des Wechsels von unfairem zu unfairem Wuerfel
		private double UUlog;
		private double UF;	 //WSK des Wechsels von unfairem zu fairem Wuerfel
		private double UFlog;

		private double WSKU; //berechnete �bergangsWSK f�r unfair
		private double WSKUprevious;
		private double WSKF; //berechnete �bergangsWSK f�r fair
		private double WSKFprevious;


		private Double[] wuerfelEmissionFair = new Double[6];
		private Double[] wuerfelEmissionUnfair = new Double[6];
		private Double[] wLogFair = new Double[6]; 		//Array der Logarithmus-Werte
		private Double[] wLogUnfair = new Double[6]; 	
		
		private ArrayList<Integer> zahlenfolge = new ArrayList<Integer>();  //Liste der eingelesenen Zahlenfolgen
		private ArrayList<Double> konfdatei = new ArrayList<Double>();		//eingelesene werte aus der Konfigurationsdatei
	
		private String zeile = "";
		private File source;
		private int zahlenwert;  //Hilfsvariable f�r die aus der Datei geparsten integer-Zahlen
		
		private boolean abort = false;		
		private boolean reverse = false;
		private boolean reverseVWTS = false;
		private Scanner scanner;
		private Scanner in;
		
		
	public static void main(String[] args) throws IOException, ParseException {
		new Viterbi_VWTS_thin();
	}
	
	public Viterbi_VWTS_thin() throws IOException, ParseException {
		begruessen();
		readData();
		chooseAlgorithm();
	}

	
	public void begruessen() throws IOException, ParseException {
		boolean weiter = true;	
		
		System.out.println("***Willkommen zu unserem Hidden Markov Model (Fair/Unfair)-W�rfel Szenario Programm!***\n");
		System.out.println("Sie muessen zuerst die Konfigurationsdatei bezueglich der Anfangszustaende, der Uebergangswahrscheinlichkeiten");
		System.out.println("und der Emissionswahrscheinlichkeiten fuer den fairen und Unfairen Wuerfel angeben!\n");		
		System.out.println("Es gibt es nur 4 Zeilen, auf der ersten Zeile stehen die Anfangszust�nde, auf der zweiten die �bergangswahrscheinlichkeiten");
		System.out.println("auf der dritten Zeile stehen die Emissionswahrscheinlichkeiten der Zahlen 1-6 des fairen Wuerfels");
		System.out.println("und auf der vierten Zeile die EmissionsWSK des unfairen Wuerfels");
		System.out.println("Dabei stehen in jeder Zeile nur die reellen Zahlenwerte als Kommazahlen (bzw. Double-Werte)");
		System.out.println("1. Zeile : WERT (Anfangszustand q0 zu fair) <LEERZEICHEN> WERT (Anfangszustand q0 zu unfair)");
		System.out.println("2. Zeile : WERT (Fair nach Fair [FF]) <Leerzeichen> WERT (Fair-unfair [FU] <LZ> [UU] <LZ> [UF]");
		System.out.println("3. Zeile (EmissionsWSK fairer Wuerfel : WERT (Zahl1) <LZ> WERT( Zahl2) <LZ> ... usw");
		System.out.println("4. Zeile analog zu 3. Zeile f�r den unfairen Wuerfel");
		System.out.println("");
		System.out.println("Beispiel: ");
		System.out.println("0,5 0,5");
		System.out.println("0,95 0,05 0,95 0,05");
		System.out.println("0,1 0,1 0,1 0,1 0,1 0,5");
		System.out.println("0,1 0,1 0,1 0,1 0,1 0,5\n");			
			
		while (weiter) {
				try {
				System.out.println("Bitte geben Sie nun den Dateipfad der Konfigurationsdatei an, oder druecke Zahl '0' zum Abbrechen:");	
				
				scanner = new Scanner(System.in);							
				String dateiname = scanner.next();
				
				if (dateiname.equals("0")) {
					weiter = false;
					abort = true;
					System.out.println("Auf Wiedersehen!");
					return;
				}					
				
				in = new Scanner(new File(dateiname));					
				weiter = false;				
				} 
				catch (Exception e) {				
				System.out.println("Falsche Eingabe oder Datei nicht gefunden! Dateipfad vorhanden?"); 					
				}
		}		
		
		while(in.hasNextDouble()) {					
			konfdatei.add(in.nextDouble());		// Werte in die ArrayList "konfdatei" einf�gen	
		}		
		in.close();
		
		//eingelesenen Werte an die Variablen �bergeben f�r die sp�teren Berechnungen
		q0Fair = konfdatei.get(0);
		q0Unfair = konfdatei.get(1);
		
		FF = konfdatei.get(2);	
		FFlog = Math.log(FF);
		FU = konfdatei.get(3);	
		FUlog = Math.log(FU);
		UU = konfdatei.get(4);
		UUlog = Math.log(UU);
		UF = konfdatei.get(5);
		UFlog = Math.log(UF);
		
		for (int t = 6 ; t <=11; t++) {   //EmissionsWSK f�r den fairen W�rfel eintragen
			wuerfelEmissionFair[t-6] = konfdatei.get(t);
			wLogFair[t-6] = Math.log(konfdatei.get(t));
		}

		for (int t = 12 ; t <=17; t++) {   //EmissionsWSK f�r den unfairen W�rfel eintragen
			wuerfelEmissionUnfair[t-12] = konfdatei.get(t);
			wLogUnfair[t-12] = Math.log(konfdatei.get(t));			
		}	
		
	}


	public void readData() throws IOException {
		boolean weiter = true;
		if (abort == true) return;	
		
		System.out.println("\nGut, nun muessen Sie die Datei zu dem Markov-String angeben!");
		
		while (weiter) {			
			System.out.println("Bitte den Namen der Datei (falls im selben Programmverzeichnis), " + "\n" + "oder den absoluten Pfad zum Dateinamen eingeben (Pfad darf keine Leerzeichen enthalten) : ");
			System.out.println("Oder die Zahl '0' druecken zum Abbrechen");
			String dateiname = scanner.next();
			
			if (dateiname.equals("0")) {
				weiter = false;			
				abort = true;
				System.out.println("Auf Wiedersehen!");
				return;
			}
			try {		
				source = new File(dateiname);
				FileReader fr = new FileReader(source);
				weiter = false;
				fr.close();
			}
			catch (Exception e) { System.out.println("Falsche Eingabe oder Datei nicht gefunden! Stimmt der Dateipfad?\n");
			}
		}
			
		FileReader fr = new FileReader(source);		
		BufferedReader br = new BufferedReader(fr);

		while( (zeile = br.readLine()) != null ) {
			if (zeile.equals("")) {			  
				continue;
			}	
			for (char c : zeile.toCharArray()) {
				if (Character.isDigit(c)) {
					zahlenwert = Character.getNumericValue(c);
					zahlenfolge.add(zahlenwert);	            //den zahlenwert in die ArrayList "zahlenfolge" eintragen, um daraus die EmissionsWSK der W�rfel zu holen
				}	
			} 
		}
		br.close();		
	}

	public void chooseAlgorithm() {
		if (abort ==true) return;

		System.out.println("Welcher Algorithmus soll durchgef�hrt werden?");
		System.out.println("(1) : Viterbi");
		System.out.println("(2) : Viterbi mit Zahlenfolge rueckwaerts");
		System.out.println("(3) : Vorwaerts-Algorithmus");
		System.out.println("(4) : Vorwaerts-Algorithmus mit Zahlenfolge rueckwaerts");
		System.out.println("(sonst) : abbrechen");

		try {
			int input = scanner.nextInt();

			if (input == 1) {
				doViterbi();
			}
			else if (input == 2) {
				reverse = true;
				doViterbi();
			}
			else if (input == 3) {
				doVorwaertsWSK();				
			}
			else if (input == 4) {
				reverseVWTS = true;
				doVorwaertsWSK();
			}
			else {		
				System.out.println("Auf Wiedersehen!");
				return;
			}	
		} 
		catch (Exception e) {System.out.println("Auf Wiedersehen!");		
		}
	}

	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	
	//Viterbi-Algorithmus
	public void doViterbi() {
		StringBuffer sbv = new StringBuffer();
		if (reverse == true) {
			Collections.reverse(zahlenfolge);
		}
		sbv.append("\n").append("Ausgabe:\n");
		
		//Zuerst wird die WSK f�r den �bergang aus dem Initialzustand ausgerechnet 		
		WSKF = wLogFair[zahlenfolge.get(0) -1] + q0Fairlog;
		WSKFprevious = WSKF;		
		WSKU = wLogUnfair[zahlenfolge.get(0)-1] + q0Unfairlog;
		WSKUprevious = WSKU;		
		
		if(WSKF > WSKU) {
			sbv.append("F");
		}
		else {
			sbv.append("U");
		}		

		//Nun wird hier f�r alle folgenden Zahlen der normale Viterbi zuerst f�r den fairen, dann f�r den unfairen W�rfel ausgerechnet
		for (int zahl : zahlenfolge.subList(1, zahlenfolge.size())) {  //erstes Element (Initialzustand) �bergehen, da schon ausgerechnet			 
			WSKU = wLogUnfair[zahl-1] + Math.max(WSKUprevious + UUlog, WSKFprevious + FUlog);
			WSKF = wLogFair[zahl-1] + Math.max(WSKFprevious + FFlog, WSKUprevious + UFlog);			
			
			if(WSKF > WSKU) {
				sbv.append("F");
			}
			else {
				sbv.append("U");
			}
			
			WSKFprevious = WSKF;
			WSKUprevious = WSKU;			
		}
		System.out.println(sbv.toString());

	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	//Hier wird die Vorw�rts-Wahrscheinlichkeit berechnet.
	public void doVorwaertsWSK() {
		StringBuffer sb = new StringBuffer();
		if (reverseVWTS == true) {
			Collections.reverse(zahlenfolge);
		}		
		sb.append("\n").append("Ausgabe:\n");		

		WSKF = wuerfelEmissionFair[zahlenfolge.get(0)-1] * q0Fair;
		WSKFprevious = WSKF;			
		WSKU = wuerfelEmissionUnfair[zahlenfolge.get(0)-1] * q0Unfair;
		WSKUprevious = WSKU;
		
		if (WSKF > WSKU) {
			sb.append("F");
		}
		else {
			sb.append("U");
		}
		
		for (int zahl : zahlenfolge.subList(1, zahlenfolge.size())) {  // Erstes Element �bergehen, da Initialwert schon errechnet			
			WSKF = (wuerfelEmissionFair[zahl-1] * ((WSKFprevious * FF) + (WSKUprevious * UF))) *5; //5 = Multiplikationsfaktor um die WSK nicht zu klein zu machen
			WSKU = (wuerfelEmissionUnfair[zahl-1] * ((WSKUprevious * UU) + (WSKFprevious * FU))) *5;
						
			if (WSKF > WSKU) {
				sb.append("F");
			}
			else {
				sb.append("U");
			}
			
			WSKFprevious = WSKF;
			WSKUprevious = WSKU;			
		}
		System.out.println(sb.toString());
	}
}