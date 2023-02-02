package com.jslib.xpath.client;

import java.lang.reflect.Proxy;

import js.rmi.RemoteFactory;
import js.rmi.UnsupportedProtocolException;
import js.util.Params;

public class XPathClientFactory implements RemoteFactory {
	@SuppressWarnings("unchecked")
	public <I> I getRemoteInstance(String implementationURL, Class<? super I> interfaceClass) throws UnsupportedProtocolException {
		Params.notNull(implementationURL, "Implementation URL");
		Params.notNull(interfaceClass, "Interface class");
		// at this point we know that interface class is a super of returned instance class so is safe to suppress warning
		return (I) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[] { interfaceClass }, new XPathTransactionHandler(interfaceClass, implementationURL));
	}
}
