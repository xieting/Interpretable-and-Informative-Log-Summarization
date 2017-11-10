package feature_management;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;



import java.util.Map.Entry;

import com.google.common.collect.HashMultiset;


/**
 * maintains a global hashmap of features
 * once a mapping is created, it cannot be changed or removed
 * @author Ting Xie
 *
 */
public class featureManager{
	//use label to find any feature
    private static HashMap<Integer,Feature> labelMap=new HashMap<Integer,Feature>();
    //the object that passed in must have defined equals() method and hashcode() method
    private static HashMap<Object,BasisFeature> basisFeatureReverseMap=new HashMap<Object,BasisFeature>();
  
    //use any set of component labels to trace back and find the feature
    private static HashMap<HashMultiset<Integer>,UnorderedBagAsFeature> UnorderedBagAsFeatureReverseMap=new HashMap<HashMultiset<Integer>,UnorderedBagAsFeature>();
    private static HashMap<ArrayList<Integer>,OrderedListAsFeature> OrderListAsFeatureReverseMap=new HashMap<ArrayList<Integer>,OrderedListAsFeature>();
    private static HashMap<ArrayList<Integer>,ParentChildRelation> ParentChildRelationReverseMap=new HashMap<ArrayList<Integer>,ParentChildRelation>();
    //remembers the last given label,labels are given in increasing order
    private static Integer lastlabelAssigned=GlobalVariables.StartValueforInt;
    
    
    public static int getlabelMapSize(){
    	return labelMap.size();
    }
    public static Feature getFeatureFromLabel(Integer label){
    	return labelMap.get(label);
    }
    public static BasisFeature getBasisFeatureFromContent(Object content){
    	return basisFeatureReverseMap.get(content);
    }
    
    public static UnorderedBagAsFeature getUnorderedBagAsFeatureFromComponentLabels(HashMultiset<Integer> set){
    	return UnorderedBagAsFeatureReverseMap.get(set);
    }
    public static OrderedListAsFeature getOrderListAsFeatureFromComponentLabels(ArrayList<Integer> list){
    	return OrderListAsFeatureReverseMap.get(list);
    }
    public static ParentChildRelation getParentChildRelationFromComponentLabels(ArrayList<Integer> value){
    	return ParentChildRelationReverseMap.get(value);
    }
    
    

    /**
     * request for new label for input MyFeature
     * if already existing feature, just return its own label
     * @param feature
     * @return
     */
    public static int requestForLabel(Feature feature){
    	int value;
    	value=lastlabelAssigned;
		lastlabelAssigned++;
    	return value;              		
    }
    
    /**
     * register this newly created feature
     * this feature must not be already existing and must have a valid label
     * @param feature
     */
    public static void registerUnorderedBagAsFeature(UnorderedBagAsFeature feature){
    	Integer key=feature.getOwnLabel();
    	labelMap.put(key, feature);   
    	UnorderedBagAsFeatureReverseMap.put(feature.getHorizontalList(), feature);    	  	   	
    }
    
    /**
     * register this newly created feature
     * this feature must not be already existing and must have a valid label
     * @param feature
     */
    public static void registerOrderListAsFeature(OrderedListAsFeature feature){
    	Integer key=feature.getOwnLabel();
    	labelMap.put(key, feature);   	    
    	OrderListAsFeatureReverseMap.put(feature.getHorizontalList(), feature);    	  	   	
    }
    
    /**
     * register this newly created feature
     * this feature must not be already existing and must have a valid label
     * @param feature
     */
    public static void registerBasisFeature(BasisFeature feature){
    	Integer key=feature.getOwnLabel();
    	labelMap.put(key, feature);
    	basisFeatureReverseMap.put(feature.getContent(),feature);       	
    }   
    /**
     * register this newly created feature
     * this feature must not be already existing and must have a valid label
     * @param feature
     */
    public static void registerParentChildRelation(ParentChildRelation feature){
    	Integer key=feature.getOwnLabel();
    	labelMap.put(key, feature);
    		int parentFeatureLabel=feature.getParentLabel();
    		int childlabel=feature.getChildLabel(); 
    		ArrayList<Integer> list=new ArrayList<Integer>();
    		list.add(parentFeatureLabel);
    		list.add(childlabel);  
    		ParentChildRelationReverseMap.put(list, feature);
    	}


    public static void savelabelMapTofileInTextForm(String fileName){
    	try {
    		BufferedWriter output = new BufferedWriter(new FileWriter(new File(fileName)));
        	output.write("labelID, labelContent\n");
        	for (Entry<Integer, Feature> entry : labelMap.entrySet()) {
        		output.write(entry.getKey() + "\n" + entry.getValue().printMySelfRecursive(0) + "\n");
        	}
        	output.close();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    	/*
    	FileOutputStream fout;
		try {
			fout = new FileOutputStream("labelMap.txt");
	    	ObjectOutputStream oos = new ObjectOutputStream(fout);
	    	oos.writeObject(labelMap);
	    	oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}*/		
    }
    
    /**
     * serialization
     * @param fileName
     */
    public static void savelabelMap(){
    	
    	try {
    		BufferedWriter output = new BufferedWriter(new FileWriter(new File("labelMap.txt")));
        	output.write("labelID, labelContent\n");
        	for (Entry<Integer, Feature> entry : labelMap.entrySet()) {
        		output.write(entry.getKey() + " : " + entry.getValue().printMySelfRecursive(0) + "\n");
        	}
        	output.close();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    	
    	FileOutputStream fout;
		try {
			fout = new FileOutputStream("labelMap");
	    	ObjectOutputStream oos = new ObjectOutputStream(fout);
	    	oos.writeObject(labelMap);	    	
	    	oos.close();
	    	fout.close();
		} catch (IOException e) {
			e.printStackTrace();
		}	
    }	
    
    @SuppressWarnings("unchecked")
	public static void loadlabelMap(){
	      try {
	          FileInputStream fileIn = new FileInputStream("labelMap");
	          ObjectInputStream in = new ObjectInputStream(fileIn);
	          labelMap = (HashMap<Integer, Feature>) in.readObject();
	          in.close();
	          fileIn.close();	          
	       }catch(IOException i) {
	          i.printStackTrace();
	          return;
	       }catch(ClassNotFoundException c) {
	          c.printStackTrace();
	          return;
	       }
    }
     
}
