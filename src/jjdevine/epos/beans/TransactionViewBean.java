package jjdevine.epos.beans;

import java.util.List;

public class TransactionViewBean 
{
	private List<TransactionItemViewBean> transactionItemViewBeans;
	private List<DiscountViewBean> discountViewBeans;
	private int itemsInTransaction;
	private double subtotal;
	
	public List<TransactionItemViewBean> getTransactionItemViewBeans() {
		return transactionItemViewBeans;
	}
	public void setTransactionItemViewBeans(
			List<TransactionItemViewBean> transactionItemViewBeans) {
		this.transactionItemViewBeans = transactionItemViewBeans;
	}
	public List<DiscountViewBean> getDiscountViewBeans() {
		return discountViewBeans;
	}
	public void setDiscountViewBeans(List<DiscountViewBean> discountViewBeans) {
		this.discountViewBeans = discountViewBeans;
	}
	public int getItemsInTransaction() {
		return itemsInTransaction;
	}
	public void setItemsInTransaction(int itemsInTransaction) {
		this.itemsInTransaction = itemsInTransaction;
	}
	public double getSubtotal() {
		return subtotal;
	}
	public void setSubtotal(double subtotal) {
		this.subtotal = subtotal;
	}
	
    /**
     * convert to string.
     * @return string value of this object
     */
    public String toString() {
       StringBuffer buff = new StringBuffer();

        buff.append("<transactionItemViewBeans=" + transactionItemViewBeans + ">");
        buff.append("<discountViewBeans=" + discountViewBeans + ">");
        buff.append("<itemsInTransaction=" + itemsInTransaction + ">");
        buff.append("<subtotal=" + subtotal + ">");

        return buff.toString();
    }
}
