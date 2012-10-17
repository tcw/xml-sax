(ns xml-sax.core-spec
(:use
  speclj.core
  xml-sax.core))

(describe "Sax parse xml"

  (it "print shit"
    (should= "Hello, foo" (sax-parse "shit.xml"))))

(run-specs)
