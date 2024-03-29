#Description

A Clojure distillation of the java xml sax parser library.
The main goal of this library is to simplify xml sax parsing.
The library provides an element matcher for easy selection of elements.

This library is in alpha state!

#Install

Add to dependencies

    [xml-sax 0.1.0]

#Usage

add to ns:

    (:use xml-sax.core)

usage:

    (pull-xml <source> <element-path> <output-format> <callback-function>)

example:

    (pull-xml "/tmp/my.xml" "item/car" :xml (fn [elem] (println elem)))


The element matcher does not match like xpath!

Let say we have the xml

    <a>
      <b>
        <c>
          <d>1</d>
          <d>2</d>
        </c>
      </b>
      <d>3</d>
      <d>4</d>
    </a>

Then element-path

    "a" selects element a and its content (all of the xml)

    "a/b" selects element b and its content

    "a/b/c" selects element c and its content

    "a/b/c/d" selects d elements with content 1 and 2

    "c/d" selects d elements with content 1 and 2

    "a/d" selects d elements with content 3 and 4

    "d" selects d elements with content 1,2,3, and 4

In other words it select the elements matching the element path signature.

There are also two convenience functions for reading xml sources:

    (-> (from-string "<a>hello</a>")
          (pull-xml "a" :xml (fn [elem] (println elem))))

and

    (-> (from-resource "some-resource-on-classpath.xml")
          (pull-xml "a" :xml (fn [elem] (println elem))))


#examples

file: /tmp/my.xml

    <a>
      <b>
        <c>
          <d>1</d>
          <d>2</d>
          <d>3</d>
          <d>4</d>
        </c>
      </b>
    </a>

function:

    (pull-xml "/tmp/my.xml" "a/b/c" :xml (fn [elem] (println elem)))

output:

    <c><d>1</d><d>2</d><d>3</d><d>4</d></c>

function:

    (pull-xml "/tmp/my.xml" "a/b/c/d" :xml (fn [elem] (println elem)))

output:

    <d>1</d>
    <d>2</d>
    <d>3</d>
    <d>4</d>

function:

    (pull-xml "/tmp/my.xml" "a/b/c" :json (fn [elem] (println elem)))

output:

    {"c":{"d":[1,2,3,4]}}

function:

    (pull-xml "/tmp/my.xml" "a/b/c/d" :json (fn [elem] (println elem)))

output:

    {"d":1}
    {"d":2}
    {"d":3}
    {"d":4}

function:

    (pull-xml "/tmp/my.xml" "a/b/c" :clj-map (fn [elem] (println elem)))

output:

    {:c {:d [1 2 3 4]}}

function:

    (pull-xml "/tmp/my.xml" "a/b/c/d" :clj-map (fn [elem] (println elem)))

output:

    {:d 1}
    {:d 2}
    {:d 3}
    {:d 4}

#License

Distributed under the Eclipse Public License, the same as Clojure.