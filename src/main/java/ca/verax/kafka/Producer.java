package ca.verax.kafka;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;

import java.sql.Timestamp;

import java.util.*;

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
		String interval = args[1];
    	String period = args[2];
		String filePath = args[3];

		BufferedReader br = new BufferedReader(new FileReader(filePath));
    	int limit = Integer.parseInt(args[4]);
		String fileLine = br.readLine();
		while (fileLine != null) {
			StringTokenizer symbolTokenizer = new StringTokenizer(fileLine, ",");
			String exchange = (String)symbolTokenizer.nextToken();
			String symbol = (String)symbolTokenizer.nextToken();
			String stream = null;
			try {
				stream = StockQuote.GetStockFeeds(symbol, exchange, period+"d", interval);
			} catch (Exception e) {
				// TODO Auto-generated catch block				
			}				    	
			try {
	            //StringTokenizer recordTokenizer = new StringTokenizer(_results, ",", false);

				Timestamp timestamp = new Timestamp(new Date().getTime());
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(timestamp.getTime());
				cal.add(Calendar.DAY_OF_MONTH, -Integer.parseInt(period));
				Timestamp startTS = new Timestamp(cal.getTime().getTime());
				System.out.println("Start Date = " + startTS);
				String lines[] = stream.split("\\|");
				System.out.println("Line size = " + lines.length );
				Timestamp tradeDate = startTS;
	    		for  (int i=0; i < limit; i++) {
					cal.setTimeInMillis(tradeDate.getTime());
					cal.add(Calendar.SECOND, Integer.parseInt(interval));
					tradeDate = new Timestamp(cal.getTime().getTime());
	    			String jasonString = getStockQuoteAsJason(exchange,symbol,lines[i],tradeDate );
	    			System.out.println("Json String = " + jasonString + "\n");
	    			ProducerRecord record = new ProducerRecord("stock-quotes",jasonString);
	                producer.send(record);
	    		}             
	        } catch (Exception e) {
	            e.printStackTrace();
	        } finally {
	            //producer.close();
	        }
			fileLine = br.readLine();
			Thread.sleep(60000);
		} 
    }

	private static String getStockQuoteAsJason(String exchange, String symbol, String symbolStream, Timestamp tradeDate) throws JsonProcessingException, JsonGenerationException, JsonMappingException {
		StringTokenizer attributeTokenizer = new StringTokenizer(symbolStream, ",");
		String attributeNames[] = {"lastTradeDate", "open", "high", "low", "close", "volume"};
		int i=0;
		StockQuote qt = new StockQuote();
		qt.setTicker(symbol);
		qt.setExchange(exchange);
		while (attributeTokenizer.hasMoreTokens()) {
			String attributeValue = (String)attributeTokenizer.nextElement();
			String attributeName = attributeNames[i];
			switch(attributeName) {
				case "lastTradeDate" :
					qt.setLastTradeDate(tradeDate);
					break;
				case "open" :
					qt.setOpen(Float.parseFloat(attributeValue));
					break;
				case "high" :
					qt.setHigh(Float.parseFloat(attributeValue));
					break;
				case "low" :
					qt.setLow(Float.parseFloat(attributeValue));
					break;
				case "close" :
					qt.setClose(Float.parseFloat(attributeValue));
					break;
				case "volume" :
					qt.setVolume(Float.parseFloat(attributeValue));
					break;
				default :
			}
			i++;
		} 
		String mapAsJson = new ObjectMapper().writeValueAsString(qt);
		
		return mapAsJson;
	}
}
