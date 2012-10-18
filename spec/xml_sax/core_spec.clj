(ns xml-sax.core-spec
  (:use speclj.core
        xml-sax.core))

(defn get-attrs [atts]
  (into {} (for [i (range (.getLength atts))]
             [(keyword (.getQName atts i))
              (.getValue atts i)])))

(defn get-string [ch start length]
  (let [st (String. ch start length)]
    (when (seq (.trim st)))))


(describe "Sax parse xml"

  (it "parses xml with sax in a xpath forward manor"
    (let [counter (atom 0)]
;      (pull-xml "shit.xml" "staff" (fn [elem] (swap! counter inc)))
      (pull-xml "/home/tom/XML/standard.xml" "site/regions/africa" (fn [elem]
                                                      (swap! counter inc)))
       (should= 21750 @counter)))
  )

(run-specs)
