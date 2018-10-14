package com.thaddeussoftware.tinge.helpers

class CollectionComparisonHelper private constructor() {
    companion object {
        /**
         * Compares all the items in two collections, using a provided lambda, to establish
         * whether either of the two collections contains items which do not have an
         * equivalent item in the other collection. Provided lambdas are then called with
         * details of each of the items without an equivalent item in the other collection.
         *
         * This method is useful if e.g. you have results from a web server, and you want to compare
         * these results to the current results cached / displayed in the UI in order to establish
         * which items need to be removed and which need to be added to reflect the live data.
         *
         * @param collectionA
         * The first collection to compare
         * @param collectionB
         * The second collection to compare
         * @param areElementsEquivalent
         * Should return whether a given item from collectionA is equivalent to a given item from
         * collectionB.
         * @param onElementInCollectionANotFoundInCollectionB
         * Will get called for each item that exists in collectionA that does not have an equivalent
         * item in collectionB.
         * It is safe to modify collectionA and collectionB on this lambda being called.
         * @param onElementInCollectionBNotFoundInCollectionA
         * Will get called for each item that exists in collectionB that does not have an equivalent
         * item in collectionA.
         * It is safe to modify collectionA and collectionB on this lambda being called.
         *
         * @param onElementFoundInBothCollections
         * Optional - will get called for each equivalent pair of items in collectionA and
         * collectionB.
         * Note that this will not work correctly if there are more than one items in either
         * collection which are equivalent to the same item in the other collection.
         *
         * @return
         * If more than
         * */
        fun <A, B> compareCollectionsAndIdentifyMissingElements(
                collectionA: Collection<A>,
                collectionB: Collection<B>,
                areElementsEquivalent: (A, B) -> Boolean,
                onElementInCollectionANotFoundInCollectionB: (A) -> Unit,
                onElementInCollectionBNotFoundInCollectionA: (B) -> Unit,
                onElementFoundInBothCollections: ((A, B) -> Unit)? = null
        ): Boolean {

            // All results are stored in lists and then the lambdas called afterwards in case
            // the collections are modified as a result of the lambdas being called, which would
            // cause errors if the collections were still being iterated over when modified.

            var itemsInListANotFoundInListB = ArrayList<A>()
            var itemsInListBNotFoundInListA = ArrayList<B>()
            var itemsInListAAlsoFoundInListB = ArrayList<A>()
            var itemsInListBAlsoFoundInListA = ArrayList<B>()

            var wereAnyDuplicateElementsFound = false

            // Populate itemsInListANotFoundInListB, itemsInListAAlsoFoundInListB and
            // itemsInListBAlsoFoundInListA:
            collectionA.forEach { itemA ->
                var equivalentItemB: B? = null
                collectionB.forEach { itemB ->
                    if (areElementsEquivalent(itemA, itemB)) {
                        if (equivalentItemB != null) wereAnyDuplicateElementsFound = true
                        equivalentItemB = itemB
                    }
                }
                if (equivalentItemB == null) {
                    itemsInListANotFoundInListB.add(itemA)
                } else {
                    itemsInListAAlsoFoundInListB.add(itemA)
                    itemsInListBAlsoFoundInListA.add(equivalentItemB!!)
                }
            }

            // Populate itemsInListBNotFoundInListA:
            collectionB.forEach { itemB ->
                var wasItemAlsoFoundInListA = false
                collectionA.forEach {  itemA ->
                    if (areElementsEquivalent(itemA, itemB)) {
                        if (wasItemAlsoFoundInListA) wereAnyDuplicateElementsFound = true
                        wasItemAlsoFoundInListA = true
                    }
                }
                if (!wasItemAlsoFoundInListA) {
                    itemsInListBNotFoundInListA.add(itemB)
                }
            }

            // Call lambdas with all elements found:
            itemsInListANotFoundInListB.forEach { onElementInCollectionANotFoundInCollectionB(it) }
            itemsInListBNotFoundInListA.forEach { onElementInCollectionBNotFoundInCollectionA(it) }
            if (onElementFoundInBothCollections != null) {
                for (i in 0..itemsInListAAlsoFoundInListB.size - 1) {
                    onElementFoundInBothCollections(
                            itemsInListAAlsoFoundInListB[i],
                            itemsInListBAlsoFoundInListA[i]
                    )
                }
            }

            return !wereAnyDuplicateElementsFound
        }
    }
}