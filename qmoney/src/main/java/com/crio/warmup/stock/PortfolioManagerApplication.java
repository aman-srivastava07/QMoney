
package com.crio.warmup.stock;


import com.crio.warmup.stock.dto.*;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.crio.warmup.stock.portfolio.PortfolioManager;
import com.crio.warmup.stock.portfolio.PortfolioManagerFactory;
import com.crio.warmup.stock.portfolio.PortfolioManagerImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
//import java.io.Serializable;
import java.net.URISyntaxException;
//import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
//import java.util.Arrays;
//import java.net.URISyntaxException;
import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.time.LocalDate;
//import java.time.temporal.ChronoUnit;
//import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestTemplate;


public class PortfolioManagerApplication {

   // TODO: CRIO_TASK_MODULE_JSON_PARSING
  //  Task:
  //       - Read the json file provided in the argument[0], The file is available in the classpath.
  //       - Go through all of the trades in the given file,
  //       - Prepare the list of all symbols a portfolio has.
  //       - if "trades.json" has trades like
  //         [{ "symbol": "MSFT"}, { "symbol": "AAPL"}, { "symbol": "GOOGL"}]
  //         Then you should return ["MSFT", "AAPL", "GOOGL"]
  //  Hints:
  //    1. Go through two functions provided - #resolveFileFromResources() and #getObjectMapper
  //       Check if they are of any help to you.
  //    2. Return the list of all symbols in the same order as provided in json.

  //  Note:
  //  1. There can be few unused imports, you will need to fix them to make the build pass.
  //  2. You can use "./gradlew build" to check if your code builds successfully.

  public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException 
  {
    List<String> symbols_list = new ArrayList<String>();
    ObjectMapper mapper = getObjectMapper();
    //File sample_file = resolveFileFromResources("trades.json");
    File sample_file = resolveFileFromResources(args[0]);
    //JsonNode node = mapper.readTree(sample_file);
    // if(node.isObject())
    // {
    //   ObjectNode obj = (ObjectNode) node;
    //   if(obj.has("symbol"))
    //   {
    //      obj.get("symbol");
    //   }
    // }
    // for(int i=0; i<node.size();i++)
    // {
    //   symbols_list.add(node.get(i).get("symbol"));
    // }
    PortfolioTrade[] trades = mapper.readValue(sample_file, PortfolioTrade[].class);
    for(PortfolioTrade trade: trades)
    {
      symbols_list.add(trade.getSymbol());
    }
    return symbols_list;
    //return Collections.emptyList();
  }

  // From google sheet
  public static List<TotalReturnsDto> mainReadQuotesHelper(String[] args, List<PortfolioTrade> trades) throws IOException, URISyntaxException 
  {
   //final String token = "c00e24080fccf5b0325a78647f715942b27072d4";
   final String token = "e760dfcb2bd8fd975dc9b8c2d78ad691144951c0";
   //LocalDate endDate = LocalDate.parse(args[1]);             
   List<TotalReturnsDto> tests = new ArrayList<TotalReturnsDto>();
   RestTemplate restTemplate = new RestTemplate();
   for(PortfolioTrade t:trades)
   {
      
      String uri = prepareUrl(t,LocalDate.parse(args[1]),token);              
      //String uri = "https://api.tiingo.com/tiingo/daily/" + t.getSymbol() + "/prices?endDate=" + args[1] + "&token=c00e24080fccf5b0325a78647f715942b27072d4";  getting this from method 
      //TiingoCandle[] results = restTemplate.getForObject(uri, TiingoCandle[].class);   correct-01
      List<TiingoCandle> results = Arrays.asList(restTemplate.getForObject(uri, TiingoCandle[].class));
      if(results != null)
      //if(results.size() != 0)   // now this will not work (It'll fail one test case) as it is not correct way to check whether array is null or not
      {
         //tests.add(new TotalReturnsDto(t.getSymbol(), results[results.length -1].getClose()));  correct-02
         tests.add(new TotalReturnsDto(t.getSymbol(), results.get(results.size() -1).getClose()));
      }
   }
   return tests;
  }


