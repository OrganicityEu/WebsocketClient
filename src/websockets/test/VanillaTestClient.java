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
		wsc= new WebsocketClientStomp(this, "ws://localhost:8080/orion");
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
				
				now = System.currentTimeMillis();
			}
		}
	}
	

	@Override
	public void messageReceived(String mesg) {
		System.out.println("Received message in Processing "+mesg);
	}
	
	
	
	public static void main(String[] args){
		System.out.println("Starting Vanilla test client");
		VanillaTestClient client = new VanillaTestClient();
	}

}
