package gp.twitter.extract.labeler.tags;

public interface Tags {

    Tag getTagBySymbol(String symbol);
    Tag getTagById(int id);
    Tag getStartTag();
    int getSize();

}
