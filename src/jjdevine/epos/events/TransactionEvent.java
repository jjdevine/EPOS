package jjdevine.epos.events;

public class TransactionEvent 
{
	public enum Type
	{
		PAYMENT, 
		SELECT_ITEM, 
		CREDIT_NOTE,
		RETURN_ITEM,
		MANAGEMENT_MENU,
		LOG_OUT,
		MODIFY_ITEMS,
		CANCEL_PAYMENT
	}
	
	public enum PaymentMethod{CARD,CASH,CHEQUE,COUPON}
	
	private Type type;
	private PaymentMethod paymentMethod;
	private int transactionIndex = -1;
	private int[] transactionIndexArray;
	
	public Type getType() {
		return type;
	}
	public void setType(Type type) {
		this.type = type;
	}
	public PaymentMethod getPaymentMethod() {
		return paymentMethod;
	}
	public void setPaymentMethod(PaymentMethod paymentMethod) {
		this.paymentMethod = paymentMethod;
	}
    public int getTransactionIndex() {
		return transactionIndex;
	}
	public void setTransactionIndex(int transactionIndex) {
		this.transactionIndex = transactionIndex;
	}
    public int[] getTransactionIndexArray() {
		return transactionIndexArray;
	}
	public void setTransactionIndexArray(int[] transactionIndexArray) {
		this.transactionIndexArray = transactionIndexArray;
	}
	/**
     * convert to string.
     * @return string value of this object
     */
    public String toString() {
       StringBuffer buff = new StringBuffer();

        buff.append("<type=" + type + ">");
        buff.append("<paymentMethod=" + paymentMethod + ">");
        buff.append("<transactionIndex=" + transactionIndex + ">");
        buff.append("<transactionIndexArray=" + transactionIndexArray + ">");

        return buff.toString();
    }


}
