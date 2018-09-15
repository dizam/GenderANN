//Brian Becker, 999646986
//Daljodh Pannu, 912303549 

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Collections;
import java.util.Random;
import java.lang.Math;

public class a_dot_out
{
	private static final double MALE = 1.0;
	private static final double FEMALE = 0.0;
	private static final int OUTPUTS = 1;
	private static final int HIDDEN = 8;
	private static final int INPUTSIZE = 128*120;
	private static final int CROSSREPS = 10;
	private static final int NUMFOLDS = 5;
	private static final int EPOCHS = 25;
	private static final double LEARNINGRATE = 0.22;

	public class Picture
	{
		public double type = 0.0;
		public ArrayList<Double> pixels = new ArrayList<Double>();

		public Picture(double gender, ArrayList<Double> data)
		{
			type = gender;
			pixels.addAll(data);
		}
	}

	static ArrayList<Picture> imageData = new ArrayList<Picture>();
	static ArrayList<ArrayList<Picture>> subsets = new ArrayList<ArrayList<Picture>>(NUMFOLDS);

	public class Neuron
	{
		public ArrayList<Double> weightVector = new ArrayList<Double>();
		public double output = 0;
		Random r = new Random();

		public Neuron(int numInputs)
		{
			for (int i = 0; i < numInputs + 1; i++)
			{
				//r.setSeed(i);
				double num = -1 + r.nextDouble() * 2;
				weightVector.add(num);
				//System.out.println("initial: " + num);
			}
		}

		public double sigmoidFunction(ArrayList<Double> inputs)
		{
			double x = 0;
			double result = 0;
			for (int i = 1; i < inputs.size(); i++)
			{
				//System.out.println("inputs: " +inputs.get(i));
				//System.out.println("weights: " + weightVector.get(i));
				x += (inputs.get(i) * weightVector.get(i));
			}
			x*=-1;
			//System.out.println("weight: " + x);
			result = 1.0/(1.0 + Math.exp(x));
			return result;
		}

		public void deltaWeight(ArrayList<Double> inputs, double middleTerm)
		{
			for (int i = 1; i < inputs.size(); i++)
			{
				weightVector.set(i, weightVector.get(i) + (LEARNINGRATE) * middleTerm * inputs.get(i));
			}
		}

	}

	public a_dot_out(String[] args) {
		for (int a = 0; a < HIDDEN; a++)
		{
			hiddenLayer.add(new Neuron(INPUTSIZE));
		}
		for (int b = 0; b < OUTPUTS; b++)
		{
			outputLayer.add(new Neuron(HIDDEN));
		}
		String trainPath1 = null;
		String trainPath2 = null;
		String testPath = null;
		try
		{
			if  (args[0].equals("-train"))
			{
				trainPath1 = args[1];
				trainPath2 = args[2];
			}
			else
			{
				throw new IllegalArgumentException("wrong argument");
			}
			if (args[3].equals("-test"))
			{
				testPath = args[4];
			}
			else
			{
				throw new IllegalArgumentException("wrong argument");
			}
		}
		catch (IllegalArgumentException wrong)
		{
			System.err.println(wrong.getMessage());
			System.out.println("Usage: java <teamname> -train <dir> <dir> -test <dir>");
			System.exit(1);
		}

		initializeData(trainPath1, trainPath2);
		//double gone = a_dot_out.train(imageData, false, true);
		//crossValidate();

		
		Collections.shuffle(imageData);
		double abyss = a_dot_out.train(imageData, false, false);	
		//System.out.println(testPath);	
		File testDir = new File(testPath);
		File[] testFiles = testDir.listFiles();
		ArrayList<Picture> testData = new ArrayList<Picture>();
		ArrayList<Double> pixels = new ArrayList<Double>();
		if (testFiles != null)
		{
			for (File child: testFiles)
			{
				try
				{
					Scanner sc = new Scanner(child);
					while (sc.hasNextInt())
					{
						pixels.add(sc.nextInt() / 256.0);
					}
					Picture test = new Picture(0, pixels);
					testData.add(test);
					pixels.clear();
				}
				catch (FileNotFoundException fail)
				{
						System.out.println("File not found");
						System.exit(1);
				}
			}
		}
		else
		{
			System.out.println("Error: not a directory");
		}
		double store = a_dot_out.test(testData, false);
		
	}

