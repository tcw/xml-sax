(ns xml-sax.core
  (:require [clojure.string :as string]
            [clojure.java.io :as jio])
  (:import (org.xml.sax Attributes InputSource)
           (org.xml.sax.helpers DefaultHandler)
           (javax.xml.parsers SAXParserFactory)
           (java.io Reader StringReader)
           (org.json XML)))

(defn from-string [s]
  (InputSource. (StringReader. s)))

(defn from-resource [r]
  (jio/as-file (jio/resource r)))

(defn sax-parse [s sf ef cf]
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
  (str "<" uri local-name q-name (list-attr atts)">"))


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
            (when (not (.isEmpty s)) (swap! xml-elem conj s))
            ))))))

(defn pull-xml [source fxpath as f]
(cond
  (= as :xml) (pull-xml-path source fxpath f)
  (= as :json) (pull-xml-path source fxpath (comp f (fn [s] (XML/toJSONObject s))))
  :else (pull-xml-path source fxpath f)))