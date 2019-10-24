package transferObject;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Staff implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String name, position;
	private int id, baseWage, subWage, baseEff, pob, insurance, imprest, holiday;
	private double dayOff;
	
	public Staff(int id, String name, String position){
		this.id = id;
		this.name = name;
		this.position = position;
		baseWage = 0; subWage = 0; baseEff = 0; 
		pob = 0; insurance = 0; imprest = 0;
		dayOff = 0;
		holiday = 0;
	}
	
	public Staff(ResultSet result) throws SQLException{
		id = result.getInt("id");
		name = result.getString("name");
		position = result.getString("position");
		baseWage = result.getInt("base_wage");
		subWage = result.getInt("sub_wage");
		baseEff = result.getInt("base_eff");
		pob = result.getInt("pob");
		insurance = result.getInt("insurance");
		imprest = result.getInt("imprest");
		dayOff = result.getDouble("day_off");
		holiday = result.getInt("holiday");
	}
	
	public int getHoliday(){
		return holiday;
	}
	
	public int getID(){
		return id;
	}
	
	public String getName(){
		return name;
	}
	
	public String getPosition(){
		return position;
	}
	
	public int getBaseWage(){
		return baseWage;
	}
	
	public int getSubWage(){
		return subWage;
	}
	
	public int getBaseEff(){
		return baseEff;
	}
	
	public int getPOB(){
		return pob;
	}
	
	public int getInsurance(){
		return insurance;
	}
	
	public int getImprest(){
		return imprest;
	}
	
	public double getDayOff(){
		return dayOff;
	}
	
	public void setStaffView(int baseWage, int subWage, int baseEff, int pob, int insurance){
		this.baseWage = baseWage;
		this.subWage = subWage;
		this.baseEff = baseEff;
		this.pob = pob;
		this.insurance = insurance;
	}
	
	public void setWageView(double dayOff, int imprest, int holiday){
		this.dayOff = dayOff;
		this.imprest = imprest;
		this.holiday = holiday;
	}
}
