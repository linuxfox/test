package kr.co.esjee.cloud.job.quantum;

import java.net.URI;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPDigestAuthFilter;
import com.sun.jersey.client.urlconnection.HTTPSProperties;

/**
 * Quantum archive tape 연동구현
 * @author fox
 *
 */
public class QuantumArchiveUtil {

	private static final String CONTEXT = "sws/v2/";
	
	private static final int CONNECT_TIMEOUT = 30000;
	private static final int READ_TIMEOUT = 30000;

	public String getFsFileInfo(boolean useHttps, String userName,
			String password, String hostName, String port, String format,
			String filePath) {

		ClientConfig config = getClientConfig();

		setupSSL(useHttps, config);

		Client client = configureClient(userName, password, config);

		WebResource service = client.resource(getBaseURI(hostName, port, useHttps));

		String mediaType = getMediaType(format);

		String response = service.path("sws/v2/file/fsfileinfo")
				.queryParam("file", filePath).queryParam("format", format)
				.queryParam("username", userName).queryParam("password", password)
				.accept(mediaType).get(String.class);

		return response;

	}

	public String postFsFileInfo(boolean useHttps, String userName,
			String password, String hostName, String port, String format,
			String filePath) {

		ClientConfig config = getClientConfig();
		setupSSL(useHttps, config);
		Client client = configureClient(userName, password, config);
		
		WebResource service = client.resource(getBaseURI(hostName, port, useHttps));

		String inputX = "file=" + filePath;
		String mediaType = getMediaType(format);
		String response = service.path("sws/v2/file/fsfileinfo")
				.accept(mediaType).post(String.class, inputX);

		return response;

	}

	public String getFsClassInfo(boolean useHttps, String userName,
			String password, String hostName, String port, String format,
			String className) {

		ClientConfig config = getClientConfig();
		setupSSL(useHttps, config);
		Client client = configureClient(userName, password, config);

		WebResource service = client.resource(getBaseURI(hostName, port, useHttps));

		String mediaType = getMediaType(format);
		String response = service.path("sws/v2/policy/fsclassinfo")
				.queryParam("policy", className).queryParam("format", format)
				.queryParam("username", userName).queryParam("password", password)
				.accept(mediaType).get(String.class);

		return response;

	}

	public String getFsDirClass(boolean useHttps, String userName,
			String password, String hostName, String port, String format,
			String filePath) {

		ClientConfig config = getClientConfig();
		setupSSL(useHttps, config);

		Client client = configureClient(userName, password, config);
		
		WebResource service = client.resource(getBaseURI(hostName, port, useHttps));

		String mediaType = getMediaType(format);
		String response = service.path("sws/v2/policy/fsdirclass")
				.queryParam("directory", filePath).queryParam("format", format)
				.queryParam("username", userName).queryParam("password", password)
				.accept(mediaType).get(String.class);

		return response;
	}

	public String getFsStore(boolean useHttps, String userName,
			String password, String hostName, String port, String format,
			String filePath, int copies) {

		ClientConfig config = getClientConfig();
		setupSSL(useHttps, config);

		Client client = configureClient(userName, password, config);
		WebResource service = client.resource(getBaseURI(hostName, port, useHttps));

		String mediaType = getMediaType(format);

		String response = service.path("sws/v2/file/fsstore").queryParam("file", filePath)
				.queryParam("copies", Integer.toString(copies)).queryParam("format", format)
				.queryParam("username", userName).queryParam("password", password).accept(mediaType).get(String.class);

		return response;

	}
	
	public String getFsStore(boolean useHttps, String userName,
			String password, String hostName, String port, String format,
			String directory, List<String> fileNames, int copies) {

		ClientConfig config = getClientConfig();
		setupSSL(useHttps, config);

		Client client = configureClient(userName, password, config);
		WebResource service = client.resource(getBaseURI(hostName, port, useHttps));

		String mediaType = getMediaType(format);

		service.path("sws/v2/file/fsstore").queryParam("directory", directory);
		
		for(String fileName : fileNames) {
			service.queryParam("file", fileName);
		}
	
		String response = service.queryParam("copies", Integer.toString(copies))
				.queryParam("format", format)
				.queryParam("recursive", "false")
				.queryParam("username", userName).queryParam("password", password).accept(mediaType).get(String.class);

		return response;

	}	

	public String getFsRmDiskCopy(boolean useHttps, String userName,
			String password, String hostName, String port, String format,
			String filePath) {

		ClientConfig config = getClientConfig();
		setupSSL(useHttps, config);
		Client client = configureClient(userName, password, config);
		WebResource service = client.resource(getBaseURI(hostName, port, useHttps));

		String mediaType = getMediaType(format);
		String response = service.path("sws/v2/file/fsrmdiskcopy")
				.queryParam("file", filePath).queryParam("format", format)
				.queryParam("username", userName).queryParam("password", password)
				.accept(mediaType).get(String.class);

		return response;

	}

