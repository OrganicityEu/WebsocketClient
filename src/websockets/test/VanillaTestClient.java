package websockets.test;

import java.util.List;

import dk.alexandra.orion.websocket.transports.OrionSubscription;
import websockets.WebsocketClientStomp;
import websockets.handlers.WebsocketCallback;

public class VanillaTestClient implements WebsocketCallback{
	
	WebsocketClientStomp wsc;
	long now;
	boolean newEllipse;
	boolean hasSubscription = false;
	
	
	public VanillaTestClient(){
		now = System.currentTimeMillis();
		
		wsc= new WebsocketClientStomp(this, "ws://localhost:8081/orion");
		//running instance of Websocket middleware:
		//wsc= new WebsocketClientStomp(this, "ws://ec2-52-40-19-99.us-west-2.compute.amazonaws.com:8081/orion");
		startLoop();
	}
	
	private void startLoop(){
		while(true){
			if(System.currentTimeMillis()>now+5000){
				if(!hasSubscription){
					  //example of how to create a subscription
					  String[] attr = new String[1];
					  attr[0] = "temperature";
					  String[] cond = new String[1];
					  cond[0] = "pressure";
					  //reference: https://fiware-orion.readthedocs.io/en/develop/user/walkthrough_apiv2/index.html#subscriptions
					  String entityId = "urn:oc:entity:experimenters:cf2c1723-3369-4123-8b32-49abe71c0e57:5846db253be86fb0409329e8:11";
					  OrionSubscription subscription = new OrionSubscription(cond, attr, "P1D", entityId, false, "Room",null);
					  System.out.println("trying to set subscrition");
					  hasSubscription = wsc.registerSubscription(subscription);
					  if(hasSubscription){
						  System.out.println("subscription request sent");
					  }  
				  }else{
					  List<String> subscriptions = wsc.getSubscriptions();
					  if(subscriptions.size()>0){
						  /*
						  if(wsc.unregisterSubscription(subscriptions.get(0))){
							  hasSubscription=false;
							  
						  }
						  */
					  }
				  }
				
				now = System.currentTimeMillis();
			}
		}
	}
	

	@Override
	public void messageReceived(String mesg) {
		System.out.println("Received message in Vanilla: "+mesg);
	}
	
	
	
	public static void main(String[] args){
		System.out.println("Starting Vanilla test client");
		VanillaTestClient client = new VanillaTestClient();
	}

}
