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

package org.commonvox.indexedcollectionmanager;

import java.util.Arrays;
import java.util.List;


/** Container class for a set of MultiKeyMaps. All MultiKeyMaps in such a set
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
     * This method takes the array of IndexedCollection objects that is passed to it
 and constructs a metamap (a IndexedCollection of CollectionIndexes) that forms the 
 centerpiece of the IndexedMetaCollection.
     * @param title Title of the directory.
     * @param multiKeyMapArray Array of IndexedCollection objects, all of which 
 provide various mappings for the same set of objects: instances of 
 class {@literal <V>}. {@literal <V>} is referred to as the MasterClass
 of the IndexedMetaCollection.
     * @throws InvalidMultiKeyException
     * @throws org.commonvox.indexedcollectionmanager.IndexedCollectionBuildFailureException
     */
    @SafeVarargs
    public final void build 
            (String title, IndexedCollection<V>... multiKeyMapArray) 
                throws InvalidMultiKeyException,
                        IndexedCollectionBuildFailureException {
        IndexedCollection.checkVarargs(multiKeyMapArray);
        for (IndexedCollection multiKeyMap : multiKeyMapArray) {
            if (maxDepth < multiKeyMap.getDepth()) {
                maxDepth = multiKeyMap.getDepth();
            }
        }
        @SuppressWarnings("unchecked")
        Class<? extends Key>[] classKeyClassArray = new Class[maxDepth + 1];
        Arrays.fill(classKeyClassArray, ClassKey.class);
        metamap = new IndexedCollection<IndexedCollection<V>>
                        (0, IndexedCollection.class, title, classKeyClassArray);
        for (IndexedCollection<V> multiKeyMap : multiKeyMapArray) {
            ClassKey[] classKeyArray 
                = new ClassKey[multiKeyMap.getKeyClassArray().length + 1];
            classKeyArray[0] = new ClassKey(multiKeyMap.getValueClass());
            for (int i=1 ; i < classKeyArray.length; i++) {
                classKeyArray[i] 
                    = new ClassKey(multiKeyMap.getKeyClassArray()[i - 1]);
            }
            boolean putSuccessful = metamap.put(multiKeyMap, classKeyArray);
            if (!putSuccessful) {
                throw new IndexedCollectionBuildFailureException
                    ("Failure in build method for <" + this.MASTER_CLASS 
                    + "> directory: 'put' failed for the following MultiKeyMap: <"
                    + multiKeyMap.getTitle() + ">");
            }
        }
        //metamap.dumpContents();
    }

    /**
     *
     * @param valueClass
     * @param keyClassArray
     * @return
     * @throws InvalidCollectionIndexQueryException
     */
    @SafeVarargs
    public final List<V> getValueList (Class<? extends V> valueClass, 
                                        Class<? extends Key>... keyClassArray) 
            throws InvalidCollectionIndexQueryException {
        IndexedCollection.checkVarargs(keyClassArray);
        
        return getMultiKeyMap(valueClass, keyClassArray).selectAll();
    }
    
    /**
     *
     * @param valueClass
     * @param mappedKeyInstance
     * @return
     * @throws InvalidCollectionIndexQueryException
     */
    public List<V> getValueList (Class<? extends V> valueClass, 
                                            IndexedKey mappedKeyInstance) 
            throws InvalidCollectionIndexQueryException {
        
        return getMultiKeyMap(valueClass, mappedKeyInstance.getClass())
                                                    .get(mappedKeyInstance);
    }

    /**
     *
     * @param valueClass
     * @param mappedKeyInstance
     * @param keyClassArray
     * @return
     * @throws InvalidCollectionIndexQueryException
     */
    @SafeVarargs
    public final List<V> getValueList (Class<? extends V> valueClass, 
                                    IndexedKey mappedKeyInstance,
                                    Class<? extends Key>... keyClassArray) 
            throws InvalidCollectionIndexQueryException {
        
        @SuppressWarnings("unchecked")
        Class<? extends Key>[] newKeyClassArray
                                    = new Class[keyClassArray.length + 1];
        newKeyClassArray[0] = mappedKeyInstance.getClass();
        for (int i=1 ; i < newKeyClassArray.length ; i++) {
            newKeyClassArray[i] = keyClassArray[i - 1];
        }
        
        return getMultiKeyMap(valueClass, newKeyClassArray)
                                                    .get(mappedKeyInstance);
    }

    /**
     *
     * @param valueClass
     * @param keyClassArray
     * @return
     * @throws InvalidCollectionIndexQueryException
     */
    @SafeVarargs
    public final IndexedCollection<V> getMultiKeyMap (Class<? extends V> valueClass, 
                                    Class<? extends Key>... keyClassArray) 
            throws InvalidCollectionIndexQueryException {
        IndexedCollection.checkVarargs(keyClassArray);
        ClassKey[] classKeyArray = new ClassKey[keyClassArray.length + 1];
        classKeyArray[0] = new ClassKey(valueClass);
        for (int i=1 ; i < classKeyArray.length; i++) {
            classKeyArray[i] 
                = new ClassKey(keyClassArray[i - 1]);
        }
        IndexedCollection<V> multiKeyMap = metamap.getFirst(classKeyArray);
        if (multiKeyMap == null) {
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
            throw new InvalidCollectionIndexQueryException
                ("No MultiKeyMap found for values of <" 
                        + valueClass.getSimpleName()
                        + "> class, ordered by <"
                        + keyClassArrayString + ">.");
        } else {
            return multiKeyMap;
        }
    } 

    /**
     *
     * @return
     */
    public boolean allMapsAutofillEnabled () {
        for (IndexedCollection multiKeyMap : metamap.selectAll()) {
            if (!multiKeyMap.autofillEnabled()) {
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
        for (IndexedCollection<V> multiKeyMap : metamap.selectAll()) {
            output.append("\n  ").append(multiKeyMap);
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
