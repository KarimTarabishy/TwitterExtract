package gp.twitter.extract.sequence.labeler;

import java.util.Map;

public class Tag {
	public int id;
	public String symbol;
	private static Map<String,Tag> tags ; 
	public static Tag[] TAGS;
	private Tag(){
		
	
	}
	public static Tag getTag(String tag){
		Tag temp= tags.get(tag);
		if(temp==null){
			throw new  RuntimeException("tag wasn't found");
		}
				
		return temp ;
		

		
	}
	
	/**
	 * start tag is special with id = -1
	 * @return
	 */
	public static Tag getStartTag(){
		return tags.get("start");
		
		
	}
	
	public static Tag getTag(int index){
		return TAGS[index];
		
		
	} 
	public static int getTagLength(){
		return TAGS.length;
		
		
		
	}

}
