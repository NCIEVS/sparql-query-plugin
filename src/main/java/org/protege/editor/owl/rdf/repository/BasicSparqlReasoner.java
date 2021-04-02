package org.protege.editor.owl.rdf.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.Query;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResultHandlerException;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.protege.editor.owl.rdf.SparqlReasoner;
import org.protege.editor.owl.rdf.SparqlReasonerException;
import org.protege.editor.owl.rdf.SparqlResultSet;
import org.protege.owl.rdf.Utilities;
import org.protege.owl.rdf.api.OwlTripleStore;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.NamespaceUtil;

public class BasicSparqlReasoner implements SparqlReasoner {
	private OWLOntologyManager manager;
	private OwlTripleStore triples;
	
	public BasicSparqlReasoner(OWLOntologyManager manager) {
		this.manager = manager;
	}
	
	@Override
	public String getSampleQuery() {
		StringBuffer sb = new StringBuffer();
		NamespaceUtil nsUtil = new NamespaceUtil();
		for (Entry<String, String> entry : nsUtil.getNamespace2PrefixMap().entrySet()) {
			String ns = entry.getKey();
			String prefix = entry.getValue();
			sb.append("PREFIX ");
			sb.append(prefix);
			sb.append(": <");
			sb.append(ns);
			sb.append(">\n");
		}
		sb.append("SELECT ?subject ?object\n\tWHERE { ?subject rdfs:subClassOf ?object }");
		return sb.toString();
	}

	@Override
	public void precalculate() throws SparqlReasonerException {
		if (triples == null) {
			try {
				triples = Utilities.getOwlTripleStore(manager, true);
			}
			catch (RepositoryException e) {
				throw new SparqlReasonerException(e);
			}
		}
	}
	
	@Override
	public SparqlResultSet executeQuery(String queryString, int timeout) throws SparqlReasonerException {
		precalculate();
		try {
			RepositoryConnection connection = null;
			try {
				connection = triples.getRepository().getConnection();
				Query query = connection.prepareQuery(QueryLanguage.SPARQL, queryString);
				if (query instanceof TupleQuery) {
					return handleTupleQuery((TupleQuery) query, timeout);
				}
				else if (query instanceof GraphQuery) {
					return handleGraphQuery((GraphQuery) query, timeout);
				}
				else if (query instanceof BooleanQuery) {
					return handleBooleanQuery((BooleanQuery) query, timeout);
				}
				else {
					throw new IllegalStateException("Can't handle queries of type " + query.getClass());
				}
			}
			finally {
				if (connection != null) {
					connection.close();
				}
			}
		}
		catch (Exception e) {
			throw new SparqlReasonerException(e);
		}
	}
	
	private SparqlResultSet handleTupleQuery(TupleQuery tupleQuery, int timeout) throws QueryEvaluationException, TupleQueryResultHandlerException {
		TupleQueryHandler handler = new TupleQueryHandler(triples);
		if (timeout > 0) {
			tupleQuery.setMaxExecutionTime(timeout);
		}
		tupleQuery.evaluate(handler);
		System.out.println("total time spent in handler " + handler.getTotTime());
		System.out.println("total time spent in convertin anon nodes " + Util.tot_tim);
		Util.tot_tim = 0;
		return handler.getQueryResult();
	}
	
	private SparqlResultSet handleGraphQuery(GraphQuery graph, int timeout) throws QueryEvaluationException, RDFHandlerException {
		GraphQueryHandler handler = new GraphQueryHandler(triples);
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
		
	}

}
