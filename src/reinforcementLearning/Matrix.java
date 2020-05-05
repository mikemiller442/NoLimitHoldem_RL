package reinforcementLearning;

public class Matrix {

	private double[][] entries;
	private int numRows;
	private int numColumns;
	private double alpha;

	public Matrix(double[][] entries) {

		this.entries = entries;
		this.numRows = entries.length;
		this.numColumns = entries[0].length;
		this.alpha = 0.000002;

	}

	public double[][] getEntries() {
		return this.entries;
	}
	
	public int getNumColumns() {
		return this.numColumns;
	}
	
	public int getNumRows() {
		return this.numRows;
	}
	
	public String toString() {
		String output = "";
		for (int i = 0; i < entries.length; i++) {
		  for (int j = 0; j < entries[0].length; j++) {
		    output = output + Double.toString(entries[i][j]) + ",";
		  }
		}
		return output.substring(0, output.length()-1);
	}

	// performs matrix multiplication between matrices
	public Matrix compose(Matrix preimage) {
		double dotProduct;
		double[][] preimageEntries = preimage.getEntries();
		double[][] imageEntries = new double[this.numRows][preimage.getNumColumns()];
		for (int i = 0; i < this.numRows; i++) {
			for (int j = 0; j < preimage.getNumColumns(); j++) {
				dotProduct = 0;
				for (int k = 0; k < this.numRows; k++) {
					dotProduct += this.entries[i][k]*preimageEntries[k][j];
				}
				imageEntries[i][j] = dotProduct;
			}
		}
		Matrix image = new Matrix(imageEntries);
		return image;
	}
	
	// multiplies a vector by a matrix
	public double[] compose(double[] preimage) {
		double dotProduct;
		double[] image = new double[this.numRows];
		for (int i = 0; i < this.entries.length; i++) {
			dotProduct = 0;
			for (int k = 0; k < preimage.length; k++) {
				dotProduct += this.entries[i][k]*preimage[k];
			}
			image[i] = dotProduct;
		}
		return image;
	}
	
	public Matrix transpose() {
      double[][] transposeEntries = new double[numColumns][numRows];
      for (int i = 0; i < this.numColumns; i++) {
          for (int j = 0; j < this.numRows; j++) {
            transposeEntries[i][j] = this.entries[j][i];
          }
      }
      return new Matrix(transposeEntries);
    }
	
	public void updateWeights(Matrix gradients) {
		double[][] gradientEntries = gradients.getEntries();
		for (int i = 0; i < this.numRows; i++) {
			for (int j = 0; j < this.numColumns; j++) {
				this.entries[i][j] += alpha*gradientEntries[i][j];
			}
		}
	}
	
}
