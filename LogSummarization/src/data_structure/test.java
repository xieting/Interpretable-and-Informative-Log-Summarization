package data_structure;

public class test {

	public static void main(String[] args) {
		String rootDirectory="/Users/tingxie/Downloads/";
		String fvpath="fv.txt";
		String mulpath="mul";
		
		Trie t=new Trie(rootDirectory+fvpath,rootDirectory+mulpath);
        t.validateTree();
        t.getSequenceEncoding(rootDirectory+"output.txt");
	}

	
}
