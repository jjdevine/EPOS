package jjdevine.epos.utils;

import java.util.ArrayList;
import java.util.List;

import jjdevine.epos.common.EposUtils;
import jjdevine.epos.common.beans.Discount;
import jjdevine.epos.common.beans.EposPurchase;
import jjdevine.epos.common.beans.PolicyDiscount;
import jjdevine.epos.common.beans.EposPurchase.Mode;
import jjdevine.epos.discountdefintion.model.beans.DiscountDefinition;
import jjdevine.epos.discountdefintion.model.beans.DiscountPolicy;
import jjdevine.epos.transaction.EposTransaction;

public class DiscountUtils 
{
	public static Discount getDiscountForTransaction(EposTransaction transaction, DiscountPolicy discountPolicy)
	{
		List<Discount> applicableDiscounts = new ArrayList<Discount>();
		
		PolicyDiscount discount = null;
		
		for(DiscountDefinition discountDefinition: discountPolicy.getDiscountDefinitions())
		{
			if(isDiscountApplicableToTransaction(transaction, discountDefinition))
			{
				discount = new PolicyDiscount();
				discount.setDescription(discountDefinition.getDiscountName());
				discount.setDiscountDefinitionId(discountDefinition.getDiscountId());
				discount.setDiscountPolicyId(discountPolicy.getPolicyId());
				discount.setType(mapDiscountDefinitionTypeToDiscountType(discountDefinition.getDiscountType()));
				discount.setValue(discountDefinition.getDiscountValue());
				
				applicableDiscounts.add(discount);
			}
				
		}
			
		return getBestDiscount(transaction, applicableDiscounts);
	}

	public static Discount getDiscountForPurchase(EposPurchase eposPurchase, DiscountPolicy discountPolicy)
	{
		List<Discount> applicableDiscounts = new ArrayList<Discount>();
		
		PolicyDiscount discount = null;
		
		for(DiscountDefinition discountDefinition: discountPolicy.getDiscountDefinitions())
		{
			if(isDiscountApplicableToGarment(eposPurchase, discountDefinition))
			{
				discount = new PolicyDiscount();
				discount.setDescription(discountDefinition.getDiscountName());
				discount.setDiscountDefinitionId(discountDefinition.getDiscountId());
				discount.setDiscountPolicyId(discountPolicy.getPolicyId());
				discount.setType(mapDiscountDefinitionTypeToDiscountType(discountDefinition.getDiscountType()));
				discount.setValue(discountDefinition.getDiscountValue());
				
				applicableDiscounts.add(discount);
			}
		}

		return getBestDiscount(eposPurchase, applicableDiscounts);
	}
	
