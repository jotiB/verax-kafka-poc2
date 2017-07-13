package ca.verax.kafka;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import com.verax.feeds.StockQuote;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * This producer will send a bunch of messages to topic "fast-messages". Every so often,
 * it will send a message to "slow-messages". This shows how messages can be sent to
 * multiple topics. On the receiving end, we will see both kinds of messages but will
 * also see how the two topics aren't really synchronized.
 */
public class Producer {
    public static void main(String[] args) throws Exception {
        // set up the producer
    	KafkaProducer<String, String> producer;
        try (InputStream props = Resources.getResource("producer.props").openStream()) {
            Properties properties = new Properties();
            properties.load(props);
            producer = new KafkaProducer<>(properties);
        }
    	String exchange = args[3];
    	String period = args[2];
    	String interval = args[1];
    	StockQuote sQuote = new StockQuote();
    	StringTokenizer symbolTokenizer = new StringTokenizer(args[4], ",");    	
		while (symbolTokenizer.hasMoreTokens()) {
			StringBuffer sb = new StringBuffer();
			String symbol = (String)symbolTokenizer.nextElement();
			String stream = null;
			try {
				stream = sQuote.GetStockFeeds(symbol, exchange, period, interval);
			} catch (Exception e) {
				// TODO Auto-generated catch block				
			}				    	
			try {
	            //StringTokenizer recordTokenizer = new StringTokenizer(_results, ",", false);
	        	String lines[] = stream.split("\\|");	           
	    		for  (int i=0; i < lines.length; i++) {    			
	    			String jasonString = getStockQuoteAsJason(symbol,lines[i]);    
	    			System.out.println("Json String = " + jasonString + "\n");
	    			ProducerRecord record = new ProducerRecord("poc-trx-2",jasonString);
	                producer.send(record);
	    		}             
	        } catch (Exception e) {
	            e.printStackTrace();
	        } finally {
	            //producer.close();
	        }
		} 
    }

	private static String getStockQuoteAsJason(String symbol, String symbolStream) throws JsonProcessingException, JsonGenerationException, JsonMappingException {
		StringTokenizer attributeTokenizer = new StringTokenizer(symbolStream, ",");
		String attributeNames[] = {"tradeDate", "open", "high", "low", "close", "lastTradeValue"};
		int i=0;
		Map recordMap = new HashMap();
		recordMap.put("ticker", symbol);
		while (attributeTokenizer.hasMoreTokens()) {
			String attributeValue = (String)attributeTokenizer.nextElement();
			String attributeName = attributeNames[i];
			if (attributeName.equals("tradeDate")) {
				Timestamp attributeTSValue = new Timestamp(new Date().getTime());				
				recordMap.put(attributeName, attributeTSValue.toString());
			} else
			recordMap.put(attributeName, attributeValue);
			i++;
		} 
		String mapAsJson = new ObjectMapper().writeValueAsString(recordMap);
		
		return mapAsJson;
	}
}
