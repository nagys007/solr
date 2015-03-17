/*
 * cmd.exe: 
 * d:
 * cd D:\java\workspace\solrtagcloud\
 * mvn jetty:run
 * (press Ctrl+C to stop or use another cmd.exe and do "mvn jetty:stop" - did not work for me though) 
 * 
 * firefox:
 * http://localhost:8080/SolrTagCloud
 */

package com.acme.solrtagcloud;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.request.QueryRequest;
//import org.apache.solr.client.solrj.response.FacetField;
//import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.TermsResponse;
import org.apache.solr.client.solrj.response.TermsResponse.Term;
import org.eclipse.jetty.server.HttpWriter;


// try URL Connection
//import java.net.*;
//import java.util.Iterator;
import java.util.List;
//import java.io.*;

//import junit.framework.Assert;

public class SolrTagCloud extends HttpServlet {
	/**
	 * generated
	 */
	private static final long serialVersionUID = -6644661224156633925L;

	protected void doGet(HttpServletRequest request,
			HttpServletResponse httpResponse) throws ServletException,
			IOException {

		httpResponse.setContentType("text/html");
		httpResponse.setStatus(HttpServletResponse.SC_OK);

		PrintWriter printWriter = httpResponse.getWriter(); 
		printWriter.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
		printWriter.println("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
		printWriter.println("<head>");
		printWriter.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />");
								// + "<title>Css Globe: tag clouds</title>"
		//
		printWriter.println("<style>"
								+ "body {margin:0;padding:0;background:#e1e1e1;font:80% Trebuchet MS, Arial, Helvetica, sans-serif;color:#555;line-height:180%;}"
								+ "a {color:#3c70d0;}"
								+ "h1 {font-size:180%;font-weight:normal;margin:0 20px;padding:1em 0;}"
								+ "h2 {font-size:160%;font-weight:normal;}"
								+ "h3 {font-size:140%;font-weight:normal;}"
								+ "img {border:none;}"
								+ "pre {display:block;font:12px \"Courier New\", Courier, monospace;padding:10px;border:1px solid #bae2f0;background:#e3f4f9;margin:.5em 0;width:500px;}"
								+ "#container {margin:0 auto;text-align:left;width:700px;background:#fff;}"
								+ "#main {float:left;display:inline;width:380px;margin-left:20px;}"
								+ "#side {float:left;display:inline;width:260px;margin-left:20px;}"
								+ "#footer {clear:both;padding:1em 0;margin:0 20px;}"
								+ "/* Tag cloud */ "
								+ "#tags ul {margin:1em 0;padding:.5em 10px;text-align:center;background:#71b5e9 url(bg_tags.gif) repeat-x;} "
								+ "#tags li {margin:0;padding:0;list-style:none;display:inline;} "
								+ "#tags li a {text-decoration:none;color:#fff;padding:0 2px;} "
								+ "#tags li a:hover {color:#cff400;} "
								+ ".tag1 {font-size:100%;} "
								+ ".tag2 {font-size:120%;} "
								+ ".tag3 {font-size:140%;} "
								+ ".tag4 {font-size:160%;} "
								+ ".tag5 {font-size:180%;} ");
		printWriter.println("/* alternative layout */");
		printWriter.println("#tags .alt {text-align:left;padding:0;background:none;}");
		printWriter.println("#tags .alt li {padding:2px 10px;background:#efefef;display:block;} ");
		printWriter.println("#tags .alt .tag1, ");
		printWriter.println("#tags .alt .tag2, ");
		printWriter.println("#tags .alt .tag3, ");
		printWriter.println("#tags .alt .tag4, ");
		printWriter.println("#tags .alt .tag5 {font-size:100%;} ");
		printWriter.println("#tags .alt .tag1 {background:#7cc0f4;} ");
		printWriter.println("#tags .alt .tag2 {background:#67abe0;} ");
		printWriter.println("#tags .alt .tag3 {background:#4d92c7;} ");
		printWriter.println("#tags .alt .tag4 {background:#3277ad;} ");
		printWriter.println("#tags .alt .tag5 {background:#266ca2;} ");
		printWriter.println("</style>");
		//
		//printWriter.println("/* Tag cloud */");
		printWriter.println("<script type=\"text/javascript\" src=\"js/jquery.js\"></script>");
		printWriter.println("<script type=\"text/javascript\">");
		printWriter.println("$(document).ready(function(){");
		printWriter.println("   var alt = 0;");
		//printWriter.println("    /* create a style switch button*/");
		printWriter.println("	var switcher = $('<a href=\"javascript:void(0)\" class=\"btn\">Change appearance</a>').click(");
		printWriter.println("		function() {");
		printWriter.println("			if ( alt == 0 ) { $(\"#tags ul\").hide().addClass(\"alt\").fadeIn(\"slow\"); alt = 1; }");
		printWriter.println("			else { $(\"#tags ul\").hide().removeClass(\"alt\").fadeIn(\"slow\"); alt = 0; }");
		printWriter.println("		}");
		printWriter.println("	);");
		printWriter.println("	$('#tags').append(switcher);");
		printWriter.println("});");
		printWriter.println("</script>"
		+"</head>"
				);
		printWriter.println("<h1>PDF Collection: Solr Tag Cloud</h1>");

		String url = "http://mfhadoopt03:8983/solr/tamap";
		HttpSolrServer server = new HttpSolrServer(url);

		SolrQuery query = new SolrQuery();
		query.setRequestHandler("/terms"); // setQueryType deprecated
		query.setTerms(true);
		query.addTermsField("text");
		query.setTermsLimit(100);
		query.setTermsMinCount(1);
		query.setTermsRegex("sql|oracle|java|hive|pig|hadoop|c|pmp|abs|dcc|microsoft|word|excel|powerpoint|windows|linux|unix"
				+ "|englisch|latein|italienisch" + "|adobe|iphone|ios");

		try {

			QueryRequest solrRequest = new QueryRequest(query);

			QueryResponse solrResponse = solrRequest.process(server);

			TermsResponse resp = solrResponse.getTermsResponse();
			List<Term> items = resp.getTerms("text");
			System.out.println(items.size());

			final int minFontsize = 10;
			final int maxFontsize = 40;

			httpResponse
					.getWriter()
					.println(
							"<div class=\"tag_cloud\" "
									+ "style=\"width: 500px; min-height: 400px; line-height: "
									+ maxFontsize + "px;\">");

			long maxCount = 0;
			long minCount = -1;
			for (Term i : items) {
				long count = i.getFrequency();
				if (count > maxCount)
					maxCount = count;
				if (count < minCount || minCount < 0)
					minCount = count;
			}
			if (maxCount == minCount)
				maxCount += 1;

			for (Term i : items) {
				double fontsize = i.getFrequency() - minCount;
				fontsize /= maxCount - minCount;
				fontsize *= maxFontsize - minFontsize;
				fontsize += minFontsize;

				System.out.println(i.getTerm() + ":" + i.getFrequency() + "->"
						+ (int) fontsize
				/*
				 * + " a=" + (maxCount - minCount) + " b=" + (i.getFrequency() -
				 * minCount) + " c=" + ((i.getFrequency() - minCount) /
				 * (maxCount - minCount)) + " d=" + ((i.getFrequency() -
				 * minCount) / (maxCount - minCount)) (maxFontsize -
				 * minFontsize)
				 */
				);

				printWriter.println("<span style=\"font-size:" + ((int) fontsize)+ "px\">");
				printWriter.println("<a href=#>" + i.getTerm() + "</a>");
				printWriter.println("</span>");
			}
			
				
			// und noch eine Version von Tag Cloud
			printWriter.println("<div id=\"tags\">");
			printWriter.println("<ul /*class=\"alt\"*/>");
			int minTag = 1;
			int maxTag = 5;
			for ( Term i : items ) {
				double tag = i.getFrequency() - minCount;
				tag /= maxCount - minCount;
				tag *= maxTag - minTag;
				tag += minTag;
				printWriter.println("<li class=\"tag"+((int) tag)+"\"><a href=\"#\">"+i.getTerm()+"</a></li>");
			}
			printWriter.println("</ul>" + "</div>");

			printWriter.println("</div>");
/*
			printWriter.println(
							""
									// +"<body>"
									+ "<div id=\"container\">"
									// +"<h1>Tag Cloud Example by Css Globe</h1>"
									+ "<div id=\"main\">"
									// +"<h2>Main column</h2>"
									// +"<!--script type=\"text/javascript\" src=\"http://cssglobe.com/ads/blogsponsor.js\"></script-->"
									+ "<p> Tag Cloud from http://cssglobe.com/articles/tagclouds/index2.html</p>"
									+ "</div>"
									+ "<div id=\"side\">"
									// +"<h2>Side column (Tag Cloud)</h2>"
									);
			printWriter.println("<div id=\"tags\">");
			printWriter.println("<ul /*class=\"alt\"* />");
			printWriter.println("<li class=\"tag1\"><a href=\"#\">Aaaaaaaaaaaaaa</a></li>");
			printWriter.println("<li class=\"tag3\"><a href=\"#\">Bbbbbbbbbb</a></li>");
			printWriter.println("<li class=\"tag5\"><a href=\"#\">cccccccccccccccc</a></li>");
			printWriter.println("<li class=\"tag2\"><a href=\"#\">DDDDD</a></li>");
			printWriter.println("<li class=\"tag4\"><a href=\"#\">Eeeeeee</a></li>");
			printWriter.println("</ul>" + "</div>");
			
			printWriter.println("</div>"
									// +"<!--div id=\"footer\"-->"
									// <p><a
									// href="http://cssglobe.com/post/4581/tag-clouds-styling-and-adding-sort-options"
									// title="tag cloud">back to the
									// article</a></p>
									// <p><strong>Tag Cloud Examples</strong>
									// are brought to you by <a
									// href="http://cssglobe.com"
									// title="web standards magazine and css news">Css
									// Globe</a> and supported by <a
									// href="http://templatica.com">Css
									// Templates</a> by Templatica</p>
									// +"</div>"

									+ "</div>"
									// +"</body>"
									// +"</html>"

									// <script
									// src="http://www.google-analytics.com/urchin.js"
									// type="text/javascript">
									// </script>
									// <script type="text/javascript">
									// _uacct = "UA-783567-1";
									// urchinTracker();
									// </script>
									+ "");*/

		} catch (SolrServerException e) {
			e.printStackTrace();
		}
	}
}
