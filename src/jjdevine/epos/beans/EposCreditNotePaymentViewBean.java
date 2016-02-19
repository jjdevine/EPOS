package jjdevine.epos.beans;

public class EposCreditNotePaymentViewBean extends EposPaymentViewBean 
{
	private String reasonForIssue;
	private boolean isGiftVoucher; 

	public String getReasonForIssue() {
		return reasonForIssue;
	}

	public void setReasonForIssue(String reasonForIssue) {
		this.reasonForIssue = reasonForIssue;
	}
	
    public boolean isGiftVoucher() {
		return isGiftVoucher;
	}

	public void setGiftVoucher(boolean isGiftVoucher) {
		this.isGiftVoucher = isGiftVoucher;
	}

	/**
     * convert to string.
     * @return string value of this object
     */
    public String toString() {
        StringBuffer buff = new StringBuffer();

        buff.append(super.toString());
        buff.append("<reasonForIssue=" + reasonForIssue + ">");
        buff.append("<isGiftVoucher=" + isGiftVoucher + ">");

        return buff.toString();
    }

}
