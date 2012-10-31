#Description

A Clojure distillation of the java xml sax parser library.
The main goal of this library is to simplifying xml sax parsing.
The library provides an element matcher for easy selection of elements.

#Install

Add to dependencies
    [xml-sax 0.1]

#Usage

add to ns:
    (:use xml-sax.core)

usage:
    (pull-xml <source> <node-path> <output-format> <callback-function>)

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

Then node-path

    "a" selects node a and its content (all of the xml)

    "a/b" selects node b and its content

    "a/b/c" selects node c and its content

    "a/b/c/d" selects d nodes with content 1 and 2

    "c/d" selects d nodes with content 1 and 2

    "a/d" selects d nodes with content 3 and 4

    "d" selects d nodes with content 1,2,3, and 4

In other words it select the nodes matching the node path signature.

(-> (from-string "<a>hello</a>")
        (pull-xml "a" :xml (fn [elem] (println elem))))


#examples

file: /home/user/xml/my.xml

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

(pull-xml "/home/user/xml/my.xml" "a/b/c" :xml (fn [node] (println node)))

output:

    <c><d>1</d><d>2</d><d>3</d><d>4</d></c>

(pull-xml "/home/user/xml/my.xml" "a/b/c/d" :xml (fn [node] (println node)))

output:

    <d>1</d>
    <d>2</d>
    <d>3</d>
    <d>4</d>

(pull-xml "/home/user/xml/my.xml" "a/b/c" :json (fn [node] (println node)))

output:

    {"c":{"d":[1,2,3,4]}}

(pull-xml "/home/user/xml/my.xml" "a/b/c/d" :json (fn [node] (println node)))

output:

    {"d":1}
    {"d":2}
    {"d":3}
    {"d":4}


(pull-xml "/home/user/xml/my.xml" "a/b/c" :clj-map (fn [node] (println node)))

output:

    {:c {:d [1 2 3 4]}}

(pull-xml "/home/user/xml/my.xml" "a/b/c/d" :clj-map (fn [node] (println node)))

output:

    {:d 1}
    {:d 2}
    {:d 3}
    {:d 4}
