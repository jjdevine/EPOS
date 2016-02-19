package jjdevine.epos.beans;

import java.util.Date;
import java.util.List;

import jjdevine.epos.discountdefintion.data.svc.DiscountDefinitionDataService;
import jjdevine.epos.discountdefintion.model.beans.DiscountPolicy;

public class ActiveDiscountPolicies 
{
	private List<DiscountPolicy> discountPolicies;
	private DiscountDefinitionDataService discountDefinitionDataService;
	
	public void refresh()
	{
		discountPolicies = discountDefinitionDataService.getActiveDiscountPoliciesForDate(new Date());
	}

	public List<DiscountPolicy> getDiscountPolicies() {
		return discountPolicies;
	}

	public void setDiscountDefinitionDataService(
			DiscountDefinitionDataService discountDefinitionDataService) {
		this.discountDefinitionDataService = discountDefinitionDataService;
	}
	
	//TODO: method to calculate discounts application to transaction
	
    /**
     * convert to string.
     * @return string value of this object
     */
    public String toString() {
       StringBuffer buff = new StringBuffer();

        buff.append("<discountPolicies=" + discountPolicies + ">");
        buff.append("<discountDefinitionDataService=" + discountDefinitionDataService + ">");

        return buff.toString();
    }

}
