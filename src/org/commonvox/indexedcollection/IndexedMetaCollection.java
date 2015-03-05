/*
 * Copyright (C) 2014 Daniel Vimont
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.commonvox.indexedcollection;

import java.util.Arrays;
import java.util.List;


/** Container class for a set of IndexNodes. All IndexNodes in such a set
 * provide mapping for the same types of objects: objects which are instances 
 * of the class or subclasses of the class denoted by {@literal <V>}.
 * Class {@literal <V>} is referred to as the MasterClass of the IndexedMetaCollection.
 * @author Daniel Vimont
 * @param <V> The MasterClass of the IndexedMetaCollection. All IndexNode objects managed
 by a IndexedMetaCollection object provide mapping for objects of class (or subclass of
 the class) denoted by {@literal <V>}.
 */
public abstract class IndexedMetaCollection<V> {
    final Class<V> MASTER_CLASS;
    IndexNode<IndexNode<V>> metamap; 
    int maxDepth = 0;
    
    /**
     *
     * @param masterClass
     */
    public IndexedMetaCollection (Class<V> masterClass) {
        this.MASTER_CLASS = masterClass;
    }

    /**
     * This method takes the array of IndexNode objects that is passed to it
 and constructs a metamap (a IndexNode of CollectionIndexes) that forms the 
 centerpiece of the IndexedMetaCollection.
     * @param title Title of the directory.
     * @param indexNodeArray Array of IndexNode objects, all of which 
 provide various mappings for the same set of objects: instances of 
 class {@literal <V>}. {@literal <V>} is referred to as the MasterClass
 of the IndexedMetaCollection.
     * @throws InvalidMultiKeyException
     * @throws org.commonvox.indexedcollection.IndexedCollectionBuildFailureException
     */
    @SafeVarargs
    public final void build 
            (String title, IndexNode<V>... indexNodeArray) 
                throws InvalidMultiKeyException,
                        IndexedCollectionBuildFailureException {
        IndexNode.checkVarargs(indexNodeArray);
        for (IndexNode indexNode : indexNodeArray) {
            if (maxDepth < indexNode.getDepth()) {
                maxDepth = indexNode.getDepth();
            }
        }
        @SuppressWarnings("unchecked")
        Class<? extends Key>[] classKeyClassArray = new Class[maxDepth + 1];
        Arrays.fill(classKeyClassArray, ClassKey.class);
        metamap = new IndexNode<IndexNode<V>>
                        (0, IndexNode.class, title, classKeyClassArray);
        for (IndexNode<V> indexNode : indexNodeArray) {
            ClassKey[] classKeyArray 
                = new ClassKey[indexNode.getKeyClassArray().length + 1];
            classKeyArray[0] = new ClassKey(indexNode.getValueClass());
            for (int i=1 ; i < classKeyArray.length; i++) {
                classKeyArray[i] 
                    = new ClassKey(indexNode.getKeyClassArray()[i - 1]);
            }
            boolean putSuccessful = metamap.put(indexNode, classKeyArray);
            if (!putSuccessful) {
                throw new IndexedCollectionBuildFailureException
                    ("Failure in build method for <" + this.MASTER_CLASS 
                    + "> directory: 'put' failed for the following IndexNode: <"
                    + indexNode.getTitle() + ">");
            }
        }
        //metamap.dumpContents();
    }

    /**
     *
     * @param valueClass
     * @param keyClassArray
     * @return
     * @throws InvalidIndexedCollectionQueryException
     */
    @SafeVarargs
    public final List<V> getValueList (Class<? extends V> valueClass, 
                                        Class<? extends Key>... keyClassArray) 
            throws InvalidIndexedCollectionQueryException {
        IndexNode.checkVarargs(keyClassArray);
        
        return getIndexNode(valueClass, keyClassArray).selectAll();
    }
    
    /**
     *
     * @param valueClass
     * @param mappedKeyInstance
     * @return
     * @throws InvalidIndexedCollectionQueryException
     */
    public List<V> getValueList (Class<? extends V> valueClass, 
                                            IndexedKey mappedKeyInstance) 
            throws InvalidIndexedCollectionQueryException {
        
        return getIndexNode(valueClass, mappedKeyInstance.getClass())
                                                    .get(mappedKeyInstance);
    }

    /**
     *
     * @param valueClass
     * @param mappedKeyInstance
     * @param keyClassArray
     * @return
     * @throws InvalidIndexedCollectionQueryException
     */
    @SafeVarargs
    public final List<V> getValueList (Class<? extends V> valueClass, 
                                    IndexedKey mappedKeyInstance,
                                    Class<? extends Key>... keyClassArray) 
            throws InvalidIndexedCollectionQueryException {
        
        @SuppressWarnings("unchecked")
        Class<? extends Key>[] newKeyClassArray
                                    = new Class[keyClassArray.length + 1];
        newKeyClassArray[0] = mappedKeyInstance.getClass();
        for (int i=1 ; i < newKeyClassArray.length ; i++) {
            newKeyClassArray[i] = keyClassArray[i - 1];
        }
        
        return getIndexNode(valueClass, newKeyClassArray)
                                                    .get(mappedKeyInstance);
    }

    /**
     *
     * @param valueClass
     * @param keyClassArray
     * @return
     * @throws InvalidIndexedCollectionQueryException
     */
    @SafeVarargs
    public final IndexNode<V> getIndexNode (Class<? extends V> valueClass, 
                                    Class<? extends Key>... keyClassArray) 
            throws InvalidIndexedCollectionQueryException {
        IndexNode.checkVarargs(keyClassArray);
        ClassKey[] classKeyArray = new ClassKey[keyClassArray.length + 1];
        classKeyArray[0] = new ClassKey(valueClass);
        for (int i=1 ; i < classKeyArray.length; i++) {
            classKeyArray[i] 
                = new ClassKey(keyClassArray[i - 1]);
        }
        IndexNode<V> indexNode = metamap.getFirst(classKeyArray);
        if (indexNode == null) {
            StringBuilder keyClassArrayString = new StringBuilder();
            boolean pastFirst = false;
            for (Class keyClass : keyClassArray) {
                if (pastFirst) {
                    keyClassArrayString.append(", ");
                } else {
                    pastFirst = true;
                }
                keyClassArrayString.append(keyClass.getSimpleName());
            }
            throw new InvalidIndexedCollectionQueryException
                ("No IndexNode found for values of <" 
                        + valueClass.getSimpleName()
                        + "> class, ordered by <"
                        + keyClassArrayString + ">.");
        } else {
            return indexNode;
        }
    } 

    /**
     *
     * @return
     */
    public boolean allMapsAutofillEnabled () {
        for (IndexNode indexNode : metamap.selectAll()) {
            if (!indexNode.autofillEnabled()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        output.append("DIRECTORY LISTING for <<").append(metamap.getTitle());
        output.append(">>");
        if (this.allMapsAutofillEnabled()) {
            output.append(" [All maps AUTOFILL-ENABLED]");
        }
        output.append("\n  Number of multi-key maps in directory: ")
                                                    .append(metamap.size());
        output.append(" ; Maximum multi-key map depth: ").append(this.maxDepth);
        output.append("\n*****");
        for (IndexNode<V> indexNode : metamap.selectAll()) {
            output.append("\n  ").append(indexNode);
        }
        output.append("\n*****");
        
        return output.toString();
    }
    
    /**
     *
     */
    public void dumpContents() {
        this.metamap.dumpContents();
    }
}
