package websockets.handlers;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import dk.alexandra.orion.websocket.transports.OutOfBandMessage;
import websockets.WebsocketClientStomp;

public class OrionStompSessionHandler implements StompSessionHandler{
	
	private WebsocketClientStomp parent;
	
	private List<String> subscriptions = new ArrayList<>();
	ObjectMapper mapper = new ObjectMapper();
	
	
	
	public OrionStompSessionHandler(WebsocketClientStomp parent) {
		if(parent==null){
			throw new IllegalArgumentException("Parent is needed");
		}
		this.parent = parent;
	}
	
	
	
	public List<String> getSubscriptionIds(){
		return subscriptions;
	}

	
	
	@Override
	public Type getPayloadType(StompHeaders arg0) {
		//System.out.println("gpl: "+arg0);
		return JsonNode.class;
	}

	
	
	@Override
	public void handleFrame(StompHeaders headers, Object payload) {
		
		if(!(payload instanceof ObjectNode)){
			//if this is not a jackson ObjectNode, we don't want to work with it
			return;
		}
		ObjectNode json = (ObjectNode)payload;
		
		//check if a registration message
		if(json.has("type")){
			
			OutOfBandMessage receivedMessage = new OutOfBandMessage();
			try{
	    		receivedMessage= mapper.convertValue(payload, OutOfBandMessage.class);
	        }catch(IllegalArgumentException e){
	        	e.printStackTrace();
	        }
			
			String type = receivedMessage.getType();
			String message = receivedMessage.getMessage();
			switch(type){
				case "sessionId":
					System.out.println("Setting sessionId");
					parent.setSessionId(message);
					//parent.sessionId = message;
					break;
				case "subscriptionId":
					System.out.println("adding subsciptionId");
					subscriptions.add(message);
					break;
				case "removeSubscription":
					System.out.println("removed subscription from list");
					subscriptions.remove(message);
					break;
				case "error":
					System.out.println("error trying to subscribe to entity: "+message);
					parent.handleMessage(json);
			}
			
		}else{
			System.out.println("Normal message - forwarded to Processing");
			System.out.println("message content: "+json);

			//normal message is forwarded to Processing part
			parent.handleMessage(json);
		}
		
		System.out.println('\n');
	}

	@Override
	public void afterConnected(StompSession session, StompHeaders headers) {
		System.out.println("Connected to server with sessionId: "+session.getSessionId());
		
		boolean connected = parent.connectNotificationEndpoint(this, session);
		String message = "Connection to server was ";
		if(connected){
			message+="successful";
		}else{
			message+="NOT successful";
		}
		System.out.println(message);
		
	}

	@Override
	public void handleException(StompSession arg0, StompCommand arg1, StompHeaders arg2, byte[] arg3,
			Throwable arg4) {
		System.out.println("handleException: ");
		System.out.println(arg0);
		System.out.println(arg2);
		System.out.println(arg3);
		arg4.printStackTrace();
		
	}

	@Override
	public void handleTransportError(StompSession arg0, Throwable arg1) {
		System.out.println("Transport Error: ");
		arg1.printStackTrace();
		
	}
	
}
