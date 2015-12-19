package gp.twitter.extract.labeler;

import gp.twitter.extract.labeler.sequence.Observation;
import gp.twitter.extract.labeler.sequence.Sentence;
import gp.twitter.extract.labeler.tags.Tag;
import gp.twitter.extract.util.DatasetReader;

import java.io.IOException;
import java.util.ArrayList;

public class Trainer {
	private MEMM model;

	public Trainer(MEMM model)
    {
        this.model = model;
    }

    /**
     * Trains the model. Use the given file as the training data set.
     * @param file_name File that contains the training data set.
     * @throws IOException if couldnt read from file
     */
	public void trainModel(String file_name) throws IOException{

		ArrayList<Sentence> sentences = getSentences( file_name, null);
        computesAndSaveFeature(sentences);
        /*
            TODO: Add the optimization part
         */
        model.finishTraining();
	}

    /**
     * Test the model with the given test data.
     * @param file_name test data set file
     * @throws IOException
     */
	public double testModel(String file_name) throws IOException{
        ArrayList<ArrayList<Tag>> tags = new ArrayList<>();
        ArrayList<Sentence> sentences = getSentences( file_name, tags);


        int total = 0;
        int correct = 0;
        for(int i = 0; i < sentences.size(); i++)
        {
            Sentence sentence = sentences.get(i);
            ArrayList<Tag> actualTags = tags.get(i);
            model.greedy_decode(sentences.get(i));

            total += sentence.getSize();
            //compare
            for(int position = 0; position < sentence.getSize(); position++)
            {
                if(actualTags.get(position).equals(sentence.getObservationTag(position)))
                {
                    correct++;
                }
            }
        }

        return (double)correct/(double)total;

    }

    /**
     * Read sentences from the given data set file.
     * @param file_name the data set file
     * @param tags optional, when supplied filled with the tags
     * @return list of all sentences read, contains tag and value for each observation in a sentence
     * @throws IOException
     */
	private ArrayList<Sentence> getSentences(String file_name,
                                              ArrayList<ArrayList<Tag>>tags) throws IOException
	{
		DatasetReader reader = new DatasetReader(file_name) ;
        ArrayList<Sentence> sentences =new ArrayList<Sentence>();

        Observation observation = null ;
		Sentence sentence = new Sentence();

        ArrayList<Tag> _tags = null;
        if(tags!= null)
        {
            _tags = new ArrayList<>();
        }

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

                //create new tag arraylist
                if(_tags!= null)
                {
                    //add the old
                    tags.add(_tags);
                    //create new
                    _tags = new ArrayList<>();
                }
			}
            //create an observation for the current values
			observation =  new Observation();
		    observation.setValue(word);
            Tag _tag = model.getTags().getTagBySymbol(tag);
			observation.setTag(_tag);
            //add tag to tags list too
            if(_tags != null)
            {
                _tags.add(_tag);
            }
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