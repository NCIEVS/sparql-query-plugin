package org.protege.editor.owl.rdf.repository;

import org.openrdf.model.Value;
import org.openrdf.repository.RepositoryException;
import org.openrdf.sail.memory.model.MemLiteral;

public class Util {
	public static long tot_tim = 0;
	private Util() {
		
	}

	public static Object convertValue(Value v) throws RepositoryException {
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
