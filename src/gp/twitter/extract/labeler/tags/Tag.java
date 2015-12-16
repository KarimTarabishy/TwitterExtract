package gp.twitter.extract.labeler.tags;

public class Tag {
	private int id;
    private String symbol;

    Tag(int id, String Symbol){
        this.id = id;
        this.symbol = symbol;
	}
    public int getId(){
        return id;
    }

    public String getSymbol()
    {
        return symbol;
    }
}
