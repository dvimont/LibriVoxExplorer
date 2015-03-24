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


/** Container class for a set of IndexedCollections. All IndexedCollections in such a set
 * provide mapping for the same types of objects: objects which are instances 
 * of the class or subclasses of the class denoted by {@literal <V>}.
 * Class {@literal <V>} is referred to as the MasterClass of the IndexedMetaCollection.
 * @author Daniel Vimont
 * @param <V> The MasterClass of the IndexedMetaCollection. All IndexedCollection objects managed
 by a IndexedMetaCollection object provide mapping for objects of class (or subclass of
 the class) denoted by {@literal <V>}.
 */
public abstract class IndexedMetaCollection<V> {
    final Class<V> MASTER_CLASS;
    IndexedCollection<IndexedCollection<V>> metamap; 
    int maxDepth = 0;
    
    /**
     *
     * @param masterClass
     */
    public IndexedMetaCollection (Class<V> masterClass) {
        this.MASTER_CLASS = masterClass;
    }

    /**
     * This method takes the array of IndexedCollection objects that is passed 
     * to it and constructs a metamap (a IndexedCollection of CollectionIndexes)
     * that forms the centerpiece of the IndexedMetaCollection.
     * @param title Title of the directory.
     * @param indexedCollectionArray Array of IndexedCollection objects, all 
     * of which provide various mappings for a single collection of objects: 
     * instances of class {@literal <V>}. {@literal <V>} is referred to as the 
     * MasterClass of the IndexedMetaCollection.
     * @throws InvalidMultiKeyException
     * @throws org.commonvox.indexedcollection.IndexedCollectionBuildFailureException
     */
    @SafeVarargs
    public final void build 
            (String title, IndexedCollection<V>... indexedCollectionArray) 
                throws InvalidMultiKeyException,
                        IndexedCollectionBuildFailureException {
        IndexedCollection.checkVarargs(indexedCollectionArray);
        for (IndexedCollection indexedCollection : indexedCollectionArray) {
            if (maxDepth < indexedCollection.getDepth()) {
                maxDepth = indexedCollection.getDepth();
            }
        }
        @SuppressWarnings("unchecked")
        Class<? extends Key>[] classKeyClassArray = new Class[maxDepth + 1];
        Arrays.fill(classKeyClassArray, ClassKey.class);
        metamap = new IndexedCollection<IndexedCollection<V>>
                        (0, IndexedCollection.class, title, classKeyClassArray);
        for (IndexedCollection<V> indexedCollection : indexedCollectionArray) {
            ClassKey[] classKeyArray 
                = new ClassKey[indexedCollection.getKeyClassArray().length + 1];
            classKeyArray[0] = new ClassKey(indexedCollection.getValueClass());
            for (int i=1 ; i < classKeyArray.length; i++) {
                classKeyArray[i] 
                    = new ClassKey(indexedCollection.getKeyClassArray()[i - 1]);
            }
            boolean putSuccessful = metamap.put(indexedCollection, classKeyArray);
            if (!putSuccessful) {
                throw new IndexedCollectionBuildFailureException
                    ("Failure in build method for <" + this.MASTER_CLASS 
                    + "> directory: 'put' failed for the following IndexedCollection: <"
                    + indexedCollection.getTitle() + ">");
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
        IndexedCollection.checkVarargs(keyClassArray);
        
        return getIndexedCollection(valueClass, keyClassArray).selectAll();
    }
    
    /**
     *
     * @param valueClass
     * @param indexedKeyInstance
     * @return
     * @throws InvalidIndexedCollectionQueryException
     */
    public List<V> getValueList (Class<? extends V> valueClass, 
                                            IndexedKey indexedKeyInstance) 
            throws InvalidIndexedCollectionQueryException {
        
        return getIndexedCollection(valueClass, indexedKeyInstance.getClass())
                                                    .get(indexedKeyInstance);
    }

    /**
     *
     * @param valueClass
     * @param indexedKeyInstance
     * @param keyClassArray
     * @return
     * @throws InvalidIndexedCollectionQueryException
     */
    @SafeVarargs
    public final List<V> getValueList (Class<? extends V> valueClass, 
                                    IndexedKey indexedKeyInstance,
                                    Class<? extends Key>... keyClassArray) 
            throws InvalidIndexedCollectionQueryException {
        
        @SuppressWarnings("unchecked")
        Class<? extends Key>[] newKeyClassArray
                                    = new Class[keyClassArray.length + 1];
        newKeyClassArray[0] = indexedKeyInstance.getClass();
        for (int i=1 ; i < newKeyClassArray.length ; i++) {
            newKeyClassArray[i] = keyClassArray[i - 1];
        }
        
        return getIndexedCollection(valueClass, newKeyClassArray)
                                                    .get(indexedKeyInstance);
    }

    /**
     *
     * @param valueClass
     * @param keyClassArray
     * @return
     * @throws InvalidIndexedCollectionQueryException
     */
    @SafeVarargs
    public final IndexedCollection<V> getIndexedCollection (Class<? extends V> valueClass, 
                                    Class<? extends Key>... keyClassArray) 
            throws InvalidIndexedCollectionQueryException {
        IndexedCollection.checkVarargs(keyClassArray);
        ClassKey[] classKeyArray = new ClassKey[keyClassArray.length + 1];
        classKeyArray[0] = new ClassKey(valueClass);
        for (int i=1 ; i < classKeyArray.length; i++) {
            classKeyArray[i] 
                = new ClassKey(keyClassArray[i - 1]);
        }
        IndexedCollection<V> indexedCollection = metamap.getFirst(classKeyArray);
        if (indexedCollection == null) {
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
                ("No IndexedCollection found for values of <" 
                        + valueClass.getSimpleName()
                        + "> class, ordered by <"
                        + keyClassArrayString + ">.");
        } else {
            return indexedCollection;
        }
    } 

    /**
     *
     * @return
     */
    public boolean allMapsAutofillEnabled () {
        for (IndexedCollection indexedCollection : metamap.selectAll()) {
            if (!indexedCollection.autofillEnabled()) {
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
        output.append("\n  Number of Indexed Collections in directory: ")
                                                    .append(metamap.size());
        output.append(" ; Maximum Indexed Collection depth: ")
                                                    .append(this.maxDepth);
        output.append("\n*****");
        for (IndexedCollection<V> indexedCollection : metamap.selectAll()) {
            output.append("\n  ").append(indexedCollection);
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
