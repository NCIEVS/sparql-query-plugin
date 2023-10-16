package org.protege.editor.owl.rdf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.rdf4j.RDF4JException;
import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.Query;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.TupleQueryResultHandlerException;
import org.eclipse.rdf4j.query.resultio.helpers.QueryResultCollector;
import org.eclipse.rdf4j.query.resultio.text.csv.SPARQLResultsCSVWriter;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;


public class RemoteSparqlReasoner implements SparqlReasoner {
	
	private String sparqlEndpoint = "http://localhost:8890/sparql";
	private Repository repo = new SPARQLRepository(sparqlEndpoint);
	
	public RemoteSparqlReasoner(String endp) {
		sparqlEndpoint = endp;
		repo = new SPARQLRepository(sparqlEndpoint);
		
		
	}

	@Override
	public void precalculate() throws SparqlReasonerException {
		// TODO Auto-generated method stub

	}

	@Override
	public String getSampleQuery() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SparqlResultSet executeQuery(String query, int integer) throws SparqlReasonerException {
		try {
			
			
			//repo.getConnection().

			TupleQuery q = repo.getConnection().prepareTupleQuery(QueryLanguage.SPARQL, query);

			if (q instanceof TupleQuery) {
				return handleTupleQuery(q, 3000);


			}
		}
		catch (Exception e) {
			throw new SparqlReasonerException(e);
		}
		return null;
	}

	private SparqlResultSet handleTupleQuery(TupleQuery query, int timeout) throws QueryEvaluationException, TupleQueryResultHandlerException {
		

		
		
		List<BindingSet> resultList;
		try (TupleQueryResult result = query.evaluate()) {
			resultList = QueryResults.asList(result);
			
			SparqlResultSet nrs = null;
			
			

			for (BindingSet bs: resultList) {
				Set<String> names = bs.getBindingNames();
				List<String> lnames = new ArrayList<String>(names);
				nrs = new SparqlResultSet(lnames); 
				Iterator<Binding> it = bs.iterator();
				while (it.hasNext()) {
					Binding b = it.next();

					System.out.println((b.getName() + " " + b.getValue()));
				}



			}
			return nrs;
		}
		catch (RDF4JException e) {
			e.printStackTrace();
		}
		
		return null;

		
		
		
		
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

}
