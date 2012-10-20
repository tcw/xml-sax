(ns xml-sax.core-spec
  (:use speclj.core
        xml-sax.core))

(defn get-attrs [atts]
  (into {} (for [i (range (.getLength atts))]
             [(keyword (.getQName atts i))
              (.getValue atts i)])))

(describe "Sax parse xml"

  (it "should handle one tag search with match"
    (let [counter (atom 0)]
      (pull-xml "/home/tom/XML/standard.xml" "item" (fn [elem] (swap! counter inc)))
      (should= 21750 @counter)))


;  (it "should handle one tag search with match"
;    (let [counter (atom 0)]
;      (pull-xml "complex.xml" "xs:schema" (fn [elem] (swap! counter inc) (println elem)))
;      (should= 1 @counter)))


  (it "should handle self closing tag"
    (let [tag (atom [])]
      (pull-xml "company.xml" "self" (fn [elem] (swap! tag conj elem)))
      (should= (apply str @tag) "<self></self>")))

  (it "should handle one tag search with match"
    (let [counter (atom 0)]
      (pull-xml "company.xml" "other" (fn [elem] (swap! counter inc)))
      (should= 1 @counter)))

  (it "should handle two tag search with match"
    (let [counter (atom 0)]
      (pull-xml "company.xml" "company/staff" (fn [elem] (swap! counter inc)))
      (should= 2 @counter)))

  (it "should handle three tag search with match"
    (let [counter (atom 0)]
      (pull-xml "company.xml" "company/staff/firstname" (fn [elem] (swap! counter inc)))
      (should= 2 @counter)))

  (it "should handle partial tag search with match"
    (let [counter (atom 0)]
      (pull-xml "company.xml" "staff/firstname" (fn [elem] (swap! counter inc)))
      (should= 2 @counter)))

  (it "should handle one tag search no match"
    (let [counter (atom 0)]
      (pull-xml "company.xml" "compan" (fn [elem] (swap! counter inc)))
      (should= 0 @counter)))
  )

(run-specs)
