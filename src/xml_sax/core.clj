(ns xml-sax.core
  (:require [clojure.string :as string]
            [clojure.java.io :as jio])
  (:import (org.xml.sax Attributes InputSource)
           (org.xml.sax.helpers DefaultHandler)
           (javax.xml.parsers SAXParserFactory)
           (java.io Reader)))

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

(defn is-match? [path path-pos q-name]
  (= (nth path path-pos) q-name))

(defn pull-xml [source fxpath f]
  (let [path (string/split fxpath #"/")
        path-pos (atom 0)
        path-final (- (count path) 1)
        start-elem (atom false)
        xml-elem (atom [])]
    (sax-parse source
      (fn [uri local-name q-name #^Attributes atts]
        (if @start-elem
          (swap! xml-elem conj (str "<" uri local-name q-name ">"))
          (when (is-match? path @path-pos q-name)
            (if (= @path-pos path-final)
              (do (swap! xml-elem conj (str "<" uri local-name q-name ">"))
              (swap! start-elem not))
              (swap! path-pos inc))))
        ),
      (fn [uri local-name q-name]
        (when @start-elem (swap! xml-elem conj (str "</" uri local-name q-name ">")))
        (when (is-match? path @path-pos q-name)
          (if @start-elem
            (do
              (swap! start-elem not)
              (f @xml-elem)
              (reset! xml-elem []))
            (swap! path-pos dec)))
        ),
      (fn [ch start length]
        (when @start-elem
          (let [s (.trim (String. ch start length))]
            (when (not (.isEmpty s)) (swap! xml-elem conj s))
            ))))))