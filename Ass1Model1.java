import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;

import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

/**
 * A CPLEX-based builder and solver for the assignment's base model
 * @author Teona Banu (504450) & Catalin Gheorghiu (504458)
 *
 */

public class Ass1Model1 {

	public static ArrayList<Integer> ambulances = new ArrayList<>();
	public static ArrayList<Integer> helicopters = new ArrayList<>();

	public static void main(String[] args) throws FileNotFoundException {
		// Reading input file
		Scanner coordinates = new Scanner(new File("Rottermanhattan_coordinates.txt"));
		ArrayList<Integer> coordList = new ArrayList<>();
		// Parse each line as a size 2 array of strings, add converted values to
		// coordinate list
		while (coordinates.hasNextLine()) {
			String[] coordSet = coordinates.nextLine().split(",");
			coordList.add(Integer.parseInt(coordSet[0]));
			coordList.add(Integer.parseInt(coordSet[1]));
		}
		coordinates.close();
		// Retrieve arrays of x and y coordinates from coordinate list
		int[] x = new int[coordList.size() / 2];
		int[] y = new int[coordList.size() / 2];
		for (int i = 0; i < coordList.size(); i = i + 2) {
			x[i / 2] = coordList.get(i);
			y[i / 2] = coordList.get(i + 1);
		}
		// Set base parameters (time measured in hours, distance measured in kilometers)
		final double costA = Math.pow(10, 7);
		final double costH = 35 * Math.pow(10, 6);
		final int speedA = 75;
		final int speedH = 200;
		final double startA = 1.0 / 12;
		final double startH = 1.0 / 6;
		final double maxTime = 1.0 / 3;
		// Derive distances & time between each two points, noting that d_ij = d_ji for
		// i,j interest points
		double[][] distanceA = new double[x.length][y.length];
		double[][] distanceH = new double[x.length][y.length];
		double[][] timeA = new double[x.length][y.length];
		double[][] timeH = new double[x.length][y.length];
		for (int i = 0; i < x.length; i++) {
			for (int j = i; j < y.length; j++) {
				distanceA[i][j] = Math.abs(x[i] - x[j]) + Math.abs(y[i] - y[j]);
				timeA[i][j] = distanceA[i][j] / speedA + startA;
				timeA[j][i] = timeA[i][j];
				distanceH[i][j] = Math.sqrt((x[i] - x[j]) * (x[i] - x[j]) + (y[i] - y[j]) * (y[i] - y[j]));
				timeH[i][j] = distanceH[i][j] / speedH + startH;
				timeH[j][i] = timeH[i][j];
			}
		}

		// Model execution
		try {
			solveModel1(costA, costH, timeA, timeH, maxTime);
		} catch (IloException e) {
			System.out.println("A Cplex exception occured: " + e.getMessage());
			e.printStackTrace();
		}

		// Output printing
		try {
			PrintStream a = new PrintStream(new File("ambulances.txt"));
			for (int i = 0; i < ambulances.size(); i++) {
				a.println(x[ambulances.get(i)] + "," + y[ambulances.get(i)]);
			}
			a.close();
			PrintStream h = new PrintStream(new File("helicopters.txt"));
			for (int i = 0; i < helicopters.size(); i++) {
				h.println(x[helicopters.get(i)] + "," + y[helicopters.get(i)]);
			}
			h.close();
		} catch (IOException e) {
			System.out.println("An error occurred on printing.");
			e.printStackTrace();
		}

	}

	/**
	 * Core method of the class, builds model and optimizes
	 * @param costA - the cost of an ambulance post
	 * @param costH - the cost of a helicopter site
	 * @param timeA - matrix of total times for getting from any point to any other point by ambulance
	 * @param timeH - matrix of total times for getting from any point to any other point by helicopter
	 * @param maxTime - maximum time allowed for travel
	 * @throws IloException
	 */
	
	public static void solveModel1(double costA, double costH, double[][] timeA, double[][] timeH, double maxTime)
			throws IloException {
		// Model created
		IloCplex cplex = new IloCplex();
		int n = timeA[0].length;
		// Auxiliary parameter for service possibility
		int[][] rA = new int[n][n];
		int[][] rH = new int[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = i; j < n; j++) {
				if (timeA[i][j] <= maxTime) {
					rA[i][j] = 1;
					rA[j][i] = 1;
				}
				if (timeH[i][j] <= maxTime) {
					rH[i][j] = 1;
					rH[j][i] = 1;
				}
			}
		}

		// Decision variables
		IloNumVar[] eA = new IloNumVar[n];
		IloNumVar[] eH = new IloNumVar[n];
		for (int i = 0; i < n; i++) {
			eA[i] = cplex.boolVar("eA(" + i + ")");
			eH[i] = cplex.boolVar("eH(" + i + ")");
		}
		// Objective function
		IloNumExpr objExpr = cplex.constant(0);
		for (int i = 0; i < n; i++)
			objExpr = cplex.sum(objExpr, cplex.sum(cplex.prod(costA, eA[i]), cplex.prod(costH, eH[i])));
		cplex.addMinimize(objExpr);
		// Service time constraints
		for (int j = 0; j < n; j++) {
			IloNumExpr serviceExpr = cplex.constant(0);
			for (int i = 0; i < n; i++)
				serviceExpr = cplex.sum(serviceExpr,
						cplex.sum(cplex.prod(rA[i][j], eA[i]), cplex.prod(rH[i][j], eH[i])));
			cplex.addGe(serviceExpr, 1, "Service for node " + j);
		}
		// Engaging solver and formatting console output
		cplex.solve();
		if (cplex.getStatus() == IloCplex.Status.Optimal) {
			System.out.println("Found optimal solution!");
			System.out.println("Objective = " + cplex.getObjValue());
			// Coordinates where emergency sites are placed get stored in lists
			System.out.print("Used ambulance nodes: ");
			for (int i = 0; i < n; i++)
				if (cplex.getValue(eA[i]) > 0) {
					ambulances.add(i);
					System.out.print(i + " ");
				}

			System.out.println();
			System.out.print("Used helicopter nodes: ");
			for (int i = 0; i < n; i++)
				if (cplex.getValue(eH[i]) > 0) {
					helicopters.add(i);
					System.out.print(i + " ");
				}
		} else {
			System.out.println("No optimal solution found");
		}
		cplex.close();
	}

	
}
