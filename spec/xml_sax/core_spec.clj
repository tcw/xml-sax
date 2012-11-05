(ns xml-sax.core-spec
  (:require [clojure.java.io :as jio])
  (:use speclj.core
        xml-sax.core))

(describe "Finds matching xml element for path"

  (with xml-file (from-resource "company.xml"))

  (it "should handle reading from string"
    (let [tag (atom [])]
      (-> (from-string "<self/>")
        (pull-xml "self" :xml (fn [elem] (swap! tag conj elem))))
      (should= (apply str @tag) "<self></self>")))

  (it "should handle self closing tag"
    (let [tag (atom [])]
      (-> @xml-file
        (pull-xml "self" :xml (fn [elem] (swap! tag conj elem))))
      (should= (apply str @tag) "<self></self>")))

  (it "should handle one tag search with match"
    (let [counter (atom 0)]
      (-> @xml-file
        (pull-xml "other" :xml (fn [elem] (swap! counter inc))))
      (should= 1 @counter)))

  (it "should handle two tag search with match"
    (let [counter (atom 0)]
      (-> @xml-file
        (pull-xml "company/staff" :xml (fn [elem] (swap! counter inc))))
      (should= 2 @counter)))

  (it "should handle three tag search with match"
    (let [counter (atom 0)]
      (-> @xml-file
        (pull-xml "company/staff/firstname" :xml (fn [elem] (swap! counter inc))))
      (should= 2 @counter)))

  (it "should handle partial tag search with match"
    (let [counter (atom 0)]
      (-> @xml-file
        (pull-xml "staff/firstname" :xml (fn [elem] (swap! counter inc))))
      (should= 2 @counter)))

  (it "should handle one tag search no match"
    (let [counter (atom 0)]
      (-> @xml-file
        (pull-xml "compan" :xml (fn [elem] (swap! counter inc))))
      (should= 0 @counter)))
  )

(describe "Finds matching xml element for path and converts to json"

  (it "should handle null"
    (let [tag (atom [])]
      (-> (from-string "<e/>")
        (pull-xml "e" :json (fn [elem] (swap! tag conj elem))))
      (should= "{\"e\":\"\"}" (apply str @tag))))

  (it "should handle text"
    (let [tag (atom [])]
      (-> (from-string "<e>text</e>")
        (pull-xml "e" :json (fn [elem] (swap! tag conj elem))))
      (should= "{\"e\":\"text\"}" (apply str @tag))))

  (it "should handle only attribute"
    (let [tag (atom [])]
      (-> (from-string "<e name=\"value\" />")
        (pull-xml "e" :json (fn [elem] (swap! tag conj elem))))
      (should= "{\"e\":{\"name\":\"value\"}}" (apply str @tag))))

  (it "should handle attribute and content"
    (let [tag (atom [])]
      (-> (from-string "<e name=\"value\">text</e>")
        (pull-xml "e" :json (fn [elem] (swap! tag conj elem))))
      (should= "{\"e\":{\"content\":\"text\",\"name\":\"value\"}}" (apply str @tag))))

  (it "should handle different content"
    (let [tag (atom [])]
      (-> (from-string "<e> <a>text</a> <b>text</b> </e>")
        (pull-xml "e" :json (fn [elem] (swap! tag conj elem))))
      (should= "{\"e\":{\"b\":\"text\",\"a\":\"text\"}}" (apply str @tag))))

  (it "should handle same content"
    (let [tag (atom [])]
      (-> (from-string "<e> <a>text</a> <a>text</a> </e>")
        (pull-xml "e" :json (fn [elem] (swap! tag conj elem))))
      (should= "{\"e\":{\"a\":[\"text\",\"text\"]}}" (apply str @tag))))

  (it "should handle text and content in same node"
    (let [tag (atom [])]
      (-> (from-string "<e> text <a>text</a> </e>")
        (pull-xml "e" :json (fn [elem] (swap! tag conj elem))))
      (should= "{\"e\":{\"content\":\"text\",\"a\":\"text\"}}" (apply str @tag))))
  )

