package jjdevine.epos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jjdevine.epos.common.EposUtils;
import jjdevine.epos.creditnote.view.beans.CreditNoteReasonViewBean;
import jjdevine.epos.creditnote.view.beans.CreditNoteViewBean;
import jjdevine.epos.customerinfo.data.svc.beans.CustomerSearchCriteria;
import jjdevine.epos.customerinfo.view.beans.CustomerInfoViewBean;
import jjdevine.epos.customersearch.view.beans.CustomerSearchCriteriaViewBean;
import jjdevine.epos.customersearch.view.beans.CustomerSearchResultViewBean;
import jjdevine.epos.discountdefinition.view.beans.DiscountDefinitionViewBean;
import jjdevine.epos.discountdefinition.view.beans.DiscountPolicyDefinitionViewBean;
import jjdevine.epos.discountdefinition.view.beans.DiscountDefinitionViewBean.CriteriaField;
import jjdevine.epos.discountdefinition.view.beans.DiscountDefinitionViewBean.CriteriaType;
import jjdevine.epos.discountdefinition.view.beans.DiscountDefinitionViewBean.DiscountType;
import jjdevine.epos.discountdefintion.model.beans.CreditNote;
import jjdevine.epos.discountdefintion.model.beans.CreditNoteReason;
import jjdevine.epos.discountdefintion.model.beans.CustomerInfo;
import jjdevine.epos.discountdefintion.model.beans.DiscountDefinition;
import jjdevine.epos.discountdefintion.model.beans.DiscountPolicy;
import jjdevine.epos.discountdefintion.model.beans.CreditNote.PaymentType;

public class BeanCopyUtils 
{
	private static Map<DiscountDefinitionViewBean.CriteriaField, DiscountDefinition.CriteriaField> criteriaFieldMap;
	private static Map<DiscountDefinitionViewBean.CriteriaType, DiscountDefinition.CriteriaType> criteriaTypeMap;
	private static Map<DiscountDefinitionViewBean.DiscountType, DiscountDefinition.DiscountType> discountTypeMap;
	