  public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException 
  {
     //ObjectMapper objectMapper = getObjectMapper();   getting this from method 
     //File sample_file = resolveFileFromResources(args[0]); getting this from method
     //File sample_file = resolveFileFromResources("trades.json"); // for debugging
     //List<PortfolioTrade> trades = Arrays.asList(objectMapper.readValue(sample_file,PortfolioTrade[].class)); getting this from method
     
     List<PortfolioTrade> trades = readTradesFromJson(args[0]);     
     List<TotalReturnsDto> sortedByValue = mainReadQuotesHelper(args, trades);
     //Collections.sort(sortedByValue, new ClosingComparator()); compiler is not taking ClosingComparator()
     Collections.sort(sortedByValue, TotalReturnsDto.closingComparator);
     List<String> stocks = new ArrayList<String>();
     for(TotalReturnsDto trd: sortedByValue)
     {
      stocks.add(trd.getSymbol());
     }
     return stocks;
  }







  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  //  Now that you have the list of PortfolioTrade and their data, calculate annualized returns
  //  for the stocks provided in the Json.
  //  Use the function you just wrote #calculateAnnualizedReturns.
  //  Return the list of AnnualizedReturns sorted by annualizedReturns in descending order.

  // Note:
  // 1. You may need to copy relevant code from #mainReadQuotes to parse the Json.
  // 2. Remember to get the latest quotes from Tiingo API.

  private static void printJsonObject(Object object) throws IOException {
   Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
   ObjectMapper mapper = new ObjectMapper();
   logger.info(mapper.writeValueAsString(object));
 }

 private static File resolveFileFromResources(String filename) throws URISyntaxException {
   return Paths.get(
       Thread.currentThread().getContextClassLoader().getResource(filename).toURI()).toFile();
       //return Paths.get(filename).toFile();
 }

 private static ObjectMapper getObjectMapper() {
   ObjectMapper objectMapper = new ObjectMapper();
   objectMapper.registerModule(new JavaTimeModule());
   return objectMapper;
 }

 // TODO: CRIO_TASK_MODULE_JSON_PARSING
  //  Follow the instructions provided in the task documentation and fill up the correct values for
  //  the variables provided. First value is provided for your reference.
  //  A. Put a breakpoint on the first line inside mainReadFile() which says
  //    return Collections.emptyList();
  //  B. Then Debug the test #mainReadFile provided in PortfoliomanagerApplicationTest.java
  //  following the instructions to run the test.
  //  Once you are able to run the test, perform following tasks and record the output as a
  //  String in the function below.
  //  Use this link to see how to evaluate expressions -
  //  https://code.visualstudio.com/docs/editor/debugging#_data-inspection
  //  1. evaluate the value of "args[0]" and set the value
  //     to the variable named valueOfArgument0 (This is implemented for your reference.)
  //  2. In the same window, evaluate the value of expression below and set it
  //  to resultOfResolveFilePathArgs0
  //     expression ==> resolveFileFromResources(args[0])
  //  3. In the same window, evaluate the value of expression below and set it
  //  to toStringOfObjectMapper.
  //  You might see some garbage numbers in the output. Dont worry, its expected.
  //    expression ==> getObjectMapper().toString()
  //  4. Now Go to the debug window and open stack trace. Put the name of the function you see at
  //  second place from top to variable functionNameFromTestFileInStackTrace
  //  5. In the same window, you will see the line number of the function in the stack trace window.
  //  assign the same to lineNumberFromTestFileInStackTrace
  //  Once you are done with above, just run the corresponding test and
  //  make sure its working as expected. use below command to do the same.
  //  ./gradlew test --tests PortfolioManagerApplicationTest.testDebugValues

  public static List<String> debugOutputs() {

   String valueOfArgument0 = "trades.json";
   String resultOfResolveFilePathArgs0 = "/home/crio-user/workspace/amansrivastava-as4-ME_QMONEY_V2/qmoney/bin/main/trades.json";
   String toStringOfObjectMapper = "com.fasterxml.jackson.databind.ObjectMapper@6150c3ec";
   String functionNameFromTestFileInStackTrace = "mainReadFile()";
   String lineNumberFromTestFileInStackTrace = "29";


  return Arrays.asList(new String[]{valueOfArgument0, resultOfResolveFilePathArgs0,
      toStringOfObjectMapper, functionNameFromTestFileInStackTrace,
      lineNumberFromTestFileInStackTrace});
}

// Note:
  // Remember to confirm that you are getting same results for annualized returns as in Module 3.
  