	static ArrayList<Neuron> hiddenLayer = new ArrayList<Neuron>();
	static ArrayList<Neuron> outputLayer = new ArrayList<Neuron>();

	public void initializeData(String trainPath1, String trainPath2)
	{
		File trainDir1 = new File(trainPath1);
		File trainDir2 = new File(trainPath2);
		ArrayList<Double> pixels = new ArrayList<Double>();
		File[] maleFiles = trainDir1.listFiles();
		if (maleFiles != null)
		{
			for (File child: maleFiles)
			{
				if (!(child.getName().equals("a")))
				{
					try
					{
						Scanner sc = new Scanner(child);
						while (sc.hasNextInt())
						{
							pixels.add(sc.nextInt() / 256.0);
						}
						Picture male = new Picture(MALE, pixels);
						imageData.add(male);
						pixels.clear();
					}
					catch (FileNotFoundException fail)
					{
						System.out.println("File not found");
						System.exit(1);
					}
				}
			}
		}
		else
		{
			System.out.println("Error: not a directory");
		}

		File[] femaleFiles = trainDir2.listFiles();
		if (femaleFiles != null)
		{
			for (File child: femaleFiles)
			{
				if (!(child.getName().equals("a")))
				{
					try
					{
						Scanner sc = new Scanner(child);
						while (sc.hasNextInt())
						{
							pixels.add(sc.nextInt() / 256.0);
						}
						Picture female = new Picture(FEMALE, pixels);
						imageData.add(female);
						pixels.clear();
					}
					catch (FileNotFoundException fail)
					{
						System.out.println("File not found");
						System.exit(1);
					}
				}
			}
		}
		else
		{
			System.out.println("Error: not a directory");
		}
	}

	public static void partitionData()
	{
		int index = 0;
		ArrayList<Integer> permutation = new ArrayList<Integer>(5);
		for (int i = 0; i < 5; i++)
		{
			permutation.add(i);
		}
		Collections.shuffle(imageData);

		int l = 0;
		for (int j = 0; j < imageData.size(); j++)
		{
			Picture cur = imageData.get(j);
			index = permutation.get(l);
			l++;
			if (l == 5)
			{
				l = 0;
			}
			subsets.get(index).add(cur);
		}
	}

