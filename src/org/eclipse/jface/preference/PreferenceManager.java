package org.eclipse.jface.preference;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.*;

import org.eclipse.jface.util.Assert;
/**
 * A preference manager maintains a hierarchy of preference nodes and
 * associated preference pages.
 */
public class PreferenceManager {
	/**
	 * Pre-order traversal means visit the root first,
	 * then the children.
	 */
	public static final int PRE_ORDER = 0;

	/**
	 * Post-order means visit the children, and then the root.
	 */
	public static final int POST_ORDER = 1;

	/**
	 * The root node.
	 * Note that the root node is a special internal node
	 * that is used to collect together all the nodes that
	 * have no parent; it is not given out to clients.
	 */
	PreferenceNode root = new PreferenceNode("");//$NON-NLS-1$

	/**
	 * The path separator character.
	 */
	String separator;
/**
 * Creates a new preference manager.
 */
public PreferenceManager() {
	this('.');
}
/**
 * Creates a new preference manager with the given
 * the path separator.
 *
 * @param separatorChar the separator character
 */
public PreferenceManager(char separatorChar) {
	separator = new String(new char[] { separatorChar });
}
/**
 * Adds the given preference node as a subnode of the
 * node at the given path.
 *
 * @param path the path
 * @param node the node to add
 * @return <code>true</code> if the add was successful,
 *  and <code>false</code> if there is no contribution at
 *  the given path
 */
public boolean addTo(String path, IPreferenceNode node) {
	IPreferenceNode target = find(path);
	if (target == null)
		return false;
	target.add(node);
	return true;
}
/**
 * Adds the given preference node as a subnode of the
 * root.
 *
 * @param node the node to add, which must implement 
 *   <code>IPreferenceNode</code>
 */
public void addToRoot(IPreferenceNode node) {
	Assert.isNotNull(node);
	root.add(node);
}
/**
 * Recursively enumerates all nodes at or below the given node
 * and adds them to the given list in the given order.
 * 
 * @param node the starting node
 * @param sequence a read-write list of preference nodes
 *  (element type: <code>IPreferenceNode</code>)
 *  in the given order
 * @param order the traversal order, one of 
 *	<code>PRE_ORDER</code> and <code>POST_ORDER</code>
 */
protected void buildSequence(IPreferenceNode node, List sequence, int order) {
	if (order == PRE_ORDER)
		sequence.add(node);
	IPreferenceNode[] subnodes = node.getSubNodes();
	for (int i = 0; i < subnodes.length; i++){
		buildSequence(subnodes[i], sequence, order);
	}
	if (order == POST_ORDER)
		sequence.add(node);
}
/**
 * Finds and returns the contribution node at the given path.
 *
 * @param path the path
 * @return the node, or <code>null</code> if none
 */
public IPreferenceNode find(String path) {
	Assert.isNotNull(path);
	StringTokenizer stok = new StringTokenizer(path, separator);
	IPreferenceNode node = root;
	while (stok.hasMoreTokens()) {
		String id = stok.nextToken();
		node = node.findSubNode(id);
		if (node == null)
			return null;
	}
	if (node == root)
		return null;
	return node;
}
/**
 * Returns all preference nodes managed by this
 * manager.
 *
 * @param order the traversal order, one of 
 *	<code>PRE_ORDER</code> and <code>POST_ORDER</code>
 * @return a list of preference nodes
 *  (element type: <code>IPreferenceNode</code>)
 *  in the given order
 */
public List getElements(int order) {
	Assert.isTrue(
		order == PRE_ORDER || order == POST_ORDER,
		"invalid traversal order");//$NON-NLS-1$
	ArrayList sequence = new ArrayList();
	IPreferenceNode[] subnodes = getRoot().getSubNodes();
	for (int i = 0; i < subnodes.length; i++)
		buildSequence(subnodes[i], sequence, order);
	return sequence;
}
/**
 * Returns the root node.
 * Note that the root node is a special internal node
 * that is used to collect together all the nodes that
 * have no parent; it is not given out to clients.
 *
 * @return the root node
 */
protected IPreferenceNode getRoot() {
	return root;
}
/**
 * Removes the prefernece node at the given path.
 *
 * @param path the path
 * @return the node that was removed, or <code>null</code>
 *  if there was no node at the given path
 */
public IPreferenceNode remove(String path) {
	Assert.isNotNull(path);
	int index = path.lastIndexOf(separator);
	if (index == -1)
		return root.remove(path);
	// Make sure that the last character in the string isn't the "."
	Assert.isTrue(index < path.length() - 1, "Path can not end with a dot");//$NON-NLS-1$
	String parentPath = path.substring(0, index);
	String id = path.substring(index + 1);
	IPreferenceNode parentNode = find(parentPath);
	if (parentNode == null)
		return null;
	return parentNode.remove(id);
}
/**
 * Removes the given prefreence node if it is managed by
 * this contribution manager.
 *
 * @param node the node to remove
 * @return <code>true</code> if the node was removed,
 *  and <code>false</code> otherwise
 */
public boolean remove(IPreferenceNode node) {
	Assert.isNotNull(node);
	
	return root.remove(node);
}
/**
 * Removes all contribution nodes known to this manager.
 */
public void removeAll() {
	root = new PreferenceNode("");//$NON-NLS-1$
}
}
