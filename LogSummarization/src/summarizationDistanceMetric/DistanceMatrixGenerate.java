package summarizationDistanceMetric;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import data_structure.Trie;
import feature_management.FeatureVector;
import summarizationDistanceMetric.DistanceMetric;
import summarizationDistanceMetric.InformationOverlap;

public class DistanceMatrixGenerate {

	public static void main(String[] args) {
		String datapath="/Users/tingxie/Documents/workspace/MyLocalInsiderThreat/data/";
		String featureVectorpath="FeatureVectors.txt";
		String featureVectorMultiplicityPath="FeatureVectorCounts.txt";
		String timePath;

		String matrixPath="Custom_DistanceMatrix.txt";
			

			Trie mytree=new Trie(datapath+featureVectorpath,datapath+featureVectorMultiplicityPath);

			ArrayList<FeatureVector> vectorList=new ArrayList<FeatureVector>();

			//read in feature vectors and store them in FP Tree
			try {   

				BufferedReader featurebr=new BufferedReader(new FileReader(new File(datapath+featureVectorpath)));
				String line;
				int index=0;
				while((line=featurebr.readLine())!=null){
					FeatureVector vector=FeatureVector.readFeatureVectorFromFormattedString(line);
					vectorList.add(vector);
					index++;
				}
				featurebr.close();		
				System.out.println("FeatureVectors consumed, there are total of "+index+" vectors involved.");				
			
				//create distance metric
				DistanceMetric metric=new InformationOverlap(mytree);
				
				ArrayList<LinkedHashMap<Integer,Float>> distances=new ArrayList<LinkedHashMap<Integer,Float>>();               
				long start=System.nanoTime();
				for(int i=0;i<vectorList.size();i++){
					int startInd=i+1;
					LinkedHashMap<Integer,Float> map=new LinkedHashMap<Integer,Float>();
					for(int j=startInd;j<vectorList.size();j++){
						double distance=metric.dist(vectorList.get(i), vectorList.get(j));
						if(distance>0)
						map.put(j,(float)distance);	
					}
					distances.add(map);
				}	
				
				long end=System.nanoTime();
				long elapsedTime=end-start;
				double seconds = (double)elapsedTime /1000000000;
				System.out.println("DistanceMatrix generate finished, time used "+seconds+" secs.");
                //clear memory
			    mytree=null;
			    metric=null;
			    vectorList=null;
			    
				PrintWriter pw;
				pw=new PrintWriter(datapath+matrixPath);
				//save to file
				for (int i=0;i<distances.size();i++){
					HashMap<Integer,Float> map=distances.get(i);					
					String vec="1";
					for(int j=0;j<i;j++){
					HashMap<Integer,Float> symmetricmap=distances.get(j);
					Float distance=symmetricmap.get(i);
					if(distance!=null)
					vec+=" "+j+":"+String.format("%6.6e",distance);
					//vec+=" "+j+":"+distance;
					}
					
					for (Entry<Integer, Float> en: map.entrySet()){
						vec+=" "+en.getKey()+":"+String.format("%6.6e",en.getValue());
						//vec+=" "+en.getKey()+":"+en.getValue();
					}
					if(vec.length()>2)
					pw.println(vec);
					else
						System.out.println("empty row of distances, please check the row # "+i);
				}
				pw.close();
				
				timePath="Custom_DistanceMatrixComputingTime.txt";

				pw=new PrintWriter(new FileWriter(datapath+timePath,true));				
				pw.println(seconds);
				pw.close();
				//clear memory;
			    distances=null;
			} catch (IOException e) {
				e.printStackTrace();
			}

		
	}

}
