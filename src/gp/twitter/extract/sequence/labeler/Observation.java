package gp.twitter.extract.sequence.labeler;

import gp.twitter.extract.util.SparseArray;

public class Observation {
	 private String value;
	 private Tag tag ;
	 private SparseArray feature ;
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public Tag getTags() {
		return tag;
	}
	public void setTags(Tag tag) {
		this.tag = tag;
	}
	public SparseArray getFeature() {
		return feature;
	}
	public void setFeature(SparseArray feature) {
		this.feature = feature;
	}
	 
	 


}
