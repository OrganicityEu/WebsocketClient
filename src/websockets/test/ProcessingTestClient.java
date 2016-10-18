package websockets.test;

import java.util.List;

import dk.alexandra.orion.websocket.transports.OrionSubscription;
import processing.core.PApplet;
import websockets.WebsocketClientStomp;
import websockets.handlers.WebsocketCallback;

public class ProcessingTestClient extends PApplet implements WebsocketCallback{
	
	WebsocketClientStomp wsc;
	int now;
	boolean newEllipse;
	boolean hasSubscription = false;
	
	public void settings(){
		size(200,200);
	}

	public void setup(){
	  newEllipse=true;
	  
	  wsc= new WebsocketClientStomp(this, "ws://localhost:8080/orion");
	  now=millis();
	}
	

	public void draw(){
	  if(newEllipse){
	    ellipse(random(width),random(height),10,10);
	    newEllipse=false;
	  }
	    
	  if(millis()>now+5000){

		  
		  if(!hasSubscription){
			  //example of how to create a subscription
			  String[] attr = new String[1];
			  attr[0] = "temperature";
			  String[] cond = new String[1];
			  cond[0] = "pressure";
			  //reference: https://fiware-orion.readthedocs.io/en/develop/user/walkthrough_apiv2/index.html#subscriptions
			  OrionSubscription subscription = new OrionSubscription(cond, attr, "P1D", "Room1", false, "Room",null);
			  System.out.println("trying to set subscrition");
			  hasSubscription = wsc.registerSubscription(subscription);
			  if(hasSubscription){
				  System.out.println("subscription set");
			  }  
		  }else{
			  List<String> subscriptions = wsc.getSubscriptions();
			  if(subscriptions.size()>0){
				  if(wsc.unregisterSubscription(subscriptions.get(0))){
					  hasSubscription=false;
					  
				  }
			  }
		  }
		  
		  now=millis();
	  }
	}

	@Override
	public void messageReceived(String msg){
	 println("Received message in Processing "+msg);
	 newEllipse=true;
	}
	
	
	
	public static void main(String[] args){
		println("Starting Processing client");
		PApplet.main("websockets.test.ProcessingTestClient");
	}

}