(describe "Finds matching xml element for path and converts to clojure map"

  (with xml-file (from-resource "company.xml"))

  (it "should handle two tag search with match"
    (let [counter (atom 0)]
      (-> @xml-file
        (pull-xml "company/staff" :clj-map (fn [elem] (swap! counter inc))))
      (should= 2 @counter)))

  (it "should get firstnames"
    (let [names (atom [])]
      (-> @xml-file
        (pull-xml "company/staff" :clj-map (fn [elem] (swap! names conj (:staff elem)))))
      (should= 2 (count @names))
      (should= "per" (:firstname (first @names)))
      (should= "peder" (:firstname (last @names)))))
  )

(describe "Makes sure readme examples are correct"

  (it "makes sure c node is selected and output is xml"
    (let [tag (atom [])]
      (-> (from-string "<a> <b> <c> <d>1</d> <d>2</d> <d>3</d> <d>4</d> </c> </b> </a>")
        (pull-xml "a/b/c" :xml (fn [elem] (swap! tag conj elem))))
      (should= "<c><d>1</d><d>2</d><d>3</d><d>4</d></c>" (apply str @tag))))

  (it "makes sure d nodes are selected and output is xml"
    (let [tag (atom [])]
      (-> (from-string "<a> <b> <c> <d>1</d> <d>2</d> <d>3</d> <d>4</d> </c> </b> </a>")
        (pull-xml "a/b/c/d" :xml (fn [elem] (swap! tag conj elem))))
      (should= "<d>1</d><d>2</d><d>3</d><d>4</d>" (apply str @tag))))

  (it "makes sure c node is selected and output is json"
    (let [tag (atom [])]
      (-> (from-string "<a> <b> <c> <d>1</d> <d>2</d> <d>3</d> <d>4</d> </c> </b> </a>")
        (pull-xml "a/b/c" :json (fn [elem] (swap! tag conj elem))))
      (should= "{\"c\":{\"d\":[1,2,3,4]}}" (apply str @tag))))

  (it "makes sure d nodes are selected and output is json"
    (let [tag (atom [])]
      (-> (from-string "<a> <b> <c> <d>1</d> <d>2</d> <d>3</d> <d>4</d> </c> </b> </a>")
        (pull-xml "a/b/c/d" :json (fn [elem] (swap! tag conj elem))))
      (should= "{\"d\":1}{\"d\":2}{\"d\":3}{\"d\":4}" (apply str @tag))))

  (it "makes sure c node is selected and output is a clojure map"
    (let [tag (atom [])]
      (-> (from-string "<a> <b> <c> <d>1</d> <d>2</d> <d>3</d> <d>4</d> </c> </b> </a>")
        (pull-xml "a/b/c" :clj-map (fn [elem] (swap! tag conj elem))))
      (should= "{:c {:d [1 2 3 4]}}" (apply str @tag))))

  (it "makes sure d nodes are selected and output is a clojure map"
    (let [tag (atom [])]
      (-> (from-string "<a> <b> <c> <d>1</d> <d>2</d> <d>3</d> <d>4</d> </c> </b> </a>")
        (pull-xml "a/b/c/d" :clj-map (fn [elem] (swap! tag conj elem))))
      (should= "{:d 1}{:d 2}{:d 3}{:d 4}" (apply str @tag))))
  )

(describe "Repeating xml tags"

  (with xml-file (from-resource "company.xml"))

  (it "should select first two item elements"
    (let [counter (atom 0)]
      (-> @xml-file
        (pull-xml "item" :xml (fn [elem] (swap! counter inc))))
      (should= 2 @counter)))

  (it "should select same first two item elements"
    (let [counter (atom 0)]
      (-> @xml-file
        (pull-xml "items/item" :xml (fn [elem] (swap! counter inc))))
      (should= 2 @counter)))

  (it "should select last four item elements"
    (let [counter (atom 0)]
      (-> @xml-file
        (pull-xml "items/item" :xml (fn [elem] (swap! counter inc))))
      (should= 2 @counter)))

  )


(run-specs)
