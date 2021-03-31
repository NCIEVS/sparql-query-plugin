package org.protege.owl.rdf.impl;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.OWLObjectDuplicator;

public class AnonymousResourceHandler {
    public static final String ANONYMOUS_SURROGATE_PREFIX = "urn:AnonId:";
    
    private OWLDataFactory factory;
    private OWLOntologyManager man;
    
    private TreeMap<OWLOntologyID, IRI> ontologyIdToSurrogateIdMap = new TreeMap<OWLOntologyID, IRI>();
    private TreeMap<IRI, OWLOntologyID> surrogateIdToOntologyId = new TreeMap<IRI, OWLOntologyID>();
    
    private TreeMap<OWLAnonymousIndividual, IRI> anonymousIndividualToIRIMap = new TreeMap<OWLAnonymousIndividual, IRI>();
    private TreeMap<IRI, OWLAnonymousIndividual> iriToAnonymousIndividualMap = new TreeMap<IRI, OWLAnonymousIndividual>();
    
    public AnonymousResourceHandler(OWLOntologyManager m) {
    	this.man = m;
        this.factory = m.getOWLDataFactory();
    }
    
    public IRI getSurrogateId(OWLOntologyID id) {
        IRI result = ontologyIdToSurrogateIdMap.get(id);
        if (id.isAnonymous() && result == null) {
            result = createSurrogate();
            addSurrogateOntologyId(id, result);
        }
        return result;
    }
    
    public OWLOntologyID getOntologyID(IRI iri) {
        OWLOntologyID id = surrogateIdToOntologyId.get(iri);
        if (id == null && isSurrogate(iri)) {
            id = new OWLOntologyID();
            addSurrogateOntologyId(id, iri);
        }
        return id;
    }
    
    public IRI getSurrogateId(OWLAnonymousIndividual i) {
        IRI result = anonymousIndividualToIRIMap.get(i);
        if (result == null) {
            result = createSurrogate();
            addSurrogateIndividual(i, result);
        }
        return result;
    }
    
    public OWLAnonymousIndividual getAnonymousIndividual(IRI iri) {
        OWLAnonymousIndividual i = iriToAnonymousIndividualMap.get(iri);
        if (i == null) {
            i = factory.getOWLAnonymousIndividual();
            addSurrogateIndividual(i, iri);
        }
        return i;
    }

    public OWLAxiom removeSurrogates(OWLAxiom axiom) {
        return new SurrogateRemover(factory).duplicateObject(axiom);
    }
    
    public OWLAxiom insertSurrogates(OWLAxiom axiom) {
    	// no need to create duplicate
        //return new SurrogateInserter(factory).duplicateObject(axiom);
    	return axiom;
    }
    
    public boolean isSurrogate(IRI iri) {
        return iri.toString().startsWith(ANONYMOUS_SURROGATE_PREFIX);
    }
    
    private void addSurrogateOntologyId(OWLOntologyID id, IRI iri) {
        ontologyIdToSurrogateIdMap.put(id, iri);
        surrogateIdToOntologyId.put(iri, id);
    }
    
    private void addSurrogateIndividual(OWLAnonymousIndividual i, IRI iri) {
        anonymousIndividualToIRIMap.put(i, iri);
        iriToAnonymousIndividualMap.put(iri, i);
    }
    
    private IRI createSurrogate() {
        UUID id = UUID.randomUUID();
        String idString = id.toString().replaceAll("-", "_");
        return IRI.create(ANONYMOUS_SURROGATE_PREFIX + idString);
    }
    
    private Set<OWLAnnotation> duplicateAxiomAnnotations(OWLAxiom axiom, OWLObjectDuplicator duplicator) {
        Set<OWLAnnotation> duplicatedAnnos = new HashSet<OWLAnnotation>();
        for(OWLAnnotation anno : axiom.getAnnotations()) {
            duplicatedAnnos.add((OWLAnnotation) duplicator.duplicateObject(anno));
        }
        return duplicatedAnnos;
    }
    
    
    
    private class SurrogateRemover extends OWLObjectDuplicator {
        
        public SurrogateRemover(OWLDataFactory factory) {
            super(man);
        }
    }
}