	static
	{
		criteriaFieldMap = new HashMap<DiscountDefinitionViewBean.CriteriaField, DiscountDefinition.CriteriaField>();
		criteriaFieldMap.put(DiscountDefinitionViewBean.CriteriaField.ALL_GARMENTS, DiscountDefinition.CriteriaField.ALL_GARMENTS);
		criteriaFieldMap.put(DiscountDefinitionViewBean.CriteriaField.BRAND, DiscountDefinition.CriteriaField.BRAND);
		criteriaFieldMap.put(DiscountDefinitionViewBean.CriteriaField.COLOUR, DiscountDefinition.CriteriaField.COLOUR);
		criteriaFieldMap.put(DiscountDefinitionViewBean.CriteriaField.CUST_IS_MEMBER, DiscountDefinition.CriteriaField.CUST_IS_MEMBER);
		criteriaFieldMap.put(DiscountDefinitionViewBean.CriteriaField.GARMENT_CODE, DiscountDefinition.CriteriaField.GARMENT_CODE);
		criteriaFieldMap.put(DiscountDefinitionViewBean.CriteriaField.GENDER, DiscountDefinition.CriteriaField.GENDER);
		criteriaFieldMap.put(DiscountDefinitionViewBean.CriteriaField.RETAIL_PRICE, DiscountDefinition.CriteriaField.RETAIL_PRICE);
		criteriaFieldMap.put(DiscountDefinitionViewBean.CriteriaField.SEASON, DiscountDefinition.CriteriaField.SEASON);
		criteriaFieldMap.put(DiscountDefinitionViewBean.CriteriaField.SIZE, DiscountDefinition.CriteriaField.SIZE);
		criteriaFieldMap.put(DiscountDefinitionViewBean.CriteriaField.STYLE, DiscountDefinition.CriteriaField.STYLE);
		criteriaFieldMap.put(DiscountDefinitionViewBean.CriteriaField.TOTAL_SPEND_EXCL_DISCOUNTS, DiscountDefinition.CriteriaField.TOTAL_SPEND_EXCL_DISCOUNTS);
		criteriaFieldMap.put(DiscountDefinitionViewBean.CriteriaField.TOTAL_SPEND_INC_DISCOUNTS, DiscountDefinition.CriteriaField.TOTAL_SPEND_INC_DISCOUNTS);
		criteriaFieldMap.put(DiscountDefinitionViewBean.CriteriaField.YEAR, DiscountDefinition.CriteriaField.YEAR);
		
		criteriaTypeMap = new HashMap<DiscountDefinitionViewBean.CriteriaType, DiscountDefinition.CriteriaType>();
		criteriaTypeMap.put(DiscountDefinitionViewBean.CriteriaType.BETWEEN, DiscountDefinition.CriteriaType.BETWEEN);
		criteriaTypeMap.put(DiscountDefinitionViewBean.CriteriaType.EQUALS, DiscountDefinition.CriteriaType.EQUALS);
		criteriaTypeMap.put(DiscountDefinitionViewBean.CriteriaType.GREATER_THAN, DiscountDefinition.CriteriaType.GREATER_THAN);
		criteriaTypeMap.put(DiscountDefinitionViewBean.CriteriaType.IN_LIST, DiscountDefinition.CriteriaType.IN_LIST);
		criteriaTypeMap.put(DiscountDefinitionViewBean.CriteriaType.LESS_THAN, DiscountDefinition.CriteriaType.LESS_THAN);
		
		discountTypeMap = new HashMap<DiscountDefinitionViewBean.DiscountType, DiscountDefinition.DiscountType>();
		discountTypeMap.put(DiscountDefinitionViewBean.DiscountType.PERCENTAGE, DiscountDefinition.DiscountType.PERCENTAGE);
		discountTypeMap.put(DiscountDefinitionViewBean.DiscountType.FLAT_DISCOUNT, DiscountDefinition.DiscountType.FLAT_DISCOUNT);
		discountTypeMap.put(DiscountDefinitionViewBean.DiscountType.FLAT_PRICE, DiscountDefinition.DiscountType.FLAT_PRICE);
	}
	
	public static DiscountDefinition copyDiscountDefinitionViewBeanToModel(DiscountDefinitionViewBean viewBean)
	{
		DiscountDefinition dto = new DiscountDefinition();
		
		dto.setDiscountId(viewBean.getId());
		dto.setDiscountName(viewBean.getDiscountName());
		dto.setCriteriaValue(viewBean.getCriteriaValue());
		dto.setDiscountValue(viewBean.getDiscountValue());
		dto.setCriteriaField(criteriaFieldMap.get(viewBean.getCriteriaField()));
		dto.setCriteriaType(criteriaTypeMap.get(viewBean.getCriteriaType()));
		dto.setDiscountType(discountTypeMap.get(viewBean.getDiscountType()));
		
		return dto;
	}
	
	public static DiscountDefinitionViewBean mapDiscountDefinitionToViewBean(DiscountDefinition discountDefinition)
	{
		DiscountDefinitionViewBean viewBean = new DiscountDefinitionViewBean();
		
		viewBean.setId(discountDefinition.getDiscountId());
		viewBean.setCriteriaValue(discountDefinition.getCriteriaValue());
		viewBean.setDiscountName(discountDefinition.getDiscountName());
		viewBean.setDiscountValue(discountDefinition.getDiscountValue());
		viewBean.setCriteriaField((CriteriaField)EposUtils.getKeyForObject(criteriaFieldMap, discountDefinition.getCriteriaField()));
		viewBean.setCriteriaType((CriteriaType)EposUtils.getKeyForObject(criteriaTypeMap, discountDefinition.getCriteriaType()));
		viewBean.setDiscountType((DiscountType)EposUtils.getKeyForObject(discountTypeMap, discountDefinition.getDiscountType()));
		
		return viewBean;
	}
	
