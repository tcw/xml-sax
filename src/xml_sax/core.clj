(ns xml-sax.core
  (:import (org.xml.sax Attributes InputSource)
           (org.xml.sax.helpers DefaultHandler)
           (javax.xml.parsers SAXParserFactory)
           (java.io Reader)))

(defn sax-parse [s sfn efn cfn]
  (let [s (if (instance? Reader s) (InputSource. s) s)]
    (.. SAXParserFactory
      newInstance
      newSAXParser
      (parse s (proxy [DefaultHandler] []
                 (startElement [uri local-name q-name #^Attributes atts]
                   (sfn uri local-name q-name atts))
                 (endElement [uri local-name q-name]
                   (efn uri local-name q-name))
                 (characters [ch start length]
                   (cfn ch start length)))))))


(defn pull-xml [source fxpath f]
  (let [start-elem (atom false) xml-elem (atom [])]
    (sax-parse source
      (fn [uri local-name q-name #^Attributes atts]
        (when (= fxpath q-name) (swap! start-elem not))
        (when @start-elem (swap! xml-elem conj (str "<" q-name ">")))
        ),
      (fn [uri local-name q-name]
        (when @start-elem (swap! xml-elem conj (str "</" q-name ">")))
        (when (= fxpath q-name)
          (swap! start-elem not)
            (f @xml-elem)
          (reset! xml-elem []))
        ),
      (fn [ch start length]
        (when @start-elem (swap! xml-elem conj (.trim (String. ch start length))))
        ))))