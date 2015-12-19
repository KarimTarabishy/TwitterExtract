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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tag tag = (Tag) o;

        return id == tag.id;

    }

    @Override
    public int hashCode() {
        return id;
    }
}
