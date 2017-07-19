package ca.verax.kafka;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;

/**
 * Created by gamer on 7/13/2017.
 */
public class StockQuote {
    // "lastTradeDate", "open", "high", "low", "close", "volume"
    private String exchange;
    private String ticker;
    private Timestamp lastTradeDate;
    private float open;
    private float high;
    private float low;
    private float close;
    private float volume;

    public Timestamp getLastTradeDate() {
        return lastTradeDate;
    }

    public void setLastTradeDate(Timestamp lastTradeDate) {
        this.lastTradeDate = lastTradeDate;
    }

    public float getOpen() {
        return open;
    }

    public void setOpen(float open) {
        this.open = open;
    }

    public float getHigh() {
        return high;
    }

    public void setHigh(float high) {
        this.high = high;
    }

    public float getLow() {
        return low;
    }

    public void setLow(float low) {
        this.low = low;
    }

    public float getClose() {
        return close;
    }

    public void setClose(float close) {
        this.close = close;
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public static String GetStockFeeds(String stockSymbol, String stockExchange, String period, String stockIntervalInSeconds) {
        String results = "";

        try {
            String baseUrl = "https://www.google.com/finance/getprices?";
            String symbol = "q=" + stockSymbol;
            String exchange = "x=" + stockExchange;
            String days = "p=" + period;
            String seconds = "i=" + stockIntervalInSeconds;
            String extra = "f=d,o,h,l,c,v";
            String finalURL = String.format("%s%s&%s&%s&%s&%s", new Object[]{baseUrl, symbol, exchange, days, seconds, extra});
            URL url = new URL(finalURL);
            URLConnection urlConn = url.openConnection();
            InputStreamReader inputStreamReader = new InputStreamReader(urlConn.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String line;
            for(int _i = 0; (line = bufferedReader.readLine()) != null; ++_i) {
                if(_i > 7) {
                    results = results + line + "|";
                }
            }

            bufferedReader.close();
            inputStreamReader.close();
        } catch (Exception var19) {
            var19.printStackTrace();
        }

        return results;
    }
}
