package boolean_formula_entities;

public enum RegionRelationship {
Overlap,NonOverLapNoMeet,NonOverLapButMeet,NotComparable,Contained,Contains;
	public String toString(){
		switch(this){
		case Overlap: return "OverLap";//actually should be overlap and complement
		case NonOverLapNoMeet: return "Non-OverLap NoMeet";
		case NonOverLapButMeet: return "NonOverLap ButMeet";
		case Contained: return "Contained";
		case Contains: return "Contains";
		default: return "NotComparable";
		}
	}
}