	public void crossValidate()
	{
		double meanTest = 0.0;
		double meanTrain = 0.0;
		ArrayList<Double> resultsTest = new ArrayList<Double>();
		ArrayList<Double> resultsTrain = new ArrayList<Double>();
		double sigmaTest = 0.0;
		double sigmaTrain = 0.0;
		double sumTest = 0.0;
		double sumTrain = 0.0;

		double meanTestFinal = 0.0;

		for (int t = 0; t < NUMFOLDS; t++)
		{
			subsets.add(new ArrayList<Picture>());
		}

		for (int i = 0; i < CROSSREPS; i++)
		{
			a_dot_out.partitionData();
			for (int j = 0; j < NUMFOLDS; j++)
			{
				ArrayList<Picture> trainingData = new ArrayList<Picture>();
				ArrayList<Picture> testingData = new ArrayList<Picture>();
				for (int q = 0; q < NUMFOLDS; q++)
				{
					if (q != j)
					{
						trainingData.addAll(subsets.get(q));
					}
					else
					{
						testingData.addAll(subsets.get(q));
					}
				}
				double temp2 = a_dot_out.train(trainingData, true, false);
				double temp = a_dot_out.test(testingData, true);
				resultsTest.add(temp);
				resultsTrain.add(temp2);
				meanTest += temp;
				meanTrain += temp2;
			}
			meanTest/=NUMFOLDS;
			meanTestFinal += meanTest;
			meanTrain/=NUMFOLDS;
			for (int s = 0; s < NUMFOLDS; s++)
			{
			sumTest += Math.pow((resultsTest.get(s) - meanTest), 2);
			}
			sumTest *= (1.0/NUMFOLDS);
			sigmaTest = Math.sqrt(sumTest);

			for (int s = 0; s < NUMFOLDS; s++)
			{
			sumTrain += Math.pow((resultsTrain.get(s) - meanTrain), 2);
			}
			sumTrain *= (1.0/NUMFOLDS);
			sigmaTrain = Math.sqrt(sumTrain);

			System.out.println("Experiment " + (i + 1));
			System.out.print("MEAN for train: ");
			System.out.printf("%.2f", meanTrain * 100);
			System.out.print("%");
			System.out.print("             StD for train: ");
			System.out.printf("%.2f", sigmaTrain * 100);
			System.out.print("%");
			System.out.println();

			System.out.print("MEAN for test: ");
			System.out.printf("%.2f", meanTest * 100);
			System.out.print("%");
			System.out.print("             StD for test: ");
			System.out.printf("%.2f", sigmaTest * 100);
			System.out.print("%");
			System.out.println();
			System.out.println();

			meanTest = 0.0;
			meanTrain = 0.0;
			resultsTest.clear();
			resultsTrain.clear();
			sigmaTest = 0.0;
			sigmaTrain = 0.0;
			sumTest = 0.0;
			sumTrain = 0.0;

			subsets.clear();
			for (int l = 0; l < NUMFOLDS; l++)
			{
				subsets.add(new ArrayList<Picture>());
			}
			hiddenLayer.clear();
			for (int p = 0; p < HIDDEN; p++)
			{
				hiddenLayer.add(new Neuron(INPUTSIZE));
			}
			outputLayer.clear();
			for (int t = 0; t < OUTPUTS; t++)
			{
				outputLayer.add(new Neuron(HIDDEN));
			}
		}

		meanTestFinal/=10;
		System.out.print("Final generalization error: ");
		System.out.printf("%.2f", meanTestFinal * 100);
		System.out.println();
		System.out.println();
	}

	public static void main(String[] args)
	{
		a_dot_out a = new a_dot_out(args);
	}

	public static double train(ArrayList<Picture> trainingData, boolean accuracy, boolean weighted)
	{
		double mean = 0;
		for (int reps = 0; reps < EPOCHS; reps++)
		{
			ArrayList<Double> outputMiddleTerm = new ArrayList<Double>();
			ArrayList<Double> hiddenMiddleTerm = new ArrayList<Double>();
			for (int i = 0; i < trainingData.size(); i++)
			{
				ArrayList<Double> biasedInput = new ArrayList<Double>();
				biasedInput.add(0, (Double)1.0);
				ArrayList<Double> biasedHiddenOutput = new ArrayList<Double>();
				biasedHiddenOutput.add(0, (Double)1.0);
				biasedInput.addAll(trainingData.get(i).pixels);
				for (int j = 0; j < HIDDEN; j++)
				{
					double hlr = hiddenLayer.get(j).sigmoidFunction(biasedInput);
					//System.out.println(hlr);
					hiddenLayer.get(j).output = hlr;
					//System.exit(1);
					biasedHiddenOutput.add(hlr);
					//System.out.println("hlr: " + hlr);		
				}
				for (int k = 0; k < OUTPUTS; k++)
				{
					double actual = outputLayer.get(k).sigmoidFunction(biasedHiddenOutput);
					outputLayer.get(k).output = actual;
					outputMiddleTerm.add(-(actual - trainingData.get(i).type) * (actual) * (1 - actual));
				}

				for (int m = 0; m < HIDDEN; m++)
				{
					double sum = 0;
					for (int s = 0; s < OUTPUTS; s++)
					{
						sum += ((outputMiddleTerm.get(s)) * (outputLayer.get(s).weightVector.get(m)));
					}
					double hiddenOutput = hiddenLayer.get(m).output;
					hiddenMiddleTerm.add((hiddenOutput) * (1 - hiddenOutput) * sum);

					hiddenLayer.get(m).deltaWeight(biasedInput, hiddenMiddleTerm.get(m));
				}

				for (int n = 0; n < OUTPUTS; n++)
				{
					outputLayer.get(n).deltaWeight(biasedHiddenOutput, outputMiddleTerm.get(n));
				}

				if (accuracy && (reps == (EPOCHS - 1)))
				{
					if (outputLayer.get(0).output >= 0.5 && trainingData.get(i).type == 1)
					{
						mean++;
					}
					else if (outputLayer.get(0).output < 0.5 && trainingData.get(i).type == 0)
					{
						mean++;
					}
					else
					{
						mean += 0;
					}
				}
				biasedInput.clear();
				biasedHiddenOutput.clear();
				outputMiddleTerm.clear();
				hiddenMiddleTerm.clear();
			}

		}
		if (weighted)
		{
			String[] fileNames = {"unit1.txt","unit2.txt", "unit3.txt", "unit4.txt", "unit5.txt", "unit6.txt",
			"unit7.txt", "unit8.txt"};
			for (int q = 0; q < HIDDEN; q++)
			{
				try
				{
					PrintWriter writer = new PrintWriter(fileNames[q]);
					for (int u = 0; u < INPUTSIZE; u++)
					{
						double value = hiddenLayer.get(q).weightVector.get(u);
						if (value < 0)
						{
							value *= -1;
						}
						value = (1 - value);
						value*=255;
						writer.print((int)value);
						
						if ((u+1)%16 == 0)
						{
							writer.println();
						}
						else {
							writer.print((char)32);
						}
					}
					writer.close();
				//	writer.println();
				}
				catch (IOException e)
				{
					System.out.println("files not good");
				}
			}
			System.exit(1);
		}
		else
		{

		}
		if (accuracy)
		{
			return (mean/trainingData.size());
		}
		else
		{
			return 0;
		}
	}