  // TODO: CRIO_TASK_MODULE_REST_API
  //  Find out the closing price of each stock on the end_date and return the list
  //  of all symbols in ascending order by its close value on end date.

  // Note:
  // 1. You may have to register on Tiingo to get the api_token.
  // 2. Look at args parameter and the module instructions carefully.
  // 2. You can copy relevant code from #mainReadFile to parse the Json.
  // 3. Use RestTemplate#getForObject in order to call the API,
  //    and deserialize the results in List<Candle>

//   public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException 
//   {
//      return Collections.emptyList();
//   }

  // TODO:
  //  After refactor, make sure that the tests pass by using these two commands
  //  ./gradlew test --tests PortfolioManagerApplicationTest.readTradesFromJson
  //  ./gradlew test --tests PortfolioManagerApplicationTest.mainReadFile
  public static List<PortfolioTrade> readTradesFromJson(String filename) throws IOException, URISyntaxException 
  {
     ObjectMapper objectMapper = getObjectMapper();
     File sample_file = resolveFileFromResources(filename);
     List<PortfolioTrade> trades = Arrays.asList(objectMapper.readValue(sample_file,PortfolioTrade[].class));
     return trades;
  }


  // TODO:
  //  Build the Url using given parameters and use this function in your code to cann the API.
  public static String prepareUrl(PortfolioTrade trade, LocalDate endDate, String token) 
  {
   //LocalDate startDate = LocalDate.parse(trade.getPurchaseDate());
   String uri = "https://api.tiingo.com/tiingo/daily/" + trade.getSymbol() + "/prices?startDate=" + trade.getPurchaseDate() + "&endDate=" + endDate + "&token=" + token;
   return uri;
  }


 



  // TODO:
  //  Ensure all tests are passing using below command
  //  ./gradlew test --tests ModuleThreeRefactorTest
  static Double getOpeningPriceOnStartDate(List<Candle> candles) 
  {
     return candles.get(0).getOpen();
     //return 0.0;
  }


  public static Double getClosingPriceOnEndDate(List<Candle> candles) 
  {
   //TiingoCandle stock_end_date = stocks_from_starts_to_end_date[stocks_from_starts_to_end_date.length -1];
     //return candles[candles.size()-1].getClose();
     //return candles[candles.size()-1];
     return candles.get(candles.size()-1).getClose();
     //return 0.0;
  }


  public static List<Candle> fetchCandles(PortfolioTrade trade, LocalDate endDate, String token) 
  {
   List<Candle> lst = new ArrayList<>();
   //final String token = "c00e24080fccf5b0325a78647f715942b27072d4";
   //String url = prepareUrl(portfolioTrade, endDate, token);
   String url = prepareUrl(trade, endDate, token);
   RestTemplate restTemplate = new RestTemplate();
   TiingoCandle[] all_stocks = restTemplate.getForObject(url, TiingoCandle[].class);
   //lst.add(all_stocks);
   for(int i=0; i<all_stocks.length; i++)
   {
      lst.add(all_stocks[i]);
   }
   return lst;
  }

  public static List<AnnualizedReturn> mainCalculateSingleReturn(String[] args)
      throws IOException, URISyntaxException 
   {
      List<AnnualizedReturn> annualizedReturns = new ArrayList<AnnualizedReturn>();
      LocalDate endDate = LocalDate.parse(args[1]);
      File file = resolveFileFromResources(args[0]);
      ObjectMapper objectMapper = getObjectMapper();
      PortfolioTrade[] trades = objectMapper.readValue(file,PortfolioTrade[].class);
      for(int i=0; i<trades.length;i++)
      {
         //annualizedReturns.add(new AnnualizedReturn(symbol, annualizedReturn, totalReturns));
         annualizedReturns.add(getAnnualizedReturn(trades[i],endDate));
      }

      Comparator<AnnualizedReturn> tobeSorted = Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
      Collections.sort(annualizedReturns,tobeSorted);
      return annualizedReturns;

     //return Collections.emptyList();
   }

  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  //  Return the populated list of AnnualizedReturn for all stocks.
  //  Annualized returns should be calculated in two steps:
  //   1. Calculate totalReturn = (sell_value - buy_value) / buy_value.
  //      1.1 Store the same as totalReturns
  //   2. Calculate extrapolated annualized returns by scaling the same in years span.
  //      The formula is:
  //      annualized_returns = (1 + total_returns) ^ (1 / total_num_years) - 1
  //      2.1 Store the same as annualized_returns
  //  Test the same using below specified command. The build should be successful.
  //     ./gradlew test --tests PortfolioManagerApplicationTest.testCalculateAnnualizedReturn

