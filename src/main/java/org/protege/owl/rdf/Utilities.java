package org.protege.owl.rdf;


import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.Sail;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.protege.owl.rdf.api.OwlTripleStore;
import org.protege.owl.rdf.impl.OwlTripleStoreImpl;
import org.protege.owl.rdf.impl.SynchronizeTripleStoreListener;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class Utilities {

	private Utilities() {
		
	}
	
	public static OwlTripleStore getOwlTripleStore(OWLOntologyManager m) throws RepositoryException {
		Sail sailStack = new MemoryStore();
		Repository repository = new SailRepository(sailStack);
		repository.initialize();
		return new OwlTripleStoreImpl(repository, m);
	}
	
	public static OwlTripleStore getOwlTripleStore(OWLOntologyManager manager, boolean sync) throws RepositoryException {
		OwlTripleStore ots = getOwlTripleStore(manager);
		for (OWLOntology ontology : manager.getOntologies()) {
			loadOwlTripleStore(ots, ontology, false);
		}
		if (sync) {
			synchronize(ots, manager);
		}
		return ots;
	}
	
	public static OwlTripleStore getOwlTripleStore(OWLOntology ontology, boolean sync) throws RepositoryException {
		OwlTripleStore ots = getOwlTripleStore(ontology.getOWLOntologyManager());
		loadOwlTripleStore(ots, ontology, sync);
		return ots;
	}

	public static void loadOwlTripleStore(OwlTripleStore ots, OWLOntology ontology, boolean sync) throws RepositoryException {
	    long beg = System.currentTimeMillis();
		ots.addAxioms(ontology.getOntologyID(), ontology.getAxioms());
		if (sync) {
			synchronize(ots, ontology.getOWLOntologyManager());
		}
		System.out.println("To load triple store with axioms and sync takes " + (System.currentTimeMillis() - beg));
	}
	
	public static void synchronize(OwlTripleStore ots, OWLOntologyManager manager) {
		manager.addOntologyChangeListener(new SynchronizeTripleStoreListener(ots));
	}
	
	public static boolean isInteger(String input) {
		try {
			Integer.parseInt(input);
			return true;
		} catch( NumberFormatException ex ) {
			return false;
		}
	}
}
