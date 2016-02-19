package jjdevine.epos.beans;

public class EposPaymentReturnedItemViewBean extends EposPaymentViewBean 
{
	private String garmentCode;
	private String brand;
	private String style;
	private String colour;
	private String size;
	
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
	
    /**
     * convert to string.
     * @return string value of this object
     */
    public String toString() {
       StringBuffer buff = new StringBuffer();
       
        buff.append(super.toString());
        buff.append("<garmentCode=" + garmentCode + ">");
        buff.append("<brand=" + brand + ">");
        buff.append("<style=" + style + ">");
        buff.append("<colour=" + colour + ">");
        buff.append("<size=" + size + ">");

        return buff.toString();
    }

}