	public static DiscountPolicy copyDiscountPolicyViewBeanToModel(DiscountPolicyDefinitionViewBean viewBean)
	{
		DiscountPolicy policy = new DiscountPolicy();
		
		policy.setPolicyId(viewBean.getId());
		policy.setEndDate(viewBean.getEndDate());
		policy.setStartDate(viewBean.getStartDate());
		policy.setPolicyName(viewBean.getPolicyName());
		policy.setEnabled(viewBean.isEnabled());
		
		List<DiscountDefinition> discounts = new ArrayList<DiscountDefinition>();
		
		for(DiscountDefinitionViewBean discountViewBean: viewBean.getDiscountsInPolicy())
		{
			discounts.add(copyDiscountDefinitionViewBeanToModel(discountViewBean));
		}
		
		policy.setDiscountDefinitions(discounts);
		
		return policy;
	}

	public static DiscountPolicyDefinitionViewBean mapDiscountPolicyToViewBean(DiscountPolicy discountPolicy) 
	{
		DiscountPolicyDefinitionViewBean viewBean = new DiscountPolicyDefinitionViewBean();
		
		viewBean.setId(discountPolicy.getPolicyId());
		viewBean.setPolicyName(discountPolicy.getPolicyName());
		viewBean.setStartDate(discountPolicy.getStartDate());
		viewBean.setEndDate(discountPolicy.getEndDate());
		viewBean.setEnabled(discountPolicy.isEnabled());
		
		List<DiscountDefinitionViewBean> discounts = new ArrayList<DiscountDefinitionViewBean>();
		
		for(DiscountDefinition definition: discountPolicy.getDiscountDefinitions())
		{
			discounts.add(mapDiscountDefinitionToViewBean(definition));
		}
		
		viewBean.setDiscountsInPolicy(discounts);
		
		return viewBean;
	}
	
	public static CustomerInfo copyCustomerInfoViewBeanToModel(CustomerInfoViewBean viewBean)
	{
		CustomerInfo custInfo = new CustomerInfo();
		
		custInfo.setCustomerId(viewBean.getCustomerId());
		custInfo.setFirstName(viewBean.getFirstName());
		custInfo.setLastName(viewBean.getLastName());
		custInfo.setEmailAddress(viewBean.getEmailAddress());
		custInfo.setTelNoHome(viewBean.getTelNoHome());
		custInfo.setTelNoMobile(viewBean.getTelNoMobile());
		custInfo.setTelNoWork(viewBean.getTelNoWork());
		custInfo.setPropertyNumber(viewBean.getHouseFlatNo());
		custInfo.setHouseName(viewBean.getHouseName());
		custInfo.setAddressLine1(viewBean.getAddressLine1());
		custInfo.setAddressLine2(viewBean.getAddressLine2());
		custInfo.setAddressLine3(viewBean.getAddressLine3());
		custInfo.setAddressLine4(viewBean.getAddressLine4());
		custInfo.setPostCode(viewBean.getPostCode());
		custInfo.setDobDay(viewBean.getDobDay());
		custInfo.setDobMonth(viewBean.getDobMonth());
		custInfo.setDobYear(viewBean.getDobYear());
		custInfo.setMember(viewBean.isMember());
		custInfo.setComments(viewBean.getComments());
	
		return custInfo;
	}
	
	public static CustomerInfoViewBean mapCustomerInfoToViewBean(CustomerInfo custInfo)
	{
		CustomerInfoViewBean viewBean = new CustomerInfoViewBean();
		
		viewBean.setCustomerId(custInfo.getCustomerId());
		viewBean.setFirstName(custInfo.getFirstName());
		viewBean.setLastName(custInfo.getLastName());
		viewBean.setEmailAddress(custInfo.getEmailAddress());
		viewBean.setTelNoHome(custInfo.getTelNoHome());
		viewBean.setTelNoMobile(custInfo.getTelNoMobile());
		viewBean.setTelNoWork(custInfo.getTelNoWork());
		viewBean.setHouseFlatNo(custInfo.getPropertyNumber());
		viewBean.setHouseName(custInfo.getHouseName());
		viewBean.setAddressLine1(custInfo.getAddressLine1());
		viewBean.setAddressLine2(custInfo.getAddressLine2());
		viewBean.setAddressLine3(custInfo.getAddressLine3());
		viewBean.setAddressLine4(custInfo.getAddressLine4());
		viewBean.setPostCode(custInfo.getPostCode());
		viewBean.setDobDay(custInfo.getDobDay());
		viewBean.setDobMonth(custInfo.getDobMonth());
		viewBean.setDobYear(custInfo.getDobYear());
		viewBean.setMember(custInfo.isMember());
		viewBean.setComments(custInfo.getComments());
		
		return viewBean;
	}
	
