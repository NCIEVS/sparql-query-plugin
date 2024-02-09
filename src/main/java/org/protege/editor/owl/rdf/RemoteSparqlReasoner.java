package org.protege.editor.owl.rdf;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.Query;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResultHandlerException;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.protege.editor.owl.rdf.repository.GraphQueryHandler;
import org.protege.editor.owl.rdf.repository.TupleQueryHandler;


public class RemoteSparqlReasoner implements SparqlReasoner {
	
	public static long tot_tim = 0;
	
	private String sparqlEndpoint = SPARQLPreferences.getServerLocation();
	private Repository repo = new SPARQLRepository(sparqlEndpoint);
	
	
	public RemoteSparqlReasoner(String endp) {
		sparqlEndpoint = endp;
		repo = new SPARQLRepository(sparqlEndpoint);
		repo.initialize();
		
		
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
	public SparqlResultSet executeQuery(String query, int timeout) throws SparqlReasonerException {
		try {
			
			
			//repo.getConnection().

			Query q = repo.getConnection().prepareQuery(QueryLanguage.SPARQL, query);

			
				
				if (q instanceof TupleQuery) {
					return handleTupleQuery((TupleQuery) q, timeout);
				}
				else if (q instanceof GraphQuery) {
					return handleGraphQuery((GraphQuery) q, timeout);
				}
				else if (q instanceof BooleanQuery) {
					return handleBooleanQuery((BooleanQuery) q, timeout);
				}
				else {
					throw new IllegalStateException("Can't handle queries of type " + query.getClass());
				}
			
			
		}
		catch (Exception e) {
			throw new SparqlReasonerException(e);
		}
	}

	private SparqlResultSet handleTupleQuery(TupleQuery query, int timeout) throws QueryEvaluationException, TupleQueryResultHandlerException {
		

		TupleQueryHandler handler = new TupleQueryHandler();
		if (timeout > 0) {
			query.setMaxExecutionTime(timeout);
		}
		query.evaluate(handler);
		System.out.println("total time spent in handler " + handler.getTotTime());
		System.out.println("total time spent in convertin anon nodes " + tot_tim);
		tot_tim = 0;
		return handler.getQueryResult();
		
		

		
		
		
		
	}
	
	private SparqlResultSet handleGraphQuery(GraphQuery graph, int timeout) throws QueryEvaluationException, RDFHandlerException {
		GraphQueryHandler handler = new GraphQueryHandler();
		if (timeout > 0) {
			graph.setMaxExecutionTime(timeout);
		}
		graph.evaluate(handler);
		return handler.getQueryResult();
	}
	
	private SparqlResultSet handleBooleanQuery(BooleanQuery booleanQuery, int timeout) throws QueryEvaluationException {
		List<String> columnNames = new ArrayList<String>();
		columnNames.add("Result");
		SparqlResultSet result = new SparqlResultSet(columnNames);
		List<Object> row = new ArrayList<Object>();
		if (timeout > 0) {
			booleanQuery.setMaxExecutionTime(timeout);
		}
		
		
		
		row.add(booleanQuery.evaluate() ? "True" : "False");
		result.addRow(row);
		return result;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

}
