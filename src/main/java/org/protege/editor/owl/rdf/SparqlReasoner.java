package org.protege.editor.owl.rdf;

public interface SparqlReasoner {
	
	void precalculate() throws SparqlReasonerException;
	
	String getSampleQuery();
	
	SparqlResultSet executeQuery(String query, int timeout) throws SparqlReasonerException;
	
	void dispose();
}
