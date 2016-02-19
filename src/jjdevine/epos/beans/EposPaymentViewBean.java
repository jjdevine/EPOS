package jjdevine.epos.beans;

public class EposPaymentViewBean implements TransactionItemViewBean 
{
	public enum Type{CASH, CARD, COUPON, CHEQUE, RETURNED_ITEM, CREDIT_NOTE}
	
	private Type type;
	private boolean cancelled;
	private double value;
	private int transactionIndex;
	
	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public void setTransactionIndex(int transactionIndex) {
		this.transactionIndex = transactionIndex;
	}
	public int getTransactionIndex() {
		return transactionIndex;
	}

    /**
     * convert to string.
     * @return string value of this object
     */
    public String toString() {
       StringBuffer buff = new StringBuffer();

        buff.append("<type=" + type + ">");
        buff.append("<cancelled=" + cancelled + ">");
        buff.append("<value=" + value + ">");
        buff.append("<transactionIndex=" + transactionIndex + ">");

        return buff.toString();
    }
}
