package websockets;

import java.util.ArrayList;
import java.util.List;

import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSession.Subscription;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.client.jetty.JettyWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import org.springframework.web.socket.sockjs.frame.Jackson2SockJsMessageCodec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import dk.alexandra.orion.websocket.transports.OrionSubscription;
import dk.alexandra.orion.websocket.transports.OutOfBandMessage;
import websockets.handlers.OrionStompSessionHandler;
import websockets.handlers.WebsocketCallback;

/**
 * 
 * @author Morten Skov
 *
 * Class for creating websocket client connections to a STOMP websocket server.  
 *
 */
public class WebsocketClientStomp {
	private WebSocketStompClient stompClient;
	private WebsocketCallback parent;
	private StompSession session;
	private OrionStompSessionHandler sessionHandler;
	
	private ObjectMapper mapper = new ObjectMapper();
	private final String subscriptionEndPoint = "/user/message/queue/orion";
	private String sessionId = null;
	
	
	
	
	/**
	 * 
	 * Initiating the client connection
	 * 
	 * @param parent The PApplet object coming from Processing
	 * @param endpointURI The URI to connect to Ex. ws://localhost:8025/john
	 */
	public WebsocketClientStomp(WebsocketCallback parent, String endpointURI) {
		if(parent!=null){
			this.parent = parent;
		}
		
		
		JettyWebSocketClient wsClient = new JettyWebSocketClient();
		wsClient.start();
		
		List<Transport> transports = new ArrayList<>(2);
	    transports.add(new WebSocketTransport(wsClient));
	    
	    ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
	    taskScheduler.afterPropertiesSet();

	    
	    SockJsClient webSocketClient = new SockJsClient(transports);
		webSocketClient.setMessageCodec(new Jackson2SockJsMessageCodec());
		
		stompClient = new WebSocketStompClient(webSocketClient);
		stompClient.setMessageConverter(new MappingJackson2MessageConverter());
		stompClient.setTaskScheduler(taskScheduler); // for heartbeats
		
		//TOOD: maybe this is not a working solution?
		sessionHandler = new OrionStompSessionHandler(this);
		
		stompClient.connect(endpointURI, sessionHandler);
		
	}

	
	
	/**
	 * 
	 * Method for forwarding the payload from WS to a Processing skecth
	 * 
	 * @param message The JSON encoded message from server
	 */
	public void handleMessage(ObjectNode message){
		System.out.println("trying to send message to processing");
		if(parent!=null){
			parent.messageReceived(message.toString());
		}
	}
	
	
	
	/**
	 * 
	 * For subscribing to a endpoint on a STOMP server
	 * 
	 * @param sessionHandler The StompSessionHandler for receiving frames from server
	 * @param session The Stompsession for sending frames to STOMP server
	 * @return boolean if it was possible to connect to the subscription endpoint
	 */
	public boolean connectNotificationEndpoint(StompSessionHandler sessionHandler, StompSession session){
		System.out.println("Subscribing to endpoint");
		Subscription subscription = session.subscribe(subscriptionEndPoint,sessionHandler);
		this.session = session;
		return subscription!=null?true:false;
	}
	
	/**
	 * 
	 * Getting list of subscription ids
	 * @return list of all subscriptions in system
	 */
	public List<String> getSubscriptions(){
		return sessionHandler.getSubscriptionIds();
	}
	
	
	/**
	 * 
	 * For unregistering a event subscription from the Orion Context Broker
	 * 
	 * @param subscriptionId The subscription id that is connected to a given subscriptions 
	 * @return boolean if it was possible to unregister the subscription
	 */
	public boolean unregisterSubscription(String subscriptionId){
		System.out.println("unregister subscription");
		OutOfBandMessage message = new OutOfBandMessage("unsubscribe",subscriptionId);
		String subscriptionJsonString;
		try {
			subscriptionJsonString = mapper.writeValueAsString(message);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		session.send("/app/unregister", subscriptionJsonString);
		return true;
	}
	
	
	
	/**
	 * 
	 * Registering a subscription to the Orion websocket server.
	 * 
	 * @param subscription {@link OrionSubscription} The POJO complying to Orion subscription specifications
	 * @return boolean if the registration was successful
	 */
	public boolean registerSubscription(OrionSubscription subscription){
		System.out.println("register subscription");
		subscription.setSubscriberId(this.sessionId);
		if(session!=null){
			String subscriptionJsonString;
			try {
				subscriptionJsonString = mapper.writeValueAsString(subscription);
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
			session.send("/app/register", subscriptionJsonString);
			return true;
		}else{
			System.out.println("No session registrered. Not able to send a subscription to unknown server");
			return false;
		}
		
	}
	
	/**
	 * 
	 * Setting the sessionId of this specific client, so the server knows who is requesting what
	 * 
	 * @param sessionId The session Id recieved from the WS/STOMP server. 
	 */
	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
		
	}
	
	
	/**
	 * 
	 * A handler method called by Processing. Kills the connection to the server
	 * 
	 */
	public void dispose(){
		System.out.println("Dispose");
		stompClient.stop();
		// Anything in here will be called automatically when 
	    // the parent sketch shuts down. For instance, this might
	    // shut down a thread used by this library.
	}
	
	
}
