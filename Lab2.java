/********************************************************/
/*	Program Name:		Lab1.java		*/
/*							*/
/*	Student Name:		Sameep Shah		*/
/*	Semester:			Spring 2023	*/
/*	Class Section:	    COSC20203-055		*/
/*	Instructor:			Dr. Rinewalt	*/
/*							*/
/*	Program Overview:	Lab #2			*/
/********************************************************/

import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class Lab2 extends JFrame implements ActionListener {
	JButton open = new JButton("Next Program");
	JTextArea result = new JTextArea(20, 40);
	JLabel errors = new JLabel();
	JScrollPane scroller = new JScrollPane();

	public Lab2() {
		setLayout(new java.awt.FlowLayout());
		setSize(500, 430);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		add(open);
		open.addActionListener(this);
		scroller.getViewport().add(result);
		add(scroller);
		add(errors);
	}

	public void actionPerformed(ActionEvent evt) {
		result.setText(""); // clear TextArea for next program
		errors.setText("");
		processProgram();
	}

	public static void main(String[] args) {
		Lab2 display = new Lab2();
		display.setVisible(true);
	}

	String getFileName() {
		JFileChooser fc = new JFileChooser();
		int returnVal = fc.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION)
			return fc.getSelectedFile().getPath();
		else
			return null;
	}

	/************************************************************************/
	/* Put your implementation of the processProgram method here. */
	/* Use the getFileName method to allow the user to select a program. */
	/* Then simulate the execution of that program. */
	/* You may add any other methods that you think are appropriate. */
	/* However, you should not change anything in the code that I have */
	/* written. */
	/************************************************************************/

	// Methods to make code look cleaner while achieving the same output

	public String[] splitLine(String linee) {
		String[] splitTok = linee.split(" ");
		return splitTok;
	}

	public boolean isDoub(String num) {
		try {
			Double n = Double.parseDouble(num);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	// Special method to check if a String input is present in an ArrayList or not
	public boolean isVariable(String var, ArrayList<String> expr) {
		if (expr.contains(var))
			return true;
		else
			return false;
	}

	public boolean canBeEval(String[] expr, ArrayList<String> vars, ArrayList<Double> vals) {
		// Making sure there is no operator in front of numbers
		String ops = "+-*/";
		if (ops.contains(expr[0])) {
			return false;
		}

		// Replacing a variable in the expression by its value
		for (int i = 0; i < expr.length; i++) {
			if (isVariable(expr[i], vars)) {
				expr[i] = "" + vals.get(vars.indexOf(expr[i]));
			}
		}

		// Making sure we dont have invalid expressions with consecutive
		// numbers/operators
		boolean isDouble = false;
		for (int i = 0; i < expr.length; i++) {
			if (isDoub(expr[i]) == false && ops.contains(expr[i]) == false) {
				return false;
			}
			if (isDoub(expr[i]) == isDouble) {
				return false;
			} else {
				isDouble = isDoub(expr[i]);
			}
		}
		return true;
	}

	// Evaluating the expression for assignment of the variables
	public Double eval(String[] expr) {
		double ans = Double.parseDouble(expr[0]);
		for (int i = 1; i < expr.length - 1; i++) {
			if (expr[i].equals("+")) {
				ans += Double.parseDouble(expr[i + 1]);
			} else if (expr[i].equals("-")) {
				ans -= Double.parseDouble(expr[i + 1]);
			} else if (expr[i].equals("*")) {
				ans *= Double.parseDouble(expr[i + 1]);
			} else if (expr[i].equals("/")) {
				ans /= Double.parseDouble(expr[i + 1]);
			}
		}
		return ans;
	}

	public void processProgram() {
		String filePath = getFileName();
		// Creating Parallel ArrayLists to store variables and corresponding values
		// Making an ArrayList containging all the lines of the input
		ArrayList<String> al = new ArrayList<String>();
		ArrayList<String> variables = new ArrayList<String>();
		ArrayList<Double> values = new ArrayList<Double>();

		// Implementing BufferedReader
		try (FileReader fr = new FileReader(filePath);
				BufferedReader br = new BufferedReader(fr);) {
			String line;
			while ((line = br.readLine()) != null) {
				al.add(line);
			}
		} catch (IOException e) {
			System.err.format("ERROR", e);
		}

		// Important variables to check if an IF statement has been used and to change
		// the line back to the original
		// after being changed to expression after "THEN"
		boolean isLineChanged = false;
		String tempString = "";

		int i = 0;
		// Making an infinite loop so that execution ends only with an END statement
		while (true) {

			String[] tokens = splitLine(al.get(i));
			// appending result when existing variable is given with a valid PRINT statement
			if (tokens[0].equals("PRINT") && variables.contains(tokens[1]) && tokens.length == 2) {

				result.append(String.format("%.2f", values.get(variables.indexOf(tokens[1]))));
				result.append("\n");

			} else if (tokens[0].equals("GOTO")) {

				try {

					int n = Integer.parseInt(tokens[1]);
					// Check for valid n in input
					if (n <= al.size()) {
						i = n - 2;
						continue;
					} else {
						errors.setText("ERROR in GOTO statement, line:" + (i + 1) + "");
						return;
					}
				} catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {

					errors.setText("ERROR in GOTO Statement in Line " + (i + 1) + "");
					return;
				}

			} else if (tokens[0].equals("END") && tokens.length == 1) {
				// Blocking use of END statement with IF statement
				if (isLineChanged) {
					errors.setText(" END is not a valid Simple Statement");
					return;
				}

				return;
			} else if (tokens[1].equals("=")) {
				// Blocking use of numbers as variable names
				if (isDoub(tokens[0])) {
					errors.setText("ILLEGAL VARIABLE NAME ERROR in line " + (i + 1) + "");
					return;
				}

				// Copy of tokens after "=" sign in statement
				String[] tokensCopy = Arrays.copyOfRange(tokens, 2, tokens.length);

				if (canBeEval(tokensCopy, variables, values)) {
					// Check for given value to be a variable or a constant
					if (variables.contains(tokens[0])) {
						Double valueTok = eval(tokensCopy);
						values.set(variables.indexOf(tokens[0]), valueTok);
					} else {
						variables.add(tokens[0]);
						Double valueTok = eval(tokensCopy);
						values.add(valueTok);
					}
				} else {
					errors.setText("ERROR in line" + (i + 1) + "");
					return;
				}
			} else if (tokens[0].equals("IF") && tokens[2].equals("IS") && tokens[4].equals("THEN")) {
				// Blocking use of nested IF Statements
				if (isLineChanged) {
					errors.setText("IF Statement is not a valid Simple Statement");
					return;
				}
				// Variable for keeping track of validity of IF Statement
				boolean isGood = false;

				if (variables.contains(tokens[1])) {
					if (variables.contains(tokens[3])) {
						// Case: IF var1 IS var2
						if (values.get(variables.indexOf(tokens[1])).equals(values.get(variables.indexOf(tokens[3])))) {
							isGood = true;
						}
					} else {
						// Case: IF var IS const
						try {
							Double constantN = Double.parseDouble(tokens[3]);
							if (values.get(variables.indexOf(tokens[1])).equals(constantN)) {
								isGood = true;
							}
						} catch (NumberFormatException nfe) {
							errors.setText("ERROR in IF IS THEN STATEMENT in line:" + (i + 1) + "");
							return;
						}
					}
					if (isGood) {
						// Important code to re-run THEN Statement without IF Statement
						String[] tempFullString = al.get(i).split(" THEN ");
						tempString = al.get(i);
						isLineChanged = true;
						al.set(i, tempFullString[1]);
						i -= 1;
					}
				} else {
					// Blanket error case for wrong syntax in IF Statement
					errors.setText("ERROR in IF IS THEN STATEMENT in line:" + (i + 1) + "");
					return;
				}
			} else {
				// Blanket error case for wrong syntax in code
				errors.setText("ILLEGAL INPUT in Line:" + (i + 1) + "");
				return;
			}
			// Making sure the IF Statement line is changed back to original after swap
			if (isLineChanged) {
				al.set(i, tempString);
				isLineChanged = false;
			}
			i++;
		}
	}
}
