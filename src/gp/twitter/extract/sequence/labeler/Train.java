package gp.twitter.extract.sequence.labeler;

import java.io.IOException;
import java.util.ArrayList;

import gp.twitter.extract.util.DatasetReader;

public class Train {

	
	
	public void train(String file_name) throws IOException{
		ArrayList<Sentence> sentences= getSentences( file_name);
        computesAndSaveFeature(sentences);
        
		
		
		
		
	}
	
	
	private ArrayList<Sentence> getSentences(String file_name) throws IOException
	{
		DatasetReader reader =new DatasetReader(file_name) ;
		String word="";
        String tag = "";
		Observation observation = null ;
		Sentence sentence =new Sentence();
		ArrayList<Sentence> sentences =new ArrayList<Sentence>();
	    
		while(reader.read(word, tag))
		{
			if(word==null){
				sentences.add(sentence);
				sentence =new Sentence();
				
			}
			observation =  new Observation();
		    observation.setValue(word);
			observation.setTag(Tag.getTag(tag));
			sentence.addObseration(observation);
		}
		return sentences;	
	}

	
	
	
	ArrayList<Sentence> computesAndSaveFeature(ArrayList<Sentence> sentences){
		MEMM memm ;
		memm=MEMM.getTrainingInstance();
		Sentence sentence;
		
		for(int i=0 ;i<sentences.size();i++){
			sentence =sentences.get(i);
			
			for(int j=0;j<sentence.getSize();j++){
				sentence =memm.savefeature(sentence, j);
			}
		}
		
		return sentences;
	}
	
	
	
}