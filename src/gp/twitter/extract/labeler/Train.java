package gp.twitter.extract.labeler;

import gp.twitter.extract.labeler.sequence.Observation;
import gp.twitter.extract.labeler.sequence.Sentence;
import gp.twitter.extract.util.DatasetReader;

import java.io.IOException;
import java.util.ArrayList;

public class Train {
	private MEMM model;

	public Train(MEMM model)
    {
        this.model = model;
    }

    /**
     * Trains the model. Use the given file as the training data set.
     * @param file_name File that contains the training data set.
     * @throws IOException if couldnt read from file
     */
	public void trainModel(String file_name) throws IOException{

		ArrayList<Sentence> sentences = getSentences( file_name);
        computesAndSaveFeature(sentences);
        /*
            TODO: Add the optimization part
         */
        model.finishTraining();
	}

    /**
     * Read sentences from the given data set file.
     * @param file_name the data set file
     * @return list of all sentences read, contains tag and value for each observation in a sentence
     * @throws IOException
     */
	private ArrayList<Sentence> getSentences(String file_name) throws IOException
	{
		DatasetReader reader = new DatasetReader(file_name) ;
        ArrayList<Sentence> sentences =new ArrayList<Sentence>();

        Observation observation = null ;
		Sentence sentence = new Sentence();
        String word="";
        String tag = "";
        // read while we have not reached the end of file
		while(reader.read(word, tag))
		{
            // check if we reached the end of the current sentence
			if(word == null){
                //add the previous sentence to sentences list
				sentences.add(sentence);
                //then create a new sentence for the current sentence
				sentence =new Sentence();
			}
            //create an observation for the current values
			observation =  new Observation();
		    observation.setValue(word);
			observation.setTag(model.getTags().getTagBySymbol(tag));
            //add the observation to the sentence
			sentence.addObservation(observation);
		}

		return sentences;	
	}


    /**
     * Compute and save all the features used the the likelihood method, in their corresponding sentences
     * @param sentences list of all training sentences
     * @return list of all training sentences with their features calculated and saved
     */
	ArrayList<Sentence> computesAndSaveFeature(ArrayList<Sentence> sentences){
		Sentence sentence;
        //loop over each sentence
		for(int i = 0 ; i < sentences.size(); i++){
			sentence = sentences.get(i);
			for(int j = 0; j < sentence.getSize(); j++){
                model.saveFeature(sentence, j);
			}
		}
		return sentences;
	}
	
}