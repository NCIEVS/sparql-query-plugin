/***************************************************************************************
 *  Modified from org/eclipse/rdf4j/examples/repository/Example15SimpleSPARQLQuery.java
 *  to run either a set of sparql queries compiled into the program, or read from
 *  the command line.  Requires jars from an rdf 3.6.2 build w java 8.
***************************************************************************************/


/*******************************************************************************
 * Copyright (c) 2016, 2017 Eclipse RDF4J contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/
// package org.eclipse.rdf4j.examples.repository;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.TupleQueryResultHandler;
import org.eclipse.rdf4j.query.resultio.text.csv.SPARQLResultsCSVWriter;
import org.eclipse.rdf4j.query.resultio.text.tsv.SPARQLResultsTSVWriter;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import org.eclipse.rdf4j.RDF4JException;

/**
 * RDF Tutorial example 15: executing a simple SPARQL query on the database
 *
 * @author Jeen Broekstra
 */
public class RDF4jSparqlQueryTest362 {

	public RDF4jSparqlQueryTest362 () {}


	public static void main(String[] args) throws IOException {

		String inputfilename = "";
		String inputquery = "";

		if ( args.length < 1 || args.length > 2) {
			System.out.println ("\n\tUsage:  java -cp <classpath> filename ['sparq_squery_text']\n");
			System.out.println ("where the  file is an rdf/xml file, and the optional 'sparql_query_text' is singly quoted.\n");
			System.exit(1);
			}
		else {
			inputfilename = args[0];
			if ( args.length == 2 )
				inputquery = args[1];
			}

		RDF4jSparqlQueryTest362 test = new RDF4jSparqlQueryTest362();
		try { test.executeTest ( inputfilename, inputquery ); }
		catch (Exception e) { System.out.println("\nError on execution"); System.out.println (e.getMessage()); }
		}


	public void executeTest (String filename, String lquery) throws Exception {

		long startTime = 0;
		long endTime = 0;

		// Create a new Repository.
		Repository db = new SailRepository(new MemoryStore());
		db.init();

		// Open a connection to the database
		try (RepositoryConnection conn = db.getConnection()) {

			startTime = System.currentTimeMillis();
			try (InputStream input = Example15SimpleSPARQLQuery362.class.getResourceAsStream("/" + filename)) {
				// add the RDF data from the inputstream directly to our database
				conn.add(input, "", RDFFormat.RDFXML);
				}
			endTime = System.currentTimeMillis();
			System.out.println("Load of vocabulary took " + (endTime - startTime) + " mSec");

			Vector<String> queries = new Vector<String>();
			if ( lquery.equals("") )
				queries = getVectorOfQueries();
			else
				queries.add( lquery );


			for ( String queryString : queries ) {

				try {
					TupleQuery query = conn.prepareTupleQuery(queryString);
					// query.setMaxExecutionTime(30);	// doesn't work, https://github.com/eclipse/rdf4j/issues/636 & 430

					startTime = System.currentTimeMillis();
					try (TupleQueryResult result = query.evaluate()) {
						while (result.hasNext()) {
							BindingSet solution = result.next();
							Set<String> boundNames = solution.getBindingNames();
							for (String name : boundNames) {
								System.out.println(name);
								System.out.println(solution.getValue(name));
								}
							}
						}
					}
				catch (RDF4JException e) { System.out.println(e.getMessage()) ; }
				endTime = System.currentTimeMillis();
				System.out.println("sparql execution+processing took " + (endTime - startTime) + " mSec");
				}
			}
		finally { db.shutDown(); }
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
