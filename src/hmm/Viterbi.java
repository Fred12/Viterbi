package hmm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.Locale;
import java.util.Scanner;

public class Viterbi {
	private Scanner scanner = new Scanner(System.in); 
	private double q0Fair;  //WSK, im Anfangszustand den fairen Wuerfel zu waehlen
	private double q0Unfair;  //WSK, im Anfangszustand den unfairen Wuerfel zu waehlen
	
	private double FF;   //WSK des Wechsels von fairem zu fairem Wuerfel
	private double FU;	 //WSK des Wechsels von fairem zu unfairem Wuerfel
	private double UU;	 //WSK des Wechsels von unfairem zu unfairem Wuerfel
	private double UF;	 //WSK des Wechsels von unfairem zu fairem Wuerfel
	
	private double WSKU; //berechnete ÜbergangsWSK für unfair
	private double WSKF; //berechnete ÜbergangsWSK für fair
	
	private ArrayList<Double> wuerfelEmissionFair = new ArrayList();
	private ArrayList<Double> wuerfelEmissionUnfair = new ArrayList();
	private Deque<Double> fairWSKStack = new ArrayDeque<Double>();
	private Deque<Double> unfairWSKStack = new ArrayDeque<Double>();
	private ArrayList<Integer> zahlenfolge = new ArrayList();

	private double e;
	private String zeile = "";
	private File source;
	private int zahlenwert;
	private int i = 0;
	private boolean userInput = false;
	private ArrayList<String> dicevalues; 
	private ArrayList<Double> dicevalues2 = new ArrayList<Double>();
	private int counter = 0;
	private boolean abort = false;	

	
	
	//*********************************************************************************************************
	//*********************************************************************************************************
	
	 
	public static void main(String[] args) throws IOException, ParseException {		
		new Viterbi();
	}
	
	
	public Viterbi() throws IOException, ParseException {		
		begruessen();
		readData();
		chooseAlgorithm();
	
	}
	
	public void begruessen() throws IOException, ParseException {
		boolean weiter = true;
		System.out.println("***Willkommen zu unserem Hidden Markov Model (Fair/Unfair)-Würfel Szenario Programm!***\n");
		System.out.println("Sollen die Eingaben zu Zustands-, Übergangs-, und -Emissionswahrscheinlichkeit von Hand (1) oder \n");
		System.out.println("von einer Textdatei (2) kommen? Bitte geben Sie 1, 2 oder 3 ein:");
				
		while(weiter) {
		System.out.println("(1) Direkteingabe (von Hand)");
		System.out.println("(2) Textdatei");
		System.out.println("(3) abbrechen");
			
		int eingabe = scanner.nextInt();
			
			
		if (eingabe == 1) {
			userInputHand();
			weiter = false;
		} 
		else if (eingabe == 2) {
			userInputText();
			weiter=false;
		}
		else if (eingabe == 3) {
			weiter = false;
			System.out.println("Auf Wiedersehen!");
			abort = true;
			return;
		}
		else {			
			System.out.println("Falsche Eingabe, bitte erneut eingeben");
		}
		}
		
	}
	