	public String getFsRetrieve(boolean useHttps, String userName,
			String password, String hostName, String port, String format,
			String filePath) {

		ClientConfig config = getClientConfig();
		setupSSL(useHttps, config);
		Client client = configureClient(userName, password, config);

		WebResource service = client.resource(getBaseURI(hostName, port, useHttps));

		String mediaType = getMediaType(format);

		String response = service.path("sws/v2/file/fsretrieve")

				.queryParam("file", filePath).queryParam("format", format)

				.queryParam("username", userName).queryParam("password", password)

				.accept(mediaType).get(String.class);

		return response;

	}

	public String getCreateSnQuota(boolean useHttps, String userName,

			String password, String hostName, String port, String format,

			String fspath, String dirPath) {

		ClientConfig config = getClientConfig();

		setupSSL(useHttps, config);

		Client client = configureClient(userName, password, config);

		WebResource service = client.resource(getBaseURI(hostName, port, useHttps));

		String mediaType = getMediaType(format);

		String response = service.path("sws/v2/quota/snquota")
				.queryParam("path", fspath).queryParam("directory", dirPath)
				.queryParam("action", "create").queryParam("format", format)
				.queryParam("username", userName).queryParam("password", password)
				.accept(mediaType).get(String.class);

		return response;

	}

	public String getSetSnQuota(boolean useHttps, String userName,
			String password, String hostName, String port, String format,
			String fspath, String dirPath, String hardLimit, String softLimit,
			String gracePeriod) {

		ClientConfig config = getClientConfig();
		setupSSL(useHttps, config);
		Client client = configureClient(userName, password, config);

		WebResource service = client.resource(getBaseURI(hostName, port, useHttps));

		String mediaType = getMediaType(format);

		String response = service.path("sws/v2/quota/snquota")
				.queryParam("path", fspath).queryParam("directory", dirPath)
				.queryParam("action", "set").queryParam("hardlimit", hardLimit)
				.queryParam("softlimit", softLimit)
				.queryParam("graceperiod", gracePeriod)
				.queryParam("username", userName).queryParam("password", password)
				.queryParam("format", format).accept(mediaType)
				.get(String.class);

		return response;

	}

	public String getListSnQuota(boolean useHttps, String userName,
			String password, String hostName, String port, String format,
			String fsname) {

		ClientConfig config = getClientConfig();
		setupSSL(useHttps, config);
		Client client = configureClient(userName, password, config);
		WebResource service = client.resource(getBaseURI(hostName, port, useHttps));

		String mediaType = getMediaType(format);
		
		String response = service.path("sws/v2/quota/snquota")
				.queryParam("fsname", fsname).queryParam("action", "listall")
				.queryParam("username", userName).queryParam("password", password)
				.queryParam("format", format).accept(mediaType)
				.get(String.class);

		return response;
	}

	private String getMediaType(String format) {

		String mediaType = MediaType.TEXT_PLAIN;

		if (format != null) {

			if (format.equalsIgnoreCase("json")) {

				mediaType = MediaType.APPLICATION_JSON;

			} else if (format.equalsIgnoreCase("xml")) {

				mediaType = MediaType.APPLICATION_XML;

			}

		}

		return mediaType;

	}

	private Client configureClient(String userName, String password,
			ClientConfig config) {

		Client client = Client.create(config);
		client.setConnectTimeout(CONNECT_TIMEOUT);
		client.setReadTimeout(READ_TIMEOUT);

		if (userName != null && userName.length() > 0 && password != null) {
			client.addFilter(new HTTPDigestAuthFilter(userName, password));
		}

		return client;
	}

	private void setupSSL(boolean useHttps, ClientConfig config) {

		if (useHttps) {
			TrustManager[] certs = new TrustManager[] { new X509TrustManager() {
				
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				}

				public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

				}

			} };

			SSLContext ctx = null;

			try {
				ctx = SSLContext.getInstance("TLS");
				ctx.init(null, certs, new SecureRandom());
			} 
			catch (java.security.GeneralSecurityException ex) {
				
			}

			HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());

			try {
				config.getProperties().put(
						HTTPSProperties.PROPERTY_HTTPS_PROPERTIES,
						new HTTPSProperties(new HostnameVerifier() {
							public boolean verify(String hostname, SSLSession session) {
								return true;
							}

						}, ctx));

			} 
			catch (Exception e) {
				
			}
		}
	}

	private ClientConfig getClientConfig() {
		ClientConfig config = new DefaultClientConfig();
		return config;
	}

	protected URI getBaseURI(String host, String port, boolean https) {

		String protocol = https ? "https://" : "http://";

		if (port != null) {
			return UriBuilder.fromUri(protocol + host + ":" + port + "/").build();
		} 
		else {
			return UriBuilder.fromUri(protocol + host + "/").build();
		}

	}

}