	/**
	 * returns true if discount definition is applicable to the provided EposPurchase
	 * @param eposPurchase
	 * @param discountDefinition
	 * @return
	 */
	public static boolean isDiscountApplicableToGarment(EposPurchase eposPurchase, DiscountDefinition discountDefinition)
	{
		switch(discountDefinition.getCriteriaField())
		{
			case ALL_GARMENTS:
				return true;
				
			case BRAND:
				switch (discountDefinition.getCriteriaType())
				{
					case EQUALS:
						return ((String)discountDefinition.getCriteriaValue()).equalsIgnoreCase(eposPurchase.getGarment().getBrand());
					case IN_LIST:
						return isValueInList((String)eposPurchase.getGarment().getBrand(), (String[])discountDefinition.getCriteriaValue());
					default:
						throw new IllegalArgumentException(discountDefinition.getCriteriaType() + " is not a valid critera type for the criteria" +
								" field " + discountDefinition.getCriteriaField());
				}
				
			case COLOUR:
				switch (discountDefinition.getCriteriaType())
				{
					case EQUALS:
						return ((String)discountDefinition.getCriteriaValue()).equalsIgnoreCase(eposPurchase.getGarment().getColourDesc());
					case IN_LIST:
						return isValueInList((String)eposPurchase.getGarment().getColourDesc(), (String[])discountDefinition.getCriteriaValue());
					default:
						throw new IllegalArgumentException(discountDefinition.getCriteriaType() + " is not a valid critera type for the criteria" +
								" field " + discountDefinition.getCriteriaField());
				}
				
			case CUST_IS_MEMBER:
				switch (discountDefinition.getCriteriaType())
				{
					case EQUALS:
						boolean member = (eposPurchase.getMode() == Mode.MEMBER || eposPurchase.getMode() == Mode.MEMBER_DISCOUNTED);
						boolean criteriaValue = (Boolean)discountDefinition.getCriteriaValue();
						return criteriaValue == member;
					default:
						throw new IllegalArgumentException(discountDefinition.getCriteriaType() + " is not a valid critera type for the criteria" +
								" field " + discountDefinition.getCriteriaField());
				}
				
			case GARMENT_CODE:
				switch (discountDefinition.getCriteriaType())
				{
					case EQUALS:
						return ((String)discountDefinition.getCriteriaValue()).equalsIgnoreCase(eposPurchase.getGarment().getGarmentCode());
					case IN_LIST:
						return isValueInList((String)eposPurchase.getGarment().getGarmentCode(), (String[])discountDefinition.getCriteriaValue());
					default:
						throw new IllegalArgumentException(discountDefinition.getCriteriaType() + " is not a valid critera type for the criteria" +
								" field " + discountDefinition.getCriteriaField());
				}
				
			case GENDER:
				switch (discountDefinition.getCriteriaType())
				{
					case EQUALS:
						return ((String)discountDefinition.getCriteriaValue()).equalsIgnoreCase(eposPurchase.getGarment().getGender());
					case IN_LIST:
						return isValueInList((String)eposPurchase.getGarment().getGender(), (String[])discountDefinition.getCriteriaValue());
					default:
						throw new IllegalArgumentException(discountDefinition.getCriteriaType() + " is not a valid critera type for the criteria" +
								" field " + discountDefinition.getCriteriaField());
				}
				
			case RETAIL_PRICE:
				switch (discountDefinition.getCriteriaType())
				{
					case GREATER_THAN:
						double criteriaValue1 = (Double)discountDefinition.getCriteriaValue();
						return eposPurchase.getGarment().getRetailPrice() > criteriaValue1; 
					case LESS_THAN:
						double criteriaValue2 = (Double)discountDefinition.getCriteriaValue();
						return eposPurchase.getGarment().getRetailPrice() < criteriaValue2; 
					case BETWEEN:
						Double[] prices = (Double[])discountDefinition.getCriteriaValue();
						double retailPrice = eposPurchase.getGarment().getRetailPrice();
						return (retailPrice >= prices[0] && retailPrice <= prices[1]);
					default:
						throw new IllegalArgumentException(discountDefinition.getCriteriaType() + " is not a valid critera type for the criteria" +
								" field " + discountDefinition.getCriteriaField());
				}
				
			case SEASON:
				switch (discountDefinition.getCriteriaType())
				{
					case EQUALS:
						return ((String)discountDefinition.getCriteriaValue()).equalsIgnoreCase(eposPurchase.getGarment().getSeason());
					case IN_LIST:
						return isValueInList((String)eposPurchase.getGarment().getSeason(), (String[])discountDefinition.getCriteriaValue());
					default:
						throw new IllegalArgumentException(discountDefinition.getCriteriaType() + " is not a valid critera type for the criteria" +
								" field " + discountDefinition.getCriteriaField());
				}
				
			case SIZE:
				switch (discountDefinition.getCriteriaType())
				{
					case EQUALS:
						return ((String)discountDefinition.getCriteriaValue()).equalsIgnoreCase(eposPurchase.getGarment().getSize1());
					case IN_LIST:
						return isValueInList((String)eposPurchase.getGarment().getSize1(), (String[])discountDefinition.getCriteriaValue());
					default:
						throw new IllegalArgumentException(discountDefinition.getCriteriaType() + " is not a valid critera type for the criteria" +
								" field " + discountDefinition.getCriteriaField());
				}
				
			case STYLE:
				switch (discountDefinition.getCriteriaType())
				{
					case EQUALS:
						return ((String)discountDefinition.getCriteriaValue()).equalsIgnoreCase(eposPurchase.getGarment().getStyle());
					case IN_LIST:
						return isValueInList((String)eposPurchase.getGarment().getStyle(), (String[])discountDefinition.getCriteriaValue());
					default:
						throw new IllegalArgumentException(discountDefinition.getCriteriaType() + " is not a valid critera type for the criteria" +
								" field " + discountDefinition.getCriteriaField());
				}
				
			case YEAR:
				switch (discountDefinition.getCriteriaType())
				{
					case EQUALS:
						return ((String)discountDefinition.getCriteriaValue()).equalsIgnoreCase(eposPurchase.getGarment().getYear());
					case IN_LIST:
						return isValueInList((String)eposPurchase.getGarment().getYear(), (String[])discountDefinition.getCriteriaValue());
					default:
						throw new IllegalArgumentException(discountDefinition.getCriteriaType() + " is not a valid critera type for the criteria" +
								" field " + discountDefinition.getCriteriaField());
				}
			default:
				return false;
		}
	}
	