	public void userInputHand() {			
		System.out.println("Bitte geben Sie die Übergangswahrscheinlichkeit (reelle Zahl, bzw. Double-Wert) an aus dem Anfangszustands den fairen Wuerfel auszuwählen: ");
		q0Fair = scanner.nextDouble();
		System.out.println("Analog dazu bitte die Wahrscheinlichkeit angeben, aus dem Anfangszustand den Unfairen Würfel zu wählen: ");
		q0Unfair = scanner.nextDouble();
		System.out.println("Geben Sie nun die WSK an, vom fairen Wuerfel beim fairen Wuerfel zu bleiben: ");
		FF = scanner.nextDouble();		
		System.out.println("Geben Sie nun die WSK an, vom fairen Wuerfel zum unfairen Wuerfel zu wechseln: ");
		FU = scanner.nextDouble();
		System.out.println("Geben Sie nun die WSK an, vom unfairen Wuerfel beim unfairen Wuerfel zu bleiben");
		UU = scanner.nextDouble();
		System.out.println("Geben Sie nun die WSK an, vom unfairen Wuerfel zum fairen Wuerfel zu wechseln: ");
		UF = scanner.nextDouble();
		///////////////////////////////////////////////////////////
		System.out.println("*************************************************************\n");
		System.out.println("Geben Sie nun nacheinander die Emissionswahrscheinlichkeiten für die Zahlen 1-6 des FAIREN Wuerfels an: ");
		for (int i = 1; i <= 6; i++) {
		System.out.println("Zahl " +i+ "= ");		
		e = scanner.nextDouble();		
		wuerfelEmissionFair.add(e);
		}
		///////////////////////////////////////////////////////////
		System.out.println("Geben Sie nun nacheinander die Emissionswahrscheinlichkeiten für die Zahlen 1-6 des UNFAIREN Wuerfels an: ");
		for (int i = 1; i <= 6; i++) {
		System.out.println("Zahl " +i+ "= ");
		e = scanner.nextDouble();
		wuerfelEmissionUnfair.add(e);		
		}
		userInput = true;
	}
	