  private static AnnualizedReturn getAnnualizedReturn(PortfolioTrade portfolioTrade,
      LocalDate endDate) 
      {
         if(portfolioTrade.getPurchaseDate().compareTo(endDate) >= 0)
            throw new RuntimeException();
         
         //final String token = "c00e24080fccf5b0325a78647f715942b27072d4";
         final String token = "e760dfcb2bd8fd975dc9b8c2d78ad691144951c0";
         String url = prepareUrl(portfolioTrade, endDate, token);
         RestTemplate restTemplate = new RestTemplate();
         TiingoCandle[] stocks_from_starts_to_end_date = restTemplate.getForObject(url, TiingoCandle[].class);

         if(stocks_from_starts_to_end_date != null)
         {
            TiingoCandle stock_start_date = stocks_from_starts_to_end_date[0];
            TiingoCandle stock_end_date = stocks_from_starts_to_end_date[stocks_from_starts_to_end_date.length -1];

            Double buying_price = stock_start_date.getOpen();
            Double selling_price = stock_end_date.getClose();

            AnnualizedReturn ar =  calculateAnnualizedReturns(endDate, portfolioTrade , buying_price, selling_price);
            return ar;
         }
         else
         {
            return new AnnualizedReturn(portfolioTrade.getSymbol(), Double.NaN, Double.NaN);
         }
         //return null;
      }
   
      
    

public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate,
      PortfolioTrade trade, Double buyPrice, Double sellPrice) 
      {
         Double absolute_return, annualized_return;
         absolute_return = (sellPrice - buyPrice)/buyPrice;
         Long total_days = ChronoUnit.DAYS.between(trade.getPurchaseDate(), endDate);
         Double total_years = (total_days)/365.0;
         annualized_return = Math.pow(absolute_return + 1, 1/total_years) - 1;
         return new AnnualizedReturn(trade.getSymbol(), annualized_return, absolute_return);
  }













  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Once you are done with the implementation inside PortfolioManagerImpl and
  //  PortfolioManagerFactory, create PortfolioManager using PortfolioManagerFactory.
  //  Refer to the code from previous modules to get the List<PortfolioTrades> and endDate, and
  //  call the newly implemented method in PortfolioManager to calculate the annualized returns.

  // Note:
  // Remember to confirm that you are getting same results for annualized returns as in Module 3.

  public static RestTemplate restTemplate = new RestTemplate();
  public static PortfolioManager portfolioManager = PortfolioManagerFactory.getPortfolioManager(restTemplate);
  public static List<AnnualizedReturn> mainCalculateReturnsAfterRefactor(String[] args)
      throws Exception {
       //String file = args[0];
   
       LocalDate endDate = LocalDate.parse(args[1]);
       //String contents = readFileAsString(file);
       ObjectMapper objectMapper = getObjectMapper();
       File file = resolveFileFromResources(args[0]);
       List<PortfolioTrade> trades_fac = Arrays.asList(objectMapper.readValue(file, PortfolioTrade[].class));
       return portfolioManager.calculateAnnualizedReturn(trades_fac, endDate);
  }




// private static String readFileAsString(String file) {
//    return file;   
// }

//public static void main(String[] args) throws Exception {















  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());


    //printJsonObject(mainReadQuotes(args));


    //printJsonObject(mainCalculateSingleReturn(args));
    printJsonObject(mainCalculateReturnsAfterRefactor(args));

     

  }

public static String getToken() {

   //return "c00e24080fccf5b0325a78647f715942b27072d4";
   return "e760dfcb2bd8fd975dc9b8c2d78ad691144951c0";
   //return null;
}


    
  


  }
//}

