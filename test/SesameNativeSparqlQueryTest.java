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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.Vector;

import org.openrdf.OpenRDFException;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.nativerdf.NativeStore;



/**
 * RDF Tutorial example 15: executing a simple SPARQL query on the database
 *
 * @author Jeen Broekstra
 */
public class SesameNativeSparqlQueryTest
{

	public SesameNativeSparqlQueryTest () {}


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

		SesameNativeSparqlQueryTest test = new SesameNativeSparqlQueryTest();
		try { test.executeTest ( inputfilename, inputquery ); }
		catch (Exception e) { 
			System.out.println("\nError on execution"); System.out.println (e.getMessage()); }
		}


	public void executeTest (String filename, String lquery) throws Exception {

		long startTime = 0;
		long endTime = 0;

		// Create a new Repository.
		//Repository db = new SailRepository(new MemoryStore());
		//db.init();
		
		File dataDir = new File("./datadir/");
		Repository db = new SailRepository(new NativeStore(dataDir));
		db.initialize();
		
		RepositoryConnection conn = null;

		// Open a connection to the database
		try {
			conn = db.getConnection();

			startTime = System.currentTimeMillis();
			try (InputStream input = SesameNativeSparqlQueryTest.class.getResourceAsStream("/" + filename)) {
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
					TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
					query.setMaxQueryTime(30);

					startTime = System.currentTimeMillis();
					try {
						TupleQueryResult result = query.evaluate(); 
						while (result.hasNext()) {
							BindingSet solution = result.next();
							Set<String> boundNames = solution.getBindingNames();
							for (String name : boundNames) {
								//System.out.println(name);
								//System.out.println(solution.getValue(name));
							}
						}
						result.close();
					} catch (Exception e) { 						
						System.out.println(e.getMessage()) ; 
					}

					endTime = System.currentTimeMillis();
					System.out.println("sparql execution+processing took " + (endTime - startTime) + " mSec");

				}
				catch (OpenRDFException e) { 
					throw new Exception (e.getMessage()); }
			}
			
			conn.commit();
			conn.close();
		}
			
		finally { db.shutDown(); }
		}


	public Vector<String> getSmallVectorOfQueries () {
		Vector<String> tmp = new Vector<String>();
		String query = "";

		// just warm up any cache, sesame, OS, what-have-you
		query = "select (count(?s) as ?warm_up_any_cache) where { ?s ?p ?o }";
		tmp.add(query);

		return tmp;
	}
	public Vector<String> getVectorOfQueries () {
		Vector<String> tmp = new Vector<String>();
		String query = "";

		// just warm up any cache, sesame, OS, what-have-you
		query = "select (count(?s) as ?warm_up_any_cache) where { ?s ?p ?o }";
		tmp.add(query);

		// count A8
		query = "PREFIX ncit: <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#> SELECT (count(distinct ?concept_code) as ?count_concepts_in_valueSet ) { ?c ncit:A8 ?concept_code . }";
		tmp.add(query);

		// count concepts in branch
		query = "prefix ncit:  <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#> select (count(distinct ?subject) as ?total_concept_count) where { values ?superclass { ncit:C25791 } ?subject (rdfs:subClassOf|(owl:equivalentClass/owl:intersectionOf/rdf:rest* /rdf:first) )* ?superclass . }";
		tmp.add(query);

		// count defined concepts in branch
		query = "prefix ncit:  <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#> select (count(distinct ?subject) as ?defined_concept_count) where { values ?superclass { ncit:C25791 } ?subject owl:equivalentClass ?an ; (rdfs:subClassOf|(owl:equivalentClass/owl:intersectionOf/rdf:rest* /rdf:first) )* ?superclass . }";
		tmp.add(query);

		// count modeled defined concepts in branch
		query = "prefix ncit:  <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#> select (count(distinct ?subject) as ?modeled_defined_concept_count) where { values ?superclass { ncit:C4196 } ?subject (rdfs:subClassOf|(owl:equivalentClass/owl:intersectionOf/rdf:rest* /rdf:first) )* ?superclass ; owl:equivalentClass ?eq . ?eq owl:intersectionOf ?in . ?in rdf:rest* ?re . ?re rdf:first ?an . ?an owl:onProperty ?role . }";
		tmp.add(query);

		// count modeled primitive concepts in branch
		query = "prefix ncit:  <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#> select (count(distinct ?subject) as ?modeled_primitive_concept_count) where { values ?superclass { ncit:C25791 } ?subject rdfs:subClassOf ?an . ?an owl:onProperty ?role . ?subject  (rdfs:subClassOf|(owl:equivalentClass/owl:intersectionOf/rdf:rest* /rdf:first) )* ?superclass . }";
		tmp.add(query);

		// count partially modeled concepts in branch, actually, this should be hidden GCIs
        query = "prefix ncit:  <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#> select (count(distinct ?subject) as ?partially_modeled_concept_count) where { values ?superclass { ncit:C7057 } ?subject rdfs:subClassOf ?an ; owl:equivalentClass ?an2 ; (rdfs:subClassOf|(owl:equivalentClass/owl:intersectionOf/rdf:rest* /rdf:first) )* ?superclass . }";
		tmp.add(query);

		// count primitive concept in branch
		query = "prefix ncit:  <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#> select (count(distinct ?subject) as ?primitive_concept_count) where { values ?superclass { ncit:C25791 } ?subject rdfs:subClassOf ?an ; (rdfs:subClassOf|(owl:equivalentClass/owl:intersectionOf/rdf:rest* /rdf:first) )* ?superclass . }";
		tmp.add(query);

		// count roles defined concepts in branch
		query = "prefix ncit:  <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#> select  (count(?prop) as ?defined_concept_role_count) where { values ?superclass { ncit:C25791 } ?subject (rdfs:subClassOf|(owl:equivalentClass/owl:intersectionOf/rdf:rest* /rdf:first) )* ?superclass ; owl:equivalentClass ?eq . ?eq owl:intersectionOf ?in . ?in rdf:rest* ?re . ?re rdf:first ?an . ?an owl:onProperty ?prop . }";
		tmp.add(query);

		// count roles in branch
		query = "prefix ncit:  <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#> select  (count(?prop) as ?total_role_count) where { values ?superclass { ncit:C3262 } { ?subject (rdfs:subClassOf|(owl:equivalentClass/owl:intersectionOf/rdf:rest* /rdf:first) )* ?superclass ; owl:equivalentClass ?eq . ?eq owl:intersectionOf ?in . ?in rdf:rest* ?re . ?re rdf:first ?an . ?an owl:onProperty ?prop . } union { ?subject rdfs:subClassOf ?an ; (rdfs:subClassOf|(owl:equivalentClass/owl:intersectionOf/rdf:rest* /rdf:first) )* ?superclass . ?an owl:onProperty ?prop . } }";
		tmp.add(query);

		// count roles primitive concepts in branch
		query = "prefix ncit:  <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#> select  (count(?an) as ?primitive_concept_role_count) where { values ?superclass { ncit:C25791 } ?subject rdfs:subClassOf ?an ; (rdfs:subClassOf|(owl:equivalentClass/owl:intersectionOf/rdf:rest* /rdf:first) )* ?superclass . ?an owl:onProperty ?role . }";
		tmp.add(query);

		// CPTAC
		query = "prefix ncit: <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#> select distinct (str(?ncicode) as ?code) (str(?ncipt) as ?nci_pt) (str(?cptacpt) as ?cptac_pt) (str(?cptacsy) as ?cptac_sy) where { values ?some_subset { ncit:C158035 ncit:C158037 ncit:C158036 ncit:C156966 ncit:C158034 } ?s ncit:NHC0 ?ncicode ; rdfs:label ?ncipt ; ncit:A8 ?some_subset . [] a owl:Axiom ; owl:annotatedSource ?s ; owl:annotatedTarget ?cptacpt ; owl:annotatedProperty ncit:P90 ; ncit:P384 ?tsp ; ncit:P383 ?ttp; filter regex (str(?ttp), \"PT\") filter regex (str(?tsp), \"CPTAC\") optional { [] a owl:Axiom ; owl:annotatedSource ?s ; owl:annotatedTarget ?cptacsy ; owl:annotatedProperty ncit:P90 ; ncit:P384 ?tss ; ncit:P383 ?tts; filter regex (str(?tts), \"SY\") filter regex (str(?tss), \"CPTAC\") } } limit 10";
		tmp.add(query);

		// CPTAC 157075
		query = "prefix ncit: <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#> select distinct (str(?ncicode) as ?code) (str(?ncipt) as ?nci_pt) (str(?cptacpt) as ?cptac_pt) (str(?cptacsy) as ?cptac_sy) (str(?ncidef) as ?nci_def) where { values ?some_subset { ncit:C157075} ?s ncit:NHC0 ?ncicode ; rdfs:label ?ncipt ; ncit:A8 ?some_subset . [] a owl:Axiom ; owl:annotatedSource ?s ; owl:annotatedTarget ?cptacpt ; owl:annotatedProperty ncit:P90 ; ncit:P384 ?tsp ; ncit:P383 ?ttp; filter regex (str(?ttp), \"PT\") filter regex (str(?tsp), \"CPTAC\") . optional { [] a owl:Axiom ; owl:annotatedSource ?s ; owl:annotatedTarget ?cptacsy ; owl:annotatedProperty ncit:P90 ; ncit:P384 ?tss ; ncit:P383 ?tts filter regex (str(?tts), \"SY\") filter regex (str(?tss), \"CPTAC\") } optional { [] a owl:Axiom ; owl:annotatedSource ?s ; owl:annotatedTarget ?ncidef ; owl:annotatedProperty ncit:P97 }} limit 10";
		tmp.add(query);

		// CTRP report
		query = "prefix ncit: <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#> select distinct ?code ?preferredname ?displayname ?fullsyn1 where { ?s ncit:NHC0 ?code . ?s ncit:P90 ?fullsyn . ?s ncit:P108 ?preferredname . ?s ncit:P107 ?displayname . [] a owl:Axiom ; owl:annotatedSource ?s ; owl:annotatedTarget ?fullsyn1 ; owl:annotatedProperty ncit:P90 ; ncit:P384 ?ts ; ncit:P383 ?tt . filter regex(str(?tt), \"DN\") . filter regex(str(?ts), \"CTRP\") . } limit 10";
		tmp.add(query);

		// empty value set
		query = "prefix ncit: <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#> SELECT distinct ?code ?preferred_name { ?x ncit:P372 \"Yes\"^^<http://www.w3.org/2001/XMLSchema#string> ; ncit:P108 ?preferred_name ; ncit:NHC0 ?code FILTER NOT EXISTS {?y ncit:A8 ?x} } limit 10";
		tmp.add(query);

		// PQCMC
		query = "prefix ncit: <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#> select distinct (str(?ncicode) as ?code) (str(?ncipt) as ?nci_pt) (str(?fdapt) as ?fda_pt) (str(?pq_alt_def) as ?alt_def_pqcmc) where { values ?some_subset { ncit:C133805 } ?s ncit:NHC0 ?ncicode ; rdfs:label ?ncipt ; ncit:A8 ?some_subset . [] a owl:Axiom ; owl:annotatedSource ?s ; owl:annotatedTarget ?fdapt ; owl:annotatedProperty ncit:P90 ; ncit:P386 ?subsource; ncit:P384 ?tsp ; ncit:P383 ?ttp; filter regex (str(?ttp), \"PT\") filter regex (str(?tsp), \"FDA\") filter regex(str(?subsource), \"PQCMC\") . [] a owl:Axiom ; owl:annotatedSource ?s ; owl:annotatedProperty ncit:P325 ; owl:annotatedTarget ?pq_alt_def; ncit:P378 ?defsource; filter regex(str(?defsource), \"PQCMC\") . } limit 10";
		tmp.add(query);

		// PQCMC Definitions
		query = "prefix ncit: <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#> select distinct (str(?ncicode) as ?code) (str(?ncipt) as ?nci_pt) (str(?fdapt) as ?fda_pt) (str(?pq_alt_def) as ?alt_def_pqcmc) where { values ?some_subset { ncit:C133805 } ?s ncit:NHC0 ?ncicode ; rdfs:label ?ncipt ; ncit:A8 ?some_subset . { [] a owl:Axiom ; owl:annotatedSource ?s ; owl:annotatedTarget ?fdapt ; owl:annotatedProperty ncit:P90 ; ncit:P386 ?subsource; ncit:P384 ?tsp ; ncit:P383 ?ttp; filter regex (str(?ttp), \"PT\") filter regex (str(?tsp), \"FDA\") filter regex(str(?subsource), \"PQCMC\") . optional { [] a owl:Axiom ; owl:annotatedSource ?s ; owl:annotatedProperty ncit:P325 ; owl:annotatedTarget ?pq_alt_def; ncit:P378 ?defsource; filter regex(str(?defsource), \"PQCMC\") . } } union { [] a owl:Axiom ; owl:annotatedSource ?s ; owl:annotatedProperty ncit:P325 ; owl:annotatedTarget ?pq_alt_def; ncit:P378 ?defsource; filter regex(str(?defsource), \"PQCMC\") . optional { [] a owl:Axiom ; owl:annotatedSource ?s ; owl:annotatedTarget ?fdapt ; owl:annotatedProperty ncit:P90 ; ncit:P386 ?subsource; ncit:P384 ?tsp ; ncit:P383 ?ttp; filter regex (str(?ttp), \"PT\") filter regex (str(?tsp), \"FDA\") filter regex(str(?subsource), \"PQCMC\") . } } } limit 10";
		tmp.add(query);

		// QA multiple URI
		query = "prefix ncit: <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#> select (str(?ccode) as ?code) (str(?clabel) as ?label) (str(?o) as ?property_value) where { ?s ?p ?o filter regex(str(?o), \"http.+http\") . ?p rdfs:label ?clabel . ?s ncit:NHC0 ?ccode . } limit 10";
		tmp.add(query);

		// QA non breaking space
		query = "prefix ncit: <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#> select (str(?ccode) as ?code) (str(?clabel) as ?label) (str(?o) as ?property_value) where { ?s ?p ?o filter regex(str(?o), \"[\u00A0]\") . ?p rdfs:label ?clabel . ?s ncit:NHC0 ?ccode . } limit 10";
		tmp.add(query);


		// QA terminology set publish location empty
		query = "PREFIX ncit:<http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#> SELECT (str(?lcode) as ?code) (str(?val_set_label) as ?label) (str(?lpublish_value) as ?publish_value) (str(?lvalue_location) as ?value_location) (str(?lempty) as ?empty) WHERE { values ?terminology_sets { ncit:C54443 } values ?publish { ncit:P372 } values ?location { ncit:P374 } values ?concept_in_subset { ncit:A8 } ?val_set rdfs:subClassOf+ ?terminology_sets ; rdfs:label ?val_set_label ; ncit:NHC0 ?lcode . optional { ?val_set ?publish ?lpublish_value . } optional { ?val_set ?location ?lvalue_location . } bind( not exists { ?concept ?concept_in_subset ?val_set } as ?lempty) } order by asc(?label)";
		tmp.add(query);

		// roles defined concepts in branch
		query = "prefix ncit: <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#> select (str(?subject_label) as ?defined_concept) (str(?prop_label) as ?role) (str(?filler_label) as ?target) where { values ?superclass { ncit:C25791 } ?subject (rdfs:subClassOf|(owl:equivalentClass/owl:intersectionOf/rdf:rest* /rdf:first) )* ?superclass ; owl:equivalentClass ?eq . ?eq owl:intersectionOf ?in . ?in rdf:rest* ?re . ?re rdf:first ?an . ?an owl:onProperty ?prop ; owl:someValuesFrom ?filler . ?filler rdfs:label ?filler_label . ?prop rdfs:label ?prop_label . ?subject rdfs:label ?subject_label . } order by ?concept ?role ?target limit 10";
		tmp.add(query);

		// roles in branch
		query = "prefix ncit: <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#> select (str(?subject_label) as ?concept) (str(?prop_label) as ?role) (str(?filler_label) as ?target) where { values ?superclass { ncit:C3262 } { ?subject (rdfs:subClassOf|(owl:equivalentClass/owl:intersectionOf/rdf:rest* /rdf:first) )* ?superclass ; owl:equivalentClass ?eq ; rdfs:label ?subject_label . ?eq owl:intersectionOf ?in . ?in rdf:rest* ?re . ?re rdf:first ?an . ?an owl:onProperty ?prop ; owl:someValuesFrom ?filler . ?filler rdfs:label ?filler_label . ?prop rdfs:label ?prop_label . } union { ?subject rdfs:subClassOf ?an ; (rdfs:subClassOf|(owl:equivalentClass/owl:intersectionOf/rdf:rest* /rdf:first) )* ?superclass ; rdfs:label ?subject_label . ?an owl:onProperty ?prop ; owl:someValuesFrom ?filler . ?filler rdfs:label ?filler_label . ?prop rdfs:label ?prop_label . } } order by ?concept ?role ?target  limit 10";
		tmp.add(query);

		// roles primitive concepts in branch
		query = "prefix ncit: <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#> select (str(?subject_label) as ?primitive_concept) (str(?prop_label) as ?role) (str(?filler_label) as ?target) where { values ?superclass { ncit:C25791 } ?subject rdfs:subClassOf ?an ; (rdfs:subClassOf|(owl:equivalentClass/owl:intersectionOf/rdf:rest* /rdf:first) )* ?superclass . ?an owl:onProperty ?prop ; owl:someValuesFrom ?filler . ?subject rdfs:label ?subject_label . ?prop rdfs:label ?prop_label . ?filler rdfs:label ?filler_label . } order by ?concept ?role ?target";
		tmp.add(query);

		// semicolon
		query = "prefix ncit: <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#> select distinct ?code ?o where { ?s ncit:NHC0 ?code . ?s ncit:P90 ?o . filter regex(str(?o), \"; \") . }  limit 10";
		tmp.add(query);

		// semicolon end
		query = "prefix ncit: <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#> select distinct ?code ?o where { ?s ncit:NHC0 ?code . ?s ncit:P90 ?o . filter regex(str(?o), \";$\") . }  limit 10";
		tmp.add(query);

		// subset unannotated
		query = "prefix ncit: <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#> select distinct (str(?concept_pt) as ?pt) (str(?concept_code) as ?code) where { values ?note_value { \"CPTAC24July\" } ?s rdfs:label ?concept_pt ; ncit:NHC0 ?concept_code . { select ?s 	{ values ?subset { ncit:C159328 } ?s ncit:A8 ?subset . } } filter not exists {?s ncit:P95 ?editor_note . filter regex (str(?editor_note), ?note_value)} } order by ?pt  limit 10";
		tmp.add(query);

		// UNII report
		query = "prefix ncit: <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#> select distinct ?code ?uniiprop ?fullsyn1 where { ?s ncit:NHC0 ?code . ?s ncit:P90 ?fullsyn . ?s ncit:P319 ?uniiprop . [] a owl:Axiom ; owl:annotatedSource ?s ; owl:annotatedTarget ?fullsyn1 ; owl:annotatedProperty ncit:P90 ; ncit:P384 ?ts ; ncit:P383 ?tt ; ncit:P385 ?sourcecode ; ncit:P386 ?subsource . filter regex (str(?tt), \"PT\") . filter regex (str(?subsource), \"UNII\") . } limit 10";
		tmp.add(query);

		// UNII QA
		query = "prefix ncit: <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#> select distinct ?code ?fullsyn1 ?sourcecode ?uniiprop where { ?s ncit:NHC0 ?code . ?s ncit:P90 ?fullsyn . ?s ncit:P319 ?uniiprop . [] a owl:Axiom ; owl:annotatedSource ?s ; owl:annotatedTarget ?fullsyn1 ; owl:annotatedProperty ncit:P90 ; ncit:P384 ?ts ; ncit:P385 ?sourcecode ; ncit:P386 ?subsource . filter regex(str(?ts), \"FDA\") . filter regex(str(?subsource), \"UNII\") . } limit 10";
		tmp.add(query);

		// test strafter string function
		query = "select (strafter(str(?s), \"#\") as ?iri) where { ?s ?p ?o } limit 10";
		tmp.add(query);

		// test group_concat aggregation
		query = "prefix ncit: <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#> select (str(?lab) as ?label) (group_concat(distinct str(?syn) ; separator=\"|\") as ?syns) where { ?s ncit:P90 ?syn ; a owl:Class ; rdfs:label ?lab . } group by ?lab limit 10";
		tmp.add(query);

		// test concat string function
		query = "prefix ncit: <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#> select distinct ?code (concat( str(?fullsyn1), \"/\", str(?sourcecode)) as ?fullSyn_code) ?uniiprop where { ?s ncit:NHC0 ?code . ?s ncit:P90 ?fullsyn . ?s ncit:P319 ?uniiprop . [] a owl:Axiom ; owl:annotatedSource ?s ; owl:annotatedTarget ?fullsyn1 ; owl:annotatedProperty ncit:P90 ; ncit:P384 ?ts ; ncit:P385 ?sourcecode ; ncit:P386 ?subsource . filter regex(str(?ts), \"FDA\") . filter regex(str(?subsource), \"UNII\") . } limit 10";
		tmp.add(query);

		// test service keyword (federated query) throws exception if endpoint is not there
		//query = "select distinct ?s ?label where { ?s rdfs:label ?label . service <http://centipede.fios-router.home:8890/sparql/> { ?s ?x ?y { select ?s where { graph <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl> {?s ?p ?o ; a owl:AnnotationProperty . } } } } }";
		//tmp.add(query);

/*		// git 435 filter not exists doesn't terminate
		query = "prefix ncit: <http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#> prefix owl: <http://www.w3.org/2002/07/owl#> select distinct ?code ?term where { ?con ncit:NHC0 ?code ; ncit:P90 ?term . filter not exists {[] owl:annotatedSource ?con ; ncit:P384 ?annval . } } order by ?code";
		tmp.add(query);

		// git 409 find dark matter (untyped classes/individuals)
		query = "select * where { ?s ?p ?o filter ( !isBlank(?s) ) . minus { { ?s a owl:Class } union {?s a owl:AnnotationProperty} union {?s a owl:ObjectProperty} union { ?s a owl:Datatype } } }";
		tmp.add(query);
*/


		return tmp;
		}
}
