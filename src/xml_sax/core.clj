(ns xml-sax.core
  (:import (org.xml.sax Attributes InputSource)
           (org.xml.sax.helpers DefaultHandler)
           (javax.xml.parsers SAXParserFactory)
           (java.io Reader)))

(defn get-attrs [atts]
  (into {} (for [i (range (.getLength atts))]
             [(keyword (.getQName atts i))
              (.getValue atts i)])))

(defn sax-parse [s]
  (let [s (if (instance? Reader s) (InputSource. s) s)]
    (.. SAXParserFactory newInstance newSAXParser (parse s (proxy [DefaultHandler] []
                    (startElement [uri local-name q-name #^Attributes atts]
                        (println (str "start -> " (keyword q-name) (get-attrs atts))))
                    (endElement [uri local-name q-name]
                      (println (str "end -> " (keyword q-name))))
                    (characters [ch start length]
                      (let [st (String. ch start length)]
                        (when (seq (.trim st))
                          (println (str "chars -> " st))))))))))