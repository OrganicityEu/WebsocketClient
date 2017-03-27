package websockets.test;

import java.util.List;

import dk.alexandra.orion.websocket.transports.OrionSubscription;
import websockets.WebsocketClientStomp;
import websockets.handlers.WebsocketCallback;

public class VanillaTestClient implements WebsocketCallback{
	
	WebsocketClientStomp wsc;
	long now;
	boolean hasSubscription = false;
	
	
	public VanillaTestClient(){
		now = System.currentTimeMillis();
		
		wsc= new WebsocketClientStomp(this, "ws://localhost:8090/orion");
		//running instance of Websocket middleware:
		//wsc= new WebsocketClientStomp(this, "ws://31.200.243.76:8090/orion");
		
		startLoop();
	}
	int count = 0;
	private void startLoop(){
		while(true){
			if(System.currentTimeMillis()>now+5000){
				if(!hasSubscription){
					  //example of how to create a subscription
					  String[] attr = new String[1];
					  attr[0] = "comments";
					  String[] cond = new String[1];
					  cond[0] = "comments";
					  //reference: https://fiware-orion.readthedocs.io/en/develop/user/walkthrough_apiv2/index.html#subscriptions
					  String entityId = "urn:oc:entity:experimenters:5a660d96-0ef7-42ca-9f6c-5dbb86d6aa20:58ab32f36f8b513746565c54:wsasset";
					  //Look there to acquire token: https://github.com/OrganicityEu/organicityeu.github.io/blob/mkdocs/docs/HowToAuthenticateAnUser.md
					  String token = "XXX.XXX.XXX";
					  OrionSubscription subscription = new OrionSubscription(cond, attr, "P1D", entityId, false, "urn:oc:entityType:userImage",null,token);
					  
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
				//count++;
				now = System.currentTimeMillis();
			}
			if(count>1){
				System.out.println("breaking");
				wsc.dispose();
				break;
			}
		}
	}
	

	@Override
	public void messageReceived(String mesg) {
		System.out.println("Received message in Vanilla: "+mesg);
	}
	
	@Override
	public void finalize(){
		System.out.println("test exit");
	}
	
	
	
	public static void main(String[] args){
		System.out.println("Starting Vanilla test client");
		VanillaTestClient client = new VanillaTestClient();
	}

}
