package org.protege.editor.owl.rdf;

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
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.protege.editor.owl.rdf.repository.TupleQueryHandler;

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

			Query q = repo.getConnection().prepareQuery(QueryLanguage.SPARQL, query);

			if (q instanceof TupleQuery) {
				return handleTupleQuery((TupleQuery) q, 3000);


			}
		}
		catch (Exception e) {
			throw new SparqlReasonerException(e);
		}
		return null;
	}

	private SparqlResultSet handleTupleQuery(TupleQuery tupleQuery, int timeout) throws QueryEvaluationException, TupleQueryResultHandlerException {
		TupleQueryHandler handler = new TupleQueryHandler();
		if (timeout > 0) {
			tupleQuery.setMaxQueryTime(timeout);
		}
		tupleQuery.evaluate(handler);
		
		return handler.getQueryResult();
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

}
