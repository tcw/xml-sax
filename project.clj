(defproject xml-sax "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.json/json "20090211"]
                 [cheshire "4.0.3"]
                 [speclj "2.1.2"]]
  :plugins [[speclj "2.1.2"]]
  :test-paths ["spec/"]
  :resource-paths ["dev_resources/"])
