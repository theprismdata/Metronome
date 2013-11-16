package tv.floe.metronome.classification.neuralnetworks.learning;

import java.util.ArrayList;

import tv.floe.metronome.classification.neuralnetworks.core.Connection;
import tv.floe.metronome.classification.neuralnetworks.core.Layer;
import tv.floe.metronome.classification.neuralnetworks.core.neurons.Neuron;
import tv.floe.metronome.classification.neuralnetworks.activation.ActivationFunction;

public class BackPropogationLearningAlgorithm extends SigmoidDeltaLearningAlgorithm {

	public BackPropogationLearningAlgorithm() {
		super();
	}


	/**
	 * This is the main driver method for the learning algorithms 
	 * train( ... )
	 * - calculate output of current instance
	 * - calculate error of output vs desired output
	 * 
	 */
	@Override
	protected void updateNetworkWeights(double[] outputError) {
				
		this.calculateErrorAndUpdateOutputNeurons(outputError); // via SigmoidDelta
		this.calculateErrorAndUpdateHiddenNeurons();            // implemented in this class
		
	}

	protected void calculateErrorAndUpdateHiddenNeurons() {
                
		ArrayList<Layer> layers = nn.getLayers();
		
	
		
		for (int l = layers.size() - 2; l > 0; l--) {
									
			for ( Neuron neuron : layers.get( l ).getNeurons() ) {	
                                
				double neuronError = this.calculateHiddenNeuronError( neuron ); 
				
				neuron.setError( neuronError );
				
				this.updateNeuronWeights( neuron );
								
			} // for
			
		} // for
		
	}

	protected double calculateHiddenNeuronError(Neuron neuron) {	
		
		double deltaSum = 0d;
		
		
		
		for (Connection connection : neuron.getOutConnections()) {	
			
			double delta = connection.getToNeuron().getError() * connection.getWeight().value;
			deltaSum += delta; // weighted delta sum from the next layer
		
			if (this.isMetricCollectionOn()) {
				this.metrics.incErrCalcOpCount();
			}
			
		} // for

		
		
		ActivationFunction transferFunction = neuron.getActivationFunction();
		
		
		double netInput = neuron.getNetInput(); // should we use input of this or other neuron?
		double fnDerv = transferFunction.getDerivative(netInput);
		double neuronError = fnDerv * deltaSum;
		return neuronError;
	}	
	
}
