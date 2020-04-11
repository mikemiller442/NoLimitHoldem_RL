package reinforcementLearning;

public interface Function {

    public void beginBackPropagation(double immediateReward, boolean ip);
	public void beginTerminalBackPropagation(double reward, boolean ip);
	public void backPropagation(double[] inputs, double target, double output, boolean ip);
	public double functionOutput(double[] features, boolean ip);
	public void setCurrentSAP(double SAP);
	public void setPreviousSAP(double SAP);
	public double getCurrentSAP();
	public double getPreviousSAP();
	public void setPreviousFeatures(double[] features);
	public double[] getCurrentFeatures();
	public void setWeights(boolean train);
	public void storeWeights();
	
}
