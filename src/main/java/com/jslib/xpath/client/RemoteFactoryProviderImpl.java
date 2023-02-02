package com.jslib.xpath.client;

import js.rmi.RemoteFactory;
import js.rmi.RemoteFactoryProvider;

public class RemoteFactoryProviderImpl implements RemoteFactoryProvider {
	private static final String[] PROTOCOLS = new String[] { "http:xpath", "https:xpath" };
	private static final RemoteFactory FACTORY = new XPathClientFactory();

	@Override
	public String[] getProtocols() {
		return PROTOCOLS;
	}

	@Override
	public RemoteFactory getRemoteFactory() {
		return FACTORY;
	}
}
