(ns xml-sax.core
  (:use cheshire.core)
  (:require [clojure.string :as string]
            [clojure.java.io :as jio])
  (:import (org.xml.sax Attributes InputSource)
           (org.xml.sax.helpers DefaultHandler)
           (javax.xml.parsers SAXParserFactory)
           (java.io Reader StringReader)
           (org.json XML)))

(defn from-string [^String s]
  "Convenience function for reading a String as input"
  (InputSource. (StringReader. s)))

(defn from-resource [r]
  "Convenience function for reading a Resource as input"
  (jio/as-file (jio/resource r)))

(defn get-attrs [atts]
  "Convenience function for getting the java Attributes object
as a clojure set with keywords"
  (set (for [i (range (.getLength atts))]
         [(keyword (.getQName atts i))
          (.getValue atts i)])))

(defn sax-parse [s sf ef cf]
  "Parse xml with sax.
   s -> File,InputSource,InputStream,String url
   sf -> Function with params [uri local-name q-name atts]
   ef -> Function with params [uri local-name q-name]
   cf -> Function with params [ch start length]
   s is the input source
   sf is the callback-function when encountering a start element
   ef is the callback-function when encountering an end element
   cf is the callback-function for the content"
  (.. SAXParserFactory
    newInstance
    newSAXParser
    (parse s (proxy [DefaultHandler] []
               (startElement [uri local-name q-name #^Attributes atts]
                 (sf uri local-name q-name atts))
               (endElement [uri local-name q-name]
                 (ef uri local-name q-name))
               (characters [ch start length]
                 (cf ch start length))))))

(defn- is-match? [path path-pos q-name]
  (= (nth path path-pos) q-name))

(defn- list-attr [atts]
  (apply str (for [i (range (.getLength atts))]
               (str " " (.getQName atts i) "=\"" (.getValue atts i) "\""))))

(defn- tag-as-string [uri local-name q-name #^Attributes atts]
  (str "<" uri local-name q-name (list-attr atts) ">"))

(defn- pull-xml-path [source fxpath f]
  (let [path (string/split fxpath #"/")
        path-pos (atom 0)
        path-final (- (count path) 1)
        start-elem (atom false)
        xml-elem (atom [])]
    (sax-parse source
      (fn [uri local-name q-name #^Attributes atts]
        (if @start-elem
          (swap! xml-elem conj (tag-as-string uri local-name q-name atts))
          (when (is-match? path @path-pos q-name)
            (if (= @path-pos path-final)
              (do (swap! xml-elem conj (tag-as-string uri local-name q-name atts))
                (swap! start-elem not))
              (swap! path-pos inc))))
        ),
      (fn [uri local-name q-name]
        (when @start-elem (swap! xml-elem conj (str "</" uri local-name q-name ">")))
        (when (is-match? path @path-pos q-name)
          (if @start-elem
            (do
              (swap! start-elem not)
              (f (apply str @xml-elem))
              (reset! xml-elem []))
            (swap! path-pos dec)))
        ),
      (fn [ch start length]
        (when @start-elem
          (let [s (.trim (String. ch start length))]
            (when (not (.isEmpty s)) (swap! xml-elem conj s))))))))

(defn pull-xml [source fxpath as f]
  "Pulls xml elements from a xml.
  source -> File,InputSource,InputStream,String url
  fxpath -> Node selection string elema/elemb...
  as -> Output formats:
        :xml produces xml string
        :json produces json string
        :clj-map produces clojure map
  f -> callback-function - example: (fn [elem] (println elem)))"
  (cond
    (= as :xml ) (pull-xml-path source fxpath f)
    (= as :json ) (pull-xml-path source fxpath (comp f (fn [s] (XML/toJSONObject s))))
    (= as :clj-map ) (pull-xml-path source fxpath (comp f
                                                    (fn [s] (parse-string s true))
                                                    (fn [s] (.toString (XML/toJSONObject s)))))
    :else (pull-xml-path source fxpath f)))