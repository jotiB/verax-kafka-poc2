package ca.verax.kafka;

import java.io.IOException;

/**
 * Pick whether we want to run as producer or consumer. This lets us
 * have a single executable as a build target.
 */
public class Run {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            throw new IllegalArgumentException("Must have either 'producer' or 'consumer' as argument");
        }        
          
        switch (args[0]) {
            case "producer":
            	if (args.length < 5) {
                     throw new IllegalArgumentException("Producer must specify interval(seconds), period(days), symbol list, limit");
                 }
            	try {	    	  
         	    	   Integer.parseInt(args[1]);
         	       }
         	    catch (NumberFormatException nfe){
         	    	   throw new IllegalArgumentException("Interval must be a number (seconds)");
         	    }
                try {
                    Integer.parseInt(args[4]);
                }
                catch (NumberFormatException nfe){
                    throw new IllegalArgumentException("Interval must be a number (seconds)");
                }
            	
            	Producer.main(args);
                break;
            case "consumer":
                Consumer.main(args);
                break;
            default:
                throw new IllegalArgumentException("Don't know how to do " + args[0]);
        }
    }
}
