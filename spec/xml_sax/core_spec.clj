(ns xml-sax.core-spec
  (:require [clojure.java.io :as jio])
  (:use speclj.core
        xml-sax.core))

(describe "Finds matching xml element for path"

  (it "should handle reading from string"
    (let [tag (atom [])]
      (-> (from-string "<self/>")
        (pull-xml "self" (fn [elem] (swap! tag conj elem))))
      (should= (apply str @tag) "<self></self>")))

  (it "should handle self closing tag"
    (let [tag (atom [])]
      (-> (from-resource "company.xml")
        (pull-xml "self" (fn [elem] (swap! tag conj elem))))
      (should= (apply str @tag) "<self></self>")))

  (it "should handle one tag search with match"
    (let [counter (atom 0)]
      (-> (from-resource "company.xml")
        (pull-xml "other" (fn [elem] (swap! counter inc))))
      (should= 1 @counter)))

  (it "should handle two tag search with match"
    (let [counter (atom 0)]
      (-> (from-resource "company.xml")
        (pull-xml "company/staff" (fn [elem] (swap! counter inc))))
      (should= 2 @counter)))

  (it "should handle three tag search with match"
    (let [counter (atom 0)]
      (-> (from-resource "company.xml")
        (pull-xml "company/staff/firstname" (fn [elem] (swap! counter inc))))
      (should= 2 @counter)))

  (it "should handle partial tag search with match"
    (let [counter (atom 0)]
      (-> (from-resource "company.xml")
        (pull-xml "staff/firstname" (fn [elem] (swap! counter inc))))
      (should= 2 @counter)))

  (it "should handle one tag search no match"
    (let [counter (atom 0)]
      (-> (from-resource "company.xml")
        (pull-xml "compan" (fn [elem] (swap! counter inc))))
      (should= 0 @counter)))
  )

(describe "Finds matching xml element for path and converts to json"

  (it "should handle null"
    (let [tag (atom [])]
      (-> (from-string "<e/>")
        (pull-xml-as-json "e" (fn [elem] (swap! tag conj elem))))
      (should= "{\"e\":\"\"}" (apply str @tag) )))

  (it "should handle text"
    (let [tag (atom [])]
      (-> (from-string "<e>text</e>")
        (pull-xml-as-json "e" (fn [elem] (swap! tag conj elem))))
      (should= "{\"e\":\"text\"}" (apply str @tag) )))

  (it "should handle only attribute"
    (let [tag (atom [])]
      (-> (from-string "<e name=\"value\" />")
        (pull-xml-as-json "e" (fn [elem] (swap! tag conj elem))))
      (should= "{\"e\":{\"name\":\"value\"}}" (apply str @tag))))

  (it "should handle attribute and content"
    (let [tag (atom [])]
      (-> (from-string "<e name=\"value\">text</e>")
        (pull-xml-as-json "e" (fn [elem] (swap! tag conj elem))))
      (should= "{\"e\":{\"content\":\"text\",\"name\":\"value\"}}" (apply str @tag))))

  (it "should handle different content"
    (let [tag (atom [])]
      (-> (from-string "<e> <a>text</a> <b>text</b> </e>")
        (pull-xml-as-json "e" (fn [elem] (swap! tag conj elem))))
      (should= "{\"e\":{\"b\":\"text\",\"a\":\"text\"}}" (apply str @tag))))

  (it "should handle same content"
    (let [tag (atom [])]
      (-> (from-string "<e> <a>text</a> <a>text</a> </e>")
        (pull-xml-as-json "e" (fn [elem] (swap! tag conj elem))))
      (should= "{\"e\":{\"a\":[\"text\",\"text\"]}}" (apply str @tag))))

  (it "should handle text and content in same node"
    (let [tag (atom [])]
      (-> (from-string "<e> text <a>text</a> </e>")
        (pull-xml-as-json "e" (fn [elem] (swap! tag conj elem))))
      (should= "{\"e\":{\"content\":\"text\",\"a\":\"text\"}}" (apply str @tag) )))
  )

(run-specs)
