package tv.floe.metronome.eval;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.InputSplit;
import org.apache.hadoop.mapred.JobConf;
import org.apache.mahout.math.Matrix;
import org.apache.mahout.math.Vector;

import com.cloudera.iterativereduce.io.TextRecordParser;

import tv.floe.metronome.berkley.Counter;
import tv.floe.metronome.deeplearning.datasets.DataSet;
import tv.floe.metronome.deeplearning.datasets.iterator.impl.MnistHDFSDataSetIterator;
import tv.floe.metronome.math.MatrixUtils;

public class Evaluation {

	private double truePositives;
	private Counter<Integer> falsePositives = new Counter<Integer>();
	private double falseNegatives;
	private ConfusionMatrix<Integer> confusion = new ConfusionMatrix<Integer>();

	
	
	
	
	
	
	
	
	private int iamax(Vector vec) {
		
		//double max = vec.get(0);
		int index = 0;
		
		for ( int x = 0; x < vec.size(); x++ ) {
			
			if ( Math.abs(vec.get(x)) > Math.abs( vec.get(index) ) ) {
				
				index = x;
				
			}
			
			
		}
		
		return index;
		
	}
	

	
	/**
	 * Collects statistics on the real outcomes vs the 
	 * guesses. This is for logistic outcome matrices such that the 
	 * 
	 * Note that an IllegalArgumentException is thrown if the two passed in
	 * matrices aren't the same length.
	 * @param realOutcomes the real outcomes (usually binary)
	 * @param guesses the guesses (usually a probability vector)
	 */
	public void eval(Matrix realOutcomes,Matrix guesses) {
		//if(realOutcomes.length != guesses.length)
		if ( MatrixUtils.length(realOutcomes) != MatrixUtils.length(guesses) ) {
			throw new IllegalArgumentException("Unable to evaluate. Outcome matrices not same length");
		}
		
		for (int i = 0; i < realOutcomes.numRows(); i++) {
			
			//Matrix currRow = realOutcomes.getRow(i);
			Vector currRow = realOutcomes.viewRow(i);
			//Matrix guessRow = guesses.getRow(i);
			Vector guessRow = guesses.viewRow(i);

			//int currMax = SimpleBlas.iamax(currRow);
			int currMax = iamax(currRow);
			//int guessMax = SimpleBlas.iamax(guessRow);
			int guessMax = iamax(guessRow);

			addToConfusion( currMax, guessMax );

			if (currMax == guessMax) {
				
				incrementTruePositives();
				
			} else {
				
				incrementFalseNegatives();
				incrementFalsePositives(guessMax);
				
			}
		}
	}

	public double correctScores() {
		return this.truePositives;
	}


	public String stats() {
		StringBuilder builder = new StringBuilder()
		.append("\n");
		Set<Integer> classes = confusion.getClasses();
		for(Integer clazz : classes) {
			for(Integer clazz2 : classes) {
				int count = confusion.getCount(clazz, clazz2);
				if(count != 0)
					builder.append("\nActual Class " + clazz + " was predicted with Predicted " + clazz2 + " with count " + count  + " times\n");
			}
		}
		builder.append("\n==========================F1 Scores========================================");
		builder.append("\n " + f1());
		builder.append("\n===========================================================================");
		return builder.toString();
	}

	/**
	 * Adds to the confusion matrix
	 * @param real the actual guess
	 * @param guess the system guess
	 */
	public void addToConfusion(int real,int guess) {
		confusion.add(real, guess);
	}

	/**
	 * Returns the number of times the given label
	 * has actually occurred
	 * @param i the label
	 * @return the number of times the label
	 * actually occurred
	 */
	public int classCount(int i) {
		return confusion.getActualTotal(i);
	}

	/**
	 * Returns the number of times a given label was predicted 
	 * @param label the label to get
	 * @return the number of times the given label was predicted
	 */
	public int numtimesPredicted(int label) {
		return confusion.getPredictedTotal(label);
	}

	/**
	 * Gets the number of times the 
	 * given class was predicted for the 
	 * given predicted label
	 * @param actual 
	 * @param predicted
	 * @return
	 */
	public int numTimesPredicted(int actual,int predicted) {
		return confusion.getCount(actual, predicted);
	}

	public double precision() {
		double prec = 0.0;
		for(Integer i : confusion.getClasses()) {
			prec += precision(i);
		}
		return prec / (double) confusion.getClasses().size();
	}


	public double f1() {
		double precision = precision();
		double recall = recall();
		if(precision == 0 || recall == 0)
			return 0;
		return 2.0 * ((precision * recall / (precision + recall)));
	}

	/**
	 * Calculate f1 score for a given class
	 * @param i the label to calculate f1 for
	 * @return the f1 score for the given label
	 */
	public double f1(int i) {
		double precision = precision(i);
		double recall = recall();
		if(precision == 0 || recall == 0)
			return 0;
		return 2.0 * ((precision * recall / (precision + recall)));
	}

	/**
	 * Returns the recall for the outcomes
	 * @return the recall for the outcomes
	 */
	public double recall() {
		if(truePositives == 0)
			return 0;
		return truePositives / (truePositives + falseNegatives);
	}

	/**
	 * Returns the precision for a given label
	 * @param i the label
	 * @return the precision for the label
	 */
	public double precision(int i) {
		if(truePositives == 0)
			return 0;
		return truePositives / (truePositives + falsePositives.getCount(i));
	}


	public void incrementTruePositives() {
		truePositives++;
	}

	public void incrementFalseNegatives() {
		falseNegatives++;
	}

	public void incrementFalsePositives(int i) {
		falsePositives.incrementCount(i, 1.0);
	}

}
