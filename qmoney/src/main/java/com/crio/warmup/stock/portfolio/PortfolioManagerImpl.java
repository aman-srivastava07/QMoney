
package com.crio.warmup.stock.portfolio;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerImpl implements PortfolioManager {




  private RestTemplate restTemplate;

  // Caution: Do not delete or modify the constructor, or else your build will break!
  // This is absolutely necessary for backward compatibility
  protected PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }


  //TODO: CRIO_TASK_MODULE_REFACTOR
  // 1. Now we want to convert our code into a module, so we will not call it from main anymore.
  //    Copy your code from Module#3 PortfolioManagerApplication#calculateAnnualizedReturn
  //    into #calculateAnnualizedReturn function here and ensure it follows the method signature.
  // 2. Logic to read Json file and convert them into Objects will not be required further as our
  //    clients will take care of it, going forward.

  // Note:
  // Make sure to exercise the tests inside PortfolioManagerTest using command below:
  // ./gradlew test --tests PortfolioManagerTest

  //CHECKSTYLE:OFF






  private Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }

  //CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Extract the logic to call Tiingo third-party APIs to a separate function.
  //  Remember to fill out the buildUri function and use that.


  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException 
    {

      if(from.compareTo(to) >= 0)
      {
        throw new RuntimeException();
      }
      String url = buildUri(symbol,from,to);
      TiingoCandle[] stock_data = restTemplate.getForObject(url, TiingoCandle[].class);
      //To prevent my application from crashing
      if(stock_data == null)
      {
        //return new List<Candle>();                throw comile time error
        return new ArrayList<Candle>();
      }
      else
      {
        List<Candle> stock_details = Arrays.asList(stock_data);
        return stock_details;
      }
      
  }

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) 
  {

    final String token = "e760dfcb2bd8fd975dc9b8c2d78ad691144951c0";
    
    String uriTemplate = "https:api.tiingo.com/tiingo/daily/$SYMBOL/prices?"
            + "startDate=$STARTDATE&endDate=$ENDDATE&token=$APIKEY";
    
    
    String url = uriTemplate.replace("$SYMBOL", symbol).replace("$STARTDATE", startDate.toString()).replace("$ENDDATE",endDate.toString()).replace("$APIKEY",token);
    
    return url;
  }

  @Override    // public-> must
  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades,
  LocalDate endDate) throws JsonProcessingException
  {

    List<AnnualizedReturn> annualizedReturn_list = new ArrayList<AnnualizedReturn>();

    for(int i=0; i<portfolioTrades.size();i++)
    {
      AnnualizedReturn a_r = getAnnualizedReturn(portfolioTrades.get(i),endDate);
      annualizedReturn_list.add(a_r);
    }
    Comparator<AnnualizedReturn> sort_technique = getComparator();
    Collections.sort(annualizedReturn_list,sort_technique);
    return annualizedReturn_list;
  }

  

  private  AnnualizedReturn getAnnualizedReturn(PortfolioTrade portfolioTrade,
      LocalDate endDate) throws JsonProcessingException 
      {
        //  if(portfolioTrade.getPurchaseDate().compareTo(endDate) >= 0)
        //     throw new RuntimeException();
         
        
        //  final String token = "e760dfcb2bd8fd975dc9b8c2d78ad691144951c0";
        //  String url = prepareUrl(portfolioTrade, endDate, token);
        //  RestTemplate restTemplate = new RestTemplate();
        //  TiingoCandle[] stocks_from_starts_to_end_date = restTemplate.getForObject(url, TiingoCandle[].class);

        //  if(stocks_from_starts_to_end_date != null)
        //  {
        //     TiingoCandle stock_start_date = stocks_from_starts_to_end_date[0];
        //     TiingoCandle stock_end_date = stocks_from_starts_to_end_date[stocks_from_starts_to_end_date.length -1];

        //     Double buying_price = stock_start_date.getOpen();
        //     Double selling_price = stock_end_date.getClose();

        //     AnnualizedReturn ar =  calculateAnnualizedReturns(endDate, portfolioTrade , buying_price, selling_price);
        //     return ar;
        //  }
        //  else
        //  {
        //     return new AnnualizedReturn(portfolioTrade.getSymbol(), Double.NaN, Double.NaN);
        //  }
        try
        {
          List<Candle> stocks_from_start_to_end_date = getStockQuote(portfolioTrade.getSymbol(),portfolioTrade.getPurchaseDate(),endDate);
        Double buyPrice = stocks_from_start_to_end_date.get(0).getOpen();
        Double sellPrice = stocks_from_start_to_end_date.get(stocks_from_start_to_end_date.size()-1).getClose();

        Double absolute_return, annualized_return;
        absolute_return = (sellPrice - buyPrice)/buyPrice;
        Long total_days = ChronoUnit.DAYS.between(portfolioTrade.getPurchaseDate(), endDate);
        Double total_years = (total_days)/365.0;
        annualized_return = Math.pow(absolute_return + 1, 1/total_years) - 1;
        return new AnnualizedReturn(portfolioTrade.getSymbol(), annualized_return, absolute_return);

        }
        catch(Exception e)
        {
          return new AnnualizedReturn(portfolioTrade.getSymbol(), Double.NaN, Double.NaN);
        }
      }




}
