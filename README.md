# Websockets for Orion Context Broker

**Create websocket clients for connecting with the Orion Context Broker (https://fiware-orion.readthedocs.io/en/master/), which makes it possible to subscribe to updates from entities in the swarm.
As the Context Broker is REST based, we provide a Spring.io based middleware (https://github.com/OrganicityEu-Platform/WebsocketMiddleware) for transforming the connections between REST and Websockets. *
**

## Dependencies
This example was created using the following tools:
  Eclipse Neon
  Maven 3.3.9
  Jetty
  Processing

## Installation
Unzip and put the extracted webSockets folder into the libraries folder of your Java application or Processing
sketches. Reference and examples are included in the webSockets folder.

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

```

### Processing client

In the following I provide the full example code of creating a websocket client in Processing and connecting to the middleware.
In the below code I create an example subscription (https://fiware-orion.readthedocs.io/en/master/user/walkthrough_apiv2/index.html#subscriptions) and send it to the server. Afterwards (max 5 seconds) I unregister the same subscription. For every time I receive a message from the Middleware i draw a new ellipse without delete the old one.

```
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

```

## Technical development details
The library has been developed on a Mac with El Capitan, I have used the Eclipse Luna IDE,
and I have only tested on Processing version 3.0.1.

The library is build with the Jetty websocket implementation, and different Jetty libraries
are therefore needed for running this library. All dependencies are included in the downloadable
zip file. The source code is available through this Github project (open source under MIT
license) as well as included in the zip file below.
