package websockets;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Base64;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.json.JSONObject;


public class TokenFetcher {
	
	private String clientId;
	private String clientSecret;
	private String grantType;
	private String token;
	private String tokenUrl;
	protected static final Logger LOGGER = Logger.getLogger(TokenFetcher.class);
	
	
	/**
	 * Helper class to exemplify how to get the token from the OAuth server.
	 * Not ment to be used in production as it does not verify the SSL certificates.
	 * It does not utilize refresh_tokens
	 * Look there to acquire token: https://organicityeu.github.io/HowToAuthenticateAnUser/
	 */
	public TokenFetcher(String clientId, String clientSecret, String grantType){
		this.clientId=clientId;
		this.clientSecret = clientSecret;
		this.grantType=grantType;
		this.tokenUrl = "https://accounts.organicity.eu";
	}
	
	public String getToken(){
		updateToken();
		return token;
	}
	
	public void updateToken(){
		
		LOGGER.info("getting new token");
		SSLContext sc = null;
		try{
			sc = SSLContext.getInstance("SSL"); 
			sc.init(null, getTrustManager(), new java.security.SecureRandom());
		}catch(NoSuchAlgorithmException|KeyManagementException e){
			LOGGER.error("Exception thrown while setting up certificats: \n"+e);
			
		}
		
		Client c = ClientBuilder.newBuilder().sslContext(sc).build();
		
		String path = "/realms/organicity/protocol/openid-connect/token";
		WebTarget webTarget = c.target(tokenUrl).path(path);
		LOGGER.info("Connecting to token Url: "+tokenUrl+path);
		String base64 = Base64.getEncoder().encodeToString((clientId+":"+clientSecret).getBytes());
		
		
		Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON_TYPE).header("Authorization","Basic "+base64);
		

		Form form = new Form();		
		form.param("grant_type", grantType);
		
		Entity entity =  Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE);
		Response response = invocationBuilder.post(entity);
		
		JSONObject tokenEntity = new JSONObject(response.readEntity(String.class));
		System.out.println(tokenEntity);
		int delay = 0;
		try{
			String expires = tokenEntity.get("expires_in").toString();
			delay = Integer.parseInt(expires);
		}catch(NumberFormatException e){
			delay = 200;
		};
		
		token =  tokenEntity.get("access_token").toString();
		
	}
	
	/**
	 * Method for getting a trust manager for handling the SSL connections
	 * This is a VERY bad solution as it accepts all certificates. 
	 * 
	 * 
	 * @return A TrustManager array
	 */
	
	private TrustManager[] getTrustManager(){
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] { 
				new X509TrustManager() {     
					public java.security.cert.X509Certificate[] getAcceptedIssuers() { 
						return new X509Certificate[0];
					} 
					public void checkClientTrusted( 
							java.security.cert.X509Certificate[] certs, String authType) {
					} 
					public void checkServerTrusted( 
							java.security.cert.X509Certificate[] certs, String authType) {
					}
				} 
		};
		return trustAllCerts;	
	}
}
