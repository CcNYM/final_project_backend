package com.training.backend.utils;

import com.training.backend.dao.RealTimeStockMapper;
import com.training.backend.dao.StockDetailsMapper;
import com.training.backend.entity.RealTimeStock;
import com.training.backend.entity.StockDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;

@Component
public class TimedDataInsertionTask {
    @Autowired
    private RealTimeStockMapper realTimeStockMapper;

    @Autowired
    private StockDetailsMapper stockDetailsMapper;

    @Autowired
    private static final Logger logger = LoggerFactory.getLogger(TimedDataInsertionTask.class);

    @Autowired
    //线程池
    private ThreadPoolExecutor executor;

    private Map<Integer, String> getStockNameMap(){

        HashMap<Integer,String> stockNameMap = new HashMap<>();
        stockNameMap.put(1,"MSFT");
        stockNameMap.put(2,"GOOG");
        stockNameMap.put(3,"AAPL");
        stockNameMap.put(4,"YHOO");
        stockNameMap.put(5,"ORCL");
        stockNameMap.put(6,"DELL");
        stockNameMap.put(7,"IBM");
        stockNameMap.put(8,"EPAY");
        stockNameMap.put(9,"INTC");
        stockNameMap.put(10,"AMZN");
        stockNameMap.put(11,"HPQ");
        stockNameMap.put(12,"QCOM");
        stockNameMap.put(13,"ERIC");
        stockNameMap.put(14,"CSCO");
        stockNameMap.put(15,"MOT");
        stockNameMap.put(16,"XRX");
        stockNameMap.put(17,"VOD");
        stockNameMap.put(18,"TXN");
        stockNameMap.put(19,"ADS");
        stockNameMap.put(20,"NOK");

        return stockNameMap;
    }

    @Scheduled(cron = "0/10 * * * * ?") //每隔20秒执行一次
    public void  addList(){



        logger.debug("task has been triggered");

        Map<Integer,String> stockNameMap = getStockNameMap();

        Thread thread = new Thread(() -> {

            List<StockDetails> stockDetailsList = new ArrayList<>();

            //add 20 stocks
            for(int stockId = 1; stockId <= 20; stockId++) {

                BigDecimal currentPrice = BigDecimal.valueOf(Math.random()*100);

                if ( realTimeStockMapper.selectRealTimeStockByStockId(stockId) == null){

                    while (currentPrice.compareTo(BigDecimal.valueOf(30)) < 0)
                        currentPrice = BigDecimal.valueOf(Math.random()*100);


                    RealTimeStock realTimeStock = new RealTimeStock();
                    realTimeStock.setStockId(stockId);
                    realTimeStock.setStockName(stockNameMap.get(stockId));
                    realTimeStock.setCurrentPrice(currentPrice);
                    realTimeStock.setFluctuation(BigDecimal.valueOf(0));
                    realTimeStockMapper.insertRealTimeStock(realTimeStock);
                }
                else {
                    BigDecimal oldPrice = realTimeStockMapper.selectRealTimeStockByStockId(stockId).getCurrentPrice();

                    if (currentPrice.compareTo(BigDecimal.valueOf(30))>0) {
                        while (currentPrice.subtract(oldPrice).abs().divide(oldPrice, 24, RoundingMode.HALF_UP).compareTo(BigDecimal.valueOf(0.30)) > 0) {
                            currentPrice = BigDecimal.valueOf(Math.random() * 100);
                        }
                    } else if (currentPrice.compareTo(BigDecimal.valueOf(30)) <= 0 && currentPrice.compareTo(BigDecimal.valueOf(10))>0) {
                        while (currentPrice.subtract(oldPrice).abs().divide(oldPrice, 24, RoundingMode.HALF_UP).compareTo(BigDecimal.valueOf(0.40)) > 0) {
                            currentPrice = BigDecimal.valueOf(Math.random() * 100);
                        }
                    } else {
                        while (currentPrice.compareTo(oldPrice) < 0 || currentPrice.subtract(oldPrice).abs().divide(oldPrice, 24, RoundingMode.HALF_UP).compareTo(BigDecimal.valueOf(0.60)) > 0 ) {
                            currentPrice = BigDecimal.valueOf(Math.random() * 100);
                        }
                    }

                    if(currentPrice.compareTo(BigDecimal.valueOf(1))<0){
                        currentPrice = BigDecimal.valueOf(1);
                    }

                    realTimeStockMapper.updatePriceByStockId(stockId,currentPrice);
                    realTimeStockMapper.updateFluctuationByStockId(stockId,(currentPrice.subtract(oldPrice)).divide(oldPrice, 24,RoundingMode.HALF_UP).setScale(2,RoundingMode.HALF_UP));
                }

                StockDetails stockDetails = new StockDetails();
                stockDetails.setStockName(stockNameMap.get(stockId));
                stockDetails.setStockId(stockId);
                stockDetails.setTime(new Date());
                stockDetails.setPrice(currentPrice);
                stockDetailsList.add(stockDetails);
               }

            stockDetailsMapper.insertBatchStocks(stockDetailsList);

            });


        try {
            executor.execute(thread);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

    }
}



