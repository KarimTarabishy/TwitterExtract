package gp.twitter.extract.labeler.sequence;

import gp.twitter.extract.labeler.tags.Tag;
import gp.twitter.extract.util.SparseArray;

import java.util.ArrayList;

public class Observation {
	private String value;
	private Tag tag ;
	private SparseArray current_transition_feature ;
	private ArrayList<SparseArray> all_transition_feature   ;
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public Tag getTag() {
		return tag;
	}
	public void setTag(Tag tag) {
		this.tag = tag;
	}
	public SparseArray getCurrentTransitionFeature() {
		return current_transition_feature;
	}
	public void setCurrentTransitionFeature(SparseArray feature) {
		this.current_transition_feature = feature;
	}
	public ArrayList<SparseArray> getAll_transition_feature() {
		return all_transition_feature;
	}
	public void setAll_transition_feature(
			ArrayList<SparseArray> all_transition_feature) {
		this.all_transition_feature = all_transition_feature;
	}
	 
	

}
