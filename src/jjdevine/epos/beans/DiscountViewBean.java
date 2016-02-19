package jjdevine.epos.beans;

public class DiscountViewBean 
{
	private int discountId = -1;
	private int discountPolicyId = -1;
	private String description;
	private double value;
	
	public int getDiscountId() {
		return discountId;
	}
	public void setDiscountId(int discountId) {
		this.discountId = discountId;
	}
	public int getDiscountPolicyId() {
		return discountPolicyId;
	}
	public void setDiscountPolicyId(int discountPolicyId) {
		this.discountPolicyId = discountPolicyId;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public double getValue() {
		return value;
	}
	public void setValue(double value) {
		this.value = value;
	}
	
    /**
     * convert to string.
     * @return string value of this object
     */
    public String toString() {
       StringBuffer buff = new StringBuffer();

        buff.append("<discountId=" + discountId + ">");
        buff.append("<discountPolicyId=" + discountPolicyId + ">");
        buff.append("<description=" + description + ">");
        buff.append("<value=" + value + ">");

        return buff.toString();
    }
}