	public static Discount.Type mapDiscountDefinitionTypeToDiscountType(DiscountDefinition.DiscountType discountType)
	{
		switch(discountType)
		{
			case FLAT_DISCOUNT:
				return Discount.Type.FLAT_DISCOUNT;
			case FLAT_PRICE:
				return Discount.Type.FLAT_PRICE;
			case PERCENTAGE:
				return Discount.Type.PERCENTAGE;
			default:
				throw new IllegalArgumentException(discountType + " is not a valid type of discount");
		}
	}
	
	private static boolean isValueInList(String value, String... list)
	{
		for(String string: list)
		{
			if(value.equalsIgnoreCase(string))
			{
				return true;
			}
		}
		
		return false;
	}
	
	public static Discount getBestDiscount(EposPurchase eposPurchase, List<Discount> discounts)
	{
		//store state so we can reset at end of method
		EposPurchase.Mode existingMode = eposPurchase.getMode();
		Discount existingDiscount = eposPurchase.getDiscount();
		
		double existingSalePrice = eposPurchase.getSalePrice();
		double lowestSalePrice = -1;
		Discount lowestSalePriceDiscount = null;
		
		
		for(Discount discount: discounts)
		{
			eposPurchase.setDiscount(discount);
			
			if(eposPurchase.getMode() == EposPurchase.Mode.NORMAL)
			{
				eposPurchase.setMode(EposPurchase.Mode.NORMAL_DISCOUNTED);
			}
			else if(eposPurchase.getMode() == EposPurchase.Mode.MEMBER)
			{
				eposPurchase.setMode(EposPurchase.Mode.MEMBER_DISCOUNTED);
			}
			double newSalePrice = eposPurchase.getSalePrice();
			
			if(newSalePrice < existingSalePrice)
			{
				if(lowestSalePriceDiscount == null || newSalePrice <lowestSalePrice)
				{
					//this discount is the best so far
					lowestSalePrice = newSalePrice;
					lowestSalePriceDiscount = discount;
				}
			}
		}
		
		//leave state of purchase as it was originally
		eposPurchase.setDiscount(existingDiscount);
		eposPurchase.setMode(existingMode);
		
		return lowestSalePriceDiscount;
	}
	
