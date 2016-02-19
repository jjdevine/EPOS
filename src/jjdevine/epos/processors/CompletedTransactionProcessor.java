package jjdevine.epos.processors;

import jjdevine.epos.transaction.EposTransaction;

public interface CompletedTransactionProcessor
{
	public int processTransaction(EposTransaction transaction);
}
