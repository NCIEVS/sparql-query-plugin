package org.protege.editor.owl.rdf.repository;

import org.openrdf.model.BNode;
import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryException;
import org.openrdf.sail.memory.model.MemLiteral;
import org.protege.owl.rdf.api.OwlTripleStore;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;

public class Util {
	public static long tot_tim = 0;
	private Util() {
		
	}

	public static Object convertValue(OwlTripleStore triples, Value v) throws RepositoryException {
		Object converted = v;
		
		if (v instanceof MemLiteral) {
			if ((((MemLiteral) v).getDatatype() != null) ||
					(((MemLiteral) v).getLanguage() != null) 
					&& (!((MemLiteral) v).getLanguage().equals(""))) {
				converted = v;
			} else {
				converted = ((MemLiteral) v).getLabel();
			}
		}
		
		
		return converted;
	}
	
	
}
