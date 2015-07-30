/*
 * Die Klasse Viterbi_Vorw.java implementiert den Viterbi-, und den Vorwärts-Algorithmus, 
 * und fragt zu Beginn dynamisch Übergangs-, Emissions-, und Zustandswahrscheinlichkeiten 
 * für die Würfelereignisse ab.
 * 
 * 
 * 
 * @author Marc Ludovici
 * @course Bioinformatik
 * @date   30.07.2015
 * @version 0.2
 */

package hmm;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class Viterbi_Vorw {	
	private Scanner scanner;
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

	private double WSKU; //berechnete ÜbergangsWSK für unfair
	private double WSKUprevious;
	private double WSKF; //berechnete ÜbergangsWSK für fair
	private double WSKFprevious;


	private Double[] wuerfelEmissionFair = new Double[6];
	private Double[] wuerfelEmissionUnfair = new Double[6];
	private Double[] wLogFair = new Double[6]; 		//Array der Logarithmus-Werte
	private Double[] wLogUnfair = new Double[6]; 	
	private ArrayList<Double> fairWSKStack = new ArrayList<Double>(); 	//ArrayList für Viterbi-Algorithmus
	private ArrayList<Double> unfairWSKStack = new ArrayList<Double>();
	private ArrayList<Integer> zahlenfolge = new ArrayList<Integer>();  //Liste der eingelesenen Zahlenfolgen
	private ArrayList<Double> konfdatei = new ArrayList<Double>();		//eingelesene werte aus der Konfigurationsdatei

	private ArrayList<Double> fairVWTS = new ArrayList<Double>();  //ArrayList für die Vorwärts-Wahrscheinlichkeiten
	private ArrayList<Double> unfairVWTS = new ArrayList<Double>();

	private double e;  //Hilfsvariable für die User-Eingaben
	private String zeile = "";
	private File source;
	private int zahlenwert;  //Hilfsvariable für die aus der Datei geparsten integer-Zahlen
	private boolean userInput = false;
	//private ArrayList<String> dicevalues; 
	private boolean abort = false;		
	private boolean reverse = false;
	private boolean reverseVWTS = false;

	//*********************************************************************************************************
	//*********************************************************************************************************


	public static void main(String[] args) throws IOException, ParseException {		
		new Viterbi_Vorw();

	}	

	public Viterbi_Vorw() throws IOException, ParseException {		
		begruessen();
		readData();
		chooseAlgorithm();	
		scanner.close();		
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

			try {
				scanner = new Scanner(System.in);
				int eingabe = scanner.nextInt();

				if (eingabe==1) {
					userInputHand();
					weiter = false;
				} 
				else if (eingabe==2) {
					userInputText();
					weiter=false;
				}
				else  {
					weiter = false;
					System.out.println("Auf Wiedersehen!");
					abort = true;
					return;
				}			

			}		
			catch (Exception e) {System.out.println("Falsche Eingabe!");	
			}			
		}
	}

	public void userInputHand() {			
		System.out.println("Bitte geben Sie die Übergangswahrscheinlichkeit (reelle Kommazahl, bzw. Double-Wert) an aus dem Anfangszustands den fairen Wuerfel auszuwählen: ");
		q0Fair = scanner.nextDouble();
		q0Fairlog = Math.log(q0Fair);
		System.out.println("Analog dazu bitte die Wahrscheinlichkeit angeben, aus dem Anfangszustand den Unfairen Würfel zu wählen: ");
		q0Unfair = scanner.nextDouble();
		q0Unfairlog = Math.log(q0Unfair);
		System.out.println("Geben Sie nun die WSK an, vom fairen Wuerfel beim fairen Wuerfel zu bleiben: ");
		FF = scanner.nextDouble();	
		FFlog = Math.log(FF);
		System.out.println("Geben Sie nun die WSK an, vom fairen Wuerfel zum unfairen Wuerfel zu wechseln: ");
		FU = scanner.nextDouble();
		FUlog = Math.log(FU);
		System.out.println("Geben Sie nun die WSK an, vom unfairen Wuerfel beim unfairen Wuerfel zu bleiben");
		UU = scanner.nextDouble();
		UUlog = Math.log(UU);
		System.out.println("Geben Sie nun die WSK an, vom unfairen Wuerfel zum fairen Wuerfel zu wechseln: ");
		UF = scanner.nextDouble();
		UFlog = Math.log(UF);
		///////////////////////////////////////////////////////////
		System.out.println("*************************************************************\n");
		System.out.println("Geben Sie nun nacheinander die Emissionswahrscheinlichkeiten für die Zahlen 1-6 des FAIREN Wuerfels an: ");
		for (int i = 1; i <= 6; i++) {
			System.out.println("Zahl " +i+ "= ");		
			e = scanner.nextDouble();		
			wuerfelEmissionFair[i-1] = e;	
			wLogFair[i-1] = Math.log(e);
		}		

		///////////////////////////////////////////////////////////
		System.out.println("Geben Sie nun nacheinander die Emissionswahrscheinlichkeiten für die Zahlen 1-6 des UNFAIREN Wuerfels an: ");
		for (int i = 1; i <= 6; i++) {
			System.out.println("Zahl " +i+ "= ");
			e = scanner.nextDouble();
			wuerfelEmissionUnfair[i-1] = e;	
			wLogUnfair[i-1] = Math.log(e);
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
		System.out.println("Beispiel: ");
		System.out.println("0,5 0,5");
		System.out.println("0,95 0,05 0,95 0,05");
		System.out.println("0,1 0,1 0,1 0,1 0,1 0,5");
		System.out.println("0,1 0,1 0,1 0,1 0,1 0,5\n");

		System.out.println("Bitte geben Sie nun den Dateipfad der Konfigurationsdatei an:");

		String dateiname = scanner.next();
		source = new File(dateiname);

		Scanner in = new Scanner(source);
		double val;

		while(in.hasNextDouble()) {
			val = in.nextDouble();
			//System.out.println(val);
			konfdatei.add(val);		// Werte in die ArrayList "konfdatei" einfügen	
		}
		in.close();
		//NumberFormat format = NumberFormat.getInstance(Locale.GERMAN);

		/*
		while( (zeile = br.readLine()) != null ) {			
			dicevalues = new ArrayList<String>(Arrays.asList(zeile.split("\\s+")));
			for (String element : dicevalues) {				  
				Number number = format.parse(element);
				double d = number.doubleValue();
				konfdatei.add(d);
			}

		}		
		 */

		//eingelesenen Werte an die Variablen übergeben für die späteren Berechnungen
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
		for (int t = 6 ; t <=11; t++) {   //EmissionsWSK für den fairen Würfel eintragen
			wuerfelEmissionFair[t-6] = konfdatei.get(t);
			wLogFair[t-6] = Math.log(konfdatei.get(t));
		}
		//System.out.println(wuerfelEmissionFair.get(t-6));


		for (int t = 12 ; t <=17; t++) {   //EmissionsWSK für den unfairen Würfel eintragen
			wuerfelEmissionUnfair[t-12] = konfdatei.get(t);
			wLogUnfair[t-12] = Math.log(konfdatei.get(t));
			//System.out.println(wuerfelEmissionUnfair.get(t-12));
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
					zahlenfolge.add(zahlenwert);	            //den zahlenwert in die ArrayList "zahlenfolge" eintragen, um daraus die EmissionsWSK der Würfel zu holen
				}	
			} 
		}
		br.close();		
	}


	public void chooseAlgorithm() {
		if (abort ==true) {return;}

		System.out.println("Welcher Algorithmus soll durchgeführt werden?");
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

	public void doViterbi() {
		StringBuffer sbv = new StringBuffer();
		if (reverse == true) {
			Collections.reverse(zahlenfolge);
		}
		sbv.append("\n").append("Ausgabe:\n");		
		//Zuerst wird die WSK für den Übergang aus dem Initialzustand ausgerechnet 
		//System.out.println("Zahl: "+zahlenfolge.get(0));	
		
		
		WSKF = wLogFair[zahlenfolge.get(0) -1] + q0Fairlog;
		WSKFprevious = WSKF;
		//System.out.println(zahlenfolge.get(0));
		//System.out.println("Fair: " +WSKF);
		WSKU = wLogUnfair[zahlenfolge.get(0)-1] + q0Unfairlog;
		WSKUprevious = WSKU;
		//System.out.println("Unfair: " +WSKU);
		
		//fairWSKStack.add(WSKF);
		//unfairWSKStack.add(WSKU);
		
		if(WSKF > WSKU) {
			sbv.append("F");
		}
		else {
			sbv.append("U");
		}
		//compareStacks();  //Vergleiche nun die Wahrscheinlichkeit beider Würfelfälle unfair/fair und wähle das Maximum, dann gib F oder U aus

		//Nun wird hier für alle folgenden Zahlen der normale Viterbi zuerst für den fairen, dann für den unfairen Würfel ausgerechnet

		for (int zahl : zahlenfolge.subList(1, zahlenfolge.size())) {  //erstes Element (Initialzustand) übergehen, da schon ausgerechnet
			//if (zahl == zahlenfolge.get(0)) continue;
			//System.out.println("Zahl: "+ zahl);	
			//fairen Fall berechnen
			//WSKF = wLogFair[zahl-1] + Math.max(fairWSKStack.get(fairWSKStack.size()-1) + FFlog, unfairWSKStack.get(unfairWSKStack.size()-1) + UFlog); 
			WSKU = wLogUnfair[zahl-1] + Math.max(WSKUprevious + UUlog, WSKFprevious + FUlog);
			WSKF = wLogFair[zahl-1] + Math.max(WSKFprevious + FFlog, WSKUprevious + UFlog);
			//unfairen Fall berechnen
			//WSKU = wLogUnfair[(zahl-1)] + Math.max(unfairWSKStack.get(unfairWSKStack.size()-1) + UUlog, fairWSKStack.get(fairWSKStack.size()-2) + FUlog); 
			
						
			//fairWSKStack.add(WSKF);
			//unfairWSKStack.add(WSKU);
			
			if(WSKF > WSKU) {
				sbv.append("F");
			}
			else {
				sbv.append("U");
			}
			
			WSKFprevious = WSKF;
			WSKUprevious = WSKU;
			/*
			viterbiFair(zahl);
			viterbiUnfair(zahl);			
			compareStacks();
			*/
		}
		System.out.println(sbv.toString());

	}
	
	/*
	//Bei dieser Methode gehen wir von einem Wurf mit einem fairen Wuerfel aus  um den Viterbi zu berechnen
	public void viterbiFair(int zahl) {
		/*
		if (fairWSKStack.isEmpty()) {
			WSKF =(wuerfelEmissionFair.get((zahl-1)) * q0Fair) ;
			fairWSKStack.push(WSKF);
		}

		else {	
		 */
		//Fälle betrachten: Emissionszahl des fairen Wuerfels * maximum von vorangegangener WSK Fair zu nochmal Fair
		//und vorangegangener WSK Unfair zu diesmal Fair
		/*System.out.println("Emission: "+wuerfelEmissionFair.get((zahl-1)));
			System.out.println("FF: " +fairWSKStack.get(fairWSKStack.size()-1) + Math.log(FF));
			System.out.println("UF: " +unfairWSKStack.get(unfairWSKStack.size()-1) + Math.log(UF));
		 */
	/*
		WSKF = wLogFair[zahl-1] + Math.max(fairWSKStack.get(fairWSKStack.size()-1) + FFlog, unfairWSKStack.get(unfairWSKStack.size()-1) + UFlog);
		//System.out.println("WSKF: "+WSKF);
		fairWSKStack.add(WSKF);
		//}

	}

	//Bei dieser Methode gehen wir von einem Wurf mit einem fairen Wuerfel aus  um den Viterbi zu berechnen
	public void viterbiUnfair(int zahl) {	
		/*
		if (unfairWSKStack.isEmpty()) {
			WSKU = q0Unfair * wuerfelEmissionUnfair.get((zahl-1));
			unfairWSKStack.push(WSKU);
		}

		else {
		 */	
	
		//Fälle betrachten: Emissionszahl des unfairen Wuerfels * maximum er vorangegangenen WSK Unfair zu Unfair
		//und vorangegangener WSK Fair zu unfair. 
		//Dabei jedoch beachten, dass aus dem fairWSKStack das vorletzte Element genommen wird für die Berechnung,
		//da doFair() zuerst ausgerechnet wird, und daher der "neue" Wert schon eingetragen wird, man jedoch noch den
		//Wert davor benötigt.
		/*
		System.out.println("Zahl: "+zahl);
		System.out.println("EmissionUnfairZahl: "+ wuerfelEmissionUnfair.get((zahl-1)));
		System.out.println("Emission: "+wuerfelEmissionUnfair.get((zahl-1)));
		System.out.println("UU: " +unfairWSKStack.get(unfairWSKStack.size()-1) + Math.log(UU));
		System.out.println("FU: " +fairWSKStack.get(fairWSKStack.size()-2) + Math.log(FU));
		 */
/*
		WSKU = wLogUnfair[(zahl-1)] + Math.max(unfairWSKStack.get(unfairWSKStack.size()-1) + UUlog, fairWSKStack.get(fairWSKStack.size()-2) + FUlog);
		//System.out.println("WSKU: "+WSKU);			
		unfairWSKStack.add(WSKU);
		//}

	}
	
	//Vergleiche die Viterbi WSK aus den vorherigen Berechnungen doFair() bzw. doUnfair() und wähle die Max. WSK der beiden aus,
	//je nachdem ob dies der faire bzw. unfaire Würfel war, gib 'F' oder 'U' aus.
	public void compareStacks() {		
		if (fairWSKStack.get(fairWSKStack.size()-1) > unfairWSKStack.get(unfairWSKStack.size()-1)){
			System.out.print("F");
		}
		else
			System.out.print("U");		
	}
*/

	//*************************************************************************************************
	//*************************************************************************************************
	//Hier wird die Vorwärts-Wahrscheinlichkeit berechnet.
	public void doVorwaertsWSK() {
		if (reverseVWTS == true) {
			Collections.reverse(zahlenfolge);
		}
		StringBuffer sb = new StringBuffer();
		sb.append("\n").append("Ausgabe:\n");		

		WSKF = wuerfelEmissionFair[zahlenfolge.get(0)-1] * q0Fair;
		WSKFprevious = WSKF;		
		//System.out.println(WSKF);
		WSKU = wuerfelEmissionUnfair[zahlenfolge.get(0)-1] * q0Unfair;
		WSKUprevious = WSKU;
		
		//fairVWTS.add(WSKF);
		//unfairVWTS.add(WSKU);
		//System.out.println(WSKU);
		if (WSKF > WSKU) {
			sb.append("F");
		}
		else {
			sb.append("U");
		}
		
		/*
		double wert = (1/10.0) * ((1/20.0) * (19.0/20.0) + (1/12.0)*(1/20.0));
		double wert2 = (1/6.0) * (((1/12.0) * (19.0/20.0)) + ((1/20.0)*(1/20.0)));
		System.out.println("Wert unfair: " +wert);
		System.out.println("Wert fair: " +wert2);
		 */
		for (int zahl : zahlenfolge.subList(1, zahlenfolge.size())) {  // Erstes Element übergehen, da Initialwert schon errechnet
			//WSKF = wuerfelEmissionFair[zahl-1] * ((fairVWTS.get(fairVWTS.size()-1) * FF) + (unfairVWTS.get(unfairVWTS.size()-1) * UF))*5; //5 = Multiplikationsfaktor um die WSK nicht zu klein zu machen
			//WSKU = wuerfelEmissionUnfair[zahl-1] * ((unfairVWTS.get(unfairVWTS.size()-1) * UU) + (fairVWTS.get(fairVWTS.size()-2)* FU))*5;
			WSKF = (wuerfelEmissionFair[zahl-1] * ((WSKFprevious * FF) + (WSKUprevious * UF))) *5; //5 = Multiplikationsfaktor um die WSK nicht zu klein zu machen
			WSKU = (wuerfelEmissionUnfair[zahl-1] * ((WSKUprevious * UU) + (WSKFprevious * FU))) *5;
			//fairVWTS.add(WSKF);
			//unfairVWTS.add(WSKU);
			
			if (WSKF > WSKU) {
				sb.append("F");
			}
			else {
				sb.append("U");
			}
			WSKFprevious = WSKF;
			WSKUprevious = WSKU;
			/*fairVWTS(zahl);
			unfairVWTS(zahl);
			compareStacksVWTS();*/
		}
		System.out.println(sb.toString());
	}

	/*
	public void fairVWTS(int zahl){
		WSKF = wuerfelEmissionFair[zahl-1] * ((fairVWTS.get(fairVWTS.size()-1) * FF) + (unfairVWTS.get(unfairVWTS.size()-1) * UF))*5; //5 = Multiplikationsfaktor um die WSK nicht zu klein zu machen
		//System.out.println("WSKFair: "+WSKF);
		fairVWTS.add(WSKF);
	}

	public void unfairVWTS(int zahl) { 
		/*
		System.out.println("Emission: " +wuerfelEmissionUnfair.get(zahl-1));
		System.out.println("unfairVWTS: " +unfairVWTS.get(unfairVWTS.size()-1));
		System.out.println("UU: " +UU);
		System.out.println("fairVWTS: " +fairVWTS.get(fairVWTS.size()-2));
		System.out.println("FU: " +FU);
		 */
	/*
		WSKU = wuerfelEmissionUnfair[zahl-1] * ((unfairVWTS.get(unfairVWTS.size()-1) * UU) + (fairVWTS.get(fairVWTS.size()-2)* FU))*5;
		//System.out.println("WSKUnfair: " +WSKU);			
		unfairVWTS.add(WSKU);
	}

	public void compareStacksVWTS() {
		if (fairVWTS.get(fairVWTS.size()-1) > unfairVWTS.get(unfairVWTS.size()-1)) {
			System.out.print("F");
		}
		else
			System.out.print("U");			
	}
	*/
}
