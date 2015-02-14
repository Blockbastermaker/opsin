package uk.ac.cam.ch.wwmm.opsin;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class SubstitutionAmbiguityChecker {

	static boolean isSubstitutionAmbiguous(List<Atom> substitutableAtoms, int numberToBeSubstituted) {
		if (substitutableAtoms.size() == 0) {
			throw new IllegalArgumentException("OPSIN Bug: Must provide at least one substituable atom");
		}
		if (substitutableAtoms.size() < numberToBeSubstituted) {
			throw new IllegalArgumentException("OPSIN Bug: substitutableAtoms must be >= numberToBeSubstituted");
		}
		if (substitutableAtoms.size() == numberToBeSubstituted){
			return false;
		}
		if (allAtomsConnectToDefaultInAtom(substitutableAtoms, numberToBeSubstituted)) {
			return false;
		}
		Set<Atom> uniqueAtoms = new HashSet<Atom>(substitutableAtoms);
		if (uniqueAtoms.size() == 1) {
			return false;
		}
		StereoAnalyser analyzer = analyzeRelevantAtomsAndBonds(uniqueAtoms);
		Set<String> uniqueEnvironments = new HashSet<String>();
		for (Atom a : substitutableAtoms) {
			Integer env = analyzer.getAtomEnvironmentNumber(a);
			if (env == null){
				throw new RuntimeException("OPSIN Bug: Atom was not part of ambiguity analysis");
			}
			uniqueEnvironments.add(env +"\t" + a.getOutValency());
		}
		if (uniqueEnvironments.size() == 1 && (numberToBeSubstituted == 1 || numberToBeSubstituted == substitutableAtoms.size() - 1)){
			return false;
		}
		return true;
	}

	private static boolean allAtomsConnectToDefaultInAtom(List<Atom> substitutableAtoms, int numberToBeSubstituted) {
		Atom defaultInAtom = substitutableAtoms.get(0).getFrag().getDefaultInAtom();
		if (defaultInAtom != null) {
			for (int i = 0; i < numberToBeSubstituted; i++) {
				if (!substitutableAtoms.get(i).equals(defaultInAtom)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	private static StereoAnalyser analyzeRelevantAtomsAndBonds(Set<Atom> startingAtoms) {
		Set<Atom> atoms = new HashSet<Atom>();
		Set<Bond> bonds = new HashSet<Bond>();
		Deque<Atom> stack = new ArrayDeque<Atom>(startingAtoms);
		while (!stack.isEmpty()) {
			Atom a = stack.removeLast();
			if (!atoms.contains(a)) {
				atoms.add(a);
				for (Bond b : a.getBonds()) {
					bonds.add(b);
					stack.add(b.getOtherAtom(a));
				}
			}
		}
		return new StereoAnalyser(atoms, bonds);
	}
}