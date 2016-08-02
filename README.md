# Overview

In this programming assignment, you will be given posting lists generated from the RCV1 news corpus
(http://www.daviddlewis.com/resources/testcollections/rcv1/). You need to get familiar with the data format of the
given posting lists and rebuild the index after reading in the data. Linked List should be used to store the index data
in memory as the examples shown in the textbook. You need to construct two index with two different ordering
strategies: with one strategy, the posting of each term should be ordered by increasing document IDs; with the other
strategy, the postings of each term should be ordered by decreasing term frequencies. After that, you are required to
implement modules that return documents based on term-at-a-time with the postings list ordered by term frequencies,
and document-at-a-time with the postings list ordered by doc IDs for a set of queries.
