package dad.us.dadVertx.watson;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;

import com.ibm.watson.developer_cloud.retrieve_and_rank.v1.RetrieveAndRank;

public class WatsonQuestionAnswer {

	private static String username = "e90ee7ce-3859-4933-b60d-1d53fb681b0f";
	private static String password = "OLnY2SxwsysY";
	private static HttpSolrClient solrClient;
	private static RetrieveAndRank serviceRetrieveAndRank;
	// sc6891d3ab_a39f_4133_9f8d_ea7b351ec170
	private String clusterId;
	// ObesityFAQ
	private String collection;
	// Obesity
	private String clusterName;

	@SuppressWarnings("deprecation")
	public WatsonQuestionAnswer(String collection, String clusterId, String clusterName) {
		this.collection = collection;
		this.clusterId = clusterId;
		this.clusterName = clusterName;

		if (serviceRetrieveAndRank == null) {
			serviceRetrieveAndRank = new RetrieveAndRank();
			serviceRetrieveAndRank.setUsernameAndPassword(username, password);
		}

		if (solrClient == null) {
			solrClient = new HttpSolrClient(serviceRetrieveAndRank.getSolrUrl(clusterId),
					createHttpClient(serviceRetrieveAndRank.getSolrUrl(clusterName), username, password));
		}
	}

	public List<String> getResponse(String question) {
		SolrQuery query = new SolrQuery(question);
		List<String> res = new ArrayList<>();
		QueryResponse response;
		try {
			response = solrClient.query(collection, query);
			res = response.getResults().stream().map(doc -> doc.get("body").toString()).collect(Collectors.toList());
		} catch (SolrServerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}

	private HttpClient createHttpClient(String uri, String username, String password) {
		final URI scopeUri = URI.create(uri);

		final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		credentialsProvider.setCredentials(new AuthScope(scopeUri.getHost(), scopeUri.getPort()),
				new UsernamePasswordCredentials(username, password));

		final HttpClientBuilder builder = HttpClientBuilder.create().setMaxConnTotal(128).setMaxConnPerRoute(32)
				.setDefaultRequestConfig(RequestConfig.copy(RequestConfig.DEFAULT).setRedirectsEnabled(true).build())
				.setDefaultCredentialsProvider(credentialsProvider)
				.addInterceptorFirst(new PreemptiveAuthInterceptor());
		return builder.build();

	}

}
