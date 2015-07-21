package hmm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Viterbi {
	Scanner scanner = new Scanner(System.in); 
	double q0F;
	double q0U;
	double FF;
	double FU;
	double UU;
	double UF;
	ArrayList<Double> arrayEmissionFair = new ArrayList();
	ArrayList<Double> arrayEmissionUnfair = new ArrayList();
	
	double e;

	
	public static void main(String[] args) throws IOException {
		//new ReadData();
		new Viterbi();
	}
	
	
	public Viterbi() {
		/*
		System.out.println("***Willkommen zu unserem Hidden Markov Model (Fair/Unfair)-Würfel Szenario!***\n");
		System.out.println("Bitte geben Sie die Übergangswahrscheinlichkeit (reelle Zahl, bzw. Double-Wert) an aus dem Anfangszustands den fairen Wuerfel auszuwählen: ");
		q0F = scanner.nextDouble();
		System.out.println("Analog dazu bitte die Wahrscheinlichkeit angeben, aus dem Anfangszustand den Unfairen Würfel zu wählen: ");
		q0U = scanner.nextDouble();
		System.out.println("Geben Sie nun die WSK an, vom fairen Wuerfel beim fairen Wuerfel zu bleiben: ");
		FF = scanner.nextDouble();		
		System.out.println("Geben Sie nun die WSK an, vom fairen Wuerfel zum unfairen Wuerfel zu wechseln: ");
		FU = scanner.nextDouble();
		System.out.println("Geben Sie nun die WSK an, vom unfairen Wuerfel beim unfairen Wuerfel zu bleiben");
		UU = scanner.nextDouble();
		System.out.println("Geben Sie nun die WSK an, vom unfairen Wuerfel zum fairen Wuerfel zu wechseln: ");
		UF = scanner.nextDouble();
		*/
		System.out.println("*************************************************************\n");
		System.out.println("Geben Sie nun nacheinander die Emissionswahrscheinlichkeiten für die Zahlen 1-6 des FAIREN Wuerfels an: ");
		for (int i = 1; i <= 6; i++) {
		System.out.println("Zahl " +i+ "= ");
		
		e = scanner.nextDouble();
		
		arrayEmissionFair.add(e);
		}
		System.out.println("Geben Sie nun nacheinander die Emissionswahrscheinlichkeiten für die Zahlen 1-6 des UNFAIREN Wuerfels an: ");
		for (int i = 1; i <= 6; i++) {
		System.out.println("Zahl " +i+ "= ");
		e = scanner.nextDouble();
		arrayEmissionUnfair.add(e);		
		}
		
		System.out.println(arrayEmissionFair.get(0));
		System.out.println(arrayEmissionFair.get(5));
		
		
		
	}

}
