package org.protege.editor.owl.rdf.repository;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rdf4j.query.Binding;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryResultHandlerException;
import org.eclipse.rdf4j.query.TupleQueryResultHandler;
import org.eclipse.rdf4j.query.TupleQueryResultHandlerException;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.model.Value;
import org.protege.editor.owl.rdf.SparqlResultSet;
import org.protege.owl.rdf.api.OwlTripleStore;

public class TupleQueryHandler implements TupleQueryResultHandler {
	private OwlTripleStore triples;
	private SparqlResultSet queryResult;
	private long tot_tim = 0;
	
	public long getTotTime() { return tot_tim; }
	
	public TupleQueryHandler(OwlTripleStore triples) {
		this.triples = triples;
	}
	
	public SparqlResultSet getQueryResult() {
		return queryResult;
	}

	@Override
	public void startQueryResult(List<String> bindingNames) throws TupleQueryResultHandlerException {
		queryResult = new SparqlResultSet(bindingNames);
	}

	@Override
	public void handleSolution(BindingSet bindingSet) throws TupleQueryResultHandlerException {
		long beg = System.currentTimeMillis();
		try {
			List<Object> row = new ArrayList<Object>();
			for (int i = 0; i < queryResult.getColumnCount(); i++) {
				String columnName = queryResult.getColumnName(i);
				Binding binding = bindingSet.getBinding(columnName);
				Value v = binding != null ? binding.getValue() : (Value) null;
				row.add(Util.convertValue(triples, v));
			}
			queryResult.addRow(row);
			tot_tim += (System.currentTimeMillis() - beg);
		}
		catch (RepositoryException re) {
			throw new TupleQueryResultHandlerException(re);
		}
	}
	
	@Override
	public void endQueryResult() throws TupleQueryResultHandlerException {

	}

	@Override
	public void handleBoolean(boolean arg0) throws QueryResultHandlerException {
		
	}

	@Override
	public void handleLinks(List<String> arg0) throws QueryResultHandlerException {
		
	}

}
