package com.thaddeussoftware.tinge.helpers

import junit.framework.Assert.*
import org.junit.Test

class CollectionComparisonHelperTests {


    @Test
    fun emptyCollections_noLambdasCalled() {
        // Arrange:
        val collection1 = ArrayList<String>()
        val collection2 = ArrayList<String>()

        // Act & Assert:
        CollectionComparisonHelper.compareCollectionsAndIdentifyMissingElements(
                collection1, collection2,
                { item1, item2 -> item1 == item2},
                { fail("There should be no missing elements") },
                { fail("There should be no missing elements") }
        )
    }
    @Test
    fun sameCollection_neitherMissingElementLambdasCalled() {
        // Arrange:
        val collection = listOf("String 1", "String 2")

        // Act & Assert:
        CollectionComparisonHelper.compareCollectionsAndIdentifyMissingElements(
                collection, collection,
                { item1, item2 -> item1 == item2},
                { fail("There should be no missing elements") },
                { fail("There should be no missing elements") }
        )
    }

    @Test
    fun identicalCollectionsInDifferentOrder_neitherMissingElementLambdasCalled() {
        // Arrange:
        val collection1 = listOf("String 2", "String 1")
        val collection2 = listOf("String 1", "String 2")

        // Act & Assert:
        CollectionComparisonHelper.compareCollectionsAndIdentifyMissingElements(
                collection1, collection2,
                { item1, item2 -> item1 == item2},
                { fail("There should be no missing elements") },
                { fail("There should be no missing elements") }
        )
    }

    @Test
    fun differentCollectionsWhichEvaluateToEquivalent_neitherMissingElementLambdasCalled() {
        // Arrange:
        val collection1 = listOf("STRING 1", "STRING 2")
        val collection2 = listOf("string 1", "string 2")

        // Act & Assert:
        CollectionComparisonHelper.compareCollectionsAndIdentifyMissingElements(
                collection1, collection2,
                { item1, item2 -> item1.equals(item2, ignoreCase = true)},
                { fail("There should be no missing elements") },
                { fail("There should be no missing elements") }
        )
    }

    @Test
    fun extraElementInCollection2_extraElementLambdaCalled() {
        // Arrange:
        val collection1 = listOf(1, 2, 3)
        val collection2 = listOf(1, 2, 3, 4)

        // Act & Assert:
        val missingElementsInCollectionA = ArrayList<Int>()
        CollectionComparisonHelper.compareCollectionsAndIdentifyMissingElements(
                collection1, collection2,
                { item1, item2 -> item1 == item2 },
                { fail("There should be no missing elements in collection A") },
                { missingElementsInCollectionA.add(it) }
        )

        // Assert:
        assertEquals(1, missingElementsInCollectionA.size)
        assertEquals(4, missingElementsInCollectionA[0])
    }

    @Test
    fun extraElementsInBothCollectionsWithDuplicateElementsAndDifferentOrder_extraElementLambdaCalled() {
        // Arrange:
        // Elements 1, 2 and 3 in both collections:
        val collection1 = listOf(7, 3, 3, 3, 1, 2, 3, 6) // Extra elements 6 and 7
        val collection2 = listOf(1, 2, 3, 1, 1, 1, 4, 5) // Extra elements 4 and 5

        // Act & Assert:
        val missingElementsInCollectionA = ArrayList<Int>()
        val missingElementsInCollectionB = ArrayList<Int>()
        CollectionComparisonHelper.compareCollectionsAndIdentifyMissingElements(
                collection1, collection2,
                { item1, item2 -> item1 == item2 },
                { missingElementsInCollectionB.add(it) },
                { missingElementsInCollectionA.add(it) }
        )

        // Assert:
        assertEquals(2, missingElementsInCollectionA.size)
        assertTrue(missingElementsInCollectionA.contains(4))
        assertTrue(missingElementsInCollectionA.contains(5))
        assertEquals(2, missingElementsInCollectionB.size)
        assertTrue(missingElementsInCollectionB.contains(6))
        assertTrue(missingElementsInCollectionB.contains(7))
    }

    @Test
    fun extraElementsInBothCollections_itemsRemovedFromCollectionsInResultLambas_noExceptionThrown() {
        // Arrange:
        val collection1 = arrayListOf(1, 2, 3)
        val collection2 = arrayListOf(4, 5, 6)

        // Act:
        CollectionComparisonHelper.compareCollectionsAndIdentifyMissingElements(
                collection1, collection2,
                { item1, item2 -> item1 == item2 },
                { collection1.remove(it) },
                { collection2.remove(it) }
        )

        // Test will fail if an exception is thrown
    }
}