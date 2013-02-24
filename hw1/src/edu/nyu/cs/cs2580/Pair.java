package edu.nyu.cs.cs2580;

public class Pair<L,R> {
    private L l;
    private R r;
    public Pair(L l, R r){
        this.l = l;
        this.r = r;
    }
    public L getL(){ return l; }
    public R getR(){ return r; }
    public void setL(L l){ this.l = l; }
    @Override
	public String toString() {
		return "Pair [l=" + l + ", r=" + r + "]";
	}
	public void setR(R r){ this.r = r; }
}
