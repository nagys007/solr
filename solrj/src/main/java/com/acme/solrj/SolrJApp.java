// https://wiki.apache.org/solr/Solrj
//http://www.solrtutorial.com/solrj-tutorial.html

package com.acme.solrj;

import java.io.IOException;
import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
//import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
//import org.apache.solr.common.SolrInputDocument;



import junit.framework.Assert;

public class SolrJApp {
	public static void main(String[] args) throws SolrServerException,
			IOException {
		// CloudSolrServer server = new
		// CloudSolrServer("http://mfhadoopt03:2181/solr"); // Zookeeper
		// firewall???
		// server.setDefaultCollection("pdfcollection");
		String url = "http://mfhadoopt03:8983/solr/pdfcollection";
		HttpSolrServer server = new HttpSolrServer(url);

		SolrQuery query = new SolrQuery();
		query.setQuery("*:*");
		// query.addFilterQuery("cat:electronics", "store:amazon.com");
		query.setFields("id", "title", "author");
		query.setRows(10);
		// query.setStart(0);
		// query.set("defType", "edismax");

		QueryResponse response = server.query(query);
		SolrDocumentList results = response.getResults();
		for (int i = 0; i < results.size(); ++i) {
			System.out.println(results.get(i));
			System.out.println(results.get(i).getFieldValue("author"));
		}

		// facet Query
		// https://svn.apache.org/repos/asf/lucene/solr/tags/release-1.3.0/client/java/solrj/test/org/apache/solr/client/solrj/SolrExampleTests.java

		query = new SolrQuery("*:*");
		// query.addFacetQuery("price:[* TO 2]");
		// query.addFacetQuery("price:[2 TO 4]");
		// query.addFacetQuery("price:[5 TO *]");
		query.addFacetField("text");
		query.setFacetMinCount(50);
		query.setFacetLimit(100);
		query.setFacetMissing(true);
		query.setFacet(true);

		response = server.query(query);
		Assert.assertEquals(0, response.getStatus());
		System.out.println(response.getResults().getNumFound());
		System.out.println(response.getFacetQuery().size());

		FacetField ff = response.getFacetField("text");
		System.out.println(ff.getValueCount());
		List<Count> counts = ff.getValues();
		for (Count c : counts) {
			System.out.println(c.getName() + ":" + c.getCount());
		}

	}
}
