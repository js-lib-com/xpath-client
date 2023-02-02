package com.jslib.xpath.client;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * HTTP connection factory, merely for tests integration.
 * 
 * @author Iulian Rotaru
 */
public class ConnectionFactory {
	/**
	 * Create HTTP connection with given URL.
	 * 
	 * @param url URL to open connection with.
	 * @return HTTP connection to requested URL.
	 * @throws IOException if HTTP connection handshake fails.
	 */
	public HttpURLConnection openConnection(URL url) throws IOException {
		return (HttpURLConnection) url.openConnection();
	}
}