	public static CustomerSearchResultViewBean mapCustomerInfoToSearchResultViewBean(CustomerInfo custInfo)
	{
		CustomerSearchResultViewBean viewBean = new CustomerSearchResultViewBean();
		
		viewBean.setCustomerId(custInfo.getCustomerId());
		viewBean.setPostCode(custInfo.getPropertyNumber());
		viewBean.setPropertyNumber(custInfo.getPropertyNumber());
		viewBean.setName(custInfo.getFirstName() + " " + custInfo.getLastName());
		viewBean.setPostCode(custInfo.getPostCode());
		viewBean.setDobDay(custInfo.getDobDay());
		viewBean.setDobMonth(custInfo.getDobMonth());
		viewBean.setDobYear(custInfo.getDobYear());
		viewBean.setMember(custInfo.isMember());
		
		return viewBean;
	}
	
	public static CustomerSearchCriteria copyCustomerSearchCriteriaViewBeanToModel(CustomerSearchCriteriaViewBean viewBean)
	{
		CustomerSearchCriteria searchCriteria = new CustomerSearchCriteria();
		
		searchCriteria.setCustomerId(viewBean.getCustomerId());
		searchCriteria.setLastName(viewBean.getLastName());
		searchCriteria.setPostCode(viewBean.getPostCode());
		searchCriteria.setPropertyNumber(viewBean.getPropertyNumber());
		searchCriteria.setDobDay(viewBean.getDobDay());
		searchCriteria.setDobMonth(viewBean.getDobMonth());
		searchCriteria.setDobYear(viewBean.getDobYear());
		searchCriteria.setMember(viewBean.isMember());
		
		return searchCriteria;
	}

	public static CreditNote copyCreditNoteViewBeanToModel(CreditNoteViewBean viewBean) 
	{
		CreditNote creditNote = new CreditNote();

		creditNote.setId(viewBean.getId());
		creditNote.setIssueDate(viewBean.getIssueDate());
		creditNote.setNameIssuedTo(viewBean.getNameIssuedTo());
		creditNote.setTotalValue(viewBean.getTotalValue());
		creditNote.setValueRemaining(viewBean.getValueRemaining());
		
		if(viewBean.getPaymentType() != null)
		{
			switch(viewBean.getPaymentType())
			{
				case CARD:
					creditNote.setPaymentType(PaymentType.CARD);
					break;
				case CASH:
					creditNote.setPaymentType(PaymentType.CASH);
					break;
				case CHEQUE:
					creditNote.setPaymentType(PaymentType.CHEQUE);
					break;
			}
		}
		
		CreditNoteReason creditNoteReason = new CreditNoteReason();
		creditNoteReason.setId(viewBean.getReasonForIssue().getId());
		creditNoteReason.setDescription(viewBean.getReasonForIssue().getDescription());
		
		creditNote.setReasonForIssue(creditNoteReason);
		
		return creditNote;
	}
	
	public static CreditNoteReasonViewBean mapCreditNoteReasonToViewBean(CreditNoteReason creditNoteReason) 
	{
		CreditNoteReasonViewBean viewBean = new CreditNoteReasonViewBean();
		
		viewBean.setId(creditNoteReason.getId());
		viewBean.setDescription(creditNoteReason.getDescription());
		
		return viewBean;
	}
}
