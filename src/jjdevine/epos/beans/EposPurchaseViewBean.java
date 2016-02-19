package jjdevine.epos.beans;

public class EposPurchaseViewBean implements TransactionItemViewBean 
{
	public enum Mode{NORMAL, MEMBER, NORMAL_DISCOUNTED, MEMBER_DISCOUNTED, PURCHASE_VOID}
	
	private Mode mode;
	private String garmentCode;
	private String brand;
	private String style;
	private String colour;
	private String size;
	private String message;
	private double retailPrice;
	private double memberPrice;
	private double salePrice;
	private int transactionIndex;
	private boolean selected;
	
	public Mode getMode() {
		return mode;
	}
	public void setMode(Mode mode) {
		this.mode = mode;
	}
	public String getGarmentCode() {
		return garmentCode;
	}
	public void setGarmentCode(String garmentCode) {
		this.garmentCode = garmentCode;
	}
	public String getBrand() {
		return brand;
	}
	public void setBrand(String brand) {
		this.brand = brand;
	}
	public String getStyle() {
		return style;
	}
	public void setStyle(String style) {
		this.style = style;
	}
	public String getColour() {
		return colour;
	}
	public void setColour(String colour) {
		this.colour = colour;
	}
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public double getRetailPrice() {
		return retailPrice;
	}
	public void setRetailPrice(double retailPrice) {
		this.retailPrice = retailPrice;
	}
	public double getMemberPrice() {
		return memberPrice;
	}
	public void setMemberPrice(double memberPrice) {
		this.memberPrice = memberPrice;
	}
	public double getSalePrice() {
		return salePrice;
	}
	public void setSalePrice(double salePrice) {
		this.salePrice = salePrice;
	}
	public int getTransactionIndex() {
		return transactionIndex;
	}
	public void setTransactionIndex(int transactionIndex) {
		this.transactionIndex = transactionIndex;
	}
	
    public boolean isSelected() {
		return selected;
	}
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

    /**
     * convert to string.
     * @return string value of this object
     */
    public String toString() {
       StringBuffer buff = new StringBuffer();

        buff.append("<mode=" + mode + ">");
        buff.append("<garmentCode=" + garmentCode + ">");
        buff.append("<brand=" + brand + ">");
        buff.append("<style=" + style + ">");
        buff.append("<colour=" + colour + ">");
        buff.append("<size=" + size + ">");
        buff.append("<message=" + message + ">");
        buff.append("<retailPrice=" + retailPrice + ">");
        buff.append("<memberPrice=" + memberPrice + ">");
        buff.append("<salePrice=" + salePrice + ">");
        buff.append("<transactionIndex=" + transactionIndex + ">");
        buff.append("<selected=" + selected + ">");

        return buff.toString();
    }

}
