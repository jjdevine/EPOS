package jjdevine.epos.events;

import jjdevine.epos.common.beans.Discount;

public class ModifyPurchasesEvent 
{
	public enum Type{DISCOUNT, VOID};
	
	private Type type;
	private Discount discount;
	private int[] affectedPurchases;
	
	public Type getType() {
		return type;
	}
	public void setType(Type type) {
		this.type = type;
	}
	public Discount getDiscount() {
		return discount;
	}
	public void setDiscount(Discount discount) {
		this.discount = discount;
	}
    public int[] getAffectedPurchases() {
		return affectedPurchases;
	}
	public void setAffectedPurchases(int[] affectedPurchases) {
		this.affectedPurchases = affectedPurchases;
	}
	/**
     * convert to string.
     * @return string value of this object
     */
    public String toString() {
       StringBuffer buff = new StringBuffer();

        buff.append("<type=" + type + ">");
        buff.append("<discount=" + discount + ">");
        buff.append("<affectedPurchases=" + affectedPurchases + ">");

        return buff.toString();
    }

}
