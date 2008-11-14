;; This script is run by the Ant build task to precompile the core
;; Clojure source files.

(println "Compiling Clojure core sources...")
(println ( System/getProperty "clojure.compile.path"))

(binding [*compile-path* (System/getProperty "clojure.compile.path")]
  (compile 'pivotal-tracker))