	public void userInputText() throws IOException, ParseException {
		System.out.println("Bitte geben Sie ihre Konfigurationsdatei bezueglich der Anfangszustaende, der Uebergangswahrscheinlichkeiten");
		System.out.println("und der Emissionswahrscheinlichkeiten fuer den fairen und Unfairen Wuerfel an!\n");		
		System.out.println("Es gibt es nur 4 Zeilen, auf der ersten Zeile stehen die Anfangszustände, auf der zweiten die Übergangswahrscheinlichkeiten");
		System.out.println("auf der dritten Zeile stehen die Emissionswahrscheinlichkeiten der Zahlen 1-6 des fairen Wuerfels");
		System.out.println("und auf der vierten Zeile die EmissionsWSK des unfairen Wuerfels");
		System.out.println("Dabei stehen in jeder Zeile nur die reellen Zahlenwerte als Kommazahlen (bzw. Double-Werte)");
		System.out.println("1. Zeile : WERT (Anfangszustand q0 zu fair) <LEERZEICHEN> WERT (Anfangszustand q0 zu unfair)");
		System.out.println("2. Zeile : WERT (Fair nach Fair [FF]) <Leerzeichen> WERT (Fair-unfair [FU] <LZ> [UU] <LZ> [UF]");
		System.out.println("3. Zeile (EmissionsWSK fairer Wuerfel : WERT (Zahl1) <LZ> WERT( Zahl2) <LZ> ... usw");
		System.out.println("4. Zeile analog zu 3. Zeile für den unfairen Wuerfel");
		System.out.println("");
		System.out.println("Bitte geben Sie nun den Dateipfad der Konfigurationsdatei an:");
		
		String dateiname = scanner.next();
		source = new File(dateiname);
		FileReader fr = new FileReader(source);		
		BufferedReader br = new BufferedReader(fr);
		NumberFormat format = NumberFormat.getInstance(Locale.GERMAN);
		
		while( (zeile = br.readLine()) != null ) {			
			dicevalues = new ArrayList<String>(Arrays.asList(zeile.split("\\s+")));
			for (String element : dicevalues) {				  
				Number number = format.parse(element);
				double d = number.doubleValue();
				dicevalues2.add(d);
			}
			
		}
		for (Double element : dicevalues2) {
			System.out.println(element);
		}
		
		q0Fair = dicevalues2.get(0);
		q0Unfair = dicevalues2.get(1);
		FF = dicevalues2.get(2);		
		FU = dicevalues2.get(3);		
		UU = dicevalues2.get(4);
		UF = dicevalues2.get(5);
		for (int t = 6 ; t <=11; t++) {
			wuerfelEmissionFair.add(dicevalues2.get(t));			
		}
		for (int t = 12 ; t <=17; t++) {
			wuerfelEmissionUnfair.add(dicevalues2.get(t));			
		}
		/*
		for (double element : wuerfelEmissionFair) {
			System.out.println(element);
		}
		for (double element : wuerfelEmissionUnfair) {
			System.out.println(element);
		}
		*/
		userInput =true;
	}
	
	
	public void readData() throws IOException {
		if (abort == true) {return;}
		if (userInput ==false) {
			System.out.println("Es wurden keine User-Eingaben bezüglich der Würfelwahrscheinlichkeiten getaetigt!");
			return;
		}
	System.out.println("Nun muessen Sie die Datei zu dem Markov-String angeben!");
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
	            zahlenwert = Character.getNumericValue(c);
	            zahlenfolge.add(zahlenwert);	            
	        }	
	    }
 
	}
	
	}
	
	public void chooseAlgorithm() {
		System.out.println("Welcher Algorithmus soll durchgeführt werden?");
		System.out.println("(1) : Viterbi");
		System.out.println("(2) : Vorwaerts");
		System.out.println("(sonst) : abbrechen");
		
		int input = scanner.nextInt();
		
		if (input == 1) {
			doViterbi();
		}
		else if (input == 2) {
			
		}
		else {
			return;
		}
	}
	


	public void doViterbi() {
		WSKF = Math.log(wuerfelEmissionFair.get(zahlenfolge.get(0)) * q0Fair);
		fairWSKStack.addFirst(WSKF);
		
		WSKU = Math.log(wuerfelEmissionUnfair.get(zahlenfolge.get(0)) * q0Unfair);
		unfairWSKStack.addFirst(WSKU);
		
		for (int zahl : zahlenfolge) {
			doFair(zahl);
			doUnfair(zahl);
			compareStacks();
		}
				
	}


	//Bei dieser Methode gehen wir von einem Wurf mit einem fairen Wuerfel aus  
	private void doFair(int zahl) {
		/*
		if (fairWSKStack.isEmpty()) {
			WSKF =(wuerfelEmissionFair.get((zahl-1)) * q0Fair) ;
			fairWSKStack.push(WSKF);
		}
		
		else {	
		*/
			//Fälle betrachten: Emissionszahl des fairen Wuerfels * maximum von vorangegangener WSK Fair zu nochmal Fair
			//und vorangegangener WSK Unfair zu diesmal Fair
			WSKF = Math.log(wuerfelEmissionFair.get((zahl-1))) + Math.max(fairWSKStack.peekFirst() + Math.log(FF), unfairWSKStack.peekFirst() + Math.log(UF));
			//System.out.println(WSKF);
			fairWSKStack.addFirst(WSKF);
		//}
		
	}

	//Bei dieser Methode gehen wir von einem Wurf mit einem unfairen Wuerfel aus 
	private void doUnfair(int zahl) {	
		/*
		if (unfairWSKStack.isEmpty()) {
			WSKU = q0Unfair * wuerfelEmissionUnfair.get((zahl-1));
			unfairWSKStack.push(WSKU);
		}
		
		else {
		*/	
			//Fälle betrachten: Emissionszahl des unfairen Wuerfels * maximum von vorangegangener WSK Unfair zu nochmal Unfair
			//und vorangegangener WSK Fair zu diesmal unfair
			WSKU = Math.log(wuerfelEmissionUnfair.get((zahl-1))) + Math.max(unfairWSKStack.peekFirst() + Math.log(UU), fairWSKStack.peekFirst() + Math.log(FU));
			//System.out.println(WSKF);			
			unfairWSKStack.addFirst(WSKU);
		//}
		
	}
	
	private void compareStacks() {
	
	if (fairWSKStack.peekFirst() > unfairWSKStack.peekFirst())
		System.out.print("F");
	else
		System.out.print("U");		
	
	}
	
	private void doVorwaerts() {
		
		
	}

}
