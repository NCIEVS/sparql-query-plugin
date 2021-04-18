/***************************************************************************************
 *  Modified from org/eclipse/rdf4j/examples/repository/Example15SimpleSPARQLQuery.java
 *  to run either a set of sparql queries compiled into the program, or read from
 *  the command line.  Requires jars from a sesame 2.7.9 or 2.7.12 build.
 *  See also RDF4jSparqlQueryTest362.java
***************************************************************************************/


import org.openrdf.repository.Repository;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.OpenRDFException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFParseException;

import java.io.InputStream;
import java.net.URL;
import java.lang.System;
import java.util.List;
import java.util.Vector;
import java.util.Set;
import java.io.IOException;

public class SesameSparqlTest279
{

	public SesameSparqlTest279 () { }

	public static void main (String[] args) {
		String inputfilename = "";
		String inputquery = "";

		if ( args.length < 1 || args.length > 2 ) {
			System.out.println ("\n\tUsage:  java -cp <classpath> filename ['sparq_squery_text']\n");
			System.out.println ("where the  file is an rdf/xml file, and the optional 'sparql_query_text' is singly quoted.\n");
			System.exit(1);
			}
		else {
			inputfilename = args[0];
			if ( args.length == 2 )
				inputquery = args[1];
			}

		SesameSparqlTest279 test = new SesameSparqlTest279();
		try { test.executeTest (inputfilename, inputquery); }
		catch (Exception e) { System.out.println (e.getMessage()); }
		}


	public void executeTest (String lfilename, String lquery) throws Exception {

		long startTime = 0;
		long endTime = 0;
		RepositoryConnection conn = null;
		TupleQueryResult result = null;

		try {
			Repository db = new SailRepository(new MemoryStore());
			db.initialize();

			InputStream input = SesameSparqlTest279.class.getResourceAsStream("/" + lfilename );
			if( input == null ) throw new Exception ("File not found");

			conn = db.getConnection();

			startTime = System.currentTimeMillis();
			conn.add ( input, "", RDFFormat.RDFXML );  //IOException, RDFParseException, RepositoryException
			endTime = System.currentTimeMillis();
			System.out.println ("Load of vocabulary took " + (endTime - startTime) + " mSec");

			Vector<String> queries = new Vector<String>();
			if ( lquery.equals("") )
				queries = getVectorOfQueries();
			else
				queries.add( lquery );

			for ( String queryString : queries ) {
				try {
					TupleQuery sparqlquery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);  // RepositoryException, MalformedQueryException

					startTime = System.currentTimeMillis();
					result = sparqlquery.evaluate();		// QueryEvaluationException
					while ( result.hasNext() ) {
						BindingSet solution = result.next();
						Set<String> boundNames = solution.getBindingNames();
						for ( String name : boundNames ) {
							System.out.println(name);
							System.out.println(solution.getValue(name));
							}
						}
					result.close();
					}
				catch (OpenRDFException e) { System.out.println(e.getMessage()) ; }
				endTime = System.currentTimeMillis();
				System.out.println("sparql execution+processing took " + (endTime - startTime) + " mSec");
				}
			}
		catch (OpenRDFException e) { throw new Exception (e.getMessage()); }
		finally {
			conn.close();
 			}
		}

	public Vector<String> getVectorOfQueries () {
		Vector<String> tmp = new Vector<String>();
		String query = "";

		// just warm up any cache, sesame, OS, what-have-you
		query = "select (count(?s) as ?warm_up_any_cache) where { ?s ?p ?o }";
		tmp.add(query);

		// add additional queries below
/*		query = "";
		tmp.add(query);
*/

		return tmp;
		}
}