	/**
	 * returns best discount or null if there is no improvement on the existing discount applied to transaction
	 * @param transaction
	 * @param discounts
	 * @return
	 */
	public static Discount getBestDiscount(EposTransaction transaction, List<Discount> discounts) 
	{
		//store state so we can reset at end of method
		Discount existingDiscount = null;
		if(transaction.getDiscounts().size() > 0)
		{
			//for now assume only one discount allowed
			existingDiscount = transaction.getDiscounts().get(0);
		}
			
		double existingSubtotal = EposUtils.calculateSubtotal(transaction);
		double lowestSubtotal = -9999;
		Discount lowestSubtotalDiscount = null;
		
		for(Discount discount: discounts)
		{
			transaction.getDiscounts().clear();
			transaction.getDiscounts().add(discount);
			
			double newSubtotal = EposUtils.calculateSubtotal(transaction);
			
			if(newSubtotal < existingSubtotal)
			{
				if(lowestSubtotalDiscount == null || newSubtotal < lowestSubtotal)
				{
					//this discount is the best so far
					lowestSubtotal = newSubtotal;
					lowestSubtotalDiscount = discount;
				}
			}
		}
		
		transaction.getDiscounts().clear();
		if(existingDiscount != null)
		{
			transaction.getDiscounts().add(existingDiscount);
		}
		
		return lowestSubtotalDiscount;
	}
	
	public static boolean isDiscountApplicableToTransaction(EposTransaction transaction, DiscountDefinition discountDefinition)
	{
		//save processing by checking if criteria field is a transaction based one (as opposed to garment based)
		switch(discountDefinition.getCriteriaField())
		{
			case TOTAL_SPEND_EXCL_DISCOUNTS:
				break;
			case TOTAL_SPEND_INC_DISCOUNTS:
				break;
			default:
				return false;
		}
		
		double totalSpendIncDiscounts = 0;
		double totalSpendExclDiscounts = 0;
		
		for(EposPurchase purchase: transaction.getPurchases())
		{
			if(purchase.getMode() != Mode.PURCHASE_VOID)
			{
				totalSpendIncDiscounts += purchase.getSalePrice();
				
				if(purchase.getDiscount() == null)
				{
					totalSpendExclDiscounts += purchase.getSalePrice();
				}
			}
		}
		
		switch(discountDefinition.getCriteriaField())
		{
			case TOTAL_SPEND_EXCL_DISCOUNTS:
				switch(discountDefinition.getCriteriaType())
				{
					case GREATER_THAN:
						double greaterThanVal = (Double)discountDefinition.getCriteriaValue();
						return totalSpendExclDiscounts >= greaterThanVal ? true : false;		
					case LESS_THAN:
						double lessThanVal = (Double)discountDefinition.getCriteriaValue();
						return totalSpendExclDiscounts <= lessThanVal ? true : false;	
					case BETWEEN:
						Double[] testVals = (Double[])discountDefinition.getCriteriaValue();
						return totalSpendExclDiscounts >= testVals[0] && totalSpendExclDiscounts <= testVals[1] ? true : false;
				}
			case TOTAL_SPEND_INC_DISCOUNTS:
				switch(discountDefinition.getCriteriaType())
				{
					case GREATER_THAN:
						double greaterThanVal = (Double)discountDefinition.getCriteriaValue();
						return totalSpendExclDiscounts >= greaterThanVal ? true : false;		
					case LESS_THAN:
						double lessThanVal = (Double)discountDefinition.getCriteriaValue();
						return totalSpendExclDiscounts <= lessThanVal ? true : false;	
					case BETWEEN:
						Double[] testVals = (Double[])discountDefinition.getCriteriaValue();
						return totalSpendExclDiscounts >= testVals[0] && totalSpendExclDiscounts <= testVals[1] ? true : false;
				}
		}
		
		//a result should have been obtained by now, if not then the discount definition is invalid:
		throw new IllegalStateException(discountDefinition.getCriteriaType() + " is not a valid criteria type for a discount on " +
				discountDefinition.getCriteriaField());
		
	}
}