	public static double test(ArrayList<Picture> testingData, boolean accuracy)
	{
		double mean = 0.0;
		for (int i = 0; i < testingData.size(); i++)
		{
			ArrayList<Double> biasedInput = new ArrayList<Double>();
			biasedInput.add(0, (Double)1.0);
			ArrayList<Double> biasedHiddenOutput = new ArrayList<Double>();
			biasedHiddenOutput.add(0, (Double)1.0);
			biasedInput.addAll(testingData.get(i).pixels);

			for (int j = 0; j < HIDDEN; j++)
			{
				double hlr = hiddenLayer.get(j).sigmoidFunction(biasedInput);
				hiddenLayer.get(j).output = hlr;
				biasedHiddenOutput.add(hlr);	
			}	

			for (int k = 0; k < OUTPUTS; k++)
			{
				double actual = outputLayer.get(k).sigmoidFunction(biasedHiddenOutput);
			//	System.out.println("actual is: " + actual);
				outputLayer.get(k).output = actual;
			}
			//System.out.println(outputLayer.get(0).output);
			if (accuracy)
			{
				if (outputLayer.get(0).output >= 0.5 && testingData.get(i).type == 1)
				{
					mean++;
				}
				else if (outputLayer.get(0).output < 0.5 && testingData.get(i).type == 0)
				{
					mean++;
				}
				else
				{
					mean += 0;
				}
			}
			if ((!accuracy) && outputLayer.get(0).output >= 0.5)
			{
				System.out.print("Output Picture " + (i+1) + ": MALE       ");
				System.out.print("Confidence: ");
				System.out.printf("%.2f", (outputLayer.get(0).output/1.0) * 100);
				System.out.print("%");
				System.out.println();
			}
			else if ((!accuracy) && outputLayer.get(0).output < 0.5)
			{
				System.out.print("Output Picture " + (i+1) + ": FEMALE.      ");
				System.out.print("Confidence: ");
				System.out.printf("%.2f", ((1 - outputLayer.get(0).output)/1.0) * 100);
				System.out.print("%");
				System.out.println();
			}
			else
			{

			}
			biasedInput.clear();
			biasedHiddenOutput.clear();

		}
		if (accuracy)
		{
			return (mean/testingData.size());
		}
		else
		{
			return 0;
		}

	}
}