package feature_management;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class test {

	public static void main(String[] args) {
		String rootDirectory="/Users/tingxie/Documents/workspace/MyLocalInsiderThreat/data/";
		try {
			BufferedReader br=new BufferedReader(new FileReader(rootDirectory+"FeatureVectors.txt"));
			FeatureVector vector=FeatureVector.readFeatureVectorFromFormattedString(br.readLine());
			System.out.println(vector);
		} catch (IOException e) {
			e.printStackTrace();
		}
 
	}

}
