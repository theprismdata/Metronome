package tv.floe.metronome.classification.neuralnetworks.core;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import org.apache.mahout.math.DenseVector;
import org.apache.mahout.math.Vector;

import tv.floe.metronome.classification.neuralnetworks.core.neurons.BiasNeuron;
import tv.floe.metronome.classification.neuralnetworks.core.neurons.Neuron;
import tv.floe.metronome.classification.neuralnetworks.learning.LearningAlgorithm;
import tv.floe.metronome.classification.neuralnetworks.math.random.RangeRandomizer;
import tv.floe.metronome.classification.neuralnetworks.math.random.WeightsRandomizer;

public class NeuralNetwork {

	public enum NetworkType { 
		
		ADALINE,
		PERCEPTRON,
		MULTI_LAYER_PERCEPTRON,
		HOPFIELD,
		KOHONEN,
		NEURO_FUZZY_REASONER,
		SUPERVISED_HEBBIAN_NETWORK,
		UNSUPERVISED_HEBBIAN_NETWORK,
		COMPETITIVE,
		MAXNET,
		INSTAR,
		OUTSTAR,
		RBF_NETWORK,
		BAM,
	    BOLTZMAN
		
	};

    private NetworkType type;
  
    private ArrayList<Layer> layers;
    
    protected Vector output_vector;
    
    private ArrayList<Neuron> inputNeurons;
    
    private ArrayList<Neuron> outputNeurons;
     
    private LearningAlgorithm learningAlgo; // learning algorithme
    
    private String label = "";
    
    public NeuralNetwork() {
    	
        this.layers = new ArrayList<Layer>();

    }
    
    public void addLayer(Layer layer) {

    	layer.setParentNetwork(this);
        this.layers.add(layer);
        
    }

    public void addLayer(int index, Layer layer) {
    
    	layer.setParentNetwork(this);
    	
    	this.layers.add(index, layer);

    }

    public void removeLayer(Layer layer) {
        int index = indexOf(layer);
        removeLayerAt(index);
    }

    public void removeLayerAt(int index) {
    	
    	this.layers.remove(index);
    	
    }

    public final ArrayList<Layer> getLayers() {
        return this.layers;
    }

    public Layer getLayerByIndex(int index) {
        return this.layers.get(index);
    }

    public int indexOf(Layer layer) {

    	return this.layers.indexOf(layer);
    	
    }

    public int getLayersCount() {
        return this.layers.size();
    }
  
    @Override
    public String toString() {
        if (label != null) {
            return label;
        }

        return super.toString();
    }

    

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
    
    public void randomizeWeights(WeightsRandomizer randomizer) {
        randomizer.randomize(this);
    }

    public void randomizeWeights() {
    
    	Random rnd = new Random();
    	
        for (Layer layer : this.getLayers()) {
        	
            for (Neuron neuron : layer.getNeurons()) {
            	
                for (Connection connection : neuron.getInConnections()) {
                	
                    connection.getWeight().setValue( rnd.nextDouble() );
                    
                }
                
            }
            
        }
        
    }
    
    public void randomizeWeights(double minWeight, double maxWeight) {
    	
        this.randomizeWeights( new RangeRandomizer( minWeight, maxWeight ) );
        
    }

    public void randomizeWeights(Random random) {
    	
        this.randomizeWeights( new WeightsRandomizer( random ) );
        
    }


    
    
    public void setInputVector(Vector vec) throws Exception {

        if (vec.size() != inputNeurons.size()) {
            throw new Exception("Input vector size is invalid for Input Neuron Layer size!");
        }

        for ( int x = 0; x < vec.size(); x++ ) {
        	
        	this.inputNeurons.get(x).setInput(vec.get(x));
        	
        }
        
    	
    }
    

    public Vector getOutputVector() {
    	
    	this.output_vector = new DenseVector(outputNeurons.size());

    	for (int i = 0; i < outputNeurons.size(); i++) {
    		
    		this.output_vector.set(i,  outputNeurons.get(i).getOutput() );
            
        }

        return this.output_vector;
    }

    public void calculate() {

    	for (Layer layer : this.layers) {
        
    		layer.calculate();
        
    	}       
        
    }

    public void reset() {

    	for (Layer layer : this.layers) {
        
    		layer.reset();
        
    	}
    	
    }

    




    
    // train against individual record
    public void train(Vector actualOutVals, Vector trainingInstance) {
    	
    	this.learningAlgo.train(actualOutVals, trainingInstance);
    	
    }
    
    
    
    
  

    public NetworkType getNetworkType() {
    
    	return type;
    
    }

    public void setNetworkType(NetworkType type) {

    	this.type = type;
    
    }


    public ArrayList<Neuron> getInputNeurons() {

    	return this.inputNeurons;

    }
    
    public int getInputsCount() {

    	return this.inputNeurons.size();

    }
    
    
    public void setInputNeurons(ArrayList<Neuron> inputNeurons) {
    	
        this.inputNeurons = inputNeurons;
        
    }

    public ArrayList<Neuron> getOutputNeurons() {
    	
        return this.outputNeurons;
        
    }

    public int getOutputsCount() {
    	
        return this.outputNeurons.size();
        
    }

    public void setOutputNeurons(ArrayList<Neuron> outputNeurons) {

    	this.outputNeurons = outputNeurons;
//        this.output = new double[outputNeurons.length];
        
    }

    public LearningAlgorithm getLearningRule() {

    	return this.learningAlgo;
    	
    }

    public void setLearningRule(LearningAlgorithm learning_algo) {

    	learning_algo.setNeuralNetwork(this);
        this.learningAlgo = learning_algo;
        
    }

    public void createConnection(Neuron fromNeuron, Neuron toNeuron, double weightVal) throws Exception {

    	toNeuron.addInConnection(fromNeuron, weightVal);

    }
    
    public Layer getOutputLayer() {
    	
    	if ( this.layers.size() < 2) {
    		return null;
    	}
    	
    	return this.layers.get( this.layers.size() - 1 );
    	
    }
    
    /**
     * TODO
     * - add logic for bias neurons
     * 
     */
    public void completeIOWiring() {
    	

    	ArrayList<Neuron> inputNeuronsList = new ArrayList<Neuron>();
    	ArrayList<Neuron> outputNeuronsList = null; //new ArrayList<Neuron>();
	    
    	Layer layer_0_input = this.getLayerByIndex(0);
    	
    	Layer layer_last_output = this.getOutputLayer();
    	
    	outputNeuronsList = layer_last_output.getNeurons();
	    
    	for (Neuron neuron : layer_0_input.getNeurons() ) {
	    
    		if (!(neuron instanceof BiasNeuron)) {  // dont set input to bias neurons
	        
    			inputNeuronsList.add(neuron);
	            
    		}
	        
    	}
	
    	// System.out.println("> n count: " + inputNeuronsList.size());
	
		this.setInputNeurons(inputNeuronsList);
		this.setOutputNeurons(outputNeuronsList); 

    }    
    
    public static void ConnectLayers(Layer l0, Layer l1) throws Exception {
    	
    	//System.out.println( ">> Connect Layers: " + l0.getLayerIndex() + " to "  + l1.getLayerIndex());
    	
		for(Neuron fromNeuron : l0.getNeurons()) {

			for (Neuron toNeuron : l1.getNeurons()) {
			
				createConnection(fromNeuron, toNeuron);
			
			} 
		} 
    	
    	
    	
    }
    
	public static void createConnection(Neuron n0, Neuron n1) throws Exception {
		
		Connection connection = new Connection(n0, n1, 1.0);
		
		n1.addInConnection(connection);
		
	}
    

    
	
}