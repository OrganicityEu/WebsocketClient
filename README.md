# Websockets for Orion Context Broker

Create websocket clients for connecting with the Orion Context Broker (https://fiware-orion.readthedocs.io/en/master/), which makes it possible to subscribe to updates from entities in the swarm.
As the Context Broker is REST based, we provide a Spring.io based middleware (https://github.com/OrganicityEu-Platform/WebsocketMiddleware) for transforming the connections between REST and Websockets.


## Dependencies
This example was created using the following tools:
  - Eclipse Neon
  - Maven 3.3.9
  - Jetty
  - Processing
  - Stomp

## Installation
Download the zip (remember to unzip) or jar file from the libs folder. 
If using it in Processing, drag the jar file over the Processing IDE to add to project and C/P the example code for Processing below to gets started.
If using it as a Java lib, include the jar file in you classpath.

## Examples explained
I have provided two simple examples on using both the pure Java implementation and one for Processing. These can be
found in the test folder. Below I will go through each example, and elaborate their usage.

### Java client

In the following I provide the full example code of creating a websocket client in pure Java and connecting to the middleware.
In the below code I create an example subscription (https://fiware-orion.readthedocs.io/en/master/user/walkthrough_apiv2/index.html#subscriptions) and send it to the server. Afterwards (max 5 seconds) I unregister the same subscription.

```
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
    wsc= new WebsocketClientStomp(this, "ws://localhost:8080/orion");
    startLoop();
  }
  
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
			String token = "<Your token here>";
			OrionSubscription subscription = new OrionSubscription(cond, attr, "P1D", entityId, false, "urn:oc:entityType:userImage",null,token);
					  
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


```

## Usage
- Make sure you have Maven installed (https://maven.apache.org/)
- Run from command line:
'''
  mvn exec:java -Dexec.mainClass="websockets.test.VanillaTestClient"
'''

## Technical development details
The library has been developed on a Mac with El Capitan, I have used the Eclipse Luna IDE,
and I have only tested on Processing version 3.0.1.

The library is build with the Jetty websocket implementation, and different Jetty libraries
are therefore needed for running this library. All dependencies are included in the downloadable
zip file. The source code is available through this Github project (open source under MIT
license) as well as included in the zip file below.
