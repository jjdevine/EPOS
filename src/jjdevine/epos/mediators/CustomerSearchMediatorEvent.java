package jjdevine.epos.mediators;

public class CustomerSearchMediatorEvent 
{
	public enum Type{CUSTOMER_SELECTED}
	
	private Type type;
	private int customerId;
	
	public Type getType() {
		return type;
	}
	public void setType(Type type) {
		this.type = type;
	}
	public int getCustomerId() {
		return customerId;
	}
	public void setCustomerId(int customerId) {
		this.customerId = customerId;
	}
	
    /**
     * convert to string.
     * @return string value of this object
     */
    public String toString() {
       StringBuffer buff = new StringBuffer();

        buff.append("<type=" + type + ">");
        buff.append("<customerId=" + customerId + ">");

        return buff.toString();
    }

}
