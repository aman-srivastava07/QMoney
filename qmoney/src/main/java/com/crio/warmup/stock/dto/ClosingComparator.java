package com.crio.warmup.stock.dto;
import java.util.Comparator;

public class ClosingComparator implements Comparator<AnnualizedReturn>
{

    @Override
    public int compare(AnnualizedReturn t1, AnnualizedReturn t2) 
    {
        // Data type of closing price is double
        return -1 * ((int) (t1.getAnnualizedReturn() - t2.getAnnualizedReturn()));  
    }
    
}
